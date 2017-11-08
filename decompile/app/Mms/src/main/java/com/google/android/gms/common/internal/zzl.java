package com.google.android.gms.common.internal;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;

/* compiled from: Unknown */
public abstract class zzl {
    private static final Object zzalX = new Object();
    private static zzl zzalY;

    public static zzl zzau(Context context) {
        synchronized (zzalX) {
            if (zzalY == null) {
                zzalY = new zzm(context.getApplicationContext());
            }
        }
        return zzalY;
    }

    public abstract boolean zza(ComponentName componentName, ServiceConnection serviceConnection, String str);

    public abstract boolean zza(String str, ServiceConnection serviceConnection, String str2);

    public abstract void zzb(ComponentName componentName, ServiceConnection serviceConnection, String str);

    public abstract void zzb(String str, ServiceConnection serviceConnection, String str2);
}
