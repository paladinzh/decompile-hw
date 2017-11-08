package com.google.android.gms.auth.api;

import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import com.google.android.gms.auth.api.credentials.CredentialsApi;
import com.google.android.gms.auth.api.credentials.PasswordSpecification;
import com.google.android.gms.auth.api.credentials.internal.zzc;
import com.google.android.gms.auth.api.credentials.internal.zze;
import com.google.android.gms.auth.api.proxy.ProxyApi;
import com.google.android.gms.auth.api.signin.internal.zzd;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.Api.ApiOptions.NoOptions;
import com.google.android.gms.common.api.Api.ApiOptions.Optional;
import com.google.android.gms.common.api.Api.zzb;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.internal.zzf;
import com.google.android.gms.internal.zzjq;
import com.google.android.gms.internal.zzjr;
import com.google.android.gms.internal.zzjs;
import com.google.android.gms.internal.zzjw;
import com.google.android.gms.internal.zzka;

/* compiled from: Unknown */
public final class Auth {
    public static final Api<AuthCredentialsOptions> CREDENTIALS_API = new Api("Auth.CREDENTIALS_API", zzQQ, zzQM);
    public static final CredentialsApi CredentialsApi = new zzc();
    public static final Api<zza> PROXY_API = new Api("Auth.PROXY_API", zzQP, zzQL);
    public static final ProxyApi ProxyApi = new zzka();
    public static final Api.zzc<zzjw> zzQL = new Api.zzc();
    public static final Api.zzc<zze> zzQM = new Api.zzc();
    public static final Api.zzc<zzjs> zzQN = new Api.zzc();
    public static final Api.zzc<zzd> zzQO = new Api.zzc();
    private static final com.google.android.gms.common.api.Api.zza<zzjw, zza> zzQP = new com.google.android.gms.common.api.Api.zza<zzjw, zza>() {
        public zzjw zza(Context context, Looper looper, zzf zzf, zza zza, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
            return new zzjw(context, looper, zzf, zza, connectionCallbacks, onConnectionFailedListener);
        }
    };
    private static final com.google.android.gms.common.api.Api.zza<zze, AuthCredentialsOptions> zzQQ = new com.google.android.gms.common.api.Api.zza<zze, AuthCredentialsOptions>() {
        public zze zza(Context context, Looper looper, zzf zzf, AuthCredentialsOptions authCredentialsOptions, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
            return new zze(context, looper, zzf, authCredentialsOptions, connectionCallbacks, onConnectionFailedListener);
        }
    };
    private static final com.google.android.gms.common.api.Api.zza<zzjs, NoOptions> zzQR = new com.google.android.gms.common.api.Api.zza<zzjs, NoOptions>() {
        public /* synthetic */ zzb zza(Context context, Looper looper, zzf zzf, Object obj, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
            return zzd(context, looper, zzf, (NoOptions) obj, connectionCallbacks, onConnectionFailedListener);
        }

        public zzjs zzd(Context context, Looper looper, zzf zzf, NoOptions noOptions, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
            return new zzjs(context, looper, zzf, connectionCallbacks, onConnectionFailedListener);
        }
    };
    private static final com.google.android.gms.common.api.Api.zza<zzd, com.google.android.gms.auth.api.signin.zze> zzQS = new com.google.android.gms.common.api.Api.zza<zzd, com.google.android.gms.auth.api.signin.zze>() {
        public zzd zza(Context context, Looper looper, zzf zzf, com.google.android.gms.auth.api.signin.zze zze, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
            return new zzd(context, looper, zzf, zze, connectionCallbacks, onConnectionFailedListener);
        }
    };
    public static final Api<com.google.android.gms.auth.api.signin.zze> zzQT = new Api("Auth.SIGN_IN_API", zzQS, zzQO);
    public static final Api<NoOptions> zzQU = new Api("Auth.ACCOUNT_STATUS_API", zzQR, zzQN);
    public static final zzjq zzQV = new zzjr();
    public static final com.google.android.gms.auth.api.signin.zzd zzQW = new com.google.android.gms.auth.api.signin.internal.zzc();

    /* compiled from: Unknown */
    public static final class AuthCredentialsOptions implements Optional {
        private final String zzQX;
        private final PasswordSpecification zzQY;

        /* compiled from: Unknown */
        public static class Builder {
            private PasswordSpecification zzQY = PasswordSpecification.zzRo;
        }

        public Bundle zzli() {
            Bundle bundle = new Bundle();
            bundle.putString("consumer_package", this.zzQX);
            bundle.putParcelable("password_specification", this.zzQY);
            return bundle;
        }
    }

    /* compiled from: Unknown */
    public static final class zza implements Optional {
        private final Bundle zzQZ;

        public Bundle zzlq() {
            return new Bundle(this.zzQZ);
        }
    }

    private Auth() {
    }
}
