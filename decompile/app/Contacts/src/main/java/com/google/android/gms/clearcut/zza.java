package com.google.android.gms.clearcut;

import android.content.Context;

/* compiled from: Unknown */
public class zza {
    private static int zzaeO = -1;
    public static final zza zzaeP = new zza();

    protected zza() {
    }

    public int zzah(Context context) {
        if (zzaeO < 0) {
            zzaeO = context.getSharedPreferences("bootCount", 0).getInt("bootCount", 1);
        }
        return zzaeO;
    }
}
