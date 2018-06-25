package ru.cloudinfosys.rc.beans;

public class VisitResult {
    private int dayCount;
    private int uniqueUserCount;

    public VisitResult() {
    }

    public VisitResult(int dayCount, int uniqueUserCount) {
        this.dayCount = dayCount;
        this.uniqueUserCount = uniqueUserCount;
    }

    public int getDayCount() {
        return dayCount;
    }

    public void setDayCount(int dayCount) {
        this.dayCount = dayCount;
    }

    public int getUniqueUserCount() {
        return uniqueUserCount;
    }

    public void setUniqueUserCount(int uniqueUserCount) {
        this.uniqueUserCount = uniqueUserCount;
    }
}
