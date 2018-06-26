package ru.cloudinfosys.rc.web;

import org.apache.log4j.Logger;
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

    @Test
    public void visit() throws Exception {
        ExecutorService es = Executors.newWorkStealingPool();

        for (int i = 0; i < 10; i++) {
            es.submit(() -> {
                try {
                    while (true) {
                        if (Thread.interrupted()) return;

                        int userId = ThreadLocalRandom.current().nextInt(0, 100000);
                        int pageId = ThreadLocalRandom.current().nextInt(0, 1000000);

                        mockMvc.perform(get("/visit")
                                .param(Visit.USER_ID, String.valueOf(userId))
                                .param(Visit.PAGE_ID, String.valueOf(pageId))
                                .accept(contentType)).andExpect(status().isOk());
                    }
                } catch (InterruptedException e) {
                    Logger.getLogger(CollectorControllerTest.class).debug("Thread interrupted");
                } catch (Exception e) {
                    Logger.getLogger(CollectorControllerTest.class).error("Error interrupted thread", e);
                }
            });
        }

        TimeUnit.SECONDS.sleep(5);
        es.shutdown();

        es.awaitTermination(50, TimeUnit.MILLISECONDS);
        es.shutdownNow();

        Logger.getLogger(getClass()).debug("userCount = " + counter.getUserCount() +
                ", visitCount = " + counter.getVisitCount());
    }

}