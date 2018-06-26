package ru.cloudinfosys.rc.beans;

import java.util.Date;

public class Visit {
    public static final String USER_ID = "userId";
    public static final String PAGE_ID = "pageId";
    public static final String DOC_DATE = "docDate";

    private int userId;
    private int pageId;
    private Date docDate;

    public Visit() {
    }

    public Visit(int userId, int pageId, Date docDate) {
        this.userId = userId;
        this.pageId = pageId;
        this.docDate = docDate;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public Date getDocDate() {
        return docDate;
    }

    public void setDocDate(Date docDate) {
        this.docDate = docDate;
    }
}
