package ru.cloudinfosys.rc.beans;

public class Stat {
    private int visitCount;
    private int uniqueUserCount;
    private int loyalUserCount;

    public Stat() {
    }

    public Stat(int visitCount, int uniqueUserCount, int loyalUserCount) {
        this.visitCount = visitCount;
        this.uniqueUserCount = uniqueUserCount;
        this.loyalUserCount = loyalUserCount;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(int visitCount) {
        this.visitCount = visitCount;
    }

    public int getUniqueUserCount() {
        return uniqueUserCount;
    }

    public void setUniqueUserCount(int uniqueUserCount) {
        this.uniqueUserCount = uniqueUserCount;
    }

    public int getLoyalUserCount() {
        return loyalUserCount;
    }

    public void setLoyalUserCount(int loyalUserCount) {
        this.loyalUserCount = loyalUserCount;
    }
}
