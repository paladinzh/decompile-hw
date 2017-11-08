package com.google.android.gms.wearable.internal;

import com.google.android.gms.wearable.internal.zzat.zza;

/* compiled from: Unknown */
public final class zzs extends zza {
    private zzl zzbac;
    private zzt zzbag;
    private final Object zzpc = new Object();

    public void zzx(int i, int i2) {
        synchronized (this.zzpc) {
            zzt zzt = this.zzbag;
            zzl zzl = new zzl(i, i2);
            this.zzbac = zzl;
        }
        if (zzt != null) {
            zzt.zzb(zzl);
        }
    }
}
