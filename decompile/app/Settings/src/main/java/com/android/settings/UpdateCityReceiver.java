package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.provider.SettingsEx.Systemex;
import android.telephony.TelephonyManager;

public class UpdateCityReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.LOCALE_CHANGED".equals(intent.getAction()) && SystemProperties.getBoolean("ro.config_hw_doubletime", false)) {
            boolean isRoaming;
            if (Utils.isMultiSimEnabled()) {
                isRoaming = ((TelephonyManager) context.getSystemService("phone")).isNetworkRoaming(Utils.getMainCardSlotId());
            } else {
                isRoaming = ((TelephonyManager) context.getSystemService("phone")).isNetworkRoaming();
            }
            if (isRoaming) {
                String secondTimezone = Systemex.getString(context.getContentResolver(), "localtime_zone_id");
                if (secondTimezone != null && !"".equals(secondTimezone)) {
                    Systemex.putString(context.getContentResolver(), "secondtime_city_name", TimeZoneUtil.getZoneNameByID(context, secondTimezone));
                }
            }
        }
    }
}
