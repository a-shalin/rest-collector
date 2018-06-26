package ru.cloudinfosys.rc.db;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

@Mapper
public interface VisitDb {
    void insertVisit(@Param("userId") int userId, @Param("pageId") int pageId,
                     @Param("docDate") Date docDate);
    int getCurrentUniqueUserCount();
    int getCurrentVisitCount();
}
