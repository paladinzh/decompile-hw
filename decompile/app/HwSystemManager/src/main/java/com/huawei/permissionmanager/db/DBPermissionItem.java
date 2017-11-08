package com.huawei.permissionmanager.db;

public class DBPermissionItem {
    public int mPermissionCfg;
    public int mPermissionCode;
    public String mPkgName;
    public int mTrustCode;

    public DBPermissionItem(String pkgName) {
        this.mPkgName = pkgName;
    }

    public int getValueByType(int type) {
        return DBAdapter.getValue(type, this.mPermissionCode, this.mPermissionCfg);
    }
}
