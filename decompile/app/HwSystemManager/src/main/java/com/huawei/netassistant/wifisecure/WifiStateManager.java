package com.huawei.netassistant.wifisecure;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import com.huawei.systemmanager.util.HwLog;

public class WifiStateManager {
    public static final String ACTION_ARP_RECONNECT_WIFI = "android.net.wifi.ARP_RECONNECT_WIFI";
    private static final String EXTRA_SSID = "ssid";
    private static final String IS_ARP_RECONNECT = "is_apr_reconnect";
    private static final String SHARE_PREF_NAME = "WifiStateManager";
    private static final String TAG = "WifiStateManager";
    private boolean mLastDisconnect = false;

    public boolean isNewWifiConnection(Context context, Intent intent) {
        String action = intent.getAction();
        if ("android.net.wifi.STATE_CHANGE".equals(action)) {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService("wifi");
            NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
            if (info == null) {
                return false;
            }
            if (wifiManager.isWifiEnabled() && DetailedState.CONNECTED.equals(info.getDetailedState())) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo == null) {
                    this.mLastDisconnect = false;
                    HwLog.i("WifiStateManager", "isNewWifiConnection: Fail to get connection info ");
                    return false;
                } else if (wifiInfo.getWifiSsid() == null) {
                    this.mLastDisconnect = false;
                    HwLog.i("WifiStateManager", "isNewWifiConnection: wifiInfo.getWifiSsid = null ");
                    return false;
                } else if (isArpReconnect(context, wifiInfo.getSSID())) {
                    this.mLastDisconnect = false;
                    HwLog.i("WifiStateManager", "isNewWifiConnection: isArpReconnect : true ");
                    setArpReconnect(context, false, null);
                    return false;
                } else {
                    setArpReconnect(context, false, null);
                    if (wifiInfo.getSupplicantState() != SupplicantState.COMPLETED) {
                        this.mLastDisconnect = false;
                        HwLog.i("WifiStateManager", "isNewWifiConnection: wifiInfo.getSupplicantState != SupplicantState.COMPLETED");
                        return false;
                    } else if (this.mLastDisconnect) {
                        HwLog.i("WifiStateManager", "isNewWifiConnection: connected to :" + wifiInfo.getSSID());
                        this.mLastDisconnect = false;
                        return true;
                    }
                }
            } else if (!info.isConnectedOrConnecting()) {
                this.mLastDisconnect = true;
            }
        } else if (ACTION_ARP_RECONNECT_WIFI.equals(action)) {
            setArpReconnect(context, true, intent.getStringExtra(EXTRA_SSID));
            HwLog.i("WifiStateManager", "isNewWifiConnection: setArpReconnect = true");
        }
        return false;
    }

    public boolean isWifiDisconnected(Context context, Intent intent) {
        if (!"android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
            return false;
        }
        NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
        if (info == null) {
            HwLog.w("WifiStateManager", "isWifiDisconnected: Fail to get wifi info");
            return false;
        } else if (State.DISCONNECTED != info.getState() || !DetailedState.DISCONNECTED.equals(info.getDetailedState())) {
            return false;
        } else {
            HwLog.i("WifiStateManager", "isWifiDisconnected: disconnect");
            return true;
        }
    }

    private void setArpReconnect(Context context, boolean flag, String ssid) {
        Editor editor = context.getSharedPreferences("WifiStateManager", 0).edit();
        editor.putBoolean(IS_ARP_RECONNECT, flag);
        editor.putString(EXTRA_SSID, ssid);
        editor.commit();
    }

    private boolean isArpReconnect(Context context, String ssid) {
        SharedPreferences sharedPrefenrence = context.getSharedPreferences("WifiStateManager", 0);
        if (sharedPrefenrence.getBoolean(IS_ARP_RECONNECT, false)) {
            return ssid.equals(sharedPrefenrence.getString(EXTRA_SSID, ""));
        }
        return false;
    }
}
