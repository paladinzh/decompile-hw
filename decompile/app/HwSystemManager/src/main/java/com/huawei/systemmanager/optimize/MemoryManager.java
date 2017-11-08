package com.huawei.systemmanager.optimize;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import com.android.internal.util.MemInfoReader;
import com.huawei.systemmanager.util.HwLog;

public class MemoryManager {
    private static final String TAG = "MemoryManager";

    public static class HsmMemoryInfo {
        private final MemoryInfo mMemoryInfo;

        public HsmMemoryInfo(MemoryInfo info) {
            this.mMemoryInfo = info;
        }

        public long getTotal() {
            return this.mMemoryInfo.totalMem;
        }

        public long getFree() {
            return this.mMemoryInfo.availMem;
        }

        public long getUsed() {
            return getTotal() - getFree();
        }

        public int getUsedPercent() {
            long total = getTotal();
            if (total > 0) {
                return (int) ((getUsed() * 100) / total);
            }
            HwLog.e(MemoryManager.TAG, "There must be wrong! totalmemory is:" + total);
            return 0;
        }
    }

    public static int getCurrentMemPercent(Context context) {
        return getMemoryInfo(context).getUsedPercent();
    }

    public static HsmMemoryInfo getMemoryInfo(Context ctx) {
        MemoryInfo outInfo = new MemoryInfo();
        if (ctx == null) {
            HwLog.e(TAG, "getMemoryInfo called, but context is null!");
            return new HsmMemoryInfo(outInfo);
        }
        ((ActivityManager) ctx.getSystemService("activity")).getMemoryInfo(outInfo);
        long huaweiRam = getHuaweiRam();
        if (huaweiRam == 0) {
            HwLog.i(TAG, "getMemoryInfo can not get huawei ram");
        } else {
            outInfo.totalMem = huaweiRam;
        }
        return new HsmMemoryInfo(outInfo);
    }

    private static long getHuaweiRam() {
        try {
            return Long.parseLong((String) Class.forName("com.huawei.android.util.SystemInfo").getMethod("getDeviceRam", new Class[0]).invoke(null, new Object[0])) * 1024;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return 0;
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
            return 0;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            return 0;
        } catch (Exception e4) {
            e4.printStackTrace();
            return 0;
        }
    }

    public static long getFreeMemoryWithBackground(Context ctx) {
        if (ctx == null) {
            HwLog.e(TAG, "getFreeMemoryWithBackground ctx is null!");
            return 0;
        }
        MemInfoReader memInfoReader = new MemInfoReader();
        memInfoReader.readMemInfo();
        ActivityManager activiyManager = (ActivityManager) ctx.getSystemService("activity");
        RunningState runningtate = new RunningState();
        runningtate.update(ctx, activiyManager);
        return (memInfoReader.getFreeSize() + memInfoReader.getCachedSize()) + runningtate.getBackgroundProcessMemory();
    }

    public static long getTotal(Context ctx) {
        long total = getHuaweiRam();
        if (total != 0) {
            return total;
        }
        ActivityManager am = (ActivityManager) ctx.getSystemService("activity");
        MemoryInfo info = new MemoryInfo();
        am.getMemoryInfo(info);
        return info.totalMem;
    }
}
