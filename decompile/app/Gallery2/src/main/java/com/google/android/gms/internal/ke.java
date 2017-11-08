package com.google.android.gms.internal;

import java.io.IOException;

/* compiled from: Unknown */
public abstract class ke {
    protected int DY = -1;

    public static final void a(ke keVar, byte[] bArr, int i, int i2) {
        try {
            jz b = jz.b(bArr, i, i2);
            keVar.a(b);
            b.kN();
        } catch (Throwable e) {
            throw new RuntimeException("Serializing to a byte array threw an IOException (should never happen).", e);
        }
    }

    public static final byte[] d(ke keVar) {
        byte[] bArr = new byte[keVar.c()];
        a(keVar, bArr, 0, bArr.length);
        return bArr;
    }

    public void a(jz jzVar) throws IOException {
    }

    public int c() {
        this.DY = 0;
        return 0;
    }

    public int eW() {
        if (this.DY < 0) {
            c();
        }
        return this.DY;
    }

    public String toString() {
        return kf.e(this);
    }
}
