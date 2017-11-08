package com.google.android.gms.wearable;

import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.data.zzf;
import com.google.android.gms.wearable.internal.zzae;

/* compiled from: Unknown */
public class DataItemBuffer extends zzf<DataItem> implements Result {
    private final Status zzQA;

    public Status getStatus() {
        return this.zzQA;
    }

    protected /* synthetic */ Object zzk(int i, int i2) {
        return zzw(i, i2);
    }

    protected String zznX() {
        return "path";
    }

    protected DataItem zzw(int i, int i2) {
        return new zzae(this.zzYX, i, i2);
    }
}
