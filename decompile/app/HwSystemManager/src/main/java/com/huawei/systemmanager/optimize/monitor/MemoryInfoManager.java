package com.huawei.systemmanager.optimize.monitor;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.os.SystemProperties;
import com.huawei.systemmanager.util.HwLog;

public final class MemoryInfoManager {
    private static final int BUFFSIZE = 8192;
    private static final String LOW_MEMORY_LIMIT = "ro.config.hsm_low_mem";
    private static final String TAG = "MemoryInfoManager";
    private static final int TOTALMEMCOLMUN = 1;
    private Context mContext = null;

    public MemoryInfoManager(Context context) {
        this.mContext = context;
    }

    public long getAvailMemory() {
        ActivityManager activityManager = (ActivityManager) this.mContext.getSystemService("activity");
        MemoryInfo memoryInfo = new MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo.availMem;
    }

    public long getMemoryLimit(Context context) {
        long memLimit = SystemProperties.getLong(LOW_MEMORY_LIMIT, -1);
        if (memLimit <= 0) {
            ActivityManager am = (ActivityManager) context.getSystemService("activity");
            MemoryInfo outInfo = new MemoryInfo();
            am.getMemoryInfo(outInfo);
            memLimit = outInfo.secondaryServerThreshold / 1048576;
        }
        HwLog.v(TAG, "getMemoryLimit is" + memLimit);
        return memLimit;
    }
}
