package com.huawei.mms.ui;

import android.content.Context;
import android.preference.SwitchPreference;

public class HwCustRingToneAndVibrateSettings {
    public boolean isSmartRingtoneSupported() {
        return false;
    }

    public void setRingtones(Context context, SwitchPreference inChatPref, SwitchPreference outChatPref) {
    }

    public void setDefaultRingtones(Context context, boolean isDefault) {
    }

    public void setDefaultInChatTone(boolean isDefault) {
    }

    public void setDefaultOutChatTone(boolean isDefault) {
    }
}
