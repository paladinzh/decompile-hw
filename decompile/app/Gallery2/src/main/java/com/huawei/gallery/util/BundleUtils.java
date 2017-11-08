package com.huawei.gallery.util;

import android.os.Bundle;
import com.android.gallery3d.util.GalleryLog;

public class BundleUtils {
    private BundleUtils() {
    }

    public static boolean isValid(Bundle bundle) {
        if (bundle == null) {
            return false;
        }
        try {
            bundle.putBoolean("test-key", true);
            return true;
        } catch (Exception e) {
            GalleryLog.d("BundleUtils", "bundle test failed. " + e.getMessage());
            return false;
        }
    }

    private static <T> T readDataSafely(Bundle bundle, String key) {
        T ret = null;
        if (bundle != null) {
            try {
                ret = bundle.get(key);
            } catch (Exception e) {
                GalleryLog.d("BundleUtils", "read data error ! " + e.getMessage());
            }
        }
        return ret;
    }

    public static <T> T getValue(Bundle bundle, String key, Object defaultValue) {
        T ret = readDataSafely(bundle, key);
        if (ret != null) {
            return ret;
        }
        GalleryLog.d("BundleUtils", "value for " + key + " is null");
        return defaultValue;
    }

    public static <T> T getValue(Bundle bundle, String key) {
        return getValue(bundle, key, null);
    }

    public static boolean getBoolean(Bundle bundle, String key, boolean defaultValue) {
        Boolean b = (Boolean) getValue(bundle, key);
        return b == null ? defaultValue : b.booleanValue();
    }

    public static int getInt(Bundle bundle, String key, int defaultValue) {
        Integer value = (Integer) getValue(bundle, key);
        return value == null ? defaultValue : value.intValue();
    }

    public static String getString(Bundle bundle, String key) {
        return (String) getValue(bundle, key);
    }
}
