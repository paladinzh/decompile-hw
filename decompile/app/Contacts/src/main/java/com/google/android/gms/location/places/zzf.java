package com.google.android.gms.location.places;

import android.os.RemoteException;
import com.google.android.gms.common.api.Api.zzc;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Status;

/* compiled from: Unknown */
public class zzf extends com.google.android.gms.location.places.internal.zzh.zza {
    private final zzb zzaPw;
    private final zza zzaPx;

    /* compiled from: Unknown */
    public static abstract class zzb<A extends com.google.android.gms.common.api.Api.zzb> extends com.google.android.gms.location.places.zzl.zzb<PlacePhotoMetadataResult, A> {
        public zzb(zzc<A> zzc, GoogleApiClient googleApiClient) {
            super(zzc, googleApiClient);
        }

        protected PlacePhotoMetadataResult zzaT(Status status) {
            return new PlacePhotoMetadataResult(status, null);
        }

        protected /* synthetic */ Result zzc(Status status) {
            return zzaT(status);
        }
    }

    /* compiled from: Unknown */
    public static abstract class zza<A extends com.google.android.gms.common.api.Api.zzb> extends com.google.android.gms.location.places.zzl.zzb<PlacePhotoResult, A> {
        public zza(zzc<A> zzc, GoogleApiClient googleApiClient) {
            super(zzc, googleApiClient);
        }

        protected PlacePhotoResult zzaS(Status status) {
            return new PlacePhotoResult(status, null);
        }

        protected /* synthetic */ Result zzc(Status status) {
            return zzaS(status);
        }
    }

    public zzf(zza zza) {
        this.zzaPw = null;
        this.zzaPx = zza;
    }

    public zzf(zzb zzb) {
        this.zzaPw = zzb;
        this.zzaPx = null;
    }

    public void zza(PlacePhotoMetadataResult placePhotoMetadataResult) throws RemoteException {
        this.zzaPw.zza((Result) placePhotoMetadataResult);
    }

    public void zza(PlacePhotoResult placePhotoResult) throws RemoteException {
        this.zzaPx.zza((Result) placePhotoResult);
    }
}
