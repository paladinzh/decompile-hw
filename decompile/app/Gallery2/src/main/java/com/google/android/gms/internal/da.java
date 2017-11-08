package com.google.android.gms.internal;

import android.util.Log;

/* compiled from: Unknown */
public final class da {
    public static void b(String str, Throwable th) {
        if (n(5)) {
            Log.w("Ads", str, th);
        }
    }

    public static boolean n(int i) {
        return (i < 5 && !Log.isLoggable("Ads", i)) ? false : i != 2;
    }

    public static void s(String str) {
        if (n(3)) {
            Log.d("Ads", str);
        }
    }

    public static void t(String str) {
        if (n(6)) {
            Log.e("Ads", str);
        }
    }

    public static void u(String str) {
        if (n(4)) {
            Log.i("Ads", str);
        }
    }

    public static void v(String str) {
        if (n(2)) {
            Log.v("Ads", str);
        }
    }

    public static void w(String str) {
        if (n(5)) {
            Log.w("Ads", str);
        }
    }
}
