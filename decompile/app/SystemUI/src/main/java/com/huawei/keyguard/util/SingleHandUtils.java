package com.huawei.keyguard.util;

import android.content.Context;
import android.os.SystemProperties;

public class SingleHandUtils {
    private static final int SUPPROT_MODE = SystemProperties.getInt("ro.config.hw_singlehand", 0);
    private static int mScreenSizeFlag = 0;

    public static boolean isSingleHandOpen(Context context) {
        if (OsUtils.getSystemInt(context, "single_hand_switch", 0) == 1 && (SUPPROT_MODE == 1 || SUPPROT_MODE == 2)) {
            return true;
        }
        return false;
    }

    public static boolean isGravitySensorModeOpen(Context context) {
        boolean z = true;
        if (context == null || SUPPROT_MODE != 2) {
            HwLog.w("KeyguardSingleHandUtils", "isGravitySensorModeOpen context is null");
            return false;
        }
        if (1 != OsUtils.getSystemInt(context, "single_hand_smart", 0)) {
            z = false;
        }
        return z;
    }

    public static boolean isMediumScreen() {
        return mScreenSizeFlag == 2;
    }

    public static int getSingleHandMode(Context context) {
        return OsUtils.getSystemInt(context, "single_hand_mode", 2);
    }

    public static boolean setSingleHandMode(Context context, int mode) {
        boolean isSingleHandOpen = true;
        if (1 > mode || mode > 2) {
            isSingleHandOpen = false;
        }
        if (isSingleHandOpen) {
            return OsUtils.putSystemInt(context, "single_hand_mode", mode);
        }
        OsUtils.putSystemInt(context, "single_hand_mode", 0);
        return false;
    }

    public static String getSingleHandleName(int mode) {
        switch (mode) {
            case 1:
                return "left";
            case 2:
                return "right";
            default:
                return "middle";
        }
    }
}
