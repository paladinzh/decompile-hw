package com.android.settings.location;

import android.support.v14.preference.SwitchPreference;

public class HwCustLocationSettingsBase {
    public int getGpsLocationMode(int mode) {
        return mode;
    }

    public void custGpsEnable(SwitchPreference locationAccess) {
    }

    public boolean isPowerSaveDefaultChoosen() {
        return false;
    }
}
