package ru.cloudinfosys.rc.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.cloudinfosys.rc.beans.Period;
import ru.cloudinfosys.rc.beans.Stat;
import ru.cloudinfosys.rc.beans.VisitResult;
import ru.cloudinfosys.rc.db.VisitDb;
import ru.cloudinfosys.rc.serv.Counter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@RestController
public class CollectorController {
    @Autowired
    Counter counter;

    @RequestMapping("/visit")
    public VisitResult visit(@RequestParam("userId") int userId, @RequestParam("pageId") int pageId) {
        return counter.visit(userId, pageId);
    }

    @Autowired
    VisitDb visitDb;

    @RequestMapping(value = "/stat")
    public Stat stat(@RequestParam("begDate") String begDate, @RequestParam("endDate") String endDate) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTime(df.parse(endDate));
            endCalendar.set(Calendar.HOUR_OF_DAY, endCalendar.getActualMaximum(Calendar.HOUR_OF_DAY));
            endCalendar.set(Calendar.MINUTE, endCalendar.getActualMaximum(Calendar.MINUTE));
            endCalendar.set(Calendar.SECOND, endCalendar.getActualMaximum(Calendar.SECOND));
            endCalendar.set(Calendar.MILLISECOND, endCalendar.getActualMaximum(Calendar.MILLISECOND));

            Period period = new Period(df.parse(begDate), endCalendar.getTime());

            int visitCount = visitDb.getPeriodVisitCount(period);
            int uniqueCount = visitDb.getPeriodUniqueUserCount(period);

            return new Stat(visitDb.getPeriodVisitCount(period),
                    visitDb.getPeriodUniqueUserCount(period),
                    visitDb.getPeriodLoyalUserCount(period));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
