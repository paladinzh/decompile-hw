package com.fyusion.sdk.common.ext.util;

import android.media.CamcorderProfile;
import android.os.Build;
import com.fyusion.sdk.common.e;

@Deprecated
/* compiled from: Unknown */
public class b {
    public static boolean a = false;
    public static boolean b = false;
    private static e c = new e(0.0d, 0.0d);
    private static CamcorderProfile d;
    private static volatile String e = null;

    public static CamcorderProfile a() {
        return d;
    }

    public static String a(String str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        char charAt = str.charAt(0);
        return !Character.isUpperCase(charAt) ? Character.toUpperCase(charAt) + str.substring(1) : str;
    }

    public static void a(CamcorderProfile camcorderProfile) {
        d = camcorderProfile;
    }

    public static String b() {
        String str = Build.MANUFACTURER;
        String str2 = Build.MODEL;
        return !str2.startsWith(str) ? a(str) + "." + str2 : a(str2);
    }
}
