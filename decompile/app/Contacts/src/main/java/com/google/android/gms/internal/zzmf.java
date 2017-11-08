package com.google.android.gms.internal;

import android.content.Context;
import android.os.Looper;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.Api.ApiOptions.NoOptions;
import com.google.android.gms.common.api.Api.zza;
import com.google.android.gms.common.api.Api.zzb;
import com.google.android.gms.common.api.Api.zzc;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.internal.zzf;

/* compiled from: Unknown */
public final class zzmf {
    public static final Api<NoOptions> API = new Api("Common.API", zzUJ, zzUI);
    public static final zzc<zzmj> zzUI = new zzc();
    private static final zza<zzmj, NoOptions> zzUJ = new zza<zzmj, NoOptions>() {
        public /* synthetic */ zzb zza(Context context, Looper looper, zzf zzf, Object obj, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
            return zzf(context, looper, zzf, (NoOptions) obj, connectionCallbacks, onConnectionFailedListener);
        }

        public zzmj zzf(Context context, Looper looper, zzf zzf, NoOptions noOptions, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
            return new zzmj(context, looper, zzf, connectionCallbacks, onConnectionFailedListener);
        }
    };
    public static final zzmg zzamA = new zzmh();
}
