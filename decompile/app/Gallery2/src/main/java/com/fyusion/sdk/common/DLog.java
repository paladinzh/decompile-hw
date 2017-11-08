package com.fyusion.sdk.common;

import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/* compiled from: Unknown */
public class DLog {
    private static SimpleDateFormat a = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);

    public static void d(String str, String str2) {
    }

    public static void d(String str, String str2, Throwable th) {
    }

    public static void e(String str, String str2) {
        if (str2 != null) {
            Log.e("com.fyusion.sdk.common", a.format(new Date()) + " [" + str + "]: " + str2);
        }
    }

    public static void e(String str, String str2, Throwable th) {
        if (str2 != null) {
            Log.e("com.fyusion.sdk.common", a.format(new Date()) + " [" + str + "]: " + str2, th);
        }
    }

    public static void i(String str, String str2) {
        if (str2 != null) {
            Log.i("com.fyusion.sdk.common", a.format(new Date()) + " [" + str + "]: " + str2);
        }
    }

    public static void i(String str, String str2, Throwable th) {
        if (str2 != null) {
            Log.i("com.fyusion.sdk.common", a.format(new Date()) + " [" + str + "]: " + str2);
        }
    }

    public static void v(String str, String str2) {
    }

    public static void v(String str, String str2, Throwable th) {
    }

    public static void w(String str, String str2) {
        if (str2 != null) {
            Log.w("com.fyusion.sdk.common", a.format(new Date()) + " [" + str + "]: " + str2);
        }
    }

    public static void w(String str, String str2, Throwable th) {
        if (str2 != null) {
            Log.w("com.fyusion.sdk.common", a.format(new Date()) + " [" + str + "]: " + str2);
        }
    }
}
