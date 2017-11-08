package com.google.android.gms.internal;

import android.os.SystemClock;

/* compiled from: Unknown */
public final class zzmt implements zzmq {
    private static zzmt zzaoa;

    public static synchronized zzmq zzsc() {
        zzmq zzmq;
        synchronized (zzmt.class) {
            if (zzaoa == null) {
                zzaoa = new zzmt();
            }
            zzmq = zzaoa;
        }
        return zzmq;
    }

    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public long elapsedRealtime() {
        return SystemClock.elapsedRealtime();
    }

    public long nanoTime() {
        return System.nanoTime();
    }
}
