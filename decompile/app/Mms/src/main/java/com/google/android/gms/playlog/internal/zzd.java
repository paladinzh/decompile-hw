package com.google.android.gms.playlog.internal;

import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.internal.zzqu.zza;

/* compiled from: Unknown */
public class zzd implements ConnectionCallbacks, OnConnectionFailedListener {
    private final zza zzbdJ;
    private boolean zzbdK = true;
    private zzf zzbdy = null;

    public zzd(zza zza) {
        this.zzbdJ = zza;
    }

    public void onConnected(Bundle connectionHint) {
        this.zzbdy.zzau(false);
        if (this.zzbdK && this.zzbdJ != null) {
            this.zzbdJ.zzES();
        }
        this.zzbdK = false;
    }

    public void onConnectionFailed(ConnectionResult result) {
        this.zzbdy.zzau(true);
        if (this.zzbdK && this.zzbdJ != null) {
            if (result.hasResolution()) {
                this.zzbdJ.zzc(result.getResolution());
            } else {
                this.zzbdJ.zzET();
            }
        }
        this.zzbdK = false;
    }

    public void onConnectionSuspended(int cause) {
        this.zzbdy.zzau(true);
    }

    public void zza(zzf zzf) {
        this.zzbdy = zzf;
    }

    public void zzat(boolean z) {
        this.zzbdK = z;
    }
}
