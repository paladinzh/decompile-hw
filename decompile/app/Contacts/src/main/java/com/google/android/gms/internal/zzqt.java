package com.google.android.gms.internal;

import android.app.PendingIntent;
import android.content.Context;
import android.util.Log;
import com.google.android.gms.internal.zzqu.zza;

@Deprecated
/* compiled from: Unknown */
public class zzqt implements zza {
    private final zzqu zzbdw;
    private boolean zzbdx;

    public zzqt(Context context, int i) {
        this(context, i, null);
    }

    public zzqt(Context context, int i, String str) {
        this(context, i, str, null, true);
    }

    public zzqt(Context context, int i, String str, String str2, boolean z) {
        this.zzbdw = new zzqu(context, i, str, str2, this, z, context == context.getApplicationContext() ? "OneTimePlayLogger" : context.getClass().getName());
        this.zzbdx = true;
    }

    private void zzER() {
        if (!this.zzbdx) {
            throw new IllegalStateException("Cannot reuse one-time logger after sending.");
        }
    }

    public void send() {
        zzER();
        this.zzbdw.start();
        this.zzbdx = false;
    }

    public void zzES() {
        this.zzbdw.stop();
    }

    public void zzET() {
        Log.w("OneTimePlayLogger", "logger connection failed");
    }

    public void zza(String str, byte[] bArr, String... strArr) {
        zzER();
        this.zzbdw.zzb(str, bArr, strArr);
    }

    public void zzc(PendingIntent pendingIntent) {
        Log.w("OneTimePlayLogger", "logger connection failed: " + pendingIntent);
    }
}
