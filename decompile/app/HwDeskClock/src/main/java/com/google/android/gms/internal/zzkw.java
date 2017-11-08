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
public final class zzkw {
    public static final Api<NoOptions> API = new Api("Common.API", zzQg, zzQf);
    public static final zzc<zzla> zzQf = new zzc();
    private static final zza<zzla, NoOptions> zzQg = new zza<zzla, NoOptions>() {
        public /* synthetic */ zzb zza(Context context, Looper looper, zzf zzf, Object obj, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
            return zze(context, looper, zzf, (NoOptions) obj, connectionCallbacks, onConnectionFailedListener);
        }

        public zzla zze(Context context, Looper looper, zzf zzf, NoOptions noOptions, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
            return new zzla(context, looper, zzf, connectionCallbacks, onConnectionFailedListener);
        }
    };
    public static final zzkx zzaev = new zzky();
}
