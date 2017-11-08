package com.android.settings;

import com.android.settingslib.drawer.DashboardCategory;
import java.util.List;

public class HwCustSettingsActivity {
    public SettingsActivity mSettingsActivity;

    public HwCustSettingsActivity(SettingsActivity settingsActivity) {
        this.mSettingsActivity = settingsActivity;
    }

    public void loadCustHeader(List<DashboardCategory> list) {
    }

    public String getMetaData(String mFragmentClass) {
        return mFragmentClass;
    }

    public boolean isValidFragment(String fragmentName) {
        return false;
    }

    public void hideSmartAssistance(List<DashboardCategory> list) {
    }
}
