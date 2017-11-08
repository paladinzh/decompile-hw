package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import com.android.server.location.gnsschrlog.GnssConnectivityLogManager;
import huawei.android.debug.HwDBGSwitchController;

public class GeoWifInfoCollectManager {
    private static final boolean DEBUG = HwDBGSwitchController.getDBGSwitch();
    private static final String TAG = "HwGnssLog_GeoWifiInfoCollectManager";
    private static final int WIFI_SCAN_RESULTS_VALID_TIME = 5000;
    private boolean isWifiApListFlashed = false;
    private Context mContext;
    private long mLastScanTimeStamp = 0;
    private WifiReceiver mWifiReceiver;
    private long mWifiScanDiffTime = 0;

    private class WifiReceiver extends BroadcastReceiver {
        private WifiReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("android.net.wifi.SCAN_RESULTS")) {
                GeoWifInfoCollectManager.this.isWifiApListFlashed = true;
                GeoWifInfoCollectManager.this.mLastScanTimeStamp = System.currentTimeMillis();
            }
        }
    }

    public GeoWifInfoCollectManager(Context context) {
        this.mContext = context;
        wifiStatusInit();
    }

    private void wifiStatusInit() {
        registerBroadcastReciver();
    }

    private void registerBroadcastReciver() {
        this.mWifiReceiver = new WifiReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.SCAN_RESULTS");
        this.mContext.registerReceiver(this.mWifiReceiver, intentFilter);
    }

    public boolean checkWifiInfoAvaiable() {
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService(GnssConnectivityLogManager.SUBSYS_WIFI);
        this.mWifiScanDiffTime = System.currentTimeMillis() - this.mLastScanTimeStamp;
        if (wifiManager != null && wifiManager.isWifiEnabled() && this.isWifiApListFlashed && this.mWifiScanDiffTime <= 5000) {
            return true;
        }
        return false;
    }

    public long getLastScanTimeStamp() {
        return this.mLastScanTimeStamp;
    }

    public void resetWifiApListFlashed() {
        this.isWifiApListFlashed = false;
    }

    public long getWifiScanDiffTime() {
        return this.mWifiScanDiffTime;
    }
}
