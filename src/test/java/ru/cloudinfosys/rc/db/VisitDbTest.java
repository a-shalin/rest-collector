package ru.cloudinfosys.rc.db;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import ru.cloudinfosys.rc.beans.Period;
import ru.cloudinfosys.rc.beans.Visit;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class VisitDbTest {
    @Autowired
    VisitDb visitDb;

    @Test
    public void testGetCurrentVisitCount() {
        assertTrue(visitDb.getCurrentVisitCount() >= 0);
    }

    @Test
    public void testGetCurrentUniqueUserCount() {
        assertTrue(visitDb.getCurrentUniqueUserCount() >= 0);
    }

    @Test
    public void testCurrentUniqueAndVisitConsistency() {
        assertTrue(visitDb.getCurrentVisitCount() >= visitDb.getCurrentUniqueUserCount());
    }

    @Test
    @Transactional
    public void testInsertVisitAndUniqueCount() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1899);
        Date docDate = calendar.getTime();

        assertEquals(0, visitDb.getPeriodUniqueUserCount(new Period(docDate, docDate)));
        assertEquals(0, visitDb.getPeriodVisitCount(new Period(docDate, docDate)));

        visitDb.insertVisit(new Visit(22, 33, docDate));
        visitDb.insertVisit(new Visit(23, 33, docDate));
        visitDb.insertVisit(new Visit(23, 33, docDate));
        visitDb.insertVisit(new Visit(24, 33, docDate));
        visitDb.insertVisit(new Visit(24, 33, docDate));

        assertEquals(3, visitDb.getPeriodUniqueUserCount(new Period(docDate, docDate)));
        assertEquals(5, visitDb.getPeriodVisitCount(new Period(docDate, docDate)));
    }
}