package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.provider.SettingsEx.Systemex;
import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class TimeZoneChangedReceiver extends BroadcastReceiver {
    private static final boolean mSupportDT = SystemProperties.getBoolean("ro.config_hw_doubletime", false);

    public void onReceive(Context context, Intent intent) {
        if (mSupportDT && "android.intent.action.TIMEZONE_CHANGED".equals(intent.getAction())) {
            boolean isRoaming;
            Log.d("TimeZoneChangedReceiver", "receive android.intent.action.TIMEZONE_CHANGED and support double time.");
            if (MSimTelephonyManager.getDefault().isMultiSimEnabled()) {
                isRoaming = MSimTelephonyManager.getDefault().isNetworkRoaming(Utils.getMainCardSlotId());
            } else {
                isRoaming = TelephonyManager.from(context).isNetworkRoaming();
            }
            if (isRoaming) {
                String newTimeZone = intent.getStringExtra("time-zone");
                String localTimeZone = Systemex.getString(context.getContentResolver(), "localtime_zone_id");
                if (newTimeZone != null) {
                    if (!(!newTimeZone.equals(localTimeZone) ? "Asia/Shanghai".equals(newTimeZone) : true)) {
                        Systemex.putString(context.getContentResolver(), "localtime_zone_id", newTimeZone);
                        Intent in = new Intent(intent);
                        in.addFlags(872415232);
                        in.setClass(context, TimeSchemeChooseActivity.class);
                        context.startActivity(in);
                    }
                }
            } else {
                Log.d("TimeZoneChangedReceiver", "the phone is not roaming.");
            }
        }
    }
}
