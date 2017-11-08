package com.android.settings.wifi.ap;

import android.net.wifi.WifiConfiguration;
import android.view.View;

public class HwCustWifiApDialogActivity {
    public WifiApDialogActivity mWifiApDialogActivity;

    public HwCustWifiApDialogActivity(WifiApDialogActivity wifiApDialogActivity) {
        this.mWifiApDialogActivity = wifiApDialogActivity;
    }

    public void onCustCreate() {
    }

    public void onCustClick(View view) {
    }

    public void custConfig(WifiConfiguration config) {
    }

    public boolean isSetWifiApBand2G() {
        return false;
    }

    public boolean isSetWifiApBand5G(String countryCode) {
        return false;
    }
}
