package com.android.settings;

import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;

public class SuspendButtonSettings extends SettingsPreferenceFragment {
    private SuspendButtonEnabler mEnabler;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230906);
        this.mEnabler = new SuspendButtonEnabler(getActivity(), (SwitchPreference) findPreference("suspend_button_switch"));
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

    protected int getMetricsCategory() {
        return 100000;
    }
}
