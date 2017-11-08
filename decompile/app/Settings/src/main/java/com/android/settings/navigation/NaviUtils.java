package com.android.settings.navigation;

import android.content.ContentResolver;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Log;
import com.android.settings.Utils;

public class NaviUtils {
    static final int[] FP_BACK_DRAWABLES = new int[]{2130837933, 2130837940, 2130837941, 2130837934, 2130837935, 2130837936, 2130837937, 2130837938, 2130837939};
    static final int[] FP_GAPP_DRAWABLES = new int[]{2130837942, 2130837950, 2130837951, 2130837952, 2130837953, 2130837954, 2130837955, 2130837956, 2130837957, 2130837943, 2130837944, 2130837945, 2130837946, 2130837947, 2130837948, 2130837949, 2130837949};
    static final int[] FP_HOME_DRAWABLES = new int[]{2130837966, 2130837975, 2130837976, 2130837967, 2130837968, 2130837969, 2130837970, 2130837971, 2130837972, 2130837973, 2130837974};
    static final int[] FP_RECENT_DRAWABLES = new int[]{2130837978, 2130837989, 2130837993, 2130837979, 2130837980, 2130837981, 2130837982, 2130837983, 2130837984};
    static final int[] FP_VOICE_DRAWABLES = new int[]{2130837995, 2130838005, 2130838006, 2130838007, 2130838008, 2130837997, 2130837998, 2130837999, 2130838000, 2130838001, 2130838002, 2130838003, 2130838004};

    public static boolean isFrontFingerNaviEnabled() {
        return SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    }

    public static int getTrikeyState() {
        return SystemProperties.getInt("ro.config.hw_front_fp_trikey", 0);
    }

    public static boolean isTrikeyDevice() {
        boolean z = false;
        if (!isFrontFingerNaviEnabled()) {
            return false;
        }
        if (getTrikeyState() == 1) {
            z = isTrikeyExist();
        }
        return z;
    }

    public static int getEnableNaviDefaultValue() {
        if (isTrikeyDevice()) {
            return 0;
        }
        if (Utils.isChinaArea()) {
            return 0;
        }
        return 1;
    }

    public static int getTrikeyTypeDefaultValue() {
        if (!isTrikeyDevice()) {
            return -1;
        }
        if (Utils.isChinaArea()) {
            return -1;
        }
        return 0;
    }

    public static boolean isTrikeyExist() {
        boolean ret = false;
        try {
            Class clazz = Class.forName("huawei.android.os.HwGeneralManager");
            ret = ((Boolean) clazz.getDeclaredMethod("isSupportTrikey", null).invoke(clazz.getDeclaredMethod("getInstance", null).invoke(clazz, (Object[]) null), (Object[]) null)).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("NaviUtils", "isTrikeyExist error !");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Log.e("NaviUtils", "isTrikeyExist = " + ret);
        return ret;
    }

    public static boolean isNaviBarEnabled(ContentResolver resolver) {
        boolean z = true;
        if (!isFrontFingerNaviEnabled()) {
            return true;
        }
        if (isTrikeyDevice()) {
            return false;
        }
        if (resolver == null) {
            Log.e("NaviUtils", "isNaviBarEnabled ContentResolver is null!");
            return false;
        }
        if (System.getInt(resolver, "enable_navbar", getEnableNaviDefaultValue()) != 1) {
            z = false;
        }
        return z;
    }

    public static int getPhysicNaviHapticDefault() {
        return 1;
    }
}
