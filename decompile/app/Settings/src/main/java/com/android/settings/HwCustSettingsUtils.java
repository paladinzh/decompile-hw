package com.android.settings;

import android.os.SystemProperties;

public class HwCustSettingsUtils {
    public static final boolean IS_SOS_ENABLED = SystemProperties.getBoolean("ro.config.enable_sos", false);
    public static final boolean IS_SPRINT;
    public static final boolean IS_SPRINT_DSS = SystemProperties.getBoolean("ro.config.sprint_dss", false);

    private HwCustSettingsUtils() {
    }

    static {
        boolean equals;
        if ("237".equals(SystemProperties.get("ro.config.hw_opta", "0"))) {
            equals = "840".equals(SystemProperties.get("ro.config.hw_optb", "0"));
        } else {
            equals = false;
        }
        IS_SPRINT = equals;
    }

    public static boolean isFlagPersistentNotificationEnabled() {
        return SystemProperties.getBoolean("ro.config.enable_persist_noti", false);
    }
}
