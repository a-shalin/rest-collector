package ru.cloudinfosys.rc.serv;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.cloudinfosys.rc.beans.Visit;
import ru.cloudinfosys.rc.beans.VisitResult;
import ru.cloudinfosys.rc.db.VisitDb;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;

import static java.lang.String.format;

/** Main processing service */
@Service
public class Counter {
    private final Logger log = LogManager.getLogger(getClass());

    private final Object lock = new Object();
    private volatile int visitCount = 0;
    private final Set<Integer> users = new HashSet<>();

    private final BlockingQueue<Visit> visits = new ArrayBlockingQueue<>(1000);

    @Autowired
    VisitDb visitDb;

    /** User visits page */
    public VisitResult visit(int userId, int pageId) {
        try {
            visits.put(new Visit(userId, pageId, new Date()));

            int count, userCount;

            synchronized (lock) {
                count = ++visitCount;

                users.add(userId);
                userCount = users.size();
            }

            return new VisitResult(count, userCount);
        } catch (InterruptedException e) {
            throw new RuntimeException("Thread was interrupted", e);
        }
    }

    /** Prepare count cache */
    private void setVisitCounts() {
        synchronized (lock) {
            this.visitCount = visitDb.getCurrentVisitCount();

            this.users.clear();
            this.users.addAll(visitDb.getCurrentUniqueUsers());
        }

        log.info(format("Cache counts were updated, visit count = %d, unique users count = %d",
                getVisitCount(), getUserCount()));
    }

    /** User count from cache */
    public int getUserCount() {
        synchronized (lock) {
            return users.size();
        }
    }

    /** Visit count from cache */
    public int getVisitCount() {
        synchronized (lock) {
            return visitCount;
        }
    }

    @Autowired
    DbHelper dbHelper;

    private static final ThreadFactory daemonThreadFactory = r -> {
        Thread t = Executors.defaultThreadFactory().newThread(r);
        t.setDaemon(true);
        return t;
    };

    /** Number of rows in one batch insert */
    public static final int BATCH_SIZE = 100;

    /** Special object used for stopping  */
    private static final Visit POISON_PILL = new Visit(Integer.MIN_VALUE, Integer.MIN_VALUE, null);

    /** Wait for visit beans in queue and inserts them in DB in batches */
    private Runnable insertVisitors = () -> {
        List<Visit> visitBatch = new ArrayList<>(BATCH_SIZE);

        try {
            while (true) {
                // try to take bean from queue for 500 ms
                Visit visit = visits.poll(500, TimeUnit.MILLISECONDS);

                // Bean successfully has been taken
                if (visit != null) {
                    // if we eat poison pill then flush last batch and return
                    if (POISON_PILL.equals(visit)) {
                        if (visitBatch.size() > 0) dbHelper.insertBatch(visitBatch);
                        return;
                    }

                    visitBatch.add(visit);
                }

                // Bean hasn't been available and batch is not empty or batch size is maximum
                // then flush batch
                if ((visit == null && visitBatch.size() > 0) || visitBatch.size() >= BATCH_SIZE) {
                    long start = System.currentTimeMillis();
                    dbHelper.insertBatch(visitBatch);
                    log.debug(String.format("Queue size = %d, batch insert time = %d ms, batch size = %d",
                            visits.size(), System.currentTimeMillis() - start, visitBatch.size()));

                    visitBatch.clear();
                }
            }
        } catch (InterruptedException ie) {
            log.error("Insert visitor thread was interrupted");
        } catch (Exception e) {
            log.error("Error in thread", e);
        } finally {
            log.debug(format("Visitor thread %s finished execution", Thread.currentThread().getName()));
        }
    };

    /** Wait for visit in queue and insert */
    private ExecutorService dataUploader = Executors.newCachedThreadPool(daemonThreadFactory);

    public static final int UPLOADERS_COUNT = Runtime.getRuntime().availableProcessors();

    /** Create DB objects if they doesn't exist and start dataUploader */
    @PostConstruct
    void init() {
        dbHelper.initDb();
        dbHelper.prepareSampleData();

        for (int i = 0; i < UPLOADERS_COUNT; i++) {
            dataUploader.submit(insertVisitors);
        }

        setVisitCounts();
    }

    /** Finish all dataUploader threads feeding them poison pill ;) */
    @PreDestroy
    void close() {
        try {
            long startDataUploader = System.currentTimeMillis();

            for (int i = 0; i < UPLOADERS_COUNT; i++) {
                visits.put(POISON_PILL);
            }

            dataUploader.shutdown();
            while (!dataUploader.awaitTermination(3, TimeUnit.SECONDS)) {
                log.info(format("Finishing data uploading, %d rows in queue", visits.size()));
            }

            log.info(format("dataUploader finished in %d ms",
                    System.currentTimeMillis()-startDataUploader));

            if (!afterShutdownTasks.isEmpty()) {
                for (Runnable task : afterShutdownTasks) task.run();
            }

        } catch (InterruptedException e) {
            log.error("dataUploader was terminated ungracefully");
        }
    }

    Set<Runnable> afterShutdownTasks = ConcurrentHashMap.newKeySet();

    /** Add listener to exec code after data uploader has been stopped */
    public void addDataUploaderShutdownListener(Runnable task) {
        afterShutdownTasks.add(task);
    }

    @Scheduled(cron="0 0 0 * * *")
    void updateCounters() {
        setVisitCounts();
    }
}
