package com.fyusion.sdk.viewer.internal.f;

import android.text.TextUtils;

/* compiled from: Unknown */
public final class d {
    public static <T> T a(T t) {
        return a((Object) t, "Argument must not be null");
    }

    public static <T> T a(T t, String str) {
        if (t != null) {
            return t;
        }
        throw new NullPointerException(str);
    }

    public static String a(String str) {
        if (!TextUtils.isEmpty(str)) {
            return str;
        }
        throw new IllegalArgumentException("Must not be null or empty");
    }

    public static void a(boolean z, String str) {
        if (!z) {
            throw new IllegalArgumentException(str);
        }
    }
}
