package com.google.android.gms.common.api;

import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api.zzb;
import com.google.android.gms.common.api.zzc.zza;
import java.util.Collections;

/* compiled from: Unknown */
public class zzh implements zzj {
    private final zzi zzZq;

    public zzh(zzi zzi) {
        this.zzZq = zzi;
    }

    public void begin() {
        this.zzZq.zznz();
        this.zzZq.zzaai = Collections.emptySet();
    }

    public void connect() {
        this.zzZq.zznA();
    }

    public void disconnect() {
        for (zze cancel : this.zzZq.zzaaa) {
            cancel.cancel();
        }
        this.zzZq.zzaaa.clear();
        this.zzZq.zzaah.clear();
        this.zzZq.zzny();
    }

    public String getName() {
        return "DISCONNECTED";
    }

    public void onConnected(Bundle connectionHint) {
    }

    public void onConnectionSuspended(int cause) {
    }

    public <A extends zzb, R extends Result, T extends zza<R, A>> T zza(T t) {
        this.zzZq.zzaaa.add(t);
        return t;
    }

    public void zza(ConnectionResult connectionResult, Api<?> api, int i) {
    }
}
