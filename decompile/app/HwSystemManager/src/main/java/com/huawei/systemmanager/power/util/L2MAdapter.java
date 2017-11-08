package com.huawei.systemmanager.power.util;

import android.os.BatteryStats;
import android.os.BatteryStats.Uid.Proc;
import com.android.internal.os.BatterySipper;

public class L2MAdapter {
    public static long cpuTime(BatterySipper sipper) {
        return sipper.cpuTimeMs;
    }

    public static long cpuFgTime(BatterySipper sipper) {
        return sipper.cpuFgTimeMs;
    }

    public static long gpsTime(BatterySipper sipper) {
        return sipper.gpsTimeMs;
    }

    public static long wifiRunningTime(BatterySipper sipper) {
        return sipper.wifiRunningTimeMs;
    }

    public static void setCpuTime(BatterySipper sipper, long cpuTime) {
        sipper.cpuTimeMs = cpuTime;
    }

    public static void setCpuFgTime(BatterySipper sipper, long cpuFgTime) {
        sipper.cpuFgTimeMs = cpuFgTime;
    }

    public static double value(BatterySipper sipper) {
        return sipper.totalPowerMah;
    }

    public static long wakeLockTime(BatterySipper sipper) {
        return sipper.wakeLockTimeMs;
    }

    public static void setValue(BatterySipper sipper, double val) {
        sipper.totalPowerMah = val;
    }

    public static long getTimeAtCpuSpeedStep(Proc proc, int step, int type) {
        return 0;
    }

    public static long getBluetoothOnTime(BatteryStats stats, long time, int which) {
        return 0;
    }

    public static int getDischargeStepDurations(BatteryStats stats) {
        return stats.getDischargeLevelStepTracker().mNumStepDurations;
    }

    public static long[] getDischargeStepDurationsArray(BatteryStats stats) {
        return stats.getDischargeLevelStepTracker().mStepDurations;
    }
}
