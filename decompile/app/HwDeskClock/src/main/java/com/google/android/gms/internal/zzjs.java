package com.google.android.gms.internal;

import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.internal.zzf;
import com.google.android.gms.common.internal.zzj;
import com.google.android.gms.internal.zzju.zza;

/* compiled from: Unknown */
public class zzjs extends zzj<zzju> {
    public zzjs(Context context, Looper looper, zzf zzf, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
        super(context, looper, 74, zzf, connectionCallbacks, onConnectionFailedListener);
    }

    protected /* synthetic */ IInterface zzV(IBinder iBinder) {
        return zzao(iBinder);
    }

    protected zzju zzao(IBinder iBinder) {
        return zza.zzaq(iBinder);
    }

    protected String zzfA() {
        return "com.google.android.gms.auth.api.accountstatus.START";
    }

    protected String zzfB() {
        return "com.google.android.gms.auth.api.accountstatus.internal.IAccountStatusService";
    }
}
