package com.google.android.gms.internal;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Status;

/* compiled from: Unknown */
abstract class zzmi<R extends Result> extends com.google.android.gms.common.api.internal.zza.zza<R, zzmj> {

    /* compiled from: Unknown */
    static abstract class zza extends zzmi<Status> {
        public zza(GoogleApiClient googleApiClient) {
            super(googleApiClient);
        }

        public Status zzb(Status status) {
            return status;
        }

        public /* synthetic */ Result zzc(Status status) {
            return zzb(status);
        }
    }

    public zzmi(GoogleApiClient googleApiClient) {
        super(zzmf.zzUI, googleApiClient);
    }
}
