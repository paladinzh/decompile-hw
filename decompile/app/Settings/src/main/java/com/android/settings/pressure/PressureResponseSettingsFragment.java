package com.android.settings.pressure;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import com.android.settings.ItemUseStat;
import com.android.settings.SettingsPreferenceFragment;

public class PressureResponseSettingsFragment extends SettingsPreferenceFragment {
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addPreferencesFromResource(2131230841);
        if (!getResources().getBoolean(2131492915)) {
            removePreference("pressure_preview_category");
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), preference);
        return super.onPreferenceTreeClick(preference);
    }

    protected int getMetricsCategory() {
        return 100000;
    }
}
