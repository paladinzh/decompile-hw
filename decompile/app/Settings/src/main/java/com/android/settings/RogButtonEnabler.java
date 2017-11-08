package com.android.settings;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.huawei.android.hardware.display.DisplayManagerCustEx;

public class RogButtonEnabler implements OnPreferenceChangeListener {
    private Context mContext;
    private DisplayManagerCustEx mDisplayManagerCustEx = new DisplayManagerCustEx();
    private SwitchPreference mSwitchPreference;

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        performCheck(((Boolean) newValue).booleanValue());
        return true;
    }

    public RogButtonEnabler(Context context, SwitchPreference switchPreference) {
        this.mContext = context;
        this.mSwitchPreference = switchPreference;
    }

    protected void performCheck(boolean isChecked) {
        try {
            Intent intent = new Intent("com.android.settings.RogSettingsActivity");
            intent.addFlags(268435456);
            this.mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
        }
    }

    protected void updateSwitchStatus() {
        boolean z = true;
        if (this.mSwitchPreference != null && this.mDisplayManagerCustEx != null) {
            SwitchPreference switchPreference = this.mSwitchPreference;
            if (1 != this.mDisplayManagerCustEx.getLowPowerDisplayLevel()) {
                z = false;
            }
            switchPreference.setChecked(z);
        }
    }

    public void onResume() {
        if (this.mSwitchPreference != null) {
            updateSwitchStatus();
            this.mSwitchPreference.setOnPreferenceChangeListener(this);
        }
    }

    public void onPause() {
        if (this.mSwitchPreference != null) {
            this.mSwitchPreference.setOnPreferenceChangeListener(null);
        }
    }
}
