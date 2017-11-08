package com.google.android.gms.internal;

import android.app.PendingIntent;
import android.util.Log;
import com.google.android.gms.internal.zzpm.zza;

@Deprecated
/* compiled from: Unknown */
public class zzpl implements zza {
    private final zzpm zzaKE;

    public void zzh(PendingIntent pendingIntent) {
        Log.w("OneTimePlayLogger", "logger connection failed: " + pendingIntent);
    }

    public void zzyC() {
        this.zzaKE.stop();
    }

    public void zzyD() {
        Log.w("OneTimePlayLogger", "logger connection failed");
    }
}
