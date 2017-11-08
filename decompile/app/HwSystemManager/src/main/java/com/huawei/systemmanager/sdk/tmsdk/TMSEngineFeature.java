package com.huawei.systemmanager.sdk.tmsdk;

import com.huawei.systemmanager.customize.CustomizeManager;
import com.huawei.systemmanager.util.HwLog;

public class TMSEngineFeature {
    public static final String TAG = "TMSEngineFeature";
    private static boolean isSupportTMS = false;

    public static boolean isSupportTMS() {
        return isSupportTMS;
    }

    public static void setSupportTMS(boolean isSupportTMS) {
        isSupportTMS = isSupportTMS;
    }

    public static boolean shouldInitTmsEngine() {
        if (netassistantNeedInitTms() || trashTMSSupportNeedInitTms()) {
            return true;
        }
        return false;
    }

    private static boolean netassistantNeedInitTms() {
        if (!CustomizeManager.getInstance().isFeatureEnabled(30)) {
            return false;
        }
        HwLog.i(TAG, " TMS support enable for netassistant.");
        return true;
    }

    private static boolean trashTMSSupportNeedInitTms() {
        boolean isTMSSupportEnable = CustomizeManager.getInstance().isFeatureEnabled(40);
        if (isTMSSupportEnable) {
            HwLog.i(TAG, " TMS support enable for trash.");
        }
        return isTMSSupportEnable;
    }
}
