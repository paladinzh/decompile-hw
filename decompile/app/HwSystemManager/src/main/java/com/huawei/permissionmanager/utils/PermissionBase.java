package com.huawei.permissionmanager.utils;

public class PermissionBase {
    private int mPermissionBaseCfg = 0;
    private int mPermissionBaseCode = 0;

    public PermissionBase(int permissionCode, int permissionCfg) {
        this.mPermissionBaseCode = permissionCode;
        this.mPermissionBaseCfg = permissionCfg;
    }

    public int getPermissionCode() {
        return this.mPermissionBaseCode;
    }

    public int getPermissionCfg() {
        return this.mPermissionBaseCfg;
    }
}
