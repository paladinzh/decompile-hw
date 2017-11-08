package com.google.android.gms.common.internal;

import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import com.google.android.gms.common.api.Api.zzd;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

/* compiled from: Unknown */
public class zzac<T extends IInterface> extends zzj<T> {
    private final zzd<T> zzaer;

    public zzac(Context context, Looper looper, int i, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener, zzf zzf, zzd zzd) {
        super(context, looper, i, zzf, connectionCallbacks, onConnectionFailedListener);
        this.zzaer = zzd;
    }

    protected T zzV(IBinder iBinder) {
        return this.zzaer.zzV(iBinder);
    }

    protected void zzc(int i, T t) {
        this.zzaer.zza(i, t);
    }

    protected String zzfA() {
        return this.zzaer.zzfA();
    }

    protected String zzfB() {
        return this.zzaer.zzfB();
    }
}
