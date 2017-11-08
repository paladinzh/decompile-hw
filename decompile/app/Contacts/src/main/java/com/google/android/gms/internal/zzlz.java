package com.google.android.gms.internal;

import android.os.Binder;

/* compiled from: Unknown */
public abstract class zzlz<T> {
    private static zza zzaiV = null;
    private static int zzaiW = 0;
    private static String zzaiX = "com.google.android.providers.gsf.permission.READ_GSERVICES";
    private static final Object zzqy = new Object();
    private T zzSC = null;
    protected final String zzvs;
    protected final T zzvt;

    /* compiled from: Unknown */
    private interface zza {
        Long getLong(String str, Long l);

        String getString(String str, String str2);

        Boolean zza(String str, Boolean bool);

        Float zzb(String str, Float f);

        Integer zzb(String str, Integer num);
    }

    protected zzlz(String str, T t) {
        this.zzvs = str;
        this.zzvt = t;
    }

    public static boolean isInitialized() {
        return zzaiV != null;
    }

    public static zzlz<Float> zza(String str, Float f) {
        return new zzlz<Float>(str, f) {
            protected /* synthetic */ Object zzct(String str) {
                return zzcx(str);
            }

            protected Float zzcx(String str) {
                return zzlz.zzaiV.zzb(this.zzvs, (Float) this.zzvt);
            }
        };
    }

    public static zzlz<Integer> zza(String str, Integer num) {
        return new zzlz<Integer>(str, num) {
            protected /* synthetic */ Object zzct(String str) {
                return zzcw(str);
            }

            protected Integer zzcw(String str) {
                return zzlz.zzaiV.zzb(this.zzvs, (Integer) this.zzvt);
            }
        };
    }

    public static zzlz<Long> zza(String str, Long l) {
        return new zzlz<Long>(str, l) {
            protected /* synthetic */ Object zzct(String str) {
                return zzcv(str);
            }

            protected Long zzcv(String str) {
                return zzlz.zzaiV.getLong(this.zzvs, (Long) this.zzvt);
            }
        };
    }

    public static zzlz<Boolean> zzk(String str, boolean z) {
        return new zzlz<Boolean>(str, Boolean.valueOf(z)) {
            protected /* synthetic */ Object zzct(String str) {
                return zzcu(str);
            }

            protected Boolean zzcu(String str) {
                return zzlz.zzaiV.zza(this.zzvs, (Boolean) this.zzvt);
            }
        };
    }

    public static int zzpW() {
        return zzaiW;
    }

    public static zzlz<String> zzv(String str, String str2) {
        return new zzlz<String>(str, str2) {
            protected /* synthetic */ Object zzct(String str) {
                return zzcy(str);
            }

            protected String zzcy(String str) {
                return zzlz.zzaiV.getString(this.zzvs, (String) this.zzvt);
            }
        };
    }

    public final T get() {
        return this.zzSC == null ? zzct(this.zzvs) : this.zzSC;
    }

    protected abstract T zzct(String str);

    public final T zzpX() {
        long clearCallingIdentity = Binder.clearCallingIdentity();
        try {
            T t = get();
            return t;
        } finally {
            Binder.restoreCallingIdentity(clearCallingIdentity);
        }
    }
}
