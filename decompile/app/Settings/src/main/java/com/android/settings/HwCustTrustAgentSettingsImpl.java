package com.android.settings;

import android.os.SystemProperties;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.PreferenceGroup;
import android.util.Log;

public class HwCustTrustAgentSettingsImpl extends HwCustTrustAgentSettings {
    private static final boolean HIDE_GOOGLELOCK = SystemProperties.getBoolean("ro.config.google_smart_lock", false);
    private static final String TAG = "HwCustTrustAgentSettingsImpl";

    public void hideGoogleSmartLock(PreferenceGroup category, SwitchPreference googleSmartLockPreference) {
        if (HIDE_GOOGLELOCK && category != null && googleSmartLockPreference != null) {
            category.removePreference(googleSmartLockPreference);
            Log.d(TAG, "Removing Google SmartLock Preference");
        }
    }
}
