package com.android.settings.wifi.ap;

import android.net.wifi.WifiConfiguration;
import com.android.settings.wifi.WifiApEnabler;

public class HwCustWifiApSettings {
    public WifiApSettings mWifiApSettings;

    public HwCustWifiApSettings(WifiApSettings wifiApSettings) {
        this.mWifiApSettings = wifiApSettings;
    }

    public boolean showCustWifiApDialog(WifiApEnabler wifiApEnabler, boolean enable) {
        return false;
    }

    public void custWifiConfiguration(WifiConfiguration config) {
    }

    public void compareWithLastWifiApConfig(WifiConfiguration config) {
    }

    public void registerReceiverForWps() {
    }

    public void unregisterReceiverForWps() {
    }

    public void showWifiApNotification() {
    }
}
