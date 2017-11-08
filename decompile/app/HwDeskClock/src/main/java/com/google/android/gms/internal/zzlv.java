package com.google.android.gms.internal;

import android.os.Build.VERSION;

/* compiled from: Unknown */
public final class zzlv {
    private static boolean zzbZ(int i) {
        return VERSION.SDK_INT >= i;
    }

    public static boolean zzpQ() {
        return zzbZ(11);
    }

    public static boolean zzpS() {
        return zzbZ(13);
    }

    public static boolean zzpT() {
        return zzbZ(14);
    }

    public static boolean zzpW() {
        return zzbZ(18);
    }

    public static boolean zzpX() {
        return zzbZ(19);
    }

    public static boolean zzpZ() {
        return zzbZ(21);
    }
}
