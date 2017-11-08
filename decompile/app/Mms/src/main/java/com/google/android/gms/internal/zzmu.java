package com.google.android.gms.internal;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

/* compiled from: Unknown */
public final class zzmu {
    @TargetApi(20)
    public static boolean zzaw(Context context) {
        return zzne.zzsl() && context.getPackageManager().hasSystemFeature("android.hardware.type.watch");
    }

    public static boolean zzb(Resources resources) {
        boolean z = false;
        if (resources == null) {
            return false;
        }
        boolean z2 = (resources.getConfiguration().screenLayout & 15) > 3;
        if ((zzne.zzsd() && z2) || zzc(resources)) {
            z = true;
        }
        return z;
    }

    @TargetApi(13)
    private static boolean zzc(Resources resources) {
        boolean z = false;
        Configuration configuration = resources.getConfiguration();
        if (!zzne.zzsf()) {
            return false;
        }
        if ((configuration.screenLayout & 15) <= 3 && configuration.smallestScreenWidthDp >= 600) {
            z = true;
        }
        return z;
    }
}
