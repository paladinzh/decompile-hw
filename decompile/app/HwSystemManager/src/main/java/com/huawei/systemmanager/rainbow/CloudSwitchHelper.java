package com.huawei.systemmanager.rainbow;

import com.huawei.systemmanager.customize.CustomizeManager;

public class CloudSwitchHelper {
    public static boolean isCloudEnabled() {
        return CustomizeManager.getInstance().isFeatureEnabled(20);
    }
}
