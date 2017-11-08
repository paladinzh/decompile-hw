package com.google.android.gms.common.api.internal;

import com.google.android.gms.common.api.Releasable;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.data.DataHolder;

/* compiled from: Unknown */
public abstract class zzf implements Releasable, Result {
    protected final Status zzUX;
    protected final DataHolder zzahi;

    protected zzf(DataHolder dataHolder, Status status) {
        this.zzUX = status;
        this.zzahi = dataHolder;
    }

    public Status getStatus() {
        return this.zzUX;
    }

    public void release() {
        if (this.zzahi != null) {
            this.zzahi.close();
        }
    }
}
