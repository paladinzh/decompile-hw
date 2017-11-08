package com.google.android.gms.common.internal;

import android.content.Context;
import android.util.Log;
import com.google.android.gms.internal.zzqt;

/* compiled from: Unknown */
public final class zzo {
    public static final int zzaml = (23 - " PII_LOG".length());
    private static final String zzamm = null;
    private final String zzamn;
    private final String zzamo;

    public zzo(String str) {
        this(str, zzamm);
    }

    public zzo(String str, String str2) {
        zzx.zzb((Object) str, (Object) "log tag cannot be null");
        zzx.zzb(str.length() <= 23, "tag \"%s\" is longer than the %d character maximum", str, Integer.valueOf(23));
        this.zzamn = str;
        if (str2 != null && str2.length() > 0) {
            this.zzamo = str2;
        } else {
            this.zzamo = zzamm;
        }
    }

    private String zzcK(String str) {
        return this.zzamo != null ? this.zzamo.concat(str) : str;
    }

    public void zzA(String str, String str2) {
        if (zzbU(6)) {
            Log.e(str, zzcK(str2));
        }
    }

    public void zza(Context context, String str, String str2, Throwable th) {
        StackTraceElement[] stackTrace = th.getStackTrace();
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        while (i < stackTrace.length && i < 2) {
            stringBuilder.append(stackTrace[i].toString());
            stringBuilder.append("\n");
            i++;
        }
        zzqt zzqt = new zzqt(context, 10);
        zzqt.zza("GMS_WTF", null, "GMS_WTF", stringBuilder.toString());
        zzqt.send();
        if (zzbU(7)) {
            Log.e(str, zzcK(str2), th);
            Log.wtf(str, zzcK(str2), th);
        }
    }

    public void zza(String str, String str2, Throwable th) {
        if (zzbU(4)) {
            Log.i(str, zzcK(str2), th);
        }
    }

    public void zzb(String str, String str2, Throwable th) {
        if (zzbU(5)) {
            Log.w(str, zzcK(str2), th);
        }
    }

    public boolean zzbU(int i) {
        return Log.isLoggable(this.zzamn, i);
    }

    public void zzc(String str, String str2, Throwable th) {
        if (zzbU(6)) {
            Log.e(str, zzcK(str2), th);
        }
    }

    public void zzy(String str, String str2) {
        if (zzbU(3)) {
            Log.d(str, zzcK(str2));
        }
    }

    public void zzz(String str, String str2) {
        if (zzbU(5)) {
            Log.w(str, zzcK(str2));
        }
    }
}
