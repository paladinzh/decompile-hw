package com.huawei.systemmanager.comm.misc;

import android.os.SystemProperties;

public class PropConfig {
    public static final boolean FE_ENABLE;
    private static final boolean FE_SECURITY = SystemProperties.getBoolean("ro.config.hw_sm_file_security", false);
    public static final boolean FM_ENABLE;

    static {
        boolean z;
        boolean z2 = true;
        if (FE_SECURITY) {
            z = true;
        } else {
            z = SystemProperties.getBoolean("ro.config.hw_sm_file_encrypt", false);
        }
        FE_ENABLE = z;
        if (!FE_SECURITY) {
            z2 = SystemProperties.getBoolean("ro.config.hw_sm_file_monitor", false);
        }
        FM_ENABLE = z2;
    }
}
