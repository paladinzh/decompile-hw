package com.huawei.keyguard.policy;

import android.os.SystemClock;
import com.huawei.keyguard.util.HwLog;

public class FingerBlackCounter {
    private static long sLockoutDeadline = 0;
    private static int sVerifyFailcount = 0;

    public static void resetErrorCount() {
        synchronized (FingerBlackCounter.class) {
            sVerifyFailcount = 0;
            sLockoutDeadline = 0;
        }
    }

    public static int getVerifyFailCount() {
        int i;
        synchronized (FingerBlackCounter.class) {
            i = sVerifyFailcount;
        }
        return i;
    }

    public static boolean shouldLockout() {
        if (getVerifyFailCount() < 30) {
            return false;
        }
        if (getRemainingTime() > 0) {
            return true;
        }
        resetErrorCount();
        return false;
    }

    public static boolean addVerifyFailCount() {
        synchronized (FingerBlackCounter.class) {
            sVerifyFailcount++;
            if (sVerifyFailcount % 30 == 0) {
                HwLog.i("FP-Black", "Black counter reach max");
                sLockoutDeadline = SystemClock.elapsedRealtime() + 30000;
                return true;
            }
            return false;
        }
    }

    public static long getRemainingTime() {
        long j;
        long current = SystemClock.elapsedRealtime();
        synchronized (FingerBlackCounter.class) {
            j = sLockoutDeadline > current ? sLockoutDeadline - current : 0;
        }
        return j;
    }
}
