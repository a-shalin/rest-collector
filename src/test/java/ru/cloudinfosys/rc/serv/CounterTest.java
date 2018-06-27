package ru.cloudinfosys.rc.serv;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CounterTest {
    @Autowired
    Counter counter;

    @Test
    public void testAsyncInsert() {
        int count = 0;

        for (int dayOffset = 0; dayOffset < DbHelper.SAMPLE_DAY_COUNT; dayOffset++) {
            for (int pageId = DbHelper.FIRST_PAGE_ID; pageId < DbHelper.FIRST_PAGE_ID + DbHelper.PAGE_COUNT; pageId++) {
                for (int userId = DbHelper.FIRST_USER_ID; userId < DbHelper.FIRST_USER_ID + DbHelper.USER_COUNT; userId++) {
                    counter.visit(userId, pageId);
                }
            }
        }

    }
}