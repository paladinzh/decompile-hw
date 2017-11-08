package com.google.android.gms.signin;

import com.google.android.gms.common.api.Api.ApiOptions.Optional;
import com.google.android.gms.common.api.GoogleApiClient.ServerAuthCodeCallbacks;

/* compiled from: Unknown */
public final class zze implements Optional {
    public static final zze zzaOf = new zza().zzzt();
    private final String zzRU;
    private final boolean zzaOg;
    private final boolean zzaOh;
    private final ServerAuthCodeCallbacks zzaOi;

    /* compiled from: Unknown */
    public static final class zza {
        private String zzaLg;
        private boolean zzaOj;
        private boolean zzaOk;
        private ServerAuthCodeCallbacks zzaOl;

        public zze zzzt() {
            return new zze(this.zzaOj, this.zzaOk, this.zzaLg, this.zzaOl);
        }
    }

    private zze(boolean z, boolean z2, String str, ServerAuthCodeCallbacks serverAuthCodeCallbacks) {
        this.zzaOg = z;
        this.zzaOh = z2;
        this.zzRU = str;
        this.zzaOi = serverAuthCodeCallbacks;
    }

    public String zzlG() {
        return this.zzRU;
    }

    public boolean zzzq() {
        return this.zzaOg;
    }

    public boolean zzzr() {
        return this.zzaOh;
    }

    public ServerAuthCodeCallbacks zzzs() {
        return this.zzaOi;
    }
}
