package com.huawei.hwid.core.d.b;

import android.content.Context;
import android.util.Log;

public class e {
    private static b a = null;

    public static synchronized void a(Context context) {
        synchronized (e.class) {
            a = d.b(context);
        }
    }

    public static void a(String str, String str2) {
        try {
            a.a(str, str2);
        } catch (NullPointerException e) {
            Log.e(str, "log has not init finished");
        }
    }

    public static void a(String str, String str2, Throwable th) {
        try {
            a.a(str, str2, th);
        } catch (NullPointerException e) {
            Log.e(str, "log has not init finished");
        }
    }

    public static void b(String str, String str2) {
        try {
            a.b(str, str2);
        } catch (NullPointerException e) {
            Log.e(str, "log has not init finished");
        }
    }

    public static void b(String str, String str2, Throwable th) {
        try {
            a.b(str, str2, th);
        } catch (NullPointerException e) {
            Log.e(str, "log has not init finished");
        }
    }

    public static void c(String str, String str2) {
        try {
            a.b(str, str2);
        } catch (NullPointerException e) {
            Log.e(str, "log has not init finished");
        }
    }

    public static void c(String str, String str2, Throwable th) {
        try {
            a.b(str, str2, th);
        } catch (NullPointerException e) {
            Log.e(str, "log has not init finished");
        }
    }

    public static void d(String str, String str2) {
        try {
            a.c(str, str2);
        } catch (NullPointerException e) {
            Log.e(str, "log has not init finished");
        }
    }

    public static void d(String str, String str2, Throwable th) {
        try {
            a.b(str, str2, th);
        } catch (NullPointerException e) {
            Log.e(str, "log has not init finished");
        }
    }

    public static void e(String str, String str2) {
        try {
            a.a(str, str2);
        } catch (NullPointerException e) {
            Log.e(str, "log has not init finished");
        }
    }

    public static void e(String str, String str2, Throwable th) {
        try {
            a.b(str, str2, th);
        } catch (NullPointerException e) {
            Log.e(str, "log has not init finished");
        }
    }
}
