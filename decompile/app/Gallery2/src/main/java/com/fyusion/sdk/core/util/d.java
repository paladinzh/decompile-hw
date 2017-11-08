package com.fyusion.sdk.core.util;

import android.annotation.TargetApi;
import android.os.Build.VERSION;
import android.os.SystemClock;
import com.amap.api.maps.model.WeightedLatLng;

/* compiled from: Unknown */
public final class d {
    private static final double a;

    static {
        double d = WeightedLatLng.DEFAULT_INTENSITY;
        if (17 <= VERSION.SDK_INT) {
            d = WeightedLatLng.DEFAULT_INTENSITY / Math.pow(10.0d, 6.0d);
        }
        a = d;
    }

    public static double a(long j) {
        return ((double) (a() - j)) * a;
    }

    @TargetApi(17)
    public static long a() {
        return 17 > VERSION.SDK_INT ? System.currentTimeMillis() : SystemClock.elapsedRealtimeNanos();
    }
}
