package ru.cloudinfosys.rc.db;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

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

}