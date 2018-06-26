package ru.cloudinfosys.rc.db;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface Ddl {
    boolean isDbInitialized();
    void dropVisit();
    void createVisit();
}
