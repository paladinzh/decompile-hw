package com.android.settings.location;

import android.content.Context;
import android.location.HwAGPSManager;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
import com.android.settings.SettingsPreferenceFragment;

public class LocationAssistSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    private TwoStatePreference mAssistedGps;
    private Preference mAssistedGpsSettings;
    private Context mContext;
    private HwAGPSManager mHwAGPSManager;
    private TwoStatePreference mPGps;
    private TwoStatePreference mTimeSynchronization;

    protected int getMetricsCategory() {
        return 100000;
    }

    public void onResume() {
        super.onResume();
        this.mContext = getActivity();
        createPreferenceHierarchy();
    }

    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        this.mHwAGPSManager = new HwAGPSManager(this.mContext);
        addPreferencesFromResource(2131230807);
        root = getPreferenceScreen();
        this.mAssistedGps = (TwoStatePreference) root.findPreference("assisted_gps");
        this.mTimeSynchronization = (TwoStatePreference) root.findPreference("time_synchronization");
        this.mPGps = (TwoStatePreference) root.findPreference("pgps_switch");
        this.mAssistedGpsSettings = root.findPreference("assisted_gps_settings");
        boolean gpsEnabled = Secure.isLocationProviderEnabled(this.mContext.getContentResolver(), "gps");
        if (this.mAssistedGps != null) {
            this.mAssistedGps.setChecked(this.mHwAGPSManager.getAGPSSwitchSettings() == 1);
            this.mAssistedGps.setEnabled(gpsEnabled);
            this.mAssistedGps.setOnPreferenceChangeListener(this);
        }
        if (this.mTimeSynchronization != null) {
            this.mTimeSynchronization.setChecked(this.mHwAGPSManager.isGpsTimeSyncEnable());
            this.mTimeSynchronization.setEnabled(gpsEnabled);
            this.mTimeSynchronization.setOnPreferenceChangeListener(this);
        }
        if (this.mPGps != null) {
            this.mPGps.setChecked(this.mHwAGPSManager.isQuickGpsEnable());
            this.mPGps.setEnabled(gpsEnabled);
            this.mPGps.setOnPreferenceChangeListener(this);
        }
        if (System.getInt(this.mContext.getContentResolver(), "has_time_synchronization", 0) == 0) {
            removePreference("time_synchronization");
        }
        if (System.getInt(this.mContext.getContentResolver(), "has_pgps_config", 0) == 0) {
            removePreference("pgps_switch");
        }
        if (System.getInt(this.mContext.getContentResolver(), "has_agps_settings", 0) == 0) {
            removePreference("assisted_gps_settings");
        }
        if (System.getInt(this.mContext.getContentResolver(), "has_assisted_gps", 0) == 0) {
            removePreference("assisted_gps");
        }
        return root;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int i = 1;
        boolean isChecked = ((Boolean) newValue).booleanValue();
        HwAGPSManager hwAGPSManager;
        if (preference == this.mAssistedGps) {
            this.mAssistedGps.setChecked(isChecked);
            hwAGPSManager = this.mHwAGPSManager;
            if (!isChecked) {
                i = 0;
            }
            hwAGPSManager.setAGPSSwitchSettings(i);
        } else if (preference == this.mTimeSynchronization) {
            this.mTimeSynchronization.setChecked(isChecked);
            hwAGPSManager = this.mHwAGPSManager;
            if (!isChecked) {
                i = 0;
            }
            hwAGPSManager.setGpsTimeSyncSettings(i);
        } else if (preference == this.mPGps) {
            this.mPGps.setChecked(isChecked);
            hwAGPSManager = this.mHwAGPSManager;
            if (!isChecked) {
                i = 0;
            }
            hwAGPSManager.setQuickGpsSettings(i);
        }
        return false;
    }
}
