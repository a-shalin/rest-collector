package ru.cloudinfosys.rc.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import ru.cloudinfosys.rc.beans.Period;
import ru.cloudinfosys.rc.beans.Visit;
import ru.cloudinfosys.rc.db.VisitDb;
import ru.cloudinfosys.rc.serv.Counter;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CollectorControllerTest {
    private final Logger log = LogManager.getLogger(getClass());
    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Autowired
    Counter counter;

    public static final int ROW_COUNT = 100000;
    public static final int TASK_COUNT = Runtime.getRuntime().availableProcessors();
    private AtomicInteger processedRows = new AtomicInteger(0);

    @Test
    public void testBanchOfVisits() throws Exception {
        long start = System.currentTimeMillis();
        log.info(format("Start processing %d rows", ROW_COUNT));

        ExecutorService es = Executors.newFixedThreadPool(TASK_COUNT);
        processedRows.set(0);

        for (int i = 0; i < TASK_COUNT; i++) {
            es.submit(() -> {
                try {
                    for (int j = 0; j < ROW_COUNT / TASK_COUNT; j++) {
                        int userId = ThreadLocalRandom.current().nextInt(0, 10000)-10000000;
                        int pageId = ThreadLocalRandom.current().nextInt(0, 1000);

                        mockMvc.perform(get("/visit")
                                .param(Visit.USER_ID, String.valueOf(userId))
                                .param(Visit.PAGE_ID, String.valueOf(pageId))
                                .accept(contentType))
                                .andExpect(status().isOk());

                        processedRows.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    log.debug("Visit generation thread interrupted");
                } catch (Exception e) {
                    log.error("Error interrupted visit generation thread", e);
                }
            });
        }

        es.shutdown();
        while (!es.awaitTermination(3, TimeUnit.SECONDS)) {
            log.info(format("Queries processed %d from %d", processedRows.get(), ROW_COUNT));
        }

        long diff = System.currentTimeMillis() - start;
        log.info(format("Processed %d REST calls in %d ms, rate = %2f calls per sec. User count = %d, visit count = %d",
                ROW_COUNT, diff, ROW_COUNT*1000.0/diff, counter.getUserCount(), counter.getVisitCount()));

        // We are waiting for data uploader to finish
        while(!counter.isVisitsQueueEmpty()) {
            TimeUnit.MILLISECONDS.sleep(100);
            Thread.sleep(100);
        }
        // Waiting for last batches to be inserted
        TimeUnit.SECONDS.sleep(2);
        // Remove all inserted data
        visitDb.deleteVisitsByUserId(0-10000000, 10000-10000000);
    }

    @Autowired
    VisitDb visitDb;

    @Test
    public void testStat() throws Exception {
        Calendar beg = Calendar.getInstance();
        beg.set(Calendar.MONTH, Calendar.JANUARY);
        beg.set(Calendar.DAY_OF_MONTH, 1);
        beg.clear(Calendar.HOUR_OF_DAY);
        beg.clear(Calendar.MINUTE);
        beg.clear(Calendar.SECOND);
        beg.clear(Calendar.MILLISECOND);

        Calendar end = Calendar.getInstance();
        end.set(Calendar.HOUR_OF_DAY, beg.getActualMaximum(Calendar.HOUR_OF_DAY));
        end.set(Calendar.MINUTE, beg.getActualMaximum(Calendar.MINUTE));
        end.set(Calendar.SECOND, beg.getActualMaximum(Calendar.SECOND));
        end.set(Calendar.MILLISECOND, beg.getActualMaximum(Calendar.MILLISECOND));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        Period period = new Period(beg.getTime(), end.getTime());

        mockMvc.perform(get("/stat")
                .param(Period.BEG_DATE, df.format(beg.getTime()))
                .param(Period.END_DATE, df.format(end.getTime()))
                .accept(contentType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visitCount", is(visitDb.getPeriodVisitCount(period))))
                .andExpect(jsonPath("$.uniqueUserCount", is(visitDb.getPeriodUniqueUserCount(period))))
                .andExpect(jsonPath("$.loyalUserCount", is(visitDb.getPeriodLoyalUserCount(period))))
        ;
    }
}