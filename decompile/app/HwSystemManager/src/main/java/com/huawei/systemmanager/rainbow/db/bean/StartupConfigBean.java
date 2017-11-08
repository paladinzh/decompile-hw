package com.huawei.systemmanager.rainbow.db.bean;

import android.database.Cursor;

public class StartupConfigBean {
    private String mIsControlledValue;
    private String mPkgName;
    private String mReceiverValue;
    private String mServiceProviderValue;

    public static StartupConfigBean fromCursor(Cursor cursor, int pkgIndex, int recIndex, int serIndex, int ctlIndex) {
        StartupConfigBean result = new StartupConfigBean();
        result.mPkgName = cursor.getString(pkgIndex);
        result.mReceiverValue = cursor.getString(recIndex);
        result.mServiceProviderValue = cursor.getString(serIndex);
        result.mIsControlledValue = cursor.getString(ctlIndex);
        return result;
    }

    public boolean isReceiverAllowStart() {
        return "0".equals(this.mReceiverValue);
    }

    public boolean isProviderServiceAllowStart() {
        return "0".equals(this.mServiceProviderValue);
    }

    @Deprecated
    public boolean isPkgControlled() {
        return "0".equals(this.mIsControlledValue);
    }

    public String getPkgName() {
        return this.mPkgName;
    }

    public String toString() {
        return "[StartupConfigBean]mPkgName:" + this.mPkgName + ",mReceiverValue:" + this.mReceiverValue + ",mServiceProviderValue:" + this.mServiceProviderValue + ",mIsControlledValue:" + this.mIsControlledValue;
    }
}
