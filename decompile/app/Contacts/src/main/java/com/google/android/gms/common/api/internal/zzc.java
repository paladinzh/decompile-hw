package com.google.android.gms.common.api.internal;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.internal.zzx;

/* compiled from: Unknown */
public class zzc implements ConnectionCallbacks, OnConnectionFailedListener {
    public final Api<?> zzagT;
    private final int zzagU;
    private zzl zzagV;

    public zzc(Api<?> api, int i) {
        this.zzagT = api;
        this.zzagU = i;
    }

    private void zzpi() {
        zzx.zzb(this.zzagV, (Object) "Callbacks must be attached to a GoogleApiClient instance before connecting the client.");
    }

    public void onConnected(@Nullable Bundle connectionHint) {
        zzpi();
        this.zzagV.onConnected(connectionHint);
    }

    public void onConnectionFailed(@NonNull ConnectionResult result) {
        zzpi();
        this.zzagV.zza(result, this.zzagT, this.zzagU);
    }

    public void onConnectionSuspended(int cause) {
        zzpi();
        this.zzagV.onConnectionSuspended(cause);
    }

    public void zza(zzl zzl) {
        this.zzagV = zzl;
    }
}
