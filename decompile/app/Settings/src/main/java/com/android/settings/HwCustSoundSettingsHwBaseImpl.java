package com.android.settings;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.System;

public class HwCustSoundSettingsHwBaseImpl extends HwCustSoundSettingsHwBase {
    private Context mContext;

    public HwCustSoundSettingsHwBaseImpl(Context context) {
        this.mContext = context;
    }

    public boolean isNotShowPowerOnToneOption() {
        return SystemProperties.getBoolean("ro.config.no_show_poweron_tone", false);
    }

    public boolean isRemoveShotSounds() {
        String shotSound = System.getString(this.mContext.getContentResolver(), "always_play_screenshot_sound");
        if (shotSound == null || !"true".equals(shotSound)) {
            return false;
        }
        return true;
    }
}
