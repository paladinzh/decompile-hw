package com.huawei.systemmanager.spacecleanner.statistics;

import android.content.Context;
import com.huawei.systemmanager.comm.concurrent.HsmExecutor;
import com.huawei.systemmanager.optimize.MemoryManager;

public class TrashInfoBuilder {
    private long mAvaExternalSotrage = 0;
    private long mAvaInnerSotrage = 0;
    private long mETime = 0;
    private long mExternalStorage = 0;
    private long mInnerStorage = 0;
    private long mSTime = 0;
    private long mScanETimeMemory = 0;
    private long mScanSTimeMemory = 0;
    private int mSuggestTrashCount = 0;
    private long mSuggestTrashSize = 0;
    private int mTotalTrashCount = 0;
    private long mTotalTrashSize = 0;

    public TrashInfoBuilder setInnerStorage(long innerStorage) {
        this.mInnerStorage = innerStorage;
        return this;
    }

    public TrashInfoBuilder setAvaInnerStorage(long innerStorage) {
        this.mAvaInnerSotrage = innerStorage;
        return this;
    }

    public TrashInfoBuilder setExternalStorage(long externalStorage) {
        this.mExternalStorage = externalStorage;
        return this;
    }

    public TrashInfoBuilder setAvaExternalStorage(long externalStorage) {
        this.mAvaExternalSotrage = externalStorage;
        return this;
    }

    public TrashInfoBuilder setTotalTrashCount(int totalTrashCount) {
        this.mTotalTrashCount = totalTrashCount;
        return this;
    }

    public TrashInfoBuilder setTotalTrashSize(long totalTrashSize) {
        this.mTotalTrashSize = totalTrashSize;
        return this;
    }

    public TrashInfoBuilder setSuggestTrashCount(int suggestTrashCount) {
        this.mSuggestTrashCount = suggestTrashCount;
        return this;
    }

    public TrashInfoBuilder setSuggestTrashSize(long suggestTrashSize) {
        this.mSuggestTrashSize = suggestTrashSize;
        return this;
    }

    public TrashInfoBuilder setStartTime(long sTime) {
        this.mSTime = sTime;
        return this;
    }

    public TrashInfoBuilder setEndTime(long eTime) {
        this.mETime = eTime;
        return this;
    }

    public TrashInfoBuilder setScanSTimeMemory(final Context context) {
        HsmExecutor.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            public void run() {
                TrashInfoBuilder.this.mScanSTimeMemory = MemoryManager.getFreeMemoryWithBackground(context);
            }
        });
        return this;
    }

    public TrashInfoBuilder setScanETimeMemory(Context context) {
        this.mScanETimeMemory = MemoryManager.getFreeMemoryWithBackground(context);
        return this;
    }

    public String toString() {
        return String.format("AVA_INNER_STORAGE=%d,INNER_STORAGE=%d,AVA_EXTERNAL_STORAGE=%d,EXTERNAL_STORAGE=%d,TOTAL_COUNT=%d,TOTAL_SIZE=%d,SUGGEST_COUNT=%d,SUGGEST_SIZE=%d,DURING_TIME=%d,STIME_MEMORY=%d,ETIME_MEMORY=%d", new Object[]{Long.valueOf(this.mAvaInnerSotrage), Long.valueOf(this.mInnerStorage), Long.valueOf(this.mAvaExternalSotrage), Long.valueOf(this.mExternalStorage), Integer.valueOf(this.mTotalTrashCount), Long.valueOf(this.mTotalTrashSize), Integer.valueOf(this.mSuggestTrashCount), Long.valueOf(this.mSuggestTrashSize), Long.valueOf(this.mETime - this.mSTime), Long.valueOf(this.mScanSTimeMemory), Long.valueOf(this.mScanETimeMemory)});
    }
}
