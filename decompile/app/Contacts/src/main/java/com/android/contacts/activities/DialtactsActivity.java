package com.android.contacts.activities;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.util.HwLog;

public class DialtactsActivity extends TransactionSafeActivity {
    public static final boolean DEBUG = HwLog.HWDBG;

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
        }
        if (ActivityManager.isUserAMonkey()) {
            finish();
        }
    }

    public static Intent getCallSettingsIntent() {
        Intent intent = new Intent("android.intent.action.MAIN");
        if (SimFactoryManager.isDualSim()) {
            intent.setClassName("com.android.phone", "com.android.phone.MSimCallFeaturesSetting");
        } else {
            intent.setClassName("com.android.phone", "com.android.phone.CallFeaturesSetting");
        }
        intent.setFlags(67108864);
        return intent;
    }

    public static Intent getVoiceMailSettingsIntent() {
        Intent intent = new Intent("android.intent.action.MAIN");
        if (SimFactoryManager.isDualSim()) {
            intent.setClassName("com.android.phone", "com.android.phone.MSimCallFeaturesSetting");
        } else {
            intent.setClassName("com.android.phone", "com.android.phone.CallFeaturesSetting");
        }
        intent.putExtra("voicemail", true);
        intent.setFlags(67108864);
        return intent;
    }
}
