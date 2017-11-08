package com.android.settings;

import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;

public class SingleHandSettings extends SettingsPreferenceFragment {
    private SingleHandEnabler mEnabler;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230892);
        this.mEnabler = new SingleHandEnabler(getActivity(), (SwitchPreference) findPreference("single_hand_switch"));
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void onResume() {
        super.onResume();
        this.mEnabler.resume();
    }

    public void onPause() {
        super.onPause();
        this.mEnabler.pause();
        ItemUseStat.getInstance().cacheData(getActivity());
    }

    public void onDestroy() {
        super.onDestroy();
        Preference preference = findPreference("single_hand_tutorial");
        if (preference instanceof ImageViewPreference) {
            ((ImageViewPreference) preference).cancelAnimation();
        }
    }

    protected int getMetricsCategory() {
        return 100000;
    }
}
