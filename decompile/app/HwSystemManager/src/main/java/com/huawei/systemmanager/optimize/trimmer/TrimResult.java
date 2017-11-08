package com.huawei.systemmanager.optimize.trimmer;

public class TrimResult {
    private int mForcestopPkgNum;
    private int mRemovedPkgNum;

    TrimResult(int removedPkgNum, int forcestopPkgNum) {
        this.mRemovedPkgNum = removedPkgNum;
        this.mForcestopPkgNum = forcestopPkgNum;
    }

    public int getRemovePkgNum() {
        return this.mRemovedPkgNum;
    }

    public int getForcestopPkgNum() {
        return this.mForcestopPkgNum;
    }
}
