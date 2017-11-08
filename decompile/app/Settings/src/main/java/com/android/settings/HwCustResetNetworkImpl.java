package com.android.settings;

import android.os.SystemProperties;

public class HwCustResetNetworkImpl extends HwCustResetNetwork {
    public String hideSimCardIndex(int size, String oldName) {
        if (size == 1 && SystemProperties.getBoolean("ro.config.hide_simcard_index", false)) {
            return oldName.replaceFirst("\\d+", "");
        }
        return oldName;
    }
}
