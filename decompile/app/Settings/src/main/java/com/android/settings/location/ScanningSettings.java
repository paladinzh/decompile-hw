package com.android.settings.location;

import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.Settings.Global;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.ItemUseStat;
import com.android.settings.SettingsPreferenceFragment;

public class ScanningSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    protected int getMetricsCategory() {
        return 131;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(false);
    }

    public void onResume() {
        super.onResume();
        createPreferenceHierarchy();
    }

    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(2131230810);
        root = getPreferenceScreen();
        initPreferences();
        return root;
    }

    private void initPreferences() {
        boolean z;
        boolean z2 = true;
        SwitchPreference wifiScanAlwaysAvailable = (SwitchPreference) findPreference("wifi_always_scanning");
        wifiScanAlwaysAvailable.setOnPreferenceChangeListener(this);
        if (Global.getInt(getContentResolver(), "wifi_scan_always_enabled", 0) == 1) {
            z = true;
        } else {
            z = false;
        }
        wifiScanAlwaysAvailable.setChecked(z);
        SwitchPreference bleScanAlwaysAvailable = (SwitchPreference) findPreference("bluetooth_always_scanning");
        bleScanAlwaysAvailable.setOnPreferenceChangeListener(this);
        if (Global.getInt(getContentResolver(), "ble_scan_always_enabled", 0) != 1) {
            z2 = false;
        }
        bleScanAlwaysAvailable.setChecked(z2);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int i = 0;
        String key = preference.getKey();
        boolean isChecked = ((Boolean) newValue).booleanValue();
        ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, newValue);
        if ("wifi_always_scanning".equals(key)) {
            Global.putInt(getContentResolver(), "wifi_scan_always_enabled", isChecked ? 1 : 0);
        } else if (!"bluetooth_always_scanning".equals(key)) {
            return false;
        } else {
            ContentResolver contentResolver = getContentResolver();
            String str = "ble_scan_always_enabled";
            if (isChecked) {
                i = 1;
            }
            Global.putInt(contentResolver, str, i);
        }
        return true;
    }
}
