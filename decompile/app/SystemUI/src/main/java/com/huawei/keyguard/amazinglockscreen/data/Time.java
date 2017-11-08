package com.huawei.keyguard.amazinglockscreen.data;

public class Time {
    private String mAMPM;
    private String mHour1;
    private String mHour2;
    private String mMin1;
    private String mMin2;
    private boolean mShowAMPM;

    public Time(String hour1, String hour2, String min1, String min2, String ampm, boolean showampm) {
        this.mHour1 = hour1;
        this.mHour2 = hour2;
        this.mMin1 = min1;
        this.mMin2 = min2;
        this.mAMPM = ampm;
        this.mShowAMPM = showampm;
    }

    public String getHour1() {
        return this.mHour1;
    }

    public void setHour1(String hour1) {
        this.mHour1 = hour1;
    }

    public String getHour2() {
        return this.mHour2;
    }

    public void setHour2(String hour2) {
        this.mHour2 = hour2;
    }

    public String getMin1() {
        return this.mMin1;
    }

    public void setMin1(String min1) {
        this.mMin1 = min1;
    }

    public String getMin2() {
        return this.mMin2;
    }

    public void setMin2(String min2) {
        this.mMin2 = min2;
    }

    public String getNow() {
        return toString();
    }

    public void setNow(String now) {
        this.mHour1 = now.substring(0, 1);
        this.mHour2 = now.substring(1, 2);
        this.mMin1 = now.substring(3, 4);
        this.mMin2 = now.substring(4, 5);
    }

    public void setShowampm(boolean showAMPM) {
        this.mShowAMPM = showAMPM;
    }

    public boolean getShowampm() {
        return this.mShowAMPM;
    }

    public void setAmpm(String str) {
        this.mAMPM = str;
    }

    public String getAmpm() {
        return this.mAMPM;
    }

    public String toString() {
        return this.mHour1 + this.mHour2 + ":" + this.mMin1 + this.mMin2;
    }
}
