package com.android.settings;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import com.android.internal.view.RotationPolicy;
import com.android.internal.view.RotationPolicy.RotationPolicyListener;
import com.android.settings.deviceinfo.RadioPreference;

public class AccelerometerSettings extends SettingsPreferenceFragment {
    private boolean clickRefresh = false;
    private boolean hasObserverChange;
    private RadioPreference mClose;
    private ContentResolver mContentResolver;
    private RadioPreference mNormal;
    private final RotationPolicyListener mRotationPolicyListener = new RotationPolicyListener() {
        public void onChange() {
            AccelerometerSettings.this.updateRadioButton();
        }
    };
    private RadioPreference mSmart;
    private ContentObserver mSmartSwitchObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            AccelerometerSettings.this.updateRadioButton();
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230721);
        this.mNormal = (RadioPreference) findPreference("normal_mode");
        this.mSmart = (RadioPreference) findPreference("smart_mode");
        this.mClose = (RadioPreference) findPreference("close_mode");
        this.mContentResolver = getContentResolver();
    }

    private void updateRadioButton() {
        this.clickRefresh = true;
        if (RotationPolicy.isRotationLocked(getActivity())) {
            this.mClose.setChecked(true);
            this.mSmart.setChecked(false);
            this.mNormal.setChecked(false);
            return;
        }
        if (1 == System.getInt(this.mContentResolver, "smart_accelerometer_rotation", 0)) {
            this.mNormal.setChecked(false);
            this.mSmart.setChecked(true);
        } else {
            this.mNormal.setChecked(true);
            this.mSmart.setChecked(false);
        }
        this.mClose.setChecked(false);
    }

    public void onResume() {
        super.onResume();
        RotationPolicy.registerRotationPolicyListener(getActivity(), this.mRotationPolicyListener);
        registerObserver();
        updateRadioButton();
    }

    public void onPause() {
        super.onPause();
        RotationPolicy.unregisterRotationPolicyListener(getActivity(), this.mRotationPolicyListener);
        unregisterObserver();
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        this.clickRefresh = false;
        if (preference == this.mNormal) {
            RotationPolicy.setRotationLockForAccessibility(getActivity(), false);
            System.putInt(this.mContentResolver, "smart_accelerometer_rotation", 0);
        } else if (preference == this.mSmart) {
            RotationPolicy.setRotationLockForAccessibility(getActivity(), false);
            System.putInt(this.mContentResolver, "smart_accelerometer_rotation", 1);
        } else {
            RotationPolicy.setRotationLockForAccessibility(getActivity(), true);
            System.putInt(this.mContentResolver, "smart_accelerometer_rotation", 0);
        }
        if (!this.clickRefresh) {
            updateRadioButton();
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void registerObserver() {
        if (!this.hasObserverChange) {
            this.mContentResolver.registerContentObserver(System.getUriFor("smart_accelerometer_rotation"), true, this.mSmartSwitchObserver);
            this.hasObserverChange = true;
        }
    }

    private void unregisterObserver() {
        if (this.hasObserverChange) {
            this.mContentResolver.unregisterContentObserver(this.mSmartSwitchObserver);
            this.hasObserverChange = false;
        }
    }

    protected int getMetricsCategory() {
        return 100000;
    }
}
