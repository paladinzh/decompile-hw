package com.google.android.gms.flags.impl;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.android.gms.internal.zzpl;
import java.util.concurrent.Callable;

/* compiled from: Unknown */
public class zzb {
    private static SharedPreferences zzaBZ = null;

    public static SharedPreferences zzw(final Context context) {
        SharedPreferences sharedPreferences;
        synchronized (SharedPreferences.class) {
            if (zzaBZ == null) {
                zzaBZ = (SharedPreferences) zzpl.zzb(new Callable<SharedPreferences>() {
                    public /* synthetic */ Object call() throws Exception {
                        return zzvw();
                    }

                    public SharedPreferences zzvw() {
                        return context.getSharedPreferences("google_sdk_flags", 1);
                    }
                });
            }
            sharedPreferences = zzaBZ;
        }
        return sharedPreferences;
    }
}
