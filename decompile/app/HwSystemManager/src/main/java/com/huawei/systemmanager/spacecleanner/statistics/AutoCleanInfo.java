package com.huawei.systemmanager.spacecleanner.statistics;

public class AutoCleanInfo {
    private long mCleanedTrashSize = 0;
    private long mETime = 0;
    private boolean mIsCleanTimeOut = false;
    private boolean mIsScanTimeOut = false;
    private long mSTime = 0;

    public long getSTime() {
        return this.mSTime;
    }

    public void setSTime(long sTime) {
        this.mSTime = sTime;
    }

    public long getETime() {
        return this.mETime;
    }

    public void setETime(long eTime) {
        this.mETime = eTime;
    }

    public boolean isScanTimeOut() {
        return this.mIsScanTimeOut;
    }

    public void setScanTimeOut(boolean isScanTimeOut) {
        this.mIsScanTimeOut = isScanTimeOut;
    }

    public boolean isCleanTimeOut() {
        return this.mIsCleanTimeOut;
    }

    public void setCleanTimeOut(boolean isCleanTimeOut) {
        this.mIsCleanTimeOut = isCleanTimeOut;
    }

    public long getCleanedTrashSize() {
        return this.mCleanedTrashSize;
    }

    public void setCleanedTrashSize(long mCleanedTrashSize) {
        this.mCleanedTrashSize = mCleanedTrashSize;
    }

    public String toString() {
        int i = 1;
        String format = "DIFF=%d,SIZE=%d,SCAN_TIMEOUT=%d,CLEAN_TIMEOUT=%d";
        Object[] objArr = new Object[4];
        objArr[0] = Long.valueOf(this.mETime - this.mSTime);
        objArr[1] = Long.valueOf(this.mCleanedTrashSize);
        objArr[2] = Integer.valueOf(this.mIsScanTimeOut ? 1 : 0);
        if (!this.mIsCleanTimeOut) {
            i = 0;
        }
        objArr[3] = Integer.valueOf(i);
        return String.format(format, objArr);
    }
}
