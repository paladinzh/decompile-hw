package com.google.android.gms.internal;

import java.io.IOException;
import java.util.Arrays;

/* compiled from: Unknown */
final class zzsw {
    final int tag;
    final byte[] zzbuv;

    zzsw(int i, byte[] bArr) {
        this.tag = i;
        this.zzbuv = bArr;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (o == this) {
            return true;
        }
        if (!(o instanceof zzsw)) {
            return false;
        }
        zzsw zzsw = (zzsw) o;
        if (this.tag == zzsw.tag) {
            if (!Arrays.equals(this.zzbuv, zzsw.zzbuv)) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public int hashCode() {
        return ((this.tag + 527) * 31) + Arrays.hashCode(this.zzbuv);
    }

    void writeTo(zzsn output) throws IOException {
        output.zzmB(this.tag);
        output.zzH(this.zzbuv);
    }

    int zzz() {
        return (zzsn.zzmC(this.tag) + 0) + this.zzbuv.length;
    }
}
