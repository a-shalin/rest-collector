package ru.cloudinfosys.rc.db;

import org.apache.ibatis.annotations.Flush;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import ru.cloudinfosys.rc.beans.Period;
import ru.cloudinfosys.rc.beans.Visit;

/** Read and write visit's table */
@Mapper
public interface VisitDb {
    void insertVisit(@Param("visit") Visit visit);
    @Flush
    void flush();

    void deleteVisits(@Param("period") Period period);

    int getCurrentUniqueUserCount();
    int getCurrentVisitCount();
    int getPeriodUniqueUserCount(@Param("period") Period period);
    int getPeriodVisitCount(@Param("period") Period period);
}
