package com.android.settings.smartcover;

import android.os.Bundle;
import com.android.settings.SettingsActivity;

public class SmartCoverAnimationModeActivity extends SettingsActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(2131629269);
    }

    protected boolean isValidFragment(String fragmentName) {
        if (SmartCoverAnimationModeSettings.class.getName().equals(fragmentName)) {
            return true;
        }
        return false;
    }
}
