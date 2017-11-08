package com.android.settings.wifi;

import android.content.IntentFilter;
import android.os.SystemProperties;

public class HwCustWifiEnablerImpl extends HwCustWifiEnabler {
    protected static final String TAG = "HwCustWifiEnablerImpl";

    public HwCustWifiEnablerImpl(WifiEnabler wifiEnabler) {
        super(wifiEnabler);
    }

    public void addAction(IntentFilter mIntentFilter) {
        if (!isSupportStaP2pCoexist()) {
            mIntentFilter.addAction(HwCustWifiEnabler.WIFI_STATE_DISABLE_HISI_ACTION);
        }
    }

    public boolean isSupportStaP2pCoexist() {
        if (SystemProperties.get("ro.connectivity.chiptype").equals("hi110x")) {
            return false;
        }
        return true;
    }
}
