package ru.cloudinfosys.rc.beans;

public class Visit {
    public static final String USER_ID = "userId";
    public static final String PAGE_ID = "pageId";


    private int userId;
    private int pageId;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
