package com.huawei.systemmanager.rainbow.db.bean;

import android.database.Cursor;

public class AddviewBean {
    private String mPackageName;
    private String mPermissionCfg;

    public static AddviewBean fromCursor(Cursor c, int pkgIndex, int perIndex) {
        AddviewBean result = new AddviewBean();
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
        return "[AddviewBean]mPackageName:" + this.mPackageName + ",mPermissionCfg:" + this.mPermissionCfg;
    }
}
