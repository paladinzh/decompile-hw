package com.android.settings.wifi.cmcc;

import android.os.SystemProperties;

public class Features {
    private static final boolean mIsCMCC = "CMCC".equalsIgnoreCase(SystemProperties.get("ro.config.operators", ""));

    public static boolean shouldShownByCategory() {
        return false;
    }

    public static boolean isCmccCommonFeatureEnabled() {
        return mIsCMCC;
    }

    public static boolean shouldSetDisconnectButton() {
        return mIsCMCC;
    }

    public static boolean shouldAddDisconnectMenu() {
        return mIsCMCC;
    }

    public static boolean shouldDisableWifiOnAirplaneMode() {
        return false;
    }

    public static boolean shouldDisplayNetmask() {
        return mIsCMCC;
    }

    public static boolean shouldDisplayPrioritySetting() {
        return mIsCMCC;
    }

    public static boolean shouldDisplayConnectionSetting() {
        return !mIsCMCC ? SystemProperties.getBoolean("ro.config.hw_wifi_connect_mode", false) : true;
    }

    public static boolean shouldDisplayCmccWarning() {
        return mIsCMCC;
    }

    public static boolean shouldDisplayWifiToWifiPop() {
        return mIsCMCC;
    }
}
