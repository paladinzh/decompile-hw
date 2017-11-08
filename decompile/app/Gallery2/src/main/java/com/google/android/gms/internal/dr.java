package com.google.android.gms.internal;

/* compiled from: Unknown */
public final class dr {
    public static <T> boolean a(T t, T t2) {
        if (!(t == null && t2 == null)) {
            if (t == null || t2 == null) {
                return false;
            }
            if (!t.equals(t2)) {
                return false;
            }
        }
        return true;
    }
}
