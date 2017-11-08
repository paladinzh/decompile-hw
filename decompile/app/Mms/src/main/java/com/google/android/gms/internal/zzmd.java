package com.google.android.gms.internal;

import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;
import com.google.android.gms.common.internal.zzw;

/* compiled from: Unknown */
public final class zzmd extends LruCache<zza, Drawable> {

    /* compiled from: Unknown */
    public static final class zza {
        public final int zzakx;
        public final int zzaky;

        public zza(int i, int i2) {
            this.zzakx = i;
            this.zzaky = i2;
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (!(obj instanceof zza)) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            zza obj2 = (zza) obj;
            if (obj2.zzakx == this.zzakx) {
                if (obj2.zzaky != this.zzaky) {
                }
                return z;
            }
            z = false;
            return z;
        }

        public int hashCode() {
            return zzw.hashCode(Integer.valueOf(this.zzakx), Integer.valueOf(this.zzaky));
        }
    }

    public zzmd() {
        super(10);
    }
}
