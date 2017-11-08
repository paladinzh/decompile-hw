package com.google.android.gms.internal;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.os.SystemClock;
import cn.com.xy.sms.sdk.constant.Constant;

/* compiled from: Unknown */
public final class zzmv {
    private static IntentFilter zzaob = new IntentFilter("android.intent.action.BATTERY_CHANGED");
    private static long zzaoc;
    private static float zzaod = Float.NaN;

    @TargetApi(20)
    public static int zzax(Context context) {
        int i = 0;
        if (context == null || context.getApplicationContext() == null) {
            return -1;
        }
        Intent registerReceiver = context.getApplicationContext().registerReceiver(null, zzaob);
        int i2 = ((registerReceiver != null ? registerReceiver.getIntExtra("plugged", 0) : 0) & 7) == 0 ? 0 : 1;
        PowerManager powerManager = (PowerManager) context.getSystemService("power");
        if (powerManager == null) {
            return -1;
        }
        int i3 = (!(!zzne.zzsl() ? powerManager.isScreenOn() : powerManager.isInteractive()) ? 0 : 1) << 1;
        if (i2 != 0) {
            i = 1;
        }
        return i3 | i;
    }

    public static synchronized float zzay(Context context) {
        synchronized (zzmv.class) {
            if ((SystemClock.elapsedRealtime() - zzaoc >= Constant.MINUTE ? 1 : null) != null || zzaod == Float.NaN) {
                Intent registerReceiver = context.getApplicationContext().registerReceiver(null, zzaob);
                if (registerReceiver != null) {
                    zzaod = ((float) registerReceiver.getIntExtra("level", -1)) / ((float) registerReceiver.getIntExtra("scale", -1));
                }
                zzaoc = SystemClock.elapsedRealtime();
                float f = zzaod;
                return f;
            }
            f = zzaod;
            return f;
        }
    }
}
