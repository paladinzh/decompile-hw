package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.SettingsEx.Systemex;
import android.support.v7.preference.PreferenceManager;
import android.telephony.TelephonyManager;

public class SimStateReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
            int simState = TelephonyManager.getDefault().getSimState();
            if (!(simState == 1 || simState == 0 || simState == 2)) {
                updateNitzSettings(context);
            }
            if (("LOADED".equals(intent.getStringExtra("ss")) || "ABSENT".equals(intent.getStringExtra("ss"))) && MCVHandler.isORANGEMCV) {
                new MCVHandler().handleSIMChangeRequest(context, intent);
            }
        }
    }

    private void updateNitzSettings(Context context) {
        boolean isTelephonicaOpt;
        if (SystemProperties.getInt("ro.config.hw_opta", 0) == 40 && SystemProperties.getInt("ro.config.hw_optb", 0) == 999) {
            isTelephonicaOpt = true;
        } else {
            isTelephonicaOpt = false;
        }
        if (isTelephonicaOpt) {
            String mCurrentMccMnc = TelephonyManager.getDefault().getSimOperator();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            Editor edit = prefs.edit();
            if (!prefs.getBoolean("nitz_init_complete", false)) {
                String isTelefonicaMccMnc = Systemex.getString(context.getContentResolver(), "hw_nitz_on_default");
                if (isTelefonicaMccMnc != null) {
                    String[] lOperatorList = isTelefonicaMccMnc.split(",");
                    for (String equals : lOperatorList) {
                        if (equals.equals(mCurrentMccMnc)) {
                            Global.putInt(context.getContentResolver(), "auto_time", 1);
                            Global.putInt(context.getContentResolver(), "auto_time_zone", 1);
                            break;
                        }
                    }
                    edit.putBoolean("nitz_init_complete", true);
                    edit.apply();
                }
            }
        }
    }
}
