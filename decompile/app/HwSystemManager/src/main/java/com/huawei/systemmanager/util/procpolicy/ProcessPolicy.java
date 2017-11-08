package com.huawei.systemmanager.util.procpolicy;

import com.huawei.systemmanager.util.content.HsmIntentService_bg;
import com.huawei.systemmanager.util.content.HsmIntentService_ui;

public class ProcessPolicy {
    public Class<?> getIntentServiceClass() {
        if (ProcessUtil.getInstance().isUiProcess()) {
            return HsmIntentService_ui.class;
        }
        if (ProcessUtil.getInstance().isServiceProcess()) {
            return HsmIntentService_bg.class;
        }
        return null;
    }

    public boolean shouldCheckConsisteny() {
        return ProcessUtil.getInstance().isUiProcess();
    }

    public boolean shouldInitTmsEngine() {
        return ProcessUtil.getInstance().isUiProcess();
    }

    public boolean shouldCheckSpaceSetting() {
        return ProcessUtil.getInstance().isServiceProcess();
    }

    public static boolean shouldCheckNetworkSetting() {
        return ProcessUtil.getInstance().isServiceProcess();
    }

    public static boolean shoudlCacheLabel() {
        return ProcessUtil.getInstance().isUiProcess();
    }

    public static boolean shouldDoFirstPermissionInit() {
        return ProcessUtil.getInstance().isServiceProcess();
    }

    public static boolean shouldEnableWifiSec() {
        return ProcessUtil.getInstance().isServiceProcess();
    }
}
