package com.google.android.gms.internal;

import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.Api.ApiOptions.HasOptions;
import com.google.android.gms.common.api.Api.zzc;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.internal.zzf;
import com.google.android.gms.signin.internal.zzg;
import com.google.android.gms.signin.internal.zzh;

/* compiled from: Unknown */
public final class zzrl {
    public static final Api<zzro> API = new Api("SignIn.API", zzUJ, zzUI);
    public static final zzc<zzh> zzUI = new zzc();
    public static final com.google.android.gms.common.api.Api.zza<zzh, zzro> zzUJ = new com.google.android.gms.common.api.Api.zza<zzh, zzro>() {
        public zzh zza(Context context, Looper looper, zzf zzf, zzro zzro, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
            return new zzh(context, looper, true, zzf, zzro != null ? zzro : zzro.zzbgV, connectionCallbacks, onConnectionFailedListener);
        }
    };
    public static final Scope zzWW = new Scope(Scopes.PROFILE);
    public static final Scope zzWX = new Scope(Scopes.EMAIL);
    public static final Api<zza> zzaoG = new Api("SignIn.INTERNAL_API", zzbgS, zzavN);
    public static final zzc<zzh> zzavN = new zzc();
    static final com.google.android.gms.common.api.Api.zza<zzh, zza> zzbgS = new com.google.android.gms.common.api.Api.zza<zzh, zza>() {
        public zzh zza(Context context, Looper looper, zzf zzf, zza zza, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
            return new zzh(context, looper, false, zzf, zza.zzFF(), connectionCallbacks, onConnectionFailedListener);
        }
    };
    public static final zzrm zzbgT = new zzg();

    /* compiled from: Unknown */
    public static class zza implements HasOptions {
        private final Bundle zzbgU;

        public Bundle zzFF() {
            return this.zzbgU;
        }
    }
}
