package com.android.settings;

import android.content.Context;
import android.support.v7.preference.Preference;

public class HwCustSecuritySettingsHwBase {
    public SecuritySettingsHwBase mSecuritySettingsHwBase;

    public HwCustSecuritySettingsHwBase(SecuritySettingsHwBase securitySettingsHwBase) {
        this.mSecuritySettingsHwBase = securitySettingsHwBase;
    }

    public void updateCustPreference(Context context) {
    }

    public boolean handlePreferenceChange(Preference preference, Object value) {
        return false;
    }

    public void enableOrDisableSimLock(Context context, Preference preference) {
    }
}
