package com.android.settings;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;

public class SmartEarphoneEnabler {
    protected final Context mContext;
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            SmartEarphoneEnabler.this.updateSwitchStatus();
            SmartEarphoneEnabler.this.updateStatusText();
        }
    };
    private OnPreferenceChangeListener mPreferenceChangedListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(SmartEarphoneEnabler.this.mContext, preference, newValue);
            SmartEarphoneEnabler.this.performCheck(((Boolean) newValue).booleanValue());
            return true;
        }
    };
    protected Preference mStatusPreference;
    protected SwitchPreference mSwitchPreference;

    public SmartEarphoneEnabler(Context context, SwitchPreference switchPreference) {
        this.mContext = context;
        this.mSwitchPreference = switchPreference;
    }

    public SmartEarphoneEnabler(Context context, Preference preference) {
        this.mContext = context;
        this.mStatusPreference = preference;
    }

    public void resume() {
        if (this.mSwitchPreference != null) {
            this.mSwitchPreference.setOnPreferenceChangeListener(this.mPreferenceChangedListener);
        }
        registerObserver();
        updateSwitchStatus();
        updateStatusText();
    }

    public void pause() {
        ItemUseStat.getInstance().cacheData(this.mContext);
        if (this.mSwitchPreference != null) {
            this.mSwitchPreference.setOnPreferenceChangeListener(null);
        }
        unregisterObserver();
    }

    protected void performCheck(boolean isChecked) {
        if (this.mContext != null) {
            System.putInt(this.mContext.getContentResolver(), "smart_earphone_control", isChecked ? 1 : 0);
        }
    }

    protected void updateStatusText() {
        if (this.mStatusPreference != null) {
            int i;
            Preference preference = this.mStatusPreference;
            if (isModeEnabled()) {
                i = 2131627698;
            } else {
                i = 2131627699;
            }
            preference.setSummary(i);
        }
    }

    protected void updateSwitchStatus() {
        if (this.mSwitchPreference != null) {
            this.mSwitchPreference.setChecked(isModeEnabled());
        }
    }

    private boolean isModeEnabled() {
        if (1 == System.getInt(this.mContext.getContentResolver(), "smart_earphone_control", 0)) {
            return true;
        }
        return false;
    }

    private void registerObserver() {
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("smart_earphone_control"), true, this.mObserver);
    }

    private void unregisterObserver() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
    }
}
