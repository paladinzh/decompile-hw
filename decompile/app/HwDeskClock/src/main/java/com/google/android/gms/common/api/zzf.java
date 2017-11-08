package com.google.android.gms.common.api;

import android.os.Bundle;
import android.os.DeadObjectException;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api.zzb;
import com.google.android.gms.common.api.zzc.zza;

/* compiled from: Unknown */
public class zzf implements zzj {
    private final zzi zzZq;

    public zzf(zzi zzi) {
        this.zzZq = zzi;
    }

    private <A extends zzb> void zza(zze<A> zze) throws DeadObjectException {
        this.zzZq.zzb((zze) zze);
        zzb zza = this.zzZq.zza(zze.zznd());
        if (!zza.isConnected() && this.zzZq.zzaah.containsKey(zze.zznd())) {
            zze.zzx(new Status(17));
        } else {
            zze.zzb(zza);
        }
    }

    public void begin() {
        while (!this.zzZq.zzaaa.isEmpty()) {
            try {
                zza((zze) this.zzZq.zzaaa.remove());
            } catch (Throwable e) {
                Log.w("GoogleApiClientConnected", "Service died while flushing queue", e);
            }
        }
    }

    public void connect() {
    }

    public void disconnect() {
        this.zzZq.zzaah.clear();
        this.zzZq.zzny();
        this.zzZq.zzg(null);
        this.zzZq.zzZZ.zzoK();
    }

    public String getName() {
        return "CONNECTED";
    }

    public void onConnected(Bundle connectionHint) {
    }

    public void onConnectionSuspended(int cause) {
        if (cause == 1) {
            this.zzZq.zznE();
        }
        for (zze zzw : this.zzZq.zzaam) {
            zzw.zzw(new Status(8, "The connection to Google Play services was lost"));
        }
        this.zzZq.zzg(null);
        this.zzZq.zzZZ.zzbB(cause);
        this.zzZq.zzZZ.zzoK();
        if (cause == 2) {
            this.zzZq.connect();
        }
    }

    public <A extends zzb, R extends Result, T extends zza<R, A>> T zza(T t) {
        return zzb(t);
    }

    public void zza(ConnectionResult connectionResult, Api<?> api, int i) {
    }

    public <A extends zzb, T extends zza<? extends Result, A>> T zzb(T t) {
        try {
            zza((zze) t);
        } catch (DeadObjectException e) {
            this.zzZq.zza(new zzb(this, this) {
                final /* synthetic */ zzf zzZr;

                public void zzno() {
                    this.zzZr.onConnectionSuspended(1);
                }
            });
        }
        return t;
    }
}
