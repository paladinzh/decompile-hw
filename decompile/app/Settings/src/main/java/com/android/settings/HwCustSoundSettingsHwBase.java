package com.android.settings;

import android.content.Context;

public class HwCustSoundSettingsHwBase {
    public HwCustSoundSettingsHwBase(Context context) {
    }

    public boolean isNotShowPowerOnToneOption() {
        return false;
    }

    public boolean isRemoveShotSounds() {
        return false;
    }
}
