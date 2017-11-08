package com.android.settings.notification;

import android.os.Bundle;
import android.provider.Settings.Global;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.CustomSwitchPreference;
import com.android.settings.ItemUseStat;
import com.android.settings.Utils;

public class ZenModeSettingsMain extends ZenModeSettingsBase implements OnPreferenceChangeListener {
    private static final String[] ENTRY_VALUES_ZEN_MODE = new String[]{"ZEN_MODE_IMPORTANT_INTERRUPTIONS", "ZEN_MODE_ALARMS", "ZEN_MODE_NO_INTERRUPTIONS"};
    protected int mLastZenMode = 3;
    protected PreferenceScreen mZenModeDefinePriority;
    protected ListPreference mZenModePreference;
    protected CustomSwitchPreference mZenModeSwitchPreference;

    protected void onZenModeChanged() {
    }

    protected void onZenModeConfigChanged() {
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230949);
        getLastZenMode();
        this.mZenModeSwitchPreference = (CustomSwitchPreference) findPreference("zen_mode_switch_base");
        this.mZenModeSwitchPreference.setOnPreferenceChangeListener(this);
        PreferenceCategory root_rule = (PreferenceCategory) getPreferenceScreen().findPreference("zen_mode_choose");
        this.mZenModePreference = (ListPreference) root_rule.findPreference("zen_mode_base");
        this.mZenModePreference.setEntries(new CharSequence[]{getResources().getString(2131626718), getResources().getString(2131626719), getResources().getString(2131626720)});
        this.mZenModePreference.setEntryValues(new CharSequence[]{String.valueOf(1), String.valueOf(3), String.valueOf(2)});
        this.mZenModePreference.setOnPreferenceChangeListener(this);
        this.mZenModeDefinePriority = (PreferenceScreen) root_rule.findPreference("priority_settings");
        this.mZenModeDefinePriority.setEnabled(false);
    }

    protected void updateControls() {
        boolean isZenModeEnabled = (getZenModeConfig() == null || this.mZenMode == 0) ? false : true;
        if (!(this.mZenModeSwitchPreference == null || this.mZenModeSwitchPreference.isChecked() == isZenModeEnabled)) {
            this.mZenModeSwitchPreference.setOnPreferenceChangeListener(null);
            this.mZenModeSwitchPreference.setChecked(isZenModeEnabled);
            this.mZenModeSwitchPreference.setOnPreferenceChangeListener(this);
        }
        if (this.mZenModePreference != null) {
            this.mZenModePreference.setOnPreferenceChangeListener(null);
            ZenModeUtils.setSelectedValue(this.mZenModePreference, String.valueOf(this.mLastZenMode));
            setZenModeInfo(this.mLastZenMode);
            if (this.mLastZenMode == 1) {
                this.mZenModeDefinePriority.setEnabled(true);
            } else {
                this.mZenModeDefinePriority.setEnabled(false);
            }
            this.mZenModePreference.setOnPreferenceChangeListener(this);
        }
    }

    private void setZenModeInfo(int zenmode) {
        if (this.mZenModeSwitchPreference != null) {
            if (zenmode == 1) {
                if (Utils.isWifiOnly(this.mContext)) {
                    this.mZenModeSwitchPreference.setSummary(2131628893);
                } else {
                    this.mZenModeSwitchPreference.setSummary(2131628890);
                }
            } else if (zenmode == 3) {
                if (Utils.isWifiOnly(this.mContext)) {
                    this.mZenModeSwitchPreference.setSummary(2131628895);
                } else {
                    this.mZenModeSwitchPreference.setSummary(2131628892);
                }
            } else if (Utils.isWifiOnly(this.mContext)) {
                this.mZenModeSwitchPreference.setSummary(2131628894);
            } else {
                this.mZenModeSwitchPreference.setSummary(2131628891);
            }
        }
    }

    protected int getMetricsCategory() {
        return 76;
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), preference);
        return super.onPreferenceTreeClick(preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == this.mZenModeSwitchPreference) {
            if (!((Boolean) newValue).booleanValue()) {
                setZenMode(0, null);
            } else if (this.mLastZenMode != 0) {
                setZenMode(this.mLastZenMode, null);
            } else {
                setZenMode(3, null);
            }
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, newValue);
            return true;
        } else if (preference != this.mZenModePreference) {
            return false;
        } else {
            int mode = Integer.parseInt(newValue.toString());
            if (this.mZenModeSwitchPreference != null && this.mZenModeSwitchPreference.isChecked()) {
                setZenMode(mode, null);
            }
            updateLastZenMode(mode);
            ZenModeUtils.setSelectedValue(this.mZenModePreference, String.valueOf(this.mLastZenMode));
            setZenModeInfo(mode);
            if (mode == 1) {
                this.mZenModeDefinePriority.setEnabled(true);
            } else {
                this.mZenModeDefinePriority.setEnabled(false);
            }
            ItemUseStat.getInstance().handleClickListPreference(getActivity(), this.mZenModePreference, ENTRY_VALUES_ZEN_MODE, String.valueOf(mode));
            return true;
        }
    }

    protected void getLastZenMode() {
        this.mLastZenMode = Global.getInt(getContentResolver(), "zen_mode_last_choosen", 3);
    }

    protected void updateLastZenMode(int mode) {
        this.mLastZenMode = mode;
        Global.putInt(getContentResolver(), "zen_mode_last_choosen", mode);
    }
}
