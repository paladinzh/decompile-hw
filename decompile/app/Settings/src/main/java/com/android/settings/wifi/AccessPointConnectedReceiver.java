package com.android.settings.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.util.Log;
import com.android.settings.ItemUseStat;
import com.android.settings.wifi.cmcc.WifiExt;

public class AccessPointConnectedReceiver extends BroadcastReceiver {
    private static boolean mLastDisconnect = false;
    private static boolean mLastDisconnectCmcc = true;

    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            handleEvent(context, intent);
        }
    }

    private void handleEvent(Context context, Intent intent) {
        String action = intent.getAction();
        if ("android.net.wifi.STATE_CHANGE".equals(action)) {
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
            if (networkInfo != null) {
                if (DetailedState.CONNECTED.equals(networkInfo.getDetailedState())) {
                    WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService("wifi");
                    if (wifiManager.isWifiEnabled()) {
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        if (wifiInfo == null) {
                            mLastDisconnect = false;
                            mLastDisconnectCmcc = false;
                        } else if (wifiInfo.getWifiSsid() == null) {
                            mLastDisconnect = false;
                            mLastDisconnectCmcc = false;
                            Log.d("AccessPointConnectedReceiver", "WifiSsid is null!");
                        } else if (isArpReconnect(context, wifiInfo.getSSID())) {
                            mLastDisconnect = false;
                            mLastDisconnectCmcc = false;
                            setArpReconnect(context, false, null);
                        } else {
                            setArpReconnect(context, false, null);
                            if (wifiInfo.getSupplicantState() != SupplicantState.COMPLETED) {
                                mLastDisconnect = false;
                                mLastDisconnectCmcc = false;
                            } else if (mLastDisconnectCmcc && WifiExt.showCmccWarring(context, wifiInfo.getSSID())) {
                                mLastDisconnectCmcc = false;
                                mLastDisconnect = false;
                            } else if (mLastDisconnect) {
                                Log.d("AccessPointConnectedReceiver", "wifi connected to:" + wifiInfo.getSSID());
                                mLastDisconnect = false;
                                if (1 == System.getInt(context.getContentResolver(), "wlan_switch_on", 0)) {
                                    System.putInt(context.getContentResolver(), "wlan_switch_on", 0);
                                    ItemUseStat.getInstance().handleClick(context, 2, "wlan_connected", 1);
                                }
                            }
                        }
                    }
                } else if (!networkInfo.isConnectedOrConnecting()) {
                    mLastDisconnect = true;
                    mLastDisconnectCmcc = true;
                }
            }
        } else if ("android.net.wifi.ARP_RECONNECT_WIFI".equals(action)) {
            setArpReconnect(context, true, intent.getStringExtra("ssid"));
        }
    }

    private void setArpReconnect(Context context, boolean isArpReconnect, String ssid) {
        Secure.putInt(context.getContentResolver(), "wifi_is_apr_reconnect", isArpReconnect ? 1 : 0);
        Secure.putString(context.getContentResolver(), "wifi_apr_reconnect_ssid", ssid);
    }

    private boolean isArpReconnect(Context context, String ssid) {
        boolean isArpReconnect = false;
        if (ssid == null) {
            return false;
        }
        if (1 == Secure.getInt(context.getContentResolver(), "wifi_is_apr_reconnect", 0)) {
            isArpReconnect = ssid.equals(Secure.getString(context.getContentResolver(), "wifi_apr_reconnect_ssid"));
        }
        if (isArpReconnect) {
            Log.e("AccessPointConnectedReceiver", "isArpReconnect: true");
        }
        return isArpReconnect;
    }
}
