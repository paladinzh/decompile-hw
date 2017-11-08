package com.android.settings;

import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;

public class SmartEarphoneSettings extends SettingsPreferenceFragment {
    private SmartEarphoneEnabler mEnabler;
    private SwitchPreference mSwitchPreference;
    private ImageViewPreference mTutorialPreference;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Utils.isChinaArea()) {
            addPreferencesFromResource(2131230899);
        } else {
            addPreferencesFromResource(2131230898);
        }
        this.mTutorialPreference = (ImageViewPreference) findPreference("smart_earphone_tutorial");
        this.mSwitchPreference = (SwitchPreference) findPreference("smart_earphone_switch");
        this.mEnabler = new SmartEarphoneEnabler(getActivity(), this.mSwitchPreference);
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
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mTutorialPreference != null) {
            this.mTutorialPreference.cancelAnimation();
        }
    }

    protected int getMetricsCategory() {
        return 100000;
    }
}
