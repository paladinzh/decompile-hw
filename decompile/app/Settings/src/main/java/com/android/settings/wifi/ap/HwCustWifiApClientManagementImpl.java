package com.android.settings.wifi.ap;

import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.support.v7.preference.ListPreference;

public class HwCustWifiApClientManagementImpl extends HwCustWifiApClientManagement {
    private static final String IS_DISABLE_ADD_ALLOWED_DEVICE = "ro.config.disable_add_device";
    private static final String KEY_DEVICES_ALLOW_STATUS = "devices_allow_status";

    public HwCustWifiApClientManagementImpl(WifiApClientManagement mWifiApClientManagement) {
        super(mWifiApClientManagement);
    }

    public void enableDevicesAllowStatus() {
        boolean z = false;
        if (SystemProperties.getBoolean(IS_DISABLE_ADD_ALLOWED_DEVICE, false)) {
            ListPreference mDevicesAllowStatus = (ListPreference) this.mWifiApClientManagement.getPreferenceScreen().findPreference(KEY_DEVICES_ALLOW_STATUS);
            if (!((WifiManager) this.mWifiApClientManagement.getContext().getApplicationContext().getSystemService("wifi")).isWifiApEnabled()) {
                z = true;
            }
            mDevicesAllowStatus.setEnabled(z);
        }
    }
}
