package com.avast.android.shepherd.obfuscated;

import android.os.Environment;
import android.util.Log;
import java.io.File;

/* compiled from: Unknown */
public class x {
    private static String a = "avast!";
    private static boolean b = false;

    public static int a(String str, String str2) {
        return !b ? 0 : Log.v(str, str2);
    }

    public static int a(String str, String str2, Throwable th) {
        return Log.w(str, str2, th);
    }

    public static int a(String str, Throwable th) {
        return a(a, str, th);
    }

    public static void a(String str) {
        boolean z = false;
        a = str;
        try {
            if (new File(Environment.getExternalStorageDirectory(), "avast-debug").exists()) {
                z = true;
            }
        } catch (Exception e) {
        }
        a(z);
    }

    private static void a(boolean z) {
        b = z;
    }

    public static int b(String str) {
        return a(a, str);
    }

    public static int b(String str, String str2) {
        return !b ? 0 : Log.d(str, str2);
    }

    public static int b(String str, String str2, Throwable th) {
        return Log.e(str, str2, th);
    }

    public static int b(String str, Throwable th) {
        return b(a, str, th);
    }

    public static int c(String str) {
        return b(a, str);
    }
}
