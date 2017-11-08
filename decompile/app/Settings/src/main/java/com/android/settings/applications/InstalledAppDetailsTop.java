package com.android.settings.applications;

import android.content.Intent;
import android.os.Bundle;
import com.android.settings.SettingsActivity;

public class InstalledAppDetailsTop extends SettingsActivity {
    public Intent getIntent() {
        if (super.getIntent() == null) {
            return null;
        }
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(":settings:show_fragment", InstalledAppDetails.class.getName());
        return modIntent;
    }

    protected boolean isValidFragment(String fragmentName) {
        if (InstalledAppDetails.class.getName().equals(fragmentName)) {
            return true;
        }
        return false;
    }

    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
