package com.huawei.systemmanager.power.data.stats;

import com.huawei.systemmanager.util.HwLog;

public class StatsUtils {
    private static final String TAG = StatsUtils.class.getSimpleName();

    public static boolean isNotifyLowAngleForHighPowerApp(long batteryRealTime, double usedPower, int highLevelStd) {
        HwLog.v(TAG, "isNotifyLowAngleForHighPowerApp brt: " + batteryRealTime + ", power: " + usedPower);
        if (batteryRealTime / 1000000 > 86400 / ((long) highLevelStd) || usedPower < 10800.0d) {
            return false;
        }
        return true;
    }
}
