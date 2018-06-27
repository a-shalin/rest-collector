package ru.cloudinfosys.rc.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.cloudinfosys.rc.beans.Period;
import ru.cloudinfosys.rc.beans.Stat;
import ru.cloudinfosys.rc.beans.VisitResult;
import ru.cloudinfosys.rc.serv.Counter;
import ru.cloudinfosys.rc.serv.DbHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
public class CollectorController {
    @Autowired
    Counter counter;

    @RequestMapping("/visit")
    public VisitResult visit(@RequestParam("userId") int userId, @RequestParam("pageId") int pageId) {
        return counter.visit(userId, pageId);
    }

    @Autowired
    DbHelper dbHelper;

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

            CompletableFuture<Integer> visitCount = dbHelper.getPeriodVisitCount(period);
            CompletableFuture<Integer> uniqueUserCount = dbHelper.getPeriodUniqueUserCount(period);
            CompletableFuture<Integer> loyalUserCount = dbHelper.getPeriodLoyalUserCount(period);
            CompletableFuture.allOf(visitCount, uniqueUserCount, loyalUserCount).join();

            return new Stat(visitCount.get(), uniqueUserCount.get(), loyalUserCount.get());
        } catch (ParseException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
