package com.google.android.gms.playlog.internal;

import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.internal.zzpm.zza;

/* compiled from: Unknown */
public class zzd implements ConnectionCallbacks, OnConnectionFailedListener {
    private zzf zzaKG;
    private final zza zzaKR;
    private boolean zzaKS;

    public void onConnected(Bundle connectionHint) {
        this.zzaKG.zzao(false);
        if (this.zzaKS && this.zzaKR != null) {
            this.zzaKR.zzyC();
        }
        this.zzaKS = false;
    }

    public void onConnectionFailed(ConnectionResult result) {
        this.zzaKG.zzao(true);
        if (this.zzaKS && this.zzaKR != null) {
            if (result.hasResolution()) {
                this.zzaKR.zzh(result.getResolution());
            } else {
                this.zzaKR.zzyD();
            }
        }
        this.zzaKS = false;
    }

    public void onConnectionSuspended(int cause) {
        this.zzaKG.zzao(true);
    }

    public void zzan(boolean z) {
        this.zzaKS = z;
    }
}
