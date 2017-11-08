package com.android.settings.inputmethod;

import android.content.Context;
import android.support.v14.preference.SwitchPreference;

class SwitchWithNoTextPreference extends SwitchPreference {
    SwitchWithNoTextPreference(Context context) {
        super(context);
        setSwitchTextOn("");
        setSwitchTextOff("");
    }
}
