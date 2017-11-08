package com.android.settings;

import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;

public class OtgSettings extends SettingsPreferenceFragment {
    private OtgEnabler mOtgEnabler;
    private SwitchPreference mSwitchPreference;
    private Preference prefText;

    protected int getMetricsCategory() {
        return 100000;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(2131629300);
        addPreferencesFromResource(2131230826);
        this.mSwitchPreference = (SwitchPreference) findPreference("otg_switch");
        this.prefText = findPreference("help");
        this.prefText.setSelectable(false);
        this.mOtgEnabler = new OtgEnabler(getActivity(), this.mSwitchPreference);
    }

    public void onResume() {
        super.onResume();
        if (this.mOtgEnabler != null) {
            this.mOtgEnabler.resume();
        }
    }

    public void onPause() {
        super.onPause();
        if (this.mOtgEnabler != null) {
            this.mOtgEnabler.pause();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mOtgEnabler != null) {
            this.mOtgEnabler.destroy();
        }
    }
}
