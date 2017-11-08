package com.android.settings.accessibility;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings.Secure;
import com.android.settings.HwAnimationReflection;
import com.android.settings.SettingsActivity;
import com.android.settings.Utils;

public class ToggleScreenMagnificationPreferenceFragmentForSetupWizardActivity extends SettingsActivity {
    public Intent getIntent() {
        boolean z = true;
        Bundle args = super.getIntent().getExtras();
        if (args != null) {
            args = new Bundle(args);
        } else {
            args = new Bundle();
        }
        Intent modIntent = new Intent(super.getIntent());
        args.putString("title", getString(2131625853));
        args.putCharSequence("summary", getText(2131625855));
        String str = "checked";
        if (Secure.getInt(getContentResolver(), "accessibility_display_magnification_enabled", 0) != 1) {
            z = false;
        }
        args.putBoolean(str, z);
        modIntent.putExtra(":settings:show_fragment_args", args);
        return modIntent;
    }

    public boolean isValidFragment(String className) {
        if (ToggleScreenMagnificationPreferenceFragmentForSetupWizard.class.getName().equals(className)) {
            return true;
        }
        return super.isValidFragment(className);
    }

    public void finish() {
        super.finish();
        new HwAnimationReflection(this).overrideTransition(2);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Utils.isTablet()) {
            setRequestedOrientation(1);
        }
        getWindow().addFlags(67108864);
        getWindow().getDecorView().setSystemUiVisibility(4352);
    }
}
