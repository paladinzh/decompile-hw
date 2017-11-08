package com.google.android.gms.flags.impl;

import android.content.SharedPreferences;
import com.google.android.gms.internal.zzpl;
import java.util.concurrent.Callable;

/* compiled from: Unknown */
public abstract class zza<T> {

    /* compiled from: Unknown */
    public static class zza extends zza<Boolean> {
        public static Boolean zza(final SharedPreferences sharedPreferences, final String str, final Boolean bool) {
            return (Boolean) zzpl.zzb(new Callable<Boolean>() {
                public /* synthetic */ Object call() throws Exception {
                    return zzvt();
                }

                public Boolean zzvt() {
                    return Boolean.valueOf(sharedPreferences.getBoolean(str, bool.booleanValue()));
                }
            });
        }
    }

    /* compiled from: Unknown */
    public static class zzb extends zza<Integer> {
        public static Integer zza(final SharedPreferences sharedPreferences, final String str, final Integer num) {
            return (Integer) zzpl.zzb(new Callable<Integer>() {
                public /* synthetic */ Object call() throws Exception {
                    return zzvu();
                }

                public Integer zzvu() {
                    return Integer.valueOf(sharedPreferences.getInt(str, num.intValue()));
                }
            });
        }
    }

    /* compiled from: Unknown */
    public static class zzc extends zza<Long> {
        public static Long zza(final SharedPreferences sharedPreferences, final String str, final Long l) {
            return (Long) zzpl.zzb(new Callable<Long>() {
                public /* synthetic */ Object call() throws Exception {
                    return zzvv();
                }

                public Long zzvv() {
                    return Long.valueOf(sharedPreferences.getLong(str, l.longValue()));
                }
            });
        }
    }

    /* compiled from: Unknown */
    public static class zzd extends zza<String> {
        public static String zza(final SharedPreferences sharedPreferences, final String str, final String str2) {
            return (String) zzpl.zzb(new Callable<String>() {
                public /* synthetic */ Object call() throws Exception {
                    return zzkp();
                }

                public String zzkp() {
                    return sharedPreferences.getString(str, str2);
                }
            });
        }
    }
}
