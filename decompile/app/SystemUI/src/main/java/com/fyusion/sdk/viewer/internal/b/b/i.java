package com.fyusion.sdk.viewer.internal.b.b;

import com.fyusion.sdk.viewer.internal.b.e;
import com.fyusion.sdk.viewer.internal.f.d;
import java.io.File;
import java.security.MessageDigest;

/* compiled from: Unknown */
class i implements e {
    private final Object b;
    private final boolean c;
    private int d;

    public i(Object obj, boolean z) {
        this.b = d.a(obj);
        this.c = z;
    }

    public void a(MessageDigest messageDigest) {
        throw new UnsupportedOperationException();
    }

    public String d() {
        return !(this.b instanceof String) ? !(this.b instanceof com.fyusion.sdk.common.i) ? !(this.b instanceof File) ? this.b.toString() : ((File) this.b).getName() : ((com.fyusion.sdk.common.i) this.b).getId() : (String) this.b;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof i)) {
            return false;
        }
        i iVar = (i) obj;
        if (this.b.equals(iVar.b) && this.c == iVar.c) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        int i = 0;
        if (this.d == 0) {
            this.d = this.b.hashCode();
            int i2 = this.d * 31;
            if (this.c) {
                i = 1;
            }
            this.d = i + i2;
        }
        return this.d;
    }

    public String toString() {
        return "EngineKey{model=" + this.b + ", isHighRes=" + this.c + ", hashCode=" + this.d + '}';
    }
}
