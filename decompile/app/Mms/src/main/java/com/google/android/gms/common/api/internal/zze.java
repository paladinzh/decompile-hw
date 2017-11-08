package com.google.android.gms.common.api.internal;

import com.google.android.gms.common.api.internal.zzq.zzb;
import com.google.android.gms.common.data.DataHolder;

/* compiled from: Unknown */
public abstract class zze<L> implements zzb<L> {
    private final DataHolder zzahi;

    protected zze(DataHolder dataHolder) {
        this.zzahi = dataHolder;
    }

    protected abstract void zza(L l, DataHolder dataHolder);

    public void zzpr() {
        if (this.zzahi != null) {
            this.zzahi.close();
        }
    }

    public final void zzt(L l) {
        zza(l, this.zzahi);
    }
}
