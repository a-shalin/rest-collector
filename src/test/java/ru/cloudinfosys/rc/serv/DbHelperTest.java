package ru.cloudinfosys.rc.serv;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import ru.cloudinfosys.rc.beans.Period;
import ru.cloudinfosys.rc.db.VisitDb;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DbHelperTest {
    @Autowired
    DbHelper dbHelper;

    @Test
    @Ignore
    public void initDbTest() {
        dbHelper.clearDb();
        assertFalse(dbHelper.isDbInitialized());

        dbHelper.initDb();
        assertTrue(dbHelper.isDbInitialized());
    }

    @Autowired
    VisitDb visitDb;

    @Test
    @Transactional
    public void testSampleData() {
        dbHelper.prepareSampleData();

        assertEquals(57000,
                visitDb.getPeriodVisitCount(new Period(DbHelper.SAMPLE_BEG_DATE, DbHelper.SAMPLE_END_DATE)));
    }
}