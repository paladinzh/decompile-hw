package com.android.settings.navigation;

import android.content.Intent;
import com.android.settings.SettingsActivity;

public class NaviTrikeySettingsActivity extends SettingsActivity {
    protected boolean isValidFragment(String fragmentName) {
        if (NavigationSettingsFragment.class.getName().equals(fragmentName)) {
            return true;
        }
        return super.isValidFragment(fragmentName);
    }

    public Intent getIntent() {
        Intent intent = super.getIntent();
        if (intent.getStringExtra(":settings:show_fragment") != null) {
            return intent;
        }
        Intent newIntent = new Intent(intent);
        newIntent.putExtra(":settings:show_fragment", NaviTrikeySettingsFragment.class.getName());
        newIntent.putExtra(":settings:show_fragment_title_resid", 2131628846);
        return newIntent;
    }
}
