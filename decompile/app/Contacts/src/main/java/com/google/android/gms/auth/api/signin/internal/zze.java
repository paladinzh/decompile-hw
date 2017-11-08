package com.google.android.gms.auth.api.signin.internal;

/* compiled from: Unknown */
public class zze {
    static int zzXy = 31;
    private int zzXz = 1;

    public zze zzP(boolean z) {
        int i = 0;
        int i2 = zzXy * this.zzXz;
        if (z) {
            i = 1;
        }
        this.zzXz = i + i2;
        return this;
    }

    public int zzne() {
        return this.zzXz;
    }

    public zze zzp(Object obj) {
        this.zzXz = (obj != null ? obj.hashCode() : 0) + (this.zzXz * zzXy);
        return this;
    }
}
