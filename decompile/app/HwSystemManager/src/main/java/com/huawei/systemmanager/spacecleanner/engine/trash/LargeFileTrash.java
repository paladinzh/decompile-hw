package com.huawei.systemmanager.spacecleanner.engine.trash;

import com.huawei.systemmanager.comm.Storage.PathEntry;
import com.huawei.systemmanager.power.util.FileAtTimeCheckUtils;
import com.huawei.systemmanager.spacecleanner.engine.base.SpaceConst;
import com.huawei.systemmanager.util.HwLog;

public class LargeFileTrash extends FileTrash {
    private static final String TAG = "LargeFileTrash";
    private long unUsedTime;

    public LargeFileTrash(String file, PathEntry pathEntry) {
        super(file, pathEntry);
        long lastAccess = getLastAccess();
        if (lastAccess <= 0) {
            HwLog.e(TAG, "lastAccess is error,do not report. lastAccess:" + lastAccess);
        } else {
            this.unUsedTime = System.currentTimeMillis() - lastAccess;
        }
    }

    public int getType() {
        return 4;
    }

    public boolean isSuggestClean() {
        return false;
    }

    public long getUnUsedTime() {
        return this.unUsedTime;
    }

    public int getUnUsedDays() {
        return (int) (this.unUsedTime / 86400000);
    }

    public boolean isNotCommonlyUsed() {
        if (getPosition() != 2 || !FileAtTimeCheckUtils.isChangeAtTimeSuccess()) {
            return false;
        }
        long unUsedTime = getUnUsedTime();
        if (unUsedTime <= 0) {
            HwLog.e(TAG, "unUsedTime is error.unUsedTime:" + unUsedTime);
            return false;
        } else if (unUsedTime <= 157680000000L && unUsedTime > SpaceConst.LARGE_FILE_EXCEED_INTERVAL_TIME) {
            return true;
        } else {
            return false;
        }
    }
}
