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
import ru.cloudinfosys.rc.beans.Visit;
import ru.cloudinfosys.rc.serv.Counter;

import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    public static final int ROW_COUNT = 50000;
    public static final int TASK_COUNT = 10;
    private AtomicInteger processedRows = new AtomicInteger(0);

    @Test
    public void testBanchOfVisits() throws Exception {
        ExecutorService es = Executors.newFixedThreadPool(TASK_COUNT);
        processedRows.set(0);

        for (int i = 0; i < TASK_COUNT; i++) {
            es.submit(() -> {
                try {
                    for (int j = 0; j < ROW_COUNT / TASK_COUNT; j++) {
                        int userId = ThreadLocalRandom.current().nextInt(0, 100000);
                        int pageId = ThreadLocalRandom.current().nextInt(0, 1000000);

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
            log.info(String.format("Queries processed %d from %d", processedRows.get(), ROW_COUNT));
        }

        log.info("userCount = " + counter.getUserCount() +
                ", visitCount = " + counter.getVisitCount());
    }

}