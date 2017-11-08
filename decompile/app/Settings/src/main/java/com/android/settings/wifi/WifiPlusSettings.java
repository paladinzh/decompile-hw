package com.android.settings.wifi;

import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.TwoStatePreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.ItemUseStat;
import com.android.settings.SettingsExtUtils;
import com.android.settings.SettingsPreferenceFragment;

public class WifiPlusSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    protected int getMetricsCategory() {
        return 100000;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230943);
        if (SettingsExtUtils.isGlobalVersion()) {
            Preference smartSwitchPrefs = findPreference("wifi_plus_smart_switch");
            if (smartSwitchPrefs != null) {
                smartSwitchPrefs.setSummary(2131628624);
            }
            removePreference("description_category", "wifi_plus_fast_connect");
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        if (getListView() != null) {
            setDivider(getResources().getDrawable(2130838531));
        }
        return root;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int i = 0;
        String key = preference.getKey();
        ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, newValue);
        if (!"smart_network_switching".equals(key)) {
            return false;
        }
        ContentResolver contentResolver = getContentResolver();
        String str = "smart_network_switching";
        if (((Boolean) newValue).booleanValue()) {
            i = 1;
        }
        System.putInt(contentResolver, str, i);
        return true;
    }

    public void onResume() {
        boolean z = true;
        super.onResume();
        TwoStatePreference smartNetworkSwitchPreference = (TwoStatePreference) findPreference("smart_network_switching");
        if (smartNetworkSwitchPreference != null) {
            if (System.getInt(getContentResolver(), "smart_network_switching", 0) != 1) {
                z = false;
            }
            smartNetworkSwitchPreference.setChecked(z);
            smartNetworkSwitchPreference.setOnPreferenceChangeListener(this);
        }
    }

    public void onPause() {
        super.onPause();
        ItemUseStat.getInstance().cacheData(getActivity());
    }
}
