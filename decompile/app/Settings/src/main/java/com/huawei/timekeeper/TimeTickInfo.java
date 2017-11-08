package com.huawei.timekeeper;

public class TimeTickInfo {
    private int mHour;
    private long mMillisUntilFinished;
    private int mMinute;
    private int mSecond;

    protected void setTime(long millisUntilFinished) {
        this.mMillisUntilFinished = millisUntilFinished;
        int minutes = ((int) (millisUntilFinished / 60000)) + 1;
        if (minutes > 1) {
            this.mHour = minutes / 60;
            this.mMinute = minutes % 60;
            this.mSecond = 0;
            return;
        }
        this.mHour = 0;
        this.mMinute = 0;
        this.mSecond = (int) (millisUntilFinished / 1000);
    }

    protected void setTime(long millisUntilFinished, int hour, int minute, int second) {
        this.mMillisUntilFinished = millisUntilFinished;
        this.mHour = hour;
        this.mMinute = minute;
        this.mSecond = second;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[mMillisUntilFinished:").append(this.mMillisUntilFinished);
        sb.append(", mHour:").append(this.mHour);
        sb.append(", mMinute:").append(this.mMinute);
        sb.append(", mSecond:").append(this.mSecond);
        sb.append("]");
        return sb.toString();
    }

    public int getHour() {
        return this.mHour;
    }

    public int getMinute() {
        return this.mMinute;
    }

    public int getSecond() {
        return this.mSecond;
    }
}
