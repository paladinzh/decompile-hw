package com.android.settings;

import android.content.Context;
import android.support.v7.preference.PreferenceScreen;

public class HwCustTestingSettingsHwBase {
    public Context mContext;

    public HwCustTestingSettingsHwBase(Context context) {
        this.mContext = context;
    }

    public void hideWifiInfoMenu(PreferenceScreen preferenceScreen) {
    }
}
