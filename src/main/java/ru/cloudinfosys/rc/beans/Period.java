package ru.cloudinfosys.rc.beans;

import java.util.Date;

public class Period {
    public static final String BEG_DATE = "begDate";
    public static final String END_DATE = "endDate";

    private Date begDate;
    private Date endDate;

    public Period() {
    }

    public Period(Date begDate, Date endDate) {
        this.begDate = begDate;
        this.endDate = endDate;
    }

    public Date getBegDate() {
        return begDate;
    }

    public void setBegDate(Date begDate) {
        this.begDate = begDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
