package com.android.settings.fuelgauge;

import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import com.android.settings.utils.VoiceSettingsActivity;

public class BatterySaverModeVoiceActivity extends VoiceSettingsActivity {
    protected boolean onVoiceSettingInteraction(Intent intent) {
        if (!intent.hasExtra("android.settings.extra.battery_saver_mode_enabled")) {
            Log.v("BatterySaverModeVoiceActivity", "Missing battery saver mode extra");
        } else if (((PowerManager) getSystemService("power")).setPowerSaveMode(intent.getBooleanExtra("android.settings.extra.battery_saver_mode_enabled", false))) {
            notifySuccess(null);
        } else {
            Log.v("BatterySaverModeVoiceActivity", "Unable to set power mode");
            notifyFailure(null);
        }
        return true;
    }
}
