package com.android.settings;

import android.content.Intent;

public class NotificationAndStatusSettingsActivity extends SettingsActivity {
    public Intent getIntent() {
        Intent intent = super.getIntent();
        if (intent.getStringExtra(":settings:show_fragment") != null) {
            return intent;
        }
        Intent newIntent = new Intent(intent);
        newIntent.putExtra(":settings:show_fragment", NotificationAndStatusSettings.class.getName());
        return newIntent;
    }

    protected boolean isValidFragment(String fragmentName) {
        if (NotificationAndStatusSettings.class.getName().equals(fragmentName)) {
            return true;
        }
        return super.isValidFragment(fragmentName);
    }
}
