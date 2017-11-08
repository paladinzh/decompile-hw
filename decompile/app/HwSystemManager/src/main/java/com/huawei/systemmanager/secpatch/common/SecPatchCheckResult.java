package com.huawei.systemmanager.secpatch.common;

import com.huawei.systemmanager.util.HwLog;

public class SecPatchCheckResult {
    private long mCheckAllVersion = 0;
    private long mCheckAvaVersion = 0;
    private int mSrvCode = -1;

    public SecPatchCheckResult(int srvCode) {
        this.mSrvCode = srvCode;
    }

    public SecPatchCheckResult(int srvCode, long allVersion) {
        this.mSrvCode = srvCode;
        this.mCheckAllVersion = allVersion;
    }

    public SecPatchCheckResult(int srvCode, long allVersion, long avaVersion) {
        this.mSrvCode = srvCode;
        this.mCheckAllVersion = allVersion;
        this.mCheckAvaVersion = avaVersion;
    }

    public long getCheckAllVersion() {
        return this.mCheckAllVersion;
    }

    public long getCheckAvaVersion() {
        return this.mCheckAvaVersion;
    }

    public boolean getResponseCodeValidStatus() {
        return this.mSrvCode == 0;
    }

    public void printVersionInfoToLog(String logTag) {
        HwLog.d(logTag, "allVersion : " + getCheckAllVersion() + ", avaVersion : " + getCheckAvaVersion());
    }
}
