package com.android.settings;

import android.os.SystemProperties;

public class HwCustNotificationAndStatusSettingsImpl extends HwCustNotificationAndStatusSettings {
    public boolean isHideNetworkSpeed() {
        return SystemProperties.getBoolean("ro.config.hw_hideNetworkSpeed", false);
    }

    public boolean isHideNetworkName() {
        return SystemProperties.getBoolean("ro.config.hw_hide_operator_name", false);
    }
}
