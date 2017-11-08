package com.android.settings.wifi;

import android.content.IntentFilter;

public class HwCustWifiEnabler {
    protected static final String TAG = "HwCustWifiEnabler";
    public static final String WIFI_STATE_DISABLE_HISI_ACTION = "android.net.hisi.wifi.WIFI_STATE_DISABLE";
    WifiEnabler mWifiEnabler;

    public HwCustWifiEnabler(WifiEnabler wifiEnabler) {
        this.mWifiEnabler = wifiEnabler;
    }

    @Deprecated
    public void addAction(IntentFilter mIntentFilter) {
    }

    @Deprecated
    public boolean isSupportStaP2pCoexist() {
        return true;
    }
}
