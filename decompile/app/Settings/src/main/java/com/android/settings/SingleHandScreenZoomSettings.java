package com.android.settings;

import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import com.android.settings.navigation.NaviUtils;

public class SingleHandScreenZoomSettings extends SettingsPreferenceFragment {
    private SingleHandScreenZoomEnabler mEnabler;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230891);
        this.mEnabler = new SingleHandScreenZoomEnabler(getActivity(), (SwitchPreference) findPreference("single_hand_screen_zoom_switch"));
        Preference descriptionPref = findPreference("single_hand_screen_zoom_tutorial");
        boolean naviEnabled = NaviUtils.isNaviBarEnabled(getActivity().getContentResolver());
        if (descriptionPref instanceof ImageViewPreference) {
            ImageViewPreference descPref = (ImageViewPreference) descriptionPref;
            if (NaviUtils.isFrontFingerNaviEnabled() && !naviEnabled) {
                descPref.setDrawable(getResources().getDrawable(2130838654));
            }
        }
        if (!getResources().getBoolean(17956970)) {
            descriptionPref.setSummary(2131627835);
        }
        if (NaviUtils.isFrontFingerNaviEnabled() && !naviEnabled) {
            descriptionPref.setSummary(2131628747);
        }
    }

    public void onResume() {
        super.onResume();
        if (this.mEnabler != null) {
            this.mEnabler.resume();
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void onPause() {
        super.onPause();
        if (this.mEnabler != null) {
            this.mEnabler.pause();
        }
        ItemUseStat.getInstance().cacheData(getActivity());
    }

    public void onDestroy() {
        super.onDestroy();
        Preference preference = findPreference("single_hand_screen_zoom_tutorial");
        if (preference instanceof ImageViewPreference) {
            ((ImageViewPreference) preference).cancelAnimation();
        }
    }

    protected int getMetricsCategory() {
        return 100000;
    }
}
