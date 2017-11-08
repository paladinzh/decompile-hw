package com.android.settings;

public class CleanSubSettings extends SettingsActivity {
    public boolean onNavigateUp() {
        finish();
        return true;
    }

    protected boolean isValidFragment(String fragmentName) {
        return true;
    }
}
