package com.android.settings;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import com.android.settings.utils.VoiceSettingsActivity;

public class AirplaneModeVoiceActivity extends VoiceSettingsActivity {
    protected boolean onVoiceSettingInteraction(Intent intent) {
        if (intent.hasExtra("airplane_mode_enabled")) {
            ((ConnectivityManager) getSystemService("connectivity")).setAirplaneMode(intent.getBooleanExtra("airplane_mode_enabled", false));
        } else {
            Log.v("AirplaneModeVoiceActivity", "Missing airplane mode extra");
        }
        return true;
    }
}
