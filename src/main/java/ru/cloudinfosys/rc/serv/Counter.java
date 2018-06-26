package ru.cloudinfosys.rc.serv;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.cloudinfosys.rc.db.Ddl;

import javax.annotation.PostConstruct;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class Counter {
    private AtomicInteger visitCount = new AtomicInteger(0);
    private Set<Integer> users = ConcurrentHashMap.newKeySet();

    public Integer getVisitCount() {
        return visitCount.get();
    }

    public Integer getUserCount() {
        return users.size();
    }

    /** User visits page */
    public void visit(int userId, int pageId) {

        // we can sync two calls, but this decrease exec speed
        visitCount.incrementAndGet();
        users.add(userId);
    }

    @Autowired
    Ddl ddl;

    @PostConstruct
    void init() {
        if (!ddl.isDbInitialized()) {
            ddl.createVisit();
        }
    }
}
