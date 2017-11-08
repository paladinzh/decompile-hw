package com.huawei.systemmanager.rainbow.db.bean;

import android.database.Cursor;

public class NetworkBean {
    private String mNetDataPermission;
    private String mNetWifiPermission;
    private String mPackageName;

    public static NetworkBean fromCursor(Cursor c, int pkgIndex, int wifiIndex, int dataIndex) {
        NetworkBean result = new NetworkBean();
        result.mPackageName = c.getString(pkgIndex);
        result.mNetWifiPermission = c.getString(wifiIndex);
        result.mNetDataPermission = c.getString(dataIndex);
        return result;
    }

    public String getPkgName() {
        return this.mPackageName;
    }

    public String getWifiPermission() {
        return this.mNetWifiPermission;
    }

    public String getDataPermission() {
        return this.mNetDataPermission;
    }

    public String toString() {
        return "[NetworkBean]mPackageName:" + this.mPackageName + ",mNetWifiPermission:" + this.mNetWifiPermission + ",mNetDataPermission:" + this.mNetDataPermission;
    }
}
