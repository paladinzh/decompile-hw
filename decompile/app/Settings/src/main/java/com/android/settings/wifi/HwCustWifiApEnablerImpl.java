package com.android.settings.wifi;

import android.support.v7.preference.TwoStatePreference;
import com.android.settings.UtilsCustEx;
import java.io.File;

public class HwCustWifiApEnablerImpl extends HwCustWifiApEnabler {
    public static final String DMPROPERTY_DIRECTORY = "/data/OtaSave/Extensions/";
    public static final String DMPROPERTY_HOTSPOT = "hotspot.disable";

    public void custHotSpotDisable(TwoStatePreference wifiTetherSwitch) {
        if (UtilsCustEx.IS_SPRINT && isHotspotRestricted()) {
            wifiTetherSwitch.setChecked(false);
            wifiTetherSwitch.setEnabled(false);
            wifiTetherSwitch.setSummary(2131629255);
        }
    }

    private boolean isHotspotRestricted() {
        if (new File("/data/OtaSave/Extensions/hotspot.disable").exists()) {
            return true;
        }
        return false;
    }
}
