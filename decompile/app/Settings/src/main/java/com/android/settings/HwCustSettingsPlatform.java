package com.android.settings;

import android.content.Context;

public class HwCustSettingsPlatform {
    public SettingsPlatformImp mSettingsPlatformImp;

    public HwCustSettingsPlatform(SettingsPlatformImp settingsPlatformImp) {
        this.mSettingsPlatformImp = settingsPlatformImp;
    }

    public Class<?> getClassForCommandCode(Context context, String commandCode) {
        return null;
    }
}
