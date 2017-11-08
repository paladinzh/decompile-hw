package com.android.settings;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.widget.Toast;

public final class ForceRotationSettings {
    public static final boolean FORCEROTATION_FLAG = SystemProperties.getBoolean("ro.config.hw_force_rotation", false);
    private final Context mContext;
    private int mDefaultStatus = 0;
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            ForceRotationSettings.this.updateByAccelerometer();
        }
    };
    private OnPreferenceChangeListener mPreferenceChangedListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            boolean isChecked = ((Boolean) newValue).booleanValue();
            ForceRotationSettings.this.performCheck(isChecked);
            if (!isChecked) {
                Toast.makeText(ForceRotationSettings.this.mContext, 2131628924, 0).show();
            }
            return true;
        }
    };
    private SwitchPreference mSwitchPreference;

    public ForceRotationSettings(Context context, SwitchPreference switchPreference) {
        this.mContext = context;
        this.mSwitchPreference = switchPreference;
    }

    public void resume() {
        if (this.mSwitchPreference != null) {
            this.mSwitchPreference.setOnPreferenceChangeListener(this.mPreferenceChangedListener);
        }
        updateByAccelerometer();
        registerObserver();
    }

    public void pause() {
        if (this.mSwitchPreference != null) {
            this.mSwitchPreference.setOnPreferenceChangeListener(null);
        }
        unregisterObserver();
    }

    private void updateByAccelerometer() {
        if (this.mSwitchPreference != null) {
            boolean z;
            SwitchPreference switchPreference = this.mSwitchPreference;
            if (System.getInt(this.mContext.getContentResolver(), "force_rotation_mode", this.mDefaultStatus) > 0) {
                z = true;
            } else {
                z = false;
            }
            switchPreference.setChecked(z);
            if (this.mContext.getResources().getConfiguration().orientation == 1) {
                if (System.getInt(this.mContext.getContentResolver(), "accelerometer_rotation", 0) <= 0) {
                    this.mSwitchPreference.setEnabled(false);
                    this.mSwitchPreference.setSummary(2131628925);
                } else {
                    this.mSwitchPreference.setEnabled(true);
                    this.mSwitchPreference.setSummary((CharSequence) "");
                }
            }
        }
    }

    private void performCheck(boolean isChecked) {
        if (this.mSwitchPreference != null) {
            this.mSwitchPreference.setChecked(isChecked);
        }
        System.putInt(this.mContext.getContentResolver(), "force_rotation_mode", isChecked ? 1 : 0);
    }

    private void registerObserver() {
        if (this.mContext != null) {
            this.mContext.getContentResolver().registerContentObserver(System.getUriFor("accelerometer_rotation"), true, this.mObserver);
        }
    }

    private void unregisterObserver() {
        if (this.mContext != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
        }
    }
}
