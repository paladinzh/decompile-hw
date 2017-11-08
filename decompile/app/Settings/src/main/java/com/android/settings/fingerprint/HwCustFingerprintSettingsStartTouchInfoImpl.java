package com.android.settings.fingerprint;

import android.os.SystemProperties;

public class HwCustFingerprintSettingsStartTouchInfoImpl extends HwCustFingerprintSettingsStartTouchInfo {
    private static final boolean FP_SHOW_NOTIFICATION_ON = SystemProperties.getBoolean("ro.config.fp_add_notification", false);
    private static final boolean HAS_FP_CUST_NAVIGATION = SystemProperties.getBoolean("ro.config.fp_navigation_plk", false);
    private static final boolean HAS_FP_NAVIGATION = SystemProperties.getBoolean("ro.config.fp_navigation", false);

    public int getCustLayout(int layoutId) {
        if (HAS_FP_CUST_NAVIGATION) {
            return 2130968808;
        }
        if (FP_SHOW_NOTIFICATION_ON && HAS_FP_NAVIGATION) {
            return 2130968806;
        }
        if (FP_SHOW_NOTIFICATION_ON) {
            return 2130968809;
        }
        return layoutId;
    }
}
