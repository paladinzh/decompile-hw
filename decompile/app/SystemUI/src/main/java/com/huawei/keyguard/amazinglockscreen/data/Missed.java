package com.huawei.keyguard.amazinglockscreen.data;

public class Missed {
    private int mCount;
    private String mText;

    public Missed(String text, int count) {
        this.mText = text;
        this.mCount = count;
    }

    public String getText() {
        return this.mText;
    }

    public int getCount() {
        return this.mCount;
    }
}
