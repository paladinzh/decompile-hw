package com.google.android.gms.auth.api.credentials.internal;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import com.google.android.gms.auth.api.Auth.AuthCredentialsOptions;
import com.google.android.gms.auth.api.credentials.internal.zzh.zza;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.internal.zzf;
import com.google.android.gms.common.internal.zzj;

/* compiled from: Unknown */
public final class zze extends zzj<zzh> {
    private final AuthCredentialsOptions zzRD;

    public zze(Context context, Looper looper, zzf zzf, AuthCredentialsOptions authCredentialsOptions, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
        super(context, looper, 68, zzf, connectionCallbacks, onConnectionFailedListener);
        this.zzRD = authCredentialsOptions;
    }

    protected /* synthetic */ IInterface zzV(IBinder iBinder) {
        return zzar(iBinder);
    }

    protected zzh zzar(IBinder iBinder) {
        return zza.zzat(iBinder);
    }

    protected String zzfA() {
        return "com.google.android.gms.auth.api.credentials.service.START";
    }

    protected String zzfB() {
        return "com.google.android.gms.auth.api.credentials.internal.ICredentialsService";
    }

    protected Bundle zzli() {
        return this.zzRD != null ? this.zzRD.zzli() : new Bundle();
    }
}
