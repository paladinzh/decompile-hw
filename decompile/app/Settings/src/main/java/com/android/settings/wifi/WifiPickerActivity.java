package com.android.settings.wifi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import com.android.settings.SettingsActivity;
import com.android.settings.wifi.p2p.WifiP2pSettings;

public class WifiPickerActivity extends SettingsActivity {
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        if (!modIntent.hasExtra(":settings:show_fragment")) {
            modIntent.putExtra(":settings:show_fragment", getWifiSettingsClass().getName());
            modIntent.putExtra(":settings:show_fragment_title_resid", 2131624907);
        }
        return modIntent;
    }

    protected boolean isValidFragment(String fragmentName) {
        if (WifiSettings.class.getName().equals(fragmentName) || WifiP2pSettings.class.getName().equals(fragmentName) || SavedAccessPointsWifiSettings.class.getName().equals(fragmentName) || AdvancedWifiSettings.class.getName().equals(fragmentName) || WifiAddFragment.class.getName().equals(fragmentName)) {
            return true;
        }
        return false;
    }

    Class<? extends PreferenceFragment> getWifiSettingsClass() {
        return WifiSettings.class;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.getIntent().putExtra("extra_prefs_show_button_bar", false);
        super.onCreate(savedInstanceState);
    }
}
