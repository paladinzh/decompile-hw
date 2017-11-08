package com.android.settingslib;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.provider.Settings.Global;
import android.util.Log;
import com.huawei.android.net.wifi.WifiManagerCommonEx;

public class WirelessUtils {
    public static boolean isRadioAllowed(Context context, String type) {
        if (!isAirplaneModeOn(context)) {
            return true;
        }
        String toggleable = Global.getString(context.getContentResolver(), "airplane_mode_toggleable_radios");
        return toggleable != null ? toggleable.contains(type) : false;
    }

    public static boolean isAirplaneModeOn(Context context) {
        return Global.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0;
    }

    public static int calculateSignalLevel(int rssi) {
        try {
            int level = WifiManagerCommonEx.calculateSignalLevelHW(rssi) - 1;
            if (level == -1) {
                return 4;
            }
            return level;
        } catch (NoClassDefFoundError error) {
            Log.e("WirelessUtils", "NoClassDefFoundError, error msg: " + error.getMessage());
            return WifiManager.calculateSignalLevel(rssi, 4);
        } catch (NoSuchMethodError error2) {
            Log.e("WirelessUtils", "NoSuchMethodError , error msg: " + error2.getMessage());
            return WifiManager.calculateSignalLevel(rssi, 4);
        } catch (Exception e) {
            Log.e("WirelessUtils", "Exception , error msg: " + e.getMessage());
            return WifiManager.calculateSignalLevel(rssi, 4);
        }
    }

    public static int calculateSignalLevelUnrevised(int rssi) {
        try {
            return WifiManagerCommonEx.calculateSignalLevelHW(rssi);
        } catch (NoClassDefFoundError error) {
            Log.e("WirelessUtils", "NoClassDefFoundError, error msg: " + error.getMessage());
            return WifiManager.calculateSignalLevel(rssi, 4);
        } catch (NoSuchMethodError error2) {
            Log.e("WirelessUtils", "NoSuchMethodError , error msg: " + error2.getMessage());
            return WifiManager.calculateSignalLevel(rssi, 4);
        } catch (Exception e) {
            Log.e("WirelessUtils", "Exception , error msg: " + e.getMessage());
            return WifiManager.calculateSignalLevel(rssi, 4);
        }
    }
}
