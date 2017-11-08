package com.android.settings.location;

import android.os.SystemProperties;

public class HwCustLocationSettingsBaseImpl extends HwCustLocationSettingsBase {
    public boolean isPowerSaveDefaultChoosen() {
        return SystemProperties.getBoolean("ro.config.defaultGpsSelected", false);
    }
}
