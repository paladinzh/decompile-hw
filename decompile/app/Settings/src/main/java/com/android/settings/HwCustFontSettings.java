package com.android.settings;

import android.content.Context;

public class HwCustFontSettings {
    public Context mContext;

    public HwCustFontSettings(Context context) {
        this.mContext = context;
    }

    public boolean hideSettingsFontStyle() {
        return false;
    }
}
