package com.android.settings.accessibility;

import android.provider.Settings.Global;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.ItemUseStat;

public class ToggleGlobalGesturePreferenceFragment extends ToggleFeaturePreferenceFragment implements OnPreferenceChangeListener {
    protected void onPreferenceToggled(String preferenceKey, boolean enabled) {
        Global.putInt(getContentResolver(), "enable_accessibility_global_gesture_enabled", enabled ? 1 : 0);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (this.mToggleSwitch == preference) {
            boolean value = ((Boolean) newValue).booleanValue();
            getArguments().putBoolean("checked", value);
            onPreferenceToggled(this.mPreferenceKey, value);
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, newValue);
        }
        return true;
    }

    protected void onInstallToggleSwitch() {
        this.mToggleSwitch.setKey("global_gesture");
        this.mToggleSwitch.setTitle(2131625856);
        this.mToggleSwitch.setOnPreferenceChangeListener(this);
    }

    protected int getMetricsCategory() {
        return 6;
    }

    public void onPause() {
        super.onPause();
        ItemUseStat.getInstance().cacheData(getActivity());
    }
}
