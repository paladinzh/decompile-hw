package com.android.settings;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings.Global;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;

public class SmartCoverEnabler {
    protected final Context mContext;
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            SmartCoverEnabler.this.updateSwitchStatus();
            SmartCoverEnabler.this.updateStatusText();
        }
    };
    private OnPreferenceChangeListener mPreferenceChangedListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(SmartCoverEnabler.this.mContext, preference, newValue);
            SmartCoverEnabler.this.performCheck(((Boolean) newValue).booleanValue());
            return true;
        }
    };
    protected Preference mStatusPreference;
    protected SwitchPreference mSwitchPreference;

    public SmartCoverEnabler(Context context, SwitchPreference switchPreference) {
        this.mContext = context;
        this.mSwitchPreference = switchPreference;
    }

    public SmartCoverEnabler(Context context, Preference preference) {
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
        if (this.mSwitchPreference != null) {
            this.mSwitchPreference.setOnPreferenceChangeListener(null);
        }
        unregisterObserver();
    }

    protected void performCheck(boolean isChecked) {
        if (this.mContext != null) {
            Global.putInt(this.mContext.getContentResolver(), HwCustSmartCoverSettingsImpl.KEY_COVER_ENALBED, isChecked ? 1 : 0);
        }
    }

    protected void updateStatusText() {
        if (this.mStatusPreference != null) {
            int i;
            boolean isModeEnabled = 1 == Global.getInt(this.mContext.getContentResolver(), HwCustSmartCoverSettingsImpl.KEY_COVER_ENALBED, 1);
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
            SwitchPreference switchPreference = this.mSwitchPreference;
            if (1 != Global.getInt(this.mContext.getContentResolver(), HwCustSmartCoverSettingsImpl.KEY_COVER_ENALBED, 1)) {
                z = false;
            }
            switchPreference.setChecked(z);
        }
    }

    private void registerObserver() {
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor(HwCustSmartCoverSettingsImpl.KEY_COVER_ENALBED), true, this.mObserver);
    }

    private void unregisterObserver() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
    }
}
