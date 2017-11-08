package com.huawei.systemmanager.power.model;

public class UsageStatusItem {
    private String date;
    private long screenofftime;
    private long screenontime;
    private long screentime;
    private int type;

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getScreentime() {
        return this.screentime;
    }

    public void setScreentime(long screentime) {
        this.screentime = screentime;
    }

    public long getScreenontime() {
        return this.screenontime;
    }

    public void setScreenontime(long screenontime) {
        this.screenontime = screenontime;
    }

    public long getScreenofftime() {
        return this.screenofftime;
    }

    public void setScreenofftime(long screenofftime) {
        this.screenofftime = screenofftime;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
