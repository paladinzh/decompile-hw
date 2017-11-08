package com.google.android.gms.internal;

import android.os.Build.VERSION;

/* compiled from: Unknown */
public final class zzne {
    @Deprecated
    public static boolean isAtLeastL() {
        return zzsm();
    }

    private static boolean zzcp(int i) {
        return VERSION.SDK_INT >= i;
    }

    public static boolean zzsd() {
        return zzcp(11);
    }

    public static boolean zzse() {
        return zzcp(12);
    }

    public static boolean zzsf() {
        return zzcp(13);
    }

    public static boolean zzsg() {
        return zzcp(14);
    }

    public static boolean zzsh() {
        return zzcp(16);
    }

    public static boolean zzsi() {
        return zzcp(17);
    }

    public static boolean zzsj() {
        return zzcp(18);
    }

    public static boolean zzsk() {
        return zzcp(19);
    }

    public static boolean zzsl() {
        return zzcp(20);
    }

    public static boolean zzsm() {
        return zzcp(21);
    }

    public static boolean zzsn() {
        return zzcp(23);
    }
}
