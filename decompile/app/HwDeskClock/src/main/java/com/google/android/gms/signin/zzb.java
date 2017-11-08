package com.google.android.gms.signin;

import android.content.Context;
import android.os.Looper;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.Api.ApiOptions.NoOptions;
import com.google.android.gms.common.api.Api.zza;
import com.google.android.gms.common.api.Api.zzc;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.internal.zzf;
import com.google.android.gms.signin.internal.zzh;
import com.google.android.gms.signin.internal.zzi;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

/* compiled from: Unknown */
public final class zzb {
    public static final Api<zze> API = new Api("SignIn.API", zzQg, zzQf);
    public static final zzc<zzi> zzQf = new zzc();
    public static final zza<zzi, zze> zzQg = new zza<zzi, zze>() {
        public zzi zza(Context context, Looper looper, zzf zzf, zze zze, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
            return new zzi(context, looper, true, zzf, zze, connectionCallbacks, onConnectionFailedListener, Executors.newSingleThreadExecutor());
        }

        public List<Scope> zza(zze zze) {
            return Arrays.asList(new Scope[]{zzb.zzaOc, zzb.zzaOd});
        }

        public /* synthetic */ List zzl(Object obj) {
            return zza((zze) obj);
        }
    };
    static final zza<zzi, NoOptions> zzaOb = new zza<zzi, NoOptions>() {
        public /* synthetic */ com.google.android.gms.common.api.Api.zzb zza(Context context, Looper looper, zzf zzf, Object obj, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
            return zzt(context, looper, zzf, (NoOptions) obj, connectionCallbacks, onConnectionFailedListener);
        }

        public zzi zzt(Context context, Looper looper, zzf zzf, NoOptions noOptions, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
            return new zzi(context, looper, false, zzf, zze.zzaOf, connectionCallbacks, onConnectionFailedListener, Executors.newSingleThreadExecutor());
        }
    };
    public static final Scope zzaOc = new Scope("profile");
    public static final Scope zzaOd = new Scope("email");
    public static final zzc zzaOe = new zzh();
    public static final Api<NoOptions> zzagB = new Api("SignIn.INTERNAL_API", zzaOb, zzanh);
    public static final zzc<zzi> zzanh = new zzc();
}
