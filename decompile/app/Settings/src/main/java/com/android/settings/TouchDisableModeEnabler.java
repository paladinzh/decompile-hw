package com.android.settings;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;

public final class TouchDisableModeEnabler {
    private final Context mContext;
    private int mDefaultStatus = 1;
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            TouchDisableModeEnabler.this.updateSwitchStatus();
        }
    };
    private OnPreferenceChangeListener mPreferenceChangedListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(TouchDisableModeEnabler.this.mContext, preference, newValue);
            TouchDisableModeEnabler.this.performCheck(((Boolean) newValue).booleanValue());
            return true;
        }
    };
    private SwitchPreference mSwitchPreference;

    public TouchDisableModeEnabler(Context context, SwitchPreference switchPreference) {
        this.mContext = context;
        this.mSwitchPreference = switchPreference;
        if ("factory".equalsIgnoreCase(SystemProperties.get("ro.runmode"))) {
            this.mDefaultStatus = 0;
        }
    }

    public void resume() {
        if (this.mSwitchPreference != null) {
            this.mSwitchPreference.setOnPreferenceChangeListener(this.mPreferenceChangedListener);
        }
        registerObserver();
        updateSwitchStatus();
    }

    public void pause() {
        ItemUseStat.getInstance().cacheData(this.mContext);
        if (this.mSwitchPreference != null) {
            this.mSwitchPreference.setOnPreferenceChangeListener(null);
        }
        unregisterObserver();
    }

    protected void updateSwitchStatus() {
        boolean z = false;
        if (this.mSwitchPreference != null) {
            SwitchPreference switchPreference = this.mSwitchPreference;
            if (System.getInt(this.mContext.getContentResolver(), "touch_disable_mode", this.mDefaultStatus) > 0) {
                z = true;
            }
            switchPreference.setChecked(z);
        }
    }

    protected void performCheck(boolean isChecked) {
        if (this.mSwitchPreference != null) {
            this.mSwitchPreference.setChecked(isChecked);
        }
        System.putInt(this.mContext.getContentResolver(), "touch_disable_mode", isChecked ? 1 : 0);
    }

    private void registerObserver() {
        if (this.mContext != null) {
            this.mContext.getContentResolver().registerContentObserver(System.getUriFor("touch_disable_mode"), true, this.mObserver);
        }
    }

    private void unregisterObserver() {
        if (this.mContext != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
        }
    }
}
