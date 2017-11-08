package com.google.android.gms.internal;

import java.util.Arrays;

/* compiled from: Unknown */
public final class kg {
    final byte[] aai;
    final int tag;

    public boolean equals(Object o) {
        boolean z = true;
        if (o == this) {
            return true;
        }
        if (!(o instanceof kg)) {
            return false;
        }
        kg kgVar = (kg) o;
        if (this.tag == kgVar.tag) {
            if (!Arrays.equals(this.aai, kgVar.aai)) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public int hashCode() {
        return ((this.tag + 527) * 31) + Arrays.hashCode(this.aai);
    }
}
