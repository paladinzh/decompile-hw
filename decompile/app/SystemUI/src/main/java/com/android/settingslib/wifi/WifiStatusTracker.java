package com.android.settingslib.wifi;

import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import com.android.settingslib.WirelessUtils;
import java.util.List;

public class WifiStatusTracker {
    public boolean connected;
    public boolean enabled;
    public int level;
    private final WifiManager mWifiManager;
    public int rssi;
    public String ssid;

    public WifiStatusTracker(WifiManager wifiManager) {
        this.mWifiManager = wifiManager;
    }

    public void handleBroadcast(Intent intent) {
        boolean z = false;
        String action = intent.getAction();
        if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
            if (intent.getIntExtra("wifi_state", 4) == 3) {
                z = true;
            }
            this.enabled = z;
        } else if (action.equals("android.net.wifi.STATE_CHANGE")) {
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
            if (networkInfo != null) {
                z = networkInfo.isConnected();
            }
            this.connected = z;
            if (this.connected) {
                WifiInfo info;
                if (intent.getParcelableExtra("wifiInfo") != null) {
                    info = (WifiInfo) intent.getParcelableExtra("wifiInfo");
                } else {
                    info = this.mWifiManager.getConnectionInfo();
                }
                if (info != null) {
                    this.ssid = getSsid(info);
                } else {
                    this.ssid = null;
                }
            } else if (!this.connected) {
                this.ssid = null;
            }
        } else if (action.equals("android.net.wifi.RSSI_CHANGED")) {
            this.rssi = intent.getIntExtra("newRssi", -200);
            this.level = WirelessUtils.calculateSignalLevel(this.rssi);
        }
    }

    private String getSsid(WifiInfo info) {
        String ssid = info.getSSID();
        if (ssid != null) {
            return ssid;
        }
        List<WifiConfiguration> networks = this.mWifiManager.getConfiguredNetworks();
        int length = networks.size();
        for (int i = 0; i < length; i++) {
            if (((WifiConfiguration) networks.get(i)).networkId == info.getNetworkId()) {
                return ((WifiConfiguration) networks.get(i)).SSID;
            }
        }
        return null;
    }
}
