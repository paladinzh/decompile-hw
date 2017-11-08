package com.huawei.systemmanager.rainbow.db.bean;

import android.database.Cursor;

public class FetchAppListBean {
    private String mPackageName;
    private String mPermissionCfg;

    public static FetchAppListBean fromCursor(Cursor c, int pkgIndex, int perIndex) {
        FetchAppListBean result = new FetchAppListBean();
        result.mPackageName = c.getString(pkgIndex);
        result.mPermissionCfg = c.getString(perIndex);
        return result;
    }

    public String getPkgName() {
        return this.mPackageName;
    }

    public String getPermissionCfg() {
        return this.mPermissionCfg;
    }

    public String toString() {
        return "[FetchAppListBean]mPackageName:" + this.mPackageName + ",mPermissionCfg:" + this.mPermissionCfg;
    }
}
