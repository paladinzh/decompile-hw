package com.android.settings.wifi;

import android.content.Context;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceGroup;

public class HwCustAdvancedWifiSettings {
    protected static final String TAG = "HwCustAdvancedWifiSettings";
    public Context mContext;

    public HwCustAdvancedWifiSettings(Context context) {
        this.mContext = context;
    }

    public boolean getIsShowsleepPolicyPref() {
        return false;
    }

    public void removeSleepPolicyPref(PreferenceGroup mWifiSettingsCategory, ListPreference sleepPolicyPref) {
    }

    public void initCustPreference(AdvancedWifiSettings advancedWifiSettings) {
    }

    public void resumeCustPreference(AdvancedWifiSettings advancedWifiSettings) {
    }

    public void onCustPreferenceChange(String key, Object newValue) {
    }
}
