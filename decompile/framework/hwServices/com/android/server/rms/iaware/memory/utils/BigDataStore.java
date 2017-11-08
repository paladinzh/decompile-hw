package com.android.server.rms.iaware.memory.utils;

import android.rms.iaware.AwareLog;

public final class BigDataStore {
    private static final String TAG = "BigDataStore";
    private static BigDataStore sInstance;
    public long aboveThresholdTime = 0;
    public long belowThresholdTime = 0;
    public long belowThresholdTimeBegin = 0;
    public long coldStartCount = 0;
    public long lmkOccurCount = 0;
    public long lmkOccurCountStash = 0;
    public long lowMemoryManageCount = 0;
    public long meminfoAllocCount = 0;
    public long meminfoAllocCountStash = 0;
    public long slowPathAllocCount = 0;
    public long slowPathAllocCountStash = 0;
    public long totalStartCount = 0;
    public long totalTimeBegin = 0;
    public long totalTimeEnd = 0;

    private BigDataStore() {
    }

    public static synchronized BigDataStore getInstance() {
        BigDataStore bigDataStore;
        synchronized (BigDataStore.class) {
            if (sInstance == null) {
                sInstance = new BigDataStore();
            }
            bigDataStore = sInstance;
        }
        return bigDataStore;
    }

    public long getLmkOccurCount() {
        this.lmkOccurCount = MemoryReader.getLmkOccurCount();
        return this.lmkOccurCount;
    }

    public void getMeminfoAllocCount() {
        try {
            for (String str : MemoryReader.getMeminfoAllocCount()) {
                String[] temp = str.split(":");
                if (temp.length == 2) {
                    if ("Total page alloc count".equals(temp[0])) {
                        this.meminfoAllocCount = Long.parseLong(temp[1]);
                    } else if ("Total slow path page alloc count".equals(temp[0])) {
                        this.slowPathAllocCount = Long.parseLong(temp[1]);
                    }
                }
            }
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "NumberFormatException...");
        }
    }
}
