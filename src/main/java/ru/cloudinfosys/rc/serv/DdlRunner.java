package ru.cloudinfosys.rc.serv;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.cloudinfosys.rc.db.Ddl;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;

/** Wrapper around MyBatis ScriptRunner to make easier script running */
@Service
public class DdlRunner {
    @Autowired
    DataSource dataSource;
    @Autowired
    Ddl ddl;

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

    public void initDb() {
        if (!ddl.isDbInitialized()) runScript("sql/init_db.sql");
    }

    public void clearDb() {
        if (ddl.isDbInitialized()) runScript("sql/clear_db.sql");
    }

    public boolean isDbInitialized() {
        return ddl.isDbInitialized();
    }
}
