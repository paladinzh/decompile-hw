package com.google.android.gms.common.api;

import android.os.Looper;
import com.google.android.gms.common.api.internal.zzr;
import com.google.android.gms.common.api.internal.zzv;
import com.google.android.gms.common.internal.zzx;

/* compiled from: Unknown */
public final class PendingResults {

    /* compiled from: Unknown */
    private static final class zza<R extends Result> extends com.google.android.gms.common.api.internal.zzb<R> {
        private final R zzagx;

        public zza(R r) {
            super(Looper.getMainLooper());
            this.zzagx = r;
        }

        protected R zzc(Status status) {
            if (status.getStatusCode() == this.zzagx.getStatus().getStatusCode()) {
                return this.zzagx;
            }
            throw new UnsupportedOperationException("Creating failed results is not supported");
        }
    }

    /* compiled from: Unknown */
    private static final class zzb<R extends Result> extends com.google.android.gms.common.api.internal.zzb<R> {
        private final R zzagy;

        public zzb(GoogleApiClient googleApiClient, R r) {
            super(googleApiClient);
            this.zzagy = r;
        }

        protected R zzc(Status status) {
            return this.zzagy;
        }
    }

    /* compiled from: Unknown */
    private static final class zzc<R extends Result> extends com.google.android.gms.common.api.internal.zzb<R> {
        public zzc(GoogleApiClient googleApiClient) {
            super(googleApiClient);
        }

        protected R zzc(Status status) {
            throw new UnsupportedOperationException("Creating failed results is not supported");
        }
    }

    private PendingResults() {
    }

    public static PendingResult<Status> canceledPendingResult() {
        PendingResult zzv = new zzv(Looper.getMainLooper());
        zzv.cancel();
        return zzv;
    }

    public static <R extends Result> PendingResult<R> canceledPendingResult(R result) {
        zzx.zzb((Object) result, (Object) "Result must not be null");
        zzx.zzb(result.getStatus().getStatusCode() == 16, (Object) "Status code must be CommonStatusCodes.CANCELED");
        PendingResult zza = new zza(result);
        zza.cancel();
        return zza;
    }

    public static <R extends Result> OptionalPendingResult<R> immediatePendingResult(R result) {
        zzx.zzb((Object) result, (Object) "Result must not be null");
        PendingResult zzc = new zzc(null);
        zzc.zza((Result) result);
        return new zzr(zzc);
    }

    public static PendingResult<Status> immediatePendingResult(Status result) {
        zzx.zzb((Object) result, (Object) "Result must not be null");
        PendingResult zzv = new zzv(Looper.getMainLooper());
        zzv.zza((Result) result);
        return zzv;
    }

    public static <R extends Result> PendingResult<R> zza(R r, GoogleApiClient googleApiClient) {
        boolean z = false;
        zzx.zzb((Object) r, (Object) "Result must not be null");
        if (!r.getStatus().isSuccess()) {
            z = true;
        }
        zzx.zzb(z, (Object) "Status code must not be SUCCESS");
        PendingResult zzb = new zzb(googleApiClient, r);
        zzb.zza((Result) r);
        return zzb;
    }

    public static PendingResult<Status> zza(Status status, GoogleApiClient googleApiClient) {
        zzx.zzb((Object) status, (Object) "Result must not be null");
        PendingResult zzv = new zzv(googleApiClient);
        zzv.zza((Result) status);
        return zzv;
    }

    public static <R extends Result> OptionalPendingResult<R> zzb(R r, GoogleApiClient googleApiClient) {
        zzx.zzb((Object) r, (Object) "Result must not be null");
        PendingResult zzc = new zzc(googleApiClient);
        zzc.zza((Result) r);
        return new zzr(zzc);
    }
}
