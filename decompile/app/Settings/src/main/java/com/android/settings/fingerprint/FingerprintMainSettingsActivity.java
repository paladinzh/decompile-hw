package com.android.settings.fingerprint;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import com.android.settings.SettingsActivity;

public class FingerprintMainSettingsActivity extends SettingsActivity {
    public Intent getIntent() {
        Intent intent = super.getIntent();
        Bundle bundle = intent.getBundleExtra("fp_fragment_bundle");
        if (intent.getStringExtra(":settings:show_fragment") != null) {
            return intent;
        }
        Intent newIntent = new Intent(intent);
        newIntent.putExtra(":settings:show_fragment", FingerprintMainSettingsFragment.class.getName());
        newIntent.putExtra(":settings:show_fragment_args", bundle);
        return newIntent;
    }

    protected boolean isValidFragment(String fragmentName) {
        if (FingerprintMainSettingsFragment.class.getName().equals(fragmentName)) {
            return true;
        }
        return super.isValidFragment(fragmentName);
    }

    public void startPreferencePanel(String fragmentClass, Bundle args, int titleRes, CharSequence titleText, Fragment resultTo, int resultRequestCode) {
        super.startPreferencePanel(fragmentClass, args, titleRes, titleText, resultTo, resultRequestCode);
    }
}
