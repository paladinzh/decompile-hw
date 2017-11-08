package com.android.settings.wifi;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.provider.Settings.System;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.android.settingslib.R$string;
import com.android.settingslib.wifi.AccessPoint;
import java.util.List;

public class WifiExtUtils {
    public static void setSelection(View root, Bundle savedInstanceState) {
        new WifiPlatformImp().setSelection(root, savedInstanceState);
    }

    public static int getDrawable(WifiP2pDevice device, int rssi) {
        return new WifiPlatformImp().getDrawable(device, rssi);
    }

    public static void setBeamPushUrisCallback(Activity activity, String uriString) {
        new WifiPlatformImp().setBeamPushUrisCallback(activity, uriString);
    }

    public static List<Object> buildHideList(AccessPoint accessPoint, int mode, View root) {
        return new WifiPlatformImp().buildHideList(accessPoint, mode, root);
    }

    public static void setPasswordView(TextView passwordView, View root) {
        new WifiPlatformImp().setPasswordView(passwordView, root);
    }

    public static void setManualConnect(Context context) {
        if (context != null) {
            System.putInt(context.getContentResolver(), "wifipro_manual_connect_ap", 1);
        }
    }

    public static int getSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return 1;
        }
        if (result.capabilities.contains("WAPI-PSK") || result.capabilities.contains("QUALCOMM-WAPI-PSK")) {
            return 4;
        }
        if (result.capabilities.contains("WAPI-CERT") || result.capabilities.contains("QUALCOMM-WAPI-CERT")) {
            return 5;
        }
        if (result.capabilities.contains("PSK")) {
            return 2;
        }
        if (result.capabilities.contains("EAP")) {
            return 3;
        }
        return 0;
    }

    public static int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(1)) {
            return 2;
        }
        if (config.allowedKeyManagement.get(2) || config.allowedKeyManagement.get(3)) {
            return 3;
        }
        if (config.wepKeys[0] != null) {
            return 1;
        }
        if (config.allowedKeyManagement.get(6) || config.allowedKeyManagement.get(8)) {
            return 4;
        }
        if (config.allowedKeyManagement.get(7) || config.allowedKeyManagement.get(9)) {
            return 5;
        }
        return 0;
    }

    public static boolean setWifiApEnabled(Context context, WifiManager wifiManager, WifiConfiguration wifiConfig, boolean enabled) {
        if (wifiManager != null) {
            try {
                return wifiManager.setWifiApEnabled(wifiConfig, enabled);
            } catch (SecurityException e) {
                e.printStackTrace();
                if (context != null) {
                    Toast.makeText(context, context.getString(R$string.tethering_settings_not_available), 0).show();
                }
            }
        }
        return false;
    }

    public static boolean isWifiConnected(Context context) {
        boolean wifiConnected = false;
        if (context == null) {
            return false;
        }
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService("connectivity");
        if (connectivity != null) {
            wifiConnected = connectivity.getNetworkInfo(1).isConnected();
        }
        return wifiConnected;
    }
}
