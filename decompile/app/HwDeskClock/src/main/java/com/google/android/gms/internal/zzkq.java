package com.google.android.gms.internal;

/* compiled from: Unknown */
public abstract class zzkq<T> {
    private static zza zzaaY = null;
    private static int zzaaZ = 0;
    private static String zzaba = "com.google.android.providers.gsf.permission.READ_GSERVICES";
    private static final Object zzpm = new Object();
    private T zzNR = null;
    protected final String zztP;
    protected final T zztQ;

    /* compiled from: Unknown */
    private interface zza {
        Long getLong(String str, Long l);

        String getString(String str, String str2);

        Integer zzb(String str, Integer num);
    }

    protected zzkq(String str, T t) {
        this.zztP = str;
        this.zztQ = t;
    }

    public static boolean isInitialized() {
        return zzaaY != null;
    }

    public static zzkq<Integer> zza(String str, Integer num) {
        return new zzkq<Integer>(str, num) {
            protected /* synthetic */ Object zzbX(String str) {
                return zzca(str);
            }

            protected Integer zzca(String str) {
                return zzkq.zzaaY.zzb(this.zztP, (Integer) this.zztQ);
            }
        };
    }

    public static zzkq<Long> zza(String str, Long l) {
        return new zzkq<Long>(str, l) {
            protected /* synthetic */ Object zzbX(String str) {
                return zzbZ(str);
            }

            protected Long zzbZ(String str) {
                return zzkq.zzaaY.getLong(this.zztP, (Long) this.zztQ);
            }
        };
    }

    public static int zznN() {
        return zzaaZ;
    }

    public static zzkq<String> zzu(String str, String str2) {
        return new zzkq<String>(str, str2) {
            protected /* synthetic */ Object zzbX(String str) {
                return zzcc(str);
            }

            protected String zzcc(String str) {
                return zzkq.zzaaY.getString(this.zztP, (String) this.zztQ);
            }
        };
    }

    public final T get() {
        return this.zzNR == null ? zzbX(this.zztP) : this.zzNR;
    }

    protected abstract T zzbX(String str);
}
