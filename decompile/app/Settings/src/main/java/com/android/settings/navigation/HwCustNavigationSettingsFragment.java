package com.android.settings.navigation;

import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.SettingNavigationBarPositionPreference;

public class HwCustNavigationSettingsFragment {
    public NavigationSettingsFragment mNavigationSettingsFragment;

    public HwCustNavigationSettingsFragment(NavigationSettingsFragment navigationSettings) {
        this.mNavigationSettingsFragment = navigationSettings;
    }

    public void initVirtualKeyPositionPreferences(OnPreferenceChangeListener mVirNaviListener, int selectedNaviType) {
    }

    public SettingNavigationBarPositionPreference getTextRadioPreference() {
        return null;
    }
}
