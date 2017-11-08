package com.android.settings;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings.Global;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;

public class ShowPedometerEnabler {
    protected final Context mContext;
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            ShowPedometerEnabler.this.updateSwitchStatus();
        }
    };
    private OnPreferenceChangeListener mPreferenceChangedListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(ShowPedometerEnabler.this.mContext, preference, newValue);
            ShowPedometerEnabler.this.performCheck(((Boolean) newValue).booleanValue());
            return true;
        }
    };
    protected SwitchPreference mSwitchPreference;

    public ShowPedometerEnabler(Context context, SwitchPreference switchPreference) {
        this.mContext = context;
        this.mSwitchPreference = switchPreference;
    }

    public void resume() {
        if (this.mSwitchPreference != null) {
            this.mSwitchPreference.setOnPreferenceChangeListener(this.mPreferenceChangedListener);
        }
        registerObserver();
        updateSwitchStatus();
    }

    public void pause() {
        if (this.mSwitchPreference != null) {
            this.mSwitchPreference.setOnPreferenceChangeListener(null);
        }
        unregisterObserver();
    }

    protected void performCheck(boolean isChecked) {
        if (this.mContext != null) {
            Global.putInt(this.mContext.getContentResolver(), "pedemeter_enabled", isChecked ? 1 : 0);
        }
    }

    protected void updateSwitchStatus() {
        boolean z = true;
        if (this.mSwitchPreference != null) {
            SwitchPreference switchPreference = this.mSwitchPreference;
            if (1 != Global.getInt(this.mContext.getContentResolver(), "pedemeter_enabled", 1)) {
                z = false;
            }
            switchPreference.setChecked(z);
        }
    }

    private void registerObserver() {
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("pedemeter_enabled"), true, this.mObserver);
    }

    private void unregisterObserver() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
    }
}
