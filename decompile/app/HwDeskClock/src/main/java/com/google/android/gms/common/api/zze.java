package com.google.android.gms.common.api;

import com.google.android.gms.common.data.DataHolder;

/* compiled from: Unknown */
public abstract class zze implements Releasable, Result {
    protected final Status zzQA;
    protected final DataHolder zzYX;

    public Status getStatus() {
        return this.zzQA;
    }

    public void release() {
        if (this.zzYX != null) {
            this.zzYX.close();
        }
    }
}
