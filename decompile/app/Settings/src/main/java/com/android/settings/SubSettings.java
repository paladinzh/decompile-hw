package com.android.settings;

import android.os.Bundle;
import android.util.Log;

public class SubSettings extends SettingsActivity {
    public boolean onNavigateUp() {
        finish();
        return true;
    }

    protected boolean isValidFragment(String fragmentName) {
        Log.d("SubSettings", "Launching fragment " + fragmentName);
        return true;
    }

    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        Log.d("SubSettings", "onCreate.Launching fragment " + getIntent().getStringExtra(":settings:show_fragment"));
    }
}
