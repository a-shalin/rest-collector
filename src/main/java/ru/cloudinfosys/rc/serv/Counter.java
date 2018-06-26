package ru.cloudinfosys.rc.serv;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cloudinfosys.rc.beans.Visit;
import ru.cloudinfosys.rc.db.VisitDb;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class Counter {
    private static final Logger log = LogManager.getLogger(Counter.class);

    private AtomicInteger visitCount = new AtomicInteger(0);
    private Set<Integer> users = ConcurrentHashMap.newKeySet();
    private BlockingQueue<Visit> visits = new LinkedBlockingQueue<>(20000);

    public Integer getVisitCount() {
        return visitCount.get();
    }

    public Integer getUserCount() {
        return users.size();
    }

    @Autowired
    VisitDb visitDb;

    /** User visits page */
    public void visit(int userId, int pageId) {
        try {
            visits.put(new Visit(userId, pageId, new Date()));
            // we can sync two calls, but this decrease exec speed
            visitCount.incrementAndGet();
            users.add(userId);
        } catch (InterruptedException e) {
            log.debug("Inserting visit to queue was interrupted");
        }
    }

    @Autowired
    DdlRunner ddlRunner;

    private static final ThreadFactory daemonThreadFactory = r -> {
        Thread t = Executors.defaultThreadFactory().newThread(r);
        t.setDaemon(true);
        return t;
    };

    public static final int BATCH_SIZE = 1000;

    @Transactional
    void insertBatch (List<Visit> visitBatch) {
        for (Visit visit : visitBatch) {
            visitDb.insertVisit(visit);
        }
    }

    private static final Visit POISON_PILL = new Visit(Integer.MIN_VALUE, Integer.MIN_VALUE, null);

    private Runnable insertVisitors = () -> {
        List<Visit> visitBatch = new ArrayList<>(BATCH_SIZE);

        try {
            while (true) {
                Visit visit = visits.poll(500, TimeUnit.MILLISECONDS);

                if (visit != null) {
                    if (POISON_PILL.equals(visit)) return;

                    visitBatch.add(visit);
                }

                if ((visit == null && visitBatch.size() > 0) || visitBatch.size() >= BATCH_SIZE) {
                    insertBatch(visitBatch);
                    visitBatch.clear();
                    LogManager.getLogger(getClass()).debug("Queue size = "+visits.size());
                }
            }
        } catch (InterruptedException e) {
            LogManager.getLogger(getClass()).error("Insert visitor thread was");
        }
    };

    private ExecutorService dataUploader = Executors.newCachedThreadPool(daemonThreadFactory);

    public static final int UPLOADERS_COUNT = 8;

    @PostConstruct
    void init() {
        ddlRunner.initDb();

        for (int i = 0; i < UPLOADERS_COUNT; i++) {
            dataUploader.submit(insertVisitors);
        }
    }

    @PreDestroy
    void close() {
        try {
            for (int i = 0; i < UPLOADERS_COUNT; i++) {
                visits.put(POISON_PILL);
            }

            dataUploader.awaitTermination(20, TimeUnit.SECONDS);

            dataUploader.shutdownNow();
            dataUploader.awaitTermination(20, TimeUnit.SECONDS);

            LogManager.getLogger(getClass()).debug("dataUploader finished");
        } catch (InterruptedException e) {
            LogManager.getLogger(getClass()).error("dataUploader was terminated ungracefully");
        }
    }
}
