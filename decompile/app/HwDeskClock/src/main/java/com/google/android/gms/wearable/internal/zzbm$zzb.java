package com.google.android.gms.wearable.internal;

import com.google.android.gms.common.api.zzc.zzb;

/* compiled from: Unknown */
abstract class zzbm$zzb<T> extends zza {
    private zzb<T> zzPW;

    public zzbm$zzb(zzb<T> zzb) {
        this.zzPW = zzb;
    }

    public void zzR(T t) {
        zzb zzb = this.zzPW;
        if (zzb != null) {
            zzb.zzn(t);
            this.zzPW = null;
        }
    }
}
