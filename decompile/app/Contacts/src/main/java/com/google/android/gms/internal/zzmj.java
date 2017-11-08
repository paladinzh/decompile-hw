package com.google.android.gms.internal;

import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.internal.zzf;
import com.google.android.gms.common.internal.zzj;
import com.google.android.gms.internal.zzml.zza;

/* compiled from: Unknown */
public class zzmj extends zzj<zzml> {
    public zzmj(Context context, Looper looper, zzf zzf, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
        super(context, looper, 39, zzf, connectionCallbacks, onConnectionFailedListener);
    }

    protected /* synthetic */ IInterface zzW(IBinder iBinder) {
        return zzaW(iBinder);
    }

    protected zzml zzaW(IBinder iBinder) {
        return zza.zzaY(iBinder);
    }

    public String zzgu() {
        return "com.google.android.gms.common.service.START";
    }

    protected String zzgv() {
        return "com.google.android.gms.common.internal.service.ICommonService";
    }
}
