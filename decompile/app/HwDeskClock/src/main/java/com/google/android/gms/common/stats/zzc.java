package com.google.android.gms.common.stats;

import com.google.android.gms.internal.zzkq;

/* compiled from: Unknown */
public final class zzc {
    public static zzkq<Integer> zzafE = zzkq.zza("gms:common:stats:max_num_of_events", Integer.valueOf(100));

    /* compiled from: Unknown */
    public static final class zza {
        public static zzkq<Integer> zzafF = zzkq.zza("gms:common:stats:connections:level", Integer.valueOf(zzd.LOG_LEVEL_OFF));
        public static zzkq<String> zzafG = zzkq.zzu("gms:common:stats:connections:ignored_calling_processes", "");
        public static zzkq<String> zzafH = zzkq.zzu("gms:common:stats:connections:ignored_calling_services", "");
        public static zzkq<String> zzafI = zzkq.zzu("gms:common:stats:connections:ignored_target_processes", "");
        public static zzkq<String> zzafJ = zzkq.zzu("gms:common:stats:connections:ignored_target_services", "com.google.android.gms.auth.GetToken");
        public static zzkq<Long> zzafK = zzkq.zza("gms:common:stats:connections:time_out_duration", Long.valueOf(600000));
    }

    /* compiled from: Unknown */
    public static final class zzb {
        public static zzkq<Integer> zzafF = zzkq.zza("gms:common:stats:wakeLocks:level", Integer.valueOf(zzd.LOG_LEVEL_OFF));
        public static zzkq<Long> zzafK = zzkq.zza("gms:common:stats:wakelocks:time_out_duration", Long.valueOf(600000));
    }
}
