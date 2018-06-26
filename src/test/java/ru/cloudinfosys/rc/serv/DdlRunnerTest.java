package ru.cloudinfosys.rc.serv;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DdlRunnerTest {
    @Autowired
    DdlRunner ddlRunner;

    @Test
    @Ignore
    public void initDbTest() {
        ddlRunner.clearDb();
        assertFalse(ddlRunner.isDbInitialized());

        ddlRunner.initDb();
        assertTrue(ddlRunner.isDbInitialized());
    }


}