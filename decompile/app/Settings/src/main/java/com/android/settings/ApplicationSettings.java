package com.android.settings;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;

public class ApplicationSettings extends SettingsPreferenceFragment {
    private ListPreference mInstallLocation;
    private CheckBoxPreference mToggleAdvancedSettings;

    protected int getMetricsCategory() {
        return 16;
    }

    public void onCreate(Bundle icicle) {
        boolean userSetInstLocation = false;
        super.onCreate(icicle);
        addPreferencesFromResource(2131230742);
        this.mToggleAdvancedSettings = (CheckBoxPreference) findPreference("toggle_advanced_settings");
        this.mToggleAdvancedSettings.setChecked(isAdvancedSettingsEnabled());
        getPreferenceScreen().removePreference(this.mToggleAdvancedSettings);
        this.mInstallLocation = (ListPreference) findPreference("app_install_location");
        if (Global.getInt(getContentResolver(), "set_install_location", 0) != 0) {
            userSetInstLocation = true;
        }
        if (userSetInstLocation) {
            this.mInstallLocation.setValue(getAppInstallLocation());
            this.mInstallLocation.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ApplicationSettings.this.handleUpdateAppInstallLocation((String) newValue);
                    return false;
                }
            });
            return;
        }
        getPreferenceScreen().removePreference(this.mInstallLocation);
    }

    protected void handleUpdateAppInstallLocation(String value) {
        if ("device".equals(value)) {
            Global.putInt(getContentResolver(), "default_install_location", 1);
        } else if ("sdcard".equals(value)) {
            Global.putInt(getContentResolver(), "default_install_location", 2);
        } else if ("auto".equals(value)) {
            Global.putInt(getContentResolver(), "default_install_location", 0);
        } else {
            Global.putInt(getContentResolver(), "default_install_location", 0);
        }
        this.mInstallLocation.setValue(value);
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == this.mToggleAdvancedSettings) {
            setAdvancedSettingsEnabled(this.mToggleAdvancedSettings.isChecked());
        }
        return super.onPreferenceTreeClick(preference);
    }

    private boolean isAdvancedSettingsEnabled() {
        return System.getInt(getContentResolver(), "advanced_settings", 0) > 0;
    }

    private void setAdvancedSettingsEnabled(boolean enabled) {
        int value = enabled ? 1 : 0;
        Secure.putInt(getContentResolver(), "advanced_settings", value);
        Intent intent = new Intent("android.intent.action.ADVANCED_SETTINGS");
        intent.putExtra("state", value);
        getActivity().sendBroadcast(intent);
    }

    private String getAppInstallLocation() {
        int selectedLocation = Global.getInt(getContentResolver(), "default_install_location", 0);
        if (selectedLocation == 1) {
            return "device";
        }
        if (selectedLocation == 2) {
            return "sdcard";
        }
        if (selectedLocation == 0) {
            return "auto";
        }
        return "auto";
    }
}
