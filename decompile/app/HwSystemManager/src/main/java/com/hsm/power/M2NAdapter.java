package com.hsm.power;

import android.os.BatteryStats.Uid;
import com.android.internal.os.PowerProfile;

public class M2NAdapter {
    public static int getNumSpeedStepsInCpuCluster(PowerProfile pf, int index) {
        return pf.getNumSpeedStepsInCpuCluster(index);
    }

    public static long getTimeAtCpuSpeed(Uid u, int cluster, int step, int which) {
        return u.getTimeAtCpuSpeed(cluster, step, which);
    }

    public static int getNumCpuClusters(PowerProfile pf) {
        return pf.getNumCpuClusters();
    }

    public static double getAveragePowerForCpu(PowerProfile pf, int cluster, int speed) {
        return pf.getAveragePowerForCpu(cluster, speed);
    }
}
