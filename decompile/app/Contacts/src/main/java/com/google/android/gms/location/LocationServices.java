package com.google.android.gms.location;

import android.content.Context;
import android.os.Looper;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.Api.ApiOptions.NoOptions;
import com.google.android.gms.common.api.Api.zzb;
import com.google.android.gms.common.api.Api.zzc;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.location.internal.zzd;
import com.google.android.gms.location.internal.zzf;
import com.google.android.gms.location.internal.zzl;
import com.google.android.gms.location.internal.zzq;

/* compiled from: Unknown */
public class LocationServices {
    public static final Api<NoOptions> API = new Api("LocationServices.API", zzUJ, zzUI);
    public static final FusedLocationProviderApi FusedLocationApi = new zzd();
    public static final GeofencingApi GeofencingApi = new zzf();
    public static final SettingsApi SettingsApi = new zzq();
    private static final zzc<zzl> zzUI = new zzc();
    private static final com.google.android.gms.common.api.Api.zza<zzl, NoOptions> zzUJ = new com.google.android.gms.common.api.Api.zza<zzl, NoOptions>() {
        public /* synthetic */ zzb zza(Context context, Looper looper, com.google.android.gms.common.internal.zzf zzf, Object obj, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
            return zzn(context, looper, zzf, (NoOptions) obj, connectionCallbacks, onConnectionFailedListener);
        }

        public zzl zzn(Context context, Looper looper, com.google.android.gms.common.internal.zzf zzf, NoOptions noOptions, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
            return new zzl(context, looper, connectionCallbacks, onConnectionFailedListener, "locationServices", zzf);
        }
    };

    /* compiled from: Unknown */
    public static abstract class zza<R extends Result> extends com.google.android.gms.common.api.internal.zza.zza<R, zzl> {
        public zza(GoogleApiClient googleApiClient) {
            super(LocationServices.zzUI, googleApiClient);
        }
    }

    private LocationServices() {
    }

    public static zzl zzi(GoogleApiClient googleApiClient) {
        boolean z = false;
        zzx.zzb(googleApiClient != null, (Object) "GoogleApiClient parameter is required.");
        zzl zzl = (zzl) googleApiClient.zza(zzUI);
        if (zzl != null) {
            z = true;
        }
        zzx.zza(z, (Object) "GoogleApiClient is not configured to use the LocationServices.API Api. Pass thisinto GoogleApiClient.Builder#addApi() to use this feature.");
        return zzl;
    }
}
