package com.android.settings.smartcover;

import android.os.Bundle;
import com.android.settings.SettingsActivity;

public class SmartCoverStandByActivity extends SettingsActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(2131629197);
    }

    protected boolean isValidFragment(String fragmentName) {
        if (SmartCoverStandBySettings.class.getName().equals(fragmentName)) {
            return true;
        }
        return false;
    }
}
