package com.huawei.systemmanager.applock.utils;

import android.content.Context;
import com.huawei.systemmanager.comm.grule.GRuleManager;
import com.huawei.systemmanager.comm.grule.scene.monitor.MonitorScenario;

public class AppLockFilterOutUtils {
    private static final String TAG = "AppLockFilterOutUtils";

    public static boolean needFilterOut(Context context, String pkgName) {
        boolean z = true;
        if (context.getPackageName().equals(pkgName)) {
            return true;
        }
        if (GRuleManager.getInstance().shouldMonitor(context, MonitorScenario.SCENARIO_APPLOCK, pkgName)) {
            z = false;
        }
        return z;
    }
}
