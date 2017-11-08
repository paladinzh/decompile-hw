package com.huawei.mms.util;

public class TextSpan {
    private int end;
    private long mCurrentTime;
    private int mDateType;
    private String mUrl;
    private int spanType;
    private int start;

    public TextSpan(String url, int start, int end, int spanType) {
        this.start = start;
        this.end = end;
        this.spanType = spanType;
        this.mUrl = url;
    }

    public TextSpan(String url, int start, int end, int spanType, int dateType, long currentTime) {
        this.start = start;
        this.end = end;
        this.spanType = spanType;
        this.mUrl = url;
        this.mDateType = dateType;
        this.mCurrentTime = currentTime;
    }

    public int getStart() {
        return this.start;
    }

    public int getEnd() {
        return this.end;
    }

    public int getSpanType() {
        return this.spanType;
    }

    public String getUrl() {
        return this.mUrl;
    }

    public int getDateType() {
        return this.mDateType;
    }

    public long getCurrentTime() {
        return this.mCurrentTime;
    }
}
