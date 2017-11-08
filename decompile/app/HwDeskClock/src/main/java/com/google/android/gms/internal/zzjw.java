package com.google.android.gms.internal;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.text.TextUtils;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.Auth.zza;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.internal.zzf;
import com.google.android.gms.common.internal.zzj;

/* compiled from: Unknown */
public final class zzjw extends zzj<zzjy> {
    private final Bundle zzQZ;

    public zzjw(Context context, Looper looper, zzf zzf, zza zza, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
        super(context, looper, 16, zzf, connectionCallbacks, onConnectionFailedListener);
        this.zzQZ = zza != null ? zza.zzlq() : new Bundle();
    }

    protected /* synthetic */ IInterface zzV(IBinder iBinder) {
        return zzau(iBinder);
    }

    protected zzjy zzau(IBinder iBinder) {
        return zzjy.zza.zzaw(iBinder);
    }

    protected String zzfA() {
        return "com.google.android.gms.auth.service.START";
    }

    protected String zzfB() {
        return "com.google.android.gms.auth.api.internal.IAuthService";
    }

    protected Bundle zzli() {
        return this.zzQZ;
    }

    public boolean zzlm() {
        zzf zzoA = zzoA();
        return (TextUtils.isEmpty(zzoA.getAccountName()) || zzoA.zzb(Auth.PROXY_API).isEmpty()) ? false : true;
    }
}
