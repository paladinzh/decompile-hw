package com.fyusion.sdk.common.ext.util;

import android.os.Build;
import com.fyusion.sdk.common.e;
import fyusion.vislib.BuildConfig;

@Deprecated
/* compiled from: Unknown */
public class b {
    public static boolean a = false;
    public static boolean b = false;
    private static e c = new e(0.0d, 0.0d);
    private static volatile String d = null;

    public static String a() {
        String str = Build.MANUFACTURER;
        String str2 = Build.MODEL;
        return !str2.startsWith(str) ? a(str) + "." + str2 : a(str2);
    }

    public static String a(String str) {
        if (str == null || str.length() == 0) {
            return BuildConfig.FLAVOR;
        }
        char charAt = str.charAt(0);
        return !Character.isUpperCase(charAt) ? Character.toUpperCase(charAt) + str.substring(1) : str;
    }
}
