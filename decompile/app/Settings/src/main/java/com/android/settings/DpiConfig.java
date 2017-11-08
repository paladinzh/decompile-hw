package com.android.settings;

public class DpiConfig {
    private int mDpiValue;
    private int mHeight;
    private String mLargeDpi;
    private String mMidDpi;
    private int mNumber;
    private String mSmallDpi;
    private int mWidth;

    public int getNumber() {
        return this.mNumber;
    }

    public void setNumber(int mNumber) {
        this.mNumber = mNumber;
    }

    public int getDpiValue() {
        return this.mDpiValue;
    }

    public void setDpiValue(int mDpiValue) {
        this.mDpiValue = mDpiValue;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public void setWidth(int mWidth) {
        this.mWidth = mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public void setHeight(int mHeight) {
        this.mHeight = mHeight;
    }

    public String getSmallDpi() {
        return this.mSmallDpi;
    }

    public void setSmallDpi(String mSmallDpi) {
        this.mSmallDpi = mSmallDpi;
    }

    public String getMidDpi() {
        return this.mMidDpi;
    }

    public void setMidDpi(String mMidDpi) {
        this.mMidDpi = mMidDpi;
    }

    public String getLargeDpi() {
        return this.mLargeDpi;
    }

    public void setLargeDpi(String mLargeDpi) {
        this.mLargeDpi = mLargeDpi;
    }
}
