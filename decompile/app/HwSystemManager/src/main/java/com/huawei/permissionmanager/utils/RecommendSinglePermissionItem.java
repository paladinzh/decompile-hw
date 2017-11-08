package com.huawei.permissionmanager.utils;

public class RecommendSinglePermissionItem {
    private int mPermissionOperation;
    private String mPkgName;

    public RecommendSinglePermissionItem(int operation, String pkgName) {
        this.mPermissionOperation = operation;
        this.mPkgName = pkgName;
    }

    public int getPermissionOperation() {
        return this.mPermissionOperation;
    }

    public String getPackageName() {
        return this.mPkgName;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{ ");
        buf.append("PermissionOperation[").append(this.mPermissionOperation).append("] ");
        buf.append("PkgName[").append(this.mPkgName).append("] ");
        buf.append("} ");
        return buf.toString();
    }
}
