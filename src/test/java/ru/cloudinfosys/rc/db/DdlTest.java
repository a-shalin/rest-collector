package ru.cloudinfosys.rc.db;

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
public class DdlTest {
    @Autowired
    Ddl ddl;

    @Test
    @Ignore
    public void initDbTest() {
        boolean isInit = ddl.isDbInitialized();

        if (isInit) {
            ddl.dropVisit();
            assertFalse(ddl.isDbInitialized());

            ddl.createVisit();
            assertTrue(ddl.isDbInitialized());
        } else {
            ddl.createVisit();
            assertTrue(ddl.isDbInitialized());
        }
    }
}