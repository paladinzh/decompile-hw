package com.google.android.gms.wearable;

import android.content.Context;
import android.os.Looper;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.Api.ApiOptions.Optional;
import com.google.android.gms.common.api.Api.zza;
import com.google.android.gms.common.api.Api.zzc;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.internal.zzf;
import com.google.android.gms.wearable.internal.zzay;
import com.google.android.gms.wearable.internal.zzba;
import com.google.android.gms.wearable.internal.zzbl;
import com.google.android.gms.wearable.internal.zzbn;
import com.google.android.gms.wearable.internal.zzbp;
import com.google.android.gms.wearable.internal.zzd;
import com.google.android.gms.wearable.internal.zzi;
import com.google.android.gms.wearable.internal.zzk;
import com.google.android.gms.wearable.internal.zzv;
import com.google.android.gms.wearable.internal.zzw;

/* compiled from: Unknown */
public class Wearable {
    public static final Api<WearableOptions> API = new Api("Wearable.API", zzQg, zzQf);
    public static final CapabilityApi CapabilityApi = new zzi();
    public static final ChannelApi ChannelApi = new zzk();
    public static final DataApi DataApi = new zzw();
    public static final MessageApi MessageApi = new zzay();
    public static final NodeApi NodeApi = new zzba();
    public static final zzc<zzbn> zzQf = new zzc();
    private static final zza<zzbn, WearableOptions> zzQg = new zza<zzbn, WearableOptions>() {
        public zzbn zza(Context context, Looper looper, zzf zzf, WearableOptions wearableOptions, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
            if (wearableOptions == null) {
                WearableOptions wearableOptions2 = new WearableOptions(new Builder());
            }
            return new zzbn(context, looper, connectionCallbacks, onConnectionFailedListener, zzf);
        }
    };
    public static final zzc zzaYZ = new com.google.android.gms.wearable.internal.zzf();
    public static final zza zzaZa = new zzd();
    public static final zzf zzaZb = new zzv();
    public static final zzi zzaZc = new zzbl();
    public static final zzk zzaZd = new zzbp();

    /* compiled from: Unknown */
    public static final class WearableOptions implements Optional {

        /* compiled from: Unknown */
        public static class Builder {
        }

        private WearableOptions(Builder builder) {
        }
    }

    private Wearable() {
    }
}
