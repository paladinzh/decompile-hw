package com.google.android.gms.internal;

import java.io.IOException;
import java.util.Arrays;

/* compiled from: Unknown */
final class zzrz {
    final int tag;
    final byte[] zzbcp;

    zzrz(int i, byte[] bArr) {
        this.tag = i;
        this.zzbcp = bArr;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (o == this) {
            return true;
        }
        if (!(o instanceof zzrz)) {
            return false;
        }
        zzrz zzrz = (zzrz) o;
        if (this.tag == zzrz.tag) {
            if (!Arrays.equals(this.zzbcp, zzrz.zzbcp)) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public int hashCode() {
        return ((this.tag + 527) * 31) + Arrays.hashCode(this.zzbcp);
    }

    int zzB() {
        return (zzrq.zzlx(this.tag) + 0) + this.zzbcp.length;
    }

    void zza(zzrq zzrq) throws IOException {
        zzrq.zzlw(this.tag);
        zzrq.zzD(this.zzbcp);
    }
}
