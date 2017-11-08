package com.huawei.systemmanager.optimize.monitor;

import android.content.Context;
import android.provider.Settings.System;
import com.huawei.systemmanager.util.HwLog;

public class MemCPUMonitorSwitchManager {
    public static final int MEMCPU_SWITCH_OFF = 0;
    public static final int MEMCPU_SWITCH_ON = 1;
    public static final String PROCESS_MANAGER_SETTING_MEMCPU_SWITCH = "processmanagersetting";

    @Deprecated
    public static int getMemCPUSwitchState(Context context) {
        return System.getInt(context.getContentResolver(), "processmanagersetting", 0);
    }

    public static boolean setMemCPUSwitchState(Context context, int stat) {
        if (stat == 1 || stat == 0) {
            return System.putInt(context.getContentResolver(), "processmanagersetting", stat);
        }
        HwLog.i(MemCPUMonitorSwitchManager.class.getSimpleName(), "stat error! " + stat);
        return false;
    }

    public static boolean isMemCpuSwitchOn(Context cotnext) {
        return getMemCPUSwitchState(cotnext) == 1;
    }
}
