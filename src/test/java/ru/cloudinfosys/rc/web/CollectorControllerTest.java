package ru.cloudinfosys.rc.web;

import org.apache.logging.log4j.LogManager;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CollectorControllerTest {
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

    private volatile boolean stopped = false;

    @Test
    public void visit() throws Exception {
        for (int i = 0; i < 50000; i++) {
            int userId = ThreadLocalRandom.current().nextInt(0, 100000);
            int pageId = ThreadLocalRandom.current().nextInt(0, 1000000);

            mockMvc.perform(get("/visit")
                    .param(Visit.USER_ID, String.valueOf(userId))
                    .param(Visit.PAGE_ID, String.valueOf(pageId))
                    .accept(contentType)).andExpect(status().isOk());
        }

//        ExecutorService es = Executors.newWorkStealingPool();
//        stopped = false;
//
//        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
//            es.submit(() -> {
//                try {
//                    while (true) {
//                        if (stopped) {
//                            LogManager.getLogger(CollectorControllerTest.class).debug("Visit generation thread finished");
//                            return;
//                        }
//
//                        int userId = ThreadLocalRandom.current().nextInt(0, 100000);
//                        int pageId = ThreadLocalRandom.current().nextInt(0, 1000000);
//
//                        mockMvc.perform(get("/visit")
//                                .param(Visit.USER_ID, String.valueOf(userId))
//                                .param(Visit.PAGE_ID, String.valueOf(pageId))
//                                .accept(contentType)).andExpect(status().isOk());
//                    }
//                } catch (InterruptedException e) {
//                    LogManager.getLogger(CollectorControllerTest.class).debug("Visit generation thread interrupted");
//                } catch (Exception e) {
//                    LogManager.getLogger(CollectorControllerTest.class).error("Error interrupted visit generation thread", e);
//                }
//            });
//        }
//
//        TimeUnit.SECONDS.sleep(30);
//
//        es.shutdown();
//        stopped = true;
//
//        es.awaitTermination(20, TimeUnit.SECONDS);
//        es.shutdownNow();
//
//        LogManager.getLogger(getClass()).debug("userCount = " + counter.getUserCount() +
//                ", visitCount = " + counter.getVisitCount());
    }

}