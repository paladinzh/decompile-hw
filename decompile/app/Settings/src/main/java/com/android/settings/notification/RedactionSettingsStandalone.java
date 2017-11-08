package com.android.settings.notification;

import android.content.Intent;
import android.os.Bundle;
import com.android.settings.SettingsActivity;
import com.android.settings.notification.RedactionInterstitial.RedactionInterstitialFragment;

public class RedactionSettingsStandalone extends SettingsActivity {
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(":settings:show_fragment", RedactionInterstitialFragment.class.getName()).putExtra("extra_prefs_show_button_bar", true).putExtra("extra_prefs_set_back_text", (String) null).putExtra("extra_prefs_set_next_text", getString(2131626777));
        return modIntent;
    }

    protected boolean isValidFragment(String fragmentName) {
        return RedactionInterstitialFragment.class.getName().equals(fragmentName);
    }

    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        finish();
    }
}
