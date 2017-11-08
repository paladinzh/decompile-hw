package com.android.settings;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.SettingsEx.Systemex;
import com.android.settings.Settings.TestingSettingsActivity;

public class HwCustSettingsPlatformImpl extends HwCustSettingsPlatform {
    private static final String CATEGORY_INTER_ROAMING_TIME_KEY = "pref_catkey_inter_roam_time";
    private static final String CONFIRM_DOUBLE_TIME_TIMESCHENE_KEY = "pref_key_double_time_timescheme";
    private static final String CONFIRM_DOUBLE_TIME_TIMEZONE_KEY = "pref_key_double_time_timezone";

    public HwCustSettingsPlatformImpl(SettingsPlatformImp settingsPlatformImp) {
        super(settingsPlatformImp);
    }

    public Class<?> getClassForCommandCode(Context context, String commandCode) {
        boolean is_showWifimacAddr = 1 == Systemex.getInt(context.getContentResolver(), "show_wifimac_addr", 0);
        if ("2846".equals(commandCode) && SystemProperties.getBoolean("ro.config.show2846", false)) {
            return TestingSettingsActivity.class;
        }
        if ("0100".equals(commandCode) && is_showWifimacAddr) {
            return TestingWifiMacAddr.class;
        }
        if ("4636".equals(commandCode)) {
            return Utils.class;
        }
        if ("6130".equals(commandCode) && SystemProperties.getBoolean("ro.config.show6130", true)) {
            return TestingSettingsActivity.class;
        }
        return null;
    }
}
