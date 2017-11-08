package com.android.settings.accounts;

import android.content.Intent;
import android.os.Bundle;
import com.android.settings.SettingsActivity;

public class SyncSettingsActivity extends SettingsActivity {
    public Intent getIntent() {
        Intent intent = super.getIntent();
        Bundle bundle = intent.getBundleExtra("ps_fragment_bundle");
        if (intent.getStringExtra(":settings:show_fragment") != null) {
            return intent;
        }
        Intent newIntent = new Intent(intent);
        newIntent.putExtra(":settings:show_fragment", SyncSettings.class.getName());
        newIntent.putExtra(":settings:show_fragment_args", bundle);
        return newIntent;
    }

    protected boolean isValidFragment(String fragmentName) {
        if (SyncSettings.class.getName().equals(fragmentName)) {
            return true;
        }
        return false;
    }
}
