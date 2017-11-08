package com.google.android.gms.internal;

import android.graphics.drawable.Drawable;
import com.google.android.gms.common.internal.zzw;

/* compiled from: Unknown */
public final class zzku extends zzlf<zza, Drawable> {

    /* compiled from: Unknown */
    public static final class zza {
        public final int zzacA;
        public final int zzacB;

        public zza(int i, int i2) {
            this.zzacA = i;
            this.zzacB = i2;
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
            if (obj2.zzacA == this.zzacA) {
                if (obj2.zzacB != this.zzacB) {
                }
                return z;
            }
            z = false;
            return z;
        }

        public int hashCode() {
            return zzw.hashCode(Integer.valueOf(this.zzacA), Integer.valueOf(this.zzacB));
        }
    }

    public zzku() {
        super(10);
    }
}
