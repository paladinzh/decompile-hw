package com.google.android.gms.internal;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Process;
import com.google.android.gms.common.internal.zzd;

/* compiled from: Unknown */
public class zzll {
    public static boolean zzi(Context context, String str) {
        boolean z = false;
        try {
            if ((context.getPackageManager().getApplicationInfo(str, 0).flags & 2097152) != 0) {
                z = true;
            }
            return z;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static boolean zzjk() {
        return zzd.zzacG && zzkq.isInitialized() && zzkq.zznN() == Process.myUid();
    }
}
