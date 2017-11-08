package com.android.settings.location;

import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.Utils;
import com.android.settings.location.RadioButtonPreference.OnClickListener;
import com.huawei.cust.HwCustUtils;

public class LocationMode extends LocationSettingsBase implements OnClickListener {
    private RadioButtonPreference mBatterySaving;
    private HwCustLocationMode mCustLocationMode;
    private RadioButtonPreference mHighAccuracy;
    private RadioButtonPreference mSensorsOnly;

    protected int getMetricsCategory() {
        return 64;
    }

    public void onResume() {
        super.onResume();
        createPreferenceHierarchy();
    }

    public void onPause() {
        super.onPause();
    }

    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(2131230808);
        root = getPreferenceScreen();
        this.mHighAccuracy = (RadioButtonPreference) root.findPreference("high_accuracy");
        this.mBatterySaving = (RadioButtonPreference) root.findPreference("battery_saving");
        this.mSensorsOnly = (RadioButtonPreference) root.findPreference("sensors_only");
        updateSummaryForWifiOnly();
        this.mHighAccuracy.setOnClickListener(this);
        this.mBatterySaving.setOnClickListener(this);
        this.mSensorsOnly.setOnClickListener(this);
        this.mCustLocationMode = (HwCustLocationMode) HwCustUtils.createObj(HwCustLocationMode.class, new Object[]{this});
        if (this.mCustLocationMode != null) {
            this.mCustLocationMode.updateCustPreference(getActivity());
        }
        refreshLocationMode();
        return root;
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        boolean result = false;
        if (this.mCustLocationMode != null) {
            result = this.mCustLocationMode.onPreferenceTreeClick(preference, getActivity());
        }
        if (result) {
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void updateRadioButtons(RadioButtonPreference activated) {
        if (activated == null) {
            this.mHighAccuracy.setChecked(false);
            this.mBatterySaving.setChecked(false);
            this.mSensorsOnly.setChecked(false);
        } else if (activated == this.mHighAccuracy) {
            this.mHighAccuracy.setChecked(true);
            this.mBatterySaving.setChecked(false);
            this.mSensorsOnly.setChecked(false);
        } else if (activated == this.mBatterySaving) {
            this.mHighAccuracy.setChecked(false);
            this.mBatterySaving.setChecked(true);
            this.mSensorsOnly.setChecked(false);
        } else if (activated == this.mSensorsOnly) {
            this.mHighAccuracy.setChecked(false);
            this.mBatterySaving.setChecked(false);
            this.mSensorsOnly.setChecked(true);
        }
    }

    public void onRadioButtonClicked(RadioButtonPreference emiter) {
        int mode = 0;
        if (emiter == this.mHighAccuracy) {
            mode = 3;
        } else if (emiter == this.mBatterySaving) {
            mode = 2;
        } else if (emiter == this.mSensorsOnly) {
            mode = 1;
        }
        setLocationMode(mode);
    }

    public void onModeChanged(int mode, boolean restricted) {
        switch (mode) {
            case 0:
                updateRadioButtons(null);
                break;
            case 1:
                updateRadioButtons(this.mSensorsOnly);
                break;
            case 2:
                updateRadioButtons(this.mBatterySaving);
                break;
            case 3:
                updateRadioButtons(this.mHighAccuracy);
                break;
        }
        boolean enabled = (mode == 0 || restricted) ? false : true;
        this.mHighAccuracy.setEnabled(enabled);
        this.mBatterySaving.setEnabled(enabled);
        this.mSensorsOnly.setEnabled(enabled);
        if (this.mCustLocationMode != null) {
            this.mCustLocationMode.onModeChanged(mode);
        }
    }

    public int getHelpResource() {
        return 2131626550;
    }

    private void updateSummaryForWifiOnly() {
        if (Utils.isWifiOnly(getActivity())) {
            this.mHighAccuracy.setTitle(2131628662);
            this.mBatterySaving.setTitle(2131628663);
            this.mHighAccuracy.setSummary(2131627851);
            this.mBatterySaving.setSummary(2131627852);
        }
    }
}
