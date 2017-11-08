package com.android.settings.inputmethod;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import com.android.settings.HwAnimationReflection;
import com.android.settings.SettingsActivity;

public class InputMethodAndSubtypeEnablerActivity extends SettingsActivity {
    private static final String FRAGMENT_NAME = InputMethodAndSubtypeEnabler.class.getName();

    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    public boolean onNavigateUp() {
        finish();
        return true;
    }

    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        if (!modIntent.hasExtra(":settings:show_fragment")) {
            modIntent.putExtra(":settings:show_fragment", FRAGMENT_NAME);
        }
        return modIntent;
    }

    protected boolean isValidFragment(String fragmentName) {
        return FRAGMENT_NAME.equals(fragmentName);
    }

    public void finish() {
        super.finish();
        new HwAnimationReflection(this).overrideTransition(2);
    }
}
