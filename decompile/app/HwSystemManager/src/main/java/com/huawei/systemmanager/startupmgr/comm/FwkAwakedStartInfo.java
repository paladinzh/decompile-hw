package com.huawei.systemmanager.startupmgr.comm;

public class FwkAwakedStartInfo {
    public boolean mAllow;
    public int mCallerPid;
    public int mCallerUid;
    public String mPkgName;
    public String mType;

    public FwkAwakedStartInfo(String pkgName, String type, int pid, int uid) {
        this.mPkgName = pkgName;
        this.mType = type;
        this.mCallerPid = pid;
        this.mCallerUid = uid;
        this.mAllow = true;
    }

    public FwkAwakedStartInfo(String pkgName, String type, int pid, int uid, boolean allow) {
        this.mPkgName = pkgName;
        this.mType = type;
        this.mCallerPid = pid;
        this.mCallerUid = uid;
        this.mAllow = allow;
    }

    public String toString() {
        return "FwkAwakeStartInfo package " + this.mPkgName + " called up by pid " + this.mCallerPid + ", uid " + this.mCallerUid + ", type is " + this.mType + ", allow " + this.mAllow;
    }
}
