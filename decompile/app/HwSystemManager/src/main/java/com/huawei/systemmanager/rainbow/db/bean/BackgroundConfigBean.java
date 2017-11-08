package com.huawei.systemmanager.rainbow.db.bean;

import android.database.Cursor;

public class BackgroundConfigBean {
    private String mIsControlled;
    private String mIsKeyTask;
    private String mIsProtected;
    private String mPkgName;

    public static BackgroundConfigBean fromCursor(Cursor cursor, int pkgIndex, int ctlIndex, int proIndex, int keyIndex) {
        BackgroundConfigBean result = new BackgroundConfigBean();
        result.mPkgName = cursor.getString(pkgIndex);
        result.mIsControlled = cursor.getString(ctlIndex);
        result.mIsProtected = cursor.getString(proIndex);
        result.mIsKeyTask = cursor.getString(keyIndex);
        return result;
    }

    public String getPkgName() {
        return this.mPkgName;
    }

    public boolean isControlled() {
        return "0".equals(this.mIsControlled);
    }

    public boolean isProtected() {
        return "0".equals(this.mIsProtected);
    }

    public boolean isKeyTask() {
        return "0".equals(this.mIsKeyTask);
    }

    public String toString() {
        return "[BackgroundConfigBean]mPkgName:" + this.mPkgName + ",mIsControlled:" + this.mIsControlled + ",mIsProtected:" + this.mIsProtected + ",mIsKeyTask:" + this.mIsKeyTask;
    }
}
