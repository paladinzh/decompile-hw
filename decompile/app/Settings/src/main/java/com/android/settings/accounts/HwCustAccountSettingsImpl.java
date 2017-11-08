package com.android.settings.accounts;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.provider.SettingsEx.Systemex;

public class HwCustAccountSettingsImpl extends HwCustAccountSettings {
    public static final String AAB_PACKAGE_NAME = "com.huawei.android.ds";
    public static final String AAB_STARTUP_KEY = "startupkey";
    static final String COMPLETED_STATE = "startup_completed";
    static final String CURRENT_STATE_KEY = "startup_state";
    static final String ENTRY_POINT_AS_SETTINGSMODULE = "entry_point_settings_module";
    public static final String START_AAB_INIT_ACTIVITY_ACTION = "com.huawei.android.startup.init";

    public boolean handleCustIntialization(CharSequence mTitle, Context context) {
        if (SystemProperties.getBoolean("ro.config.att.aab", false)) {
            String currentRegState = Systemex.getString(context.getContentResolver(), CURRENT_STATE_KEY);
            String mAttTitle = context.getResources().getString(2131629155);
            if ((currentRegState == null || !currentRegState.equalsIgnoreCase(COMPLETED_STATE)) && mTitle != null && mTitle.equals(mAttTitle)) {
                startAABClient(context);
                return true;
            }
        }
        return false;
    }

    private void startAABClient(Context context) {
        Intent lIntent = new Intent(START_AAB_INIT_ACTIVITY_ACTION);
        lIntent.setPackage(AAB_PACKAGE_NAME);
        lIntent.putExtra(AAB_STARTUP_KEY, false);
        lIntent.putExtra(ENTRY_POINT_AS_SETTINGSMODULE, true);
        lIntent.setFlags(268435456);
        try {
            context.startActivity(lIntent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
}
