package com.android.settings;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;

public class SingleHandEnabler {
    private boolean hasObserverChange = false;
    protected final Context mContext;
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            SingleHandEnabler.this.updateSwitchStatus();
            SingleHandEnabler.this.updateStatusText();
        }
    };
    private OnPreferenceChangeListener mPreferenceChangedListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(SingleHandEnabler.this.mContext, preference, newValue);
            SingleHandEnabler.this.performCheck(((Boolean) newValue).booleanValue());
            return true;
        }
    };
    protected Preference mStatusPreference;
    protected SwitchPreference mSwitchPreference;

    public SingleHandEnabler(Context context, SwitchPreference switchPreference) {
        this.mContext = context;
        this.mSwitchPreference = switchPreference;
    }

    public SingleHandEnabler(Context context, Preference preference) {
        this.mContext = context;
        this.mStatusPreference = preference;
    }

    public void resume() {
        if (this.mSwitchPreference != null) {
            this.mSwitchPreference.setOnPreferenceChangeListener(this.mPreferenceChangedListener);
        }
        registerObserver();
        updateStatusText();
        updateSwitchStatus();
    }

    public void pause() {
        if (this.mSwitchPreference != null) {
            this.mSwitchPreference.setOnPreferenceChangeListener(null);
        }
        unregisterObserver();
    }

    protected void updateStatusText() {
        if (this.mStatusPreference != null) {
            int i;
            boolean isModeEnabled = System.getInt(this.mContext.getContentResolver(), "single_hand_switch", 0) > 0;
            Preference preference = this.mStatusPreference;
            if (isModeEnabled) {
                i = 2131627698;
            } else {
                i = 2131627699;
            }
            preference.setSummary(i);
        }
    }

    protected void updateSwitchStatus() {
        boolean z = true;
        if (this.mSwitchPreference != null) {
            if (isSystemSupported()) {
                this.mSwitchPreference.setEnabled(true);
            } else {
                this.mSwitchPreference.setEnabled(false);
            }
            SwitchPreference switchPreference = this.mSwitchPreference;
            if (System.getInt(this.mContext.getContentResolver(), "single_hand_switch", 0) <= 0) {
                z = false;
            }
            switchPreference.setChecked(z);
        }
    }

    protected void performCheck(boolean isChecked) {
        if (isSystemSupported()) {
            this.mSwitchPreference.setChecked(isChecked);
            if (isChecked) {
                System.putInt(this.mContext.getContentResolver(), "single_hand_switch", 1);
                if (-1 == System.getInt(this.mContext.getContentResolver(), "single_hand_smart", -1)) {
                    System.putInt(this.mContext.getContentResolver(), "single_hand_smart", 1);
                }
            } else {
                System.putInt(this.mContext.getContentResolver(), "single_hand_switch", 0);
            }
            return;
        }
        this.mSwitchPreference.setEnabled(false);
        MLog.e("single_hand_switch", "the single hand mode is closed for users, how can they click it.Disable it!");
    }

    private boolean isSystemSupported() {
        return SystemProperties.getInt("ro.config.hw_singlehand", 0) > 0;
    }

    private void registerObserver() {
        if (!this.hasObserverChange) {
            this.mContext.getContentResolver().registerContentObserver(System.getUriFor("single_hand_switch"), true, this.mObserver);
            this.hasObserverChange = true;
        }
    }

    private void unregisterObserver() {
        if (this.hasObserverChange) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
            this.hasObserverChange = false;
        }
    }
}
