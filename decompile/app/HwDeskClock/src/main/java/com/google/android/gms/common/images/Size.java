package com.google.android.gms.common.images;

/* compiled from: Unknown */
public final class Size {
    private final int zznP;
    private final int zznQ;

    public boolean equals(Object obj) {
        boolean z = true;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Size)) {
            return false;
        }
        Size size = (Size) obj;
        if (this.zznP == size.zznP) {
            if (this.zznQ != size.zznQ) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public int hashCode() {
        return this.zznQ ^ ((this.zznP << 16) | (this.zznP >>> 16));
    }

    public String toString() {
        return this.zznP + "x" + this.zznQ;
    }
}
