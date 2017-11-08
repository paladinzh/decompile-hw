package com.android.settings.smartcover;

import android.os.Bundle;
import com.android.settings.SettingsActivity;

public class SmartCoverBackgroundActivity extends SettingsActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(2131629195);
    }

    protected boolean isValidFragment(String fragmentName) {
        if (LocalResFragment.class.getName().equals(fragmentName)) {
            return true;
        }
        return false;
    }
}
