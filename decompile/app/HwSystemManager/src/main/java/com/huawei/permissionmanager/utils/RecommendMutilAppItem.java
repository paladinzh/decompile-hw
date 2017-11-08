package com.huawei.permissionmanager.utils;

public class RecommendMutilAppItem {
    private static final String LOG_TAG = "RecommendMutilAppItem";
    private int mAppUid;
    private int mPermissionCfg;
    private int mPermissionCode;
    private String mPkgName;
    private boolean mTrust = false;

    public RecommendMutilAppItem(boolean trust, int uid, String pkgName, int permissionCode, int permissionCfg) {
        this.mTrust = trust;
        this.mAppUid = uid;
        this.mPkgName = pkgName;
        this.mPermissionCode = permissionCode;
        this.mPermissionCfg = permissionCfg;
    }

    public String getAppPkgName() {
        return this.mPkgName;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{ ");
        buf.append("PkgName[").append(this.mPkgName).append("] ");
        buf.append("TrustStatus[").append(this.mTrust).append("] ");
        buf.append("AppUid[").append(this.mAppUid).append("] ");
        buf.append("PermissionCode[").append(this.mPermissionCode).append("] ");
        buf.append("PermissionCfg[").append(this.mPermissionCfg).append("] ");
        buf.append("} ");
        return buf.toString();
    }
}
