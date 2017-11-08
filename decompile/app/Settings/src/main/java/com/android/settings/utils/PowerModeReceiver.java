package com.android.settings.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.android.settings.SettingsPreferenceFragment;

public class PowerModeReceiver extends BroadcastReceiver {
    SettingsPreferenceFragment mSettingsPreferenceFragment;

    public PowerModeReceiver(SettingsPreferenceFragment mFragment) {
        this.mSettingsPreferenceFragment = mFragment;
    }

    public void registerReceiver(Context mContext) {
        mContext.registerReceiver(this, new IntentFilter("huawei.intent.action.POWER_MODE_CHANGED_ACTION"));
    }

    public void unregisterReceiver(Context mContext) {
        mContext.unregisterReceiver(this);
    }

    public void onReceive(Context context, Intent intent) {
        int powerState = 2;
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("huawei.intent.action.POWER_MODE_CHANGED_ACTION")) {
                    powerState = intent.getIntExtra("state", 2);
                }
                if (powerState == 1) {
                    this.mSettingsPreferenceFragment.applyLowPowerMode(true);
                } else if (powerState == 2) {
                    this.mSettingsPreferenceFragment.applyLowPowerMode(false);
                }
            }
        }
    }
}
