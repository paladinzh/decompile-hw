package com.android.settings;

import android.content.Context;

public class HwCustWirelessSettings {
    public WirelessSettings mWirelessSettings;

    public HwCustWirelessSettings(WirelessSettings wirelessSettings) {
        this.mWirelessSettings = wirelessSettings;
    }

    public boolean isNfcDisabled(Context context) {
        return false;
    }

    public void updateCustPreference(Context context) {
    }

    public void updateEnable4GPreferenceTitle(CustomSwitchPreference mPreference) {
    }

    public void removeCustPreference(Context context, String key) {
    }
}
