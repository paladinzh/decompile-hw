package com.huawei.systemmanager.power.ui;

import android.graphics.drawable.Drawable;

public class ConsumeDetailInfo {
    private Drawable mIcon;
    private String mPkgName;
    private String mPkgTitle;
    private int mPowerLevel;
    private double mPowerValue;

    public Drawable getmIcon() {
        return this.mIcon;
    }

    public void setmIcon(Drawable mIcon) {
        this.mIcon = mIcon;
    }

    public String getmPkgTitle() {
        return this.mPkgTitle;
    }

    public void setmPkgTitle(String mPkgTitle) {
        this.mPkgTitle = mPkgTitle;
    }

    public String getmPkgName() {
        return this.mPkgName;
    }

    public void setmPkgName(String mPkgName) {
        this.mPkgName = mPkgName;
    }

    public double getmPowerValue() {
        return this.mPowerValue;
    }

    public void setmPowerValue(double mPowerValue) {
        this.mPowerValue = mPowerValue;
    }

    public int getmPowerLevel() {
        return this.mPowerLevel;
    }

    public void setmPowerLevel(int mPowerLevel) {
        this.mPowerLevel = mPowerLevel;
    }
}
