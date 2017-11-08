package com.huawei.watermark.wmutil;

import android.util.Log;

public class WMCustomConfigurationUtil {
    private static final String TAG = ("CAMERA3WATERMARK_" + WMCustomConfigurationUtil.class.getSimpleName());
    private static boolean isDMSupported = false;
    private static ReflectClass properties;

    static {
        try {
            properties = new ReflectClass("android.os.SystemProperties", new Class[0]);
        } catch (Exception e) {
            Log.e(TAG, "Initialize SystemProperties failed.");
        }
    }

    public static boolean isEuropeanZone() {
        if (((Integer) properties.invokeS("getInt", "ro.config.hw_opta", Integer.valueOf(0))).intValue() == 432) {
            if (((Integer) properties.invokeS("getInt", "ro.config.hw_optb", Integer.valueOf(0))).intValue() == 999) {
                return true;
            }
        }
        return ((Boolean) properties.invokeS("getBoolean", "ro.config.show_centigrade", Boolean.valueOf(false))).booleanValue();
    }

    public static boolean isChineseZone() {
        return ((Integer) properties.invokeS("getInt", "ro.config.hw_optb", Integer.valueOf(0))).intValue() == 156;
    }

    public static void setIsDMSupported(boolean supported) {
        isDMSupported = supported;
    }

    public static boolean isDMSupported() {
        return isDMSupported;
    }
}
