package com.google.android.gms.location.internal;

import android.content.Context;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.internal.zzf;
import com.google.android.gms.common.internal.zzj;
import com.google.android.gms.location.internal.zzi.zza;

/* compiled from: Unknown */
public class zzb extends zzj<zzi> {
    private final String zzaOs;
    protected final zzp<zzi> zzaOt = new zzp<zzi>(this) {
        final /* synthetic */ zzb zzaOu;

        {
            this.zzaOu = r1;
        }

        public void zzqI() {
            this.zzaOu.zzqI();
        }

        public /* synthetic */ IInterface zzqJ() throws DeadObjectException {
            return zzyM();
        }

        public zzi zzyM() throws DeadObjectException {
            return (zzi) this.zzaOu.zzqJ();
        }
    };

    public zzb(Context context, Looper looper, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener, String str, zzf zzf) {
        super(context, looper, 23, zzf, connectionCallbacks, onConnectionFailedListener);
        this.zzaOs = str;
    }

    protected /* synthetic */ IInterface zzW(IBinder iBinder) {
        return zzcg(iBinder);
    }

    protected zzi zzcg(IBinder iBinder) {
        return zza.zzcj(iBinder);
    }

    protected String zzgu() {
        return "com.google.android.location.internal.GoogleLocationManagerService.START";
    }

    protected String zzgv() {
        return "com.google.android.gms.location.internal.IGoogleLocationManagerService";
    }

    protected Bundle zzml() {
        Bundle bundle = new Bundle();
        bundle.putString("client_name", this.zzaOs);
        return bundle;
    }
}
