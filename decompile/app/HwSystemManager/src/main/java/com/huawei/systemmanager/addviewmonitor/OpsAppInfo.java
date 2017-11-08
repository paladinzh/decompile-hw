package com.huawei.systemmanager.addviewmonitor;

public class OpsAppInfo {
    public boolean mAddViewAllow;
    public String mPkgName;
    public int mUid;

    public OpsAppInfo() {
        this.mPkgName = null;
        this.mUid = -1;
        this.mAddViewAllow = false;
    }

    public OpsAppInfo(AddViewAppInfo appInfo) {
        this.mPkgName = appInfo.mPkgName;
        this.mUid = appInfo.mUid;
        this.mAddViewAllow = appInfo.mAddViewAllow;
    }
}
