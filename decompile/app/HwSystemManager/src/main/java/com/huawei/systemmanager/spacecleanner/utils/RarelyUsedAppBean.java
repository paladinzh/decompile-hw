package com.huawei.systemmanager.spacecleanner.utils;

import android.os.Bundle;

public class RarelyUsedAppBean {
    private static final String KEY_APP_NAME = "mAppName";
    private static final String KEY_APP_SIZE = "mAppSize";
    private static final String KEY_DAY_NOT_UNUSED = "mDayNotUsed";
    private static final String KEY_PACKAGE_NAME = "mPackageName";
    private static final String KEY_TIME_STAMP = "mTimestamp";
    private final Bundle data = new Bundle();

    public String getAppName() {
        return this.data.getString(KEY_APP_NAME);
    }

    public void setAppName(String appName) {
        this.data.putString(KEY_APP_NAME, appName);
    }

    public String getPackgeName() {
        return this.data.getString(KEY_PACKAGE_NAME);
    }

    public void setPackageName(String pkgName) {
        this.data.putString(KEY_PACKAGE_NAME, pkgName);
    }

    public long getAppSize() {
        return this.data.getLong(KEY_APP_SIZE);
    }

    public void setAppSize(long size) {
        this.data.putLong(KEY_APP_SIZE, size);
    }

    public int getDayNotUsed() {
        return this.data.getInt(KEY_DAY_NOT_UNUSED);
    }

    public void setDayNotUsed(int day) {
        this.data.putInt(KEY_DAY_NOT_UNUSED, day);
    }

    public long getTimestamp() {
        return this.data.getLong(KEY_TIME_STAMP);
    }

    public void setTimestamp(long time) {
        this.data.putLong(KEY_TIME_STAMP, time);
    }
}
