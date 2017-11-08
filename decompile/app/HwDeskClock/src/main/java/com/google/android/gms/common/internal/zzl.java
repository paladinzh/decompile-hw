package com.google.android.gms.common.internal;

import android.content.Context;
import android.content.ServiceConnection;

/* compiled from: Unknown */
public abstract class zzl {
    private static final Object zzadU = new Object();
    private static zzl zzadV;

    public static zzl zzak(Context context) {
        synchronized (zzadU) {
            if (zzadV == null) {
                zzadV = new zzm(context.getApplicationContext());
            }
        }
        return zzadV;
    }

    public abstract boolean zza(String str, String str2, ServiceConnection serviceConnection, String str3);

    public abstract void zzb(String str, String str2, ServiceConnection serviceConnection, String str3);
}
