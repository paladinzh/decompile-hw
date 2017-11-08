package com.google.android.gms.internal;

import android.os.Build.VERSION;

/* compiled from: Unknown */
public final class fr {
    private static boolean ac(int i) {
        return VERSION.SDK_INT >= i;
    }

    public static boolean eJ() {
        return ac(11);
    }

    public static boolean eL() {
        return ac(13);
    }

    public static boolean eO() {
        return ac(17);
    }
}
