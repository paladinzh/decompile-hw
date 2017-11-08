package com.android.settingslib;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.widget.Toast;

public class TetherUtil {
    public static ComponentName TETHER_SERVICE = ComponentName.unflattenFromString(Resources.getSystem().getString(17039414));

    public static boolean setWifiTethering(boolean enable, Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService("wifi");
        ContentResolver cr = context.getContentResolver();
        int wifiState = wifiManager.getWifiState();
        boolean success = setWifiApEnabled(context, wifiManager, null, enable);
        if (enable && (wifiState == 2 || wifiState == 3)) {
            wifiManager.setWifiEnabled(false);
            Global.putInt(cr, "wifi_saved_state", 1);
        }
        if (!enable && Global.getInt(cr, "wifi_saved_state", 0) == 1) {
            wifiManager.setWifiEnabled(true);
            Global.putInt(cr, "wifi_saved_state", 0);
        }
        return success;
    }

    public static boolean isProvisioningNeeded(Context context) {
        boolean z = false;
        String[] provisionApp = context.getResources().getStringArray(17235992);
        if (SystemProperties.getBoolean("net.tethering.noprovisioning", false) || provisionApp == null) {
            return false;
        }
        if (provisionApp.length == 2) {
            z = true;
        }
        return z;
    }

    public static boolean isTetheringSupported(Context context) {
        boolean isSecondaryUser;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        if (ActivityManager.getCurrentUser() != 0) {
            isSecondaryUser = true;
        } else {
            isSecondaryUser = false;
        }
        if (isSecondaryUser) {
            return false;
        }
        return cm.isTetheringSupported();
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
}
