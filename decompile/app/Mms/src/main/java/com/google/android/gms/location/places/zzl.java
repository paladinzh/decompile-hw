package com.google.android.gms.location.places;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.data.DataHolder;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.internal.zzng;

/* compiled from: Unknown */
public class zzl extends com.google.android.gms.location.places.internal.zzi.zza {
    private static final String TAG = zzl.class.getSimpleName();
    private final Context mContext;
    private final zzd zzaPP;
    private final zza zzaPQ;
    private final zze zzaPR;
    private final zzf zzaPS;
    private final zzc zzaPT;

    /* compiled from: Unknown */
    public static abstract class zzb<R extends Result, A extends com.google.android.gms.common.api.Api.zzb> extends com.google.android.gms.common.api.internal.zza.zza<R, A> {
        public zzb(com.google.android.gms.common.api.Api.zzc<A> zzc, GoogleApiClient googleApiClient) {
            super(zzc, googleApiClient);
        }
    }

    /* compiled from: Unknown */
    public static abstract class zzc<A extends com.google.android.gms.common.api.Api.zzb> extends zzb<PlaceBuffer, A> {
        public zzc(com.google.android.gms.common.api.Api.zzc<A> zzc, GoogleApiClient googleApiClient) {
            super(zzc, googleApiClient);
        }

        protected PlaceBuffer zzaW(Status status) {
            return new PlaceBuffer(DataHolder.zzbI(status.getStatusCode()), null);
        }

        protected /* synthetic */ Result zzc(Status status) {
            return zzaW(status);
        }
    }

    /* compiled from: Unknown */
    public static abstract class zza<A extends com.google.android.gms.common.api.Api.zzb> extends zzb<AutocompletePredictionBuffer, A> {
        public zza(com.google.android.gms.common.api.Api.zzc<A> zzc, GoogleApiClient googleApiClient) {
            super(zzc, googleApiClient);
        }

        protected AutocompletePredictionBuffer zzaV(Status status) {
            return new AutocompletePredictionBuffer(DataHolder.zzbI(status.getStatusCode()));
        }

        protected /* synthetic */ Result zzc(Status status) {
            return zzaV(status);
        }
    }

    /* compiled from: Unknown */
    public static abstract class zzd<A extends com.google.android.gms.common.api.Api.zzb> extends zzb<PlaceLikelihoodBuffer, A> {
        public zzd(com.google.android.gms.common.api.Api.zzc<A> zzc, GoogleApiClient googleApiClient) {
            super(zzc, googleApiClient);
        }

        protected PlaceLikelihoodBuffer zzaX(Status status) {
            return new PlaceLikelihoodBuffer(DataHolder.zzbI(status.getStatusCode()), 100, null);
        }

        protected /* synthetic */ Result zzc(Status status) {
            return zzaX(status);
        }
    }

    /* compiled from: Unknown */
    public static abstract class zzf<A extends com.google.android.gms.common.api.Api.zzb> extends zzb<Status, A> {
        public zzf(com.google.android.gms.common.api.Api.zzc<A> zzc, GoogleApiClient googleApiClient) {
            super(zzc, googleApiClient);
        }

        protected Status zzb(Status status) {
            return status;
        }

        protected /* synthetic */ Result zzc(Status status) {
            return zzb(status);
        }
    }

    /* compiled from: Unknown */
    public static abstract class zze<A extends com.google.android.gms.common.api.Api.zzb> extends zzb<com.google.android.gms.location.places.personalized.zzd, A> {
        protected com.google.android.gms.location.places.personalized.zzd zzaY(Status status) {
            return com.google.android.gms.location.places.personalized.zzd.zzaZ(status);
        }

        protected /* synthetic */ Result zzc(Status status) {
            return zzaY(status);
        }
    }

    public zzl(zza zza) {
        this.zzaPP = null;
        this.zzaPQ = zza;
        this.zzaPR = null;
        this.zzaPS = null;
        this.zzaPT = null;
        this.mContext = null;
    }

    public zzl(zzc zzc, Context context) {
        this.zzaPP = null;
        this.zzaPQ = null;
        this.zzaPR = null;
        this.zzaPS = null;
        this.zzaPT = zzc;
        this.mContext = context.getApplicationContext();
    }

    public zzl(zzd zzd, Context context) {
        this.zzaPP = zzd;
        this.zzaPQ = null;
        this.zzaPR = null;
        this.zzaPS = null;
        this.zzaPT = null;
        this.mContext = context.getApplicationContext();
    }

    public zzl(zzf zzf) {
        this.zzaPP = null;
        this.zzaPQ = null;
        this.zzaPR = null;
        this.zzaPS = zzf;
        this.zzaPT = null;
        this.mContext = null;
    }

    public void zzaU(Status status) throws RemoteException {
        this.zzaPS.zza((Result) status);
    }

    public void zzac(DataHolder dataHolder) throws RemoteException {
        boolean z = false;
        if (this.zzaPP != null) {
            z = true;
        }
        zzx.zza(z, (Object) "placeEstimator cannot be null");
        if (dataHolder != null) {
            Bundle zzpZ = dataHolder.zzpZ();
            this.zzaPP.zza(new PlaceLikelihoodBuffer(dataHolder, zzpZ != null ? PlaceLikelihoodBuffer.zzH(zzpZ) : 100, this.mContext));
            return;
        }
        if (Log.isLoggable(TAG, 6)) {
            Log.e(TAG, "onPlaceEstimated received null DataHolder: " + zzng.zzso());
        }
        this.zzaPP.zzw(Status.zzagE);
    }

    public void zzad(DataHolder dataHolder) throws RemoteException {
        if (dataHolder != null) {
            this.zzaPQ.zza(new AutocompletePredictionBuffer(dataHolder));
            return;
        }
        if (Log.isLoggable(TAG, 6)) {
            Log.e(TAG, "onAutocompletePrediction received null DataHolder: " + zzng.zzso());
        }
        this.zzaPQ.zzw(Status.zzagE);
    }

    public void zzae(DataHolder dataHolder) throws RemoteException {
        if (dataHolder != null) {
            this.zzaPR.zza(new com.google.android.gms.location.places.personalized.zzd(dataHolder));
            return;
        }
        if (Log.isLoggable(TAG, 6)) {
            Log.e(TAG, "onPlaceUserDataFetched received null DataHolder: " + zzng.zzso());
        }
        this.zzaPR.zzw(Status.zzagE);
    }

    public void zzaf(DataHolder dataHolder) throws RemoteException {
        this.zzaPT.zza(new PlaceBuffer(dataHolder, this.mContext));
    }
}
