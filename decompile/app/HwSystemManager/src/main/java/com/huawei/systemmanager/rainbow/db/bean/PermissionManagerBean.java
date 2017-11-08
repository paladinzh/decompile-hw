package com.huawei.systemmanager.rainbow.db.bean;

import android.database.Cursor;

public class PermissionManagerBean {
    private String mPackageName;
    private int mPermissionCfg;
    private int mPermissionCode;
    private String mTrust;

    public static PermissionManagerBean fromCursor(Cursor c, int pkgIndex, int trustIndex, int perCodeIndex, int perCfgIndex) {
        PermissionManagerBean result = new PermissionManagerBean();
        result.mPackageName = c.getString(pkgIndex);
        result.mTrust = c.getString(trustIndex);
        result.mPermissionCode = c.getInt(perCodeIndex);
        result.mPermissionCfg = c.getInt(perCfgIndex);
        return result;
    }

    public String getPkgName() {
        return this.mPackageName;
    }

    public boolean isTrust() {
        return "0".equals(this.mTrust);
    }

    public int getPermissionCode() {
        return this.mPermissionCode;
    }

    public int getPermissionCfg() {
        return this.mPermissionCfg;
    }

    public int getPermValue(int type) {
        int cfg = this.mPermissionCfg & type;
        if ((this.mPermissionCode & type) != type) {
            return 0;
        }
        if (cfg == type) {
            return 2;
        }
        return 1;
    }

    public String toString() {
        return "[PermissionManagerBean]mPackageName:" + this.mPackageName + ",mTrust:" + this.mTrust + ",mPermissionCode:" + this.mPermissionCode + ",mPermissionCfg:" + this.mPermissionCfg;
    }
}
