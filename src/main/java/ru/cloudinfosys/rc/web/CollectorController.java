package ru.cloudinfosys.rc.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.cloudinfosys.rc.beans.VisitResult;
import ru.cloudinfosys.rc.serv.Counter;

@RestController
public class CollectorController {
    @Autowired
    Counter counter;

    @RequestMapping("/visit")
    public VisitResult visit(@RequestParam("userId") int userId, @RequestParam("pageId") int pageId) {
        counter.visit(userId, pageId);

        return new VisitResult(0, 0);
    }
}
