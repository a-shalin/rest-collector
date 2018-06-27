package ru.cloudinfosys.rc.serv;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cloudinfosys.rc.beans.Period;
import ru.cloudinfosys.rc.beans.Visit;
import ru.cloudinfosys.rc.db.Ddl;
import ru.cloudinfosys.rc.db.VisitDb;

import javax.sql.DataSource;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/** Wrapper around MyBatis ScriptRunner to make easier script running */
@Service
public class DbHelper {
    private static final Logger log = LogManager.getLogger(DbHelper.class);

    @Autowired
    DataSource dataSource;
    @Autowired
    Ddl ddl;

    /** Open and run script from classpath */
    public void runScript(String scriptName) {
        try (Connection connection = dataSource.getConnection()) {
            ScriptRunner scriptRunner = new ScriptRunner(connection);
            scriptRunner.setStopOnError(false);
            scriptRunner.setAutoCommit(true);
            scriptRunner.runScript(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(scriptName)));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Init DB structure */
    public void initDb() {
        if (!ddl.isDbInitialized()) runScript("sql/init_db.sql");
    }

    /** Clear DB structure */
    public void clearDb() {
        if (ddl.isDbInitialized()) runScript("sql/clear_db.sql");
    }

    /** Check is DB was initialized */
    public boolean isDbInitialized() {
        return ddl.isDbInitialized();
    }

    public static final Date SAMPLE_BEG_DATE;
    public static final Date SAMPLE_END_DATE;
    public static final int SAMPLE_DAY_COUNT = 20;
    public static final int USER_COUNT = 300;
    public static final int FIRST_USER_ID = 1;
    public static final int PAGE_COUNT = 10;
    public static final int FIRST_PAGE_ID = 500;

    static {
        Calendar calendar = Calendar.getInstance();
        calendar.set(1913, 2, 1, 0,0,0);
        SAMPLE_BEG_DATE = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, SAMPLE_DAY_COUNT);
        SAMPLE_END_DATE = calendar.getTime();

        log.debug("Sample beg date " + SAMPLE_BEG_DATE);
        log.debug("Sample end date " + SAMPLE_END_DATE);
    }

    @Autowired
    VisitDb visitDb;

    /** Prepare batch by inserting to ThreadLocal cache and push it in transaction */
    @Transactional
    public void insertBatch(final List<Visit> visits) {
        for (Visit visit : visits) {
            visitDb.insertVisit(visit);
        }
        visitDb.flush();
    }

    /** Prepare sample data for test purposes */
    @Transactional
    public void prepareSampleData() {
        if (visitDb.getPeriodVisitCount(new Period(SAMPLE_BEG_DATE, SAMPLE_END_DATE)) > 0)
            return;

        Calendar begDate = Calendar.getInstance();
        begDate.setTime(SAMPLE_BEG_DATE);
        long start = System.currentTimeMillis();

        List<Visit> batch = new ArrayList<>(100);

        for (int dayOffset = 0; dayOffset < SAMPLE_DAY_COUNT; dayOffset++) {
            for (int pageId = FIRST_PAGE_ID; pageId < FIRST_PAGE_ID + PAGE_COUNT; pageId++) {
                for (int userId = FIRST_USER_ID; userId < FIRST_USER_ID + USER_COUNT; userId++) {
                    Calendar docDate = Calendar.getInstance();
                    docDate.set(Calendar.YEAR, begDate.get(Calendar.YEAR));
                    docDate.set(Calendar.MONTH, begDate.get(Calendar.MONTH));
                    docDate.set(Calendar.DAY_OF_MONTH, begDate.get(Calendar.DAY_OF_MONTH));
                    docDate.add(Calendar.DAY_OF_MONTH, dayOffset);

                    batch.add(new Visit(userId, pageId, docDate.getTime()));

                    if (batch.size() >= 100) {
                        insertBatch(batch);
                        batch.clear();

                        log.debug(String.format("Batch insert time = %d ms", System.currentTimeMillis() - start));
                        start = System.currentTimeMillis();
                    }
                }
            }
        }

        insertBatch(batch);
    }

    /** Clear sample data from DB */
    @Transactional
    public void clearSampleData() {
        if (visitDb.getPeriodVisitCount(new Period(SAMPLE_BEG_DATE, SAMPLE_END_DATE)) == 0)
            return;

        visitDb.deleteVisits(new Period(SAMPLE_BEG_DATE, SAMPLE_END_DATE));
    }
}
