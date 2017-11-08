package com.google.android.gms.common.api.internal;

import android.os.Looper;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Status;

/* compiled from: Unknown */
public class zzv extends zzb<Status> {
    @Deprecated
    public zzv(Looper looper) {
        super(looper);
    }

    public zzv(GoogleApiClient googleApiClient) {
        super(googleApiClient);
    }

    protected Status zzb(Status status) {
        return status;
    }

    protected /* synthetic */ Result zzc(Status status) {
        return zzb(status);
    }
}
