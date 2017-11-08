package com.avast.android.shepherd.obfuscated;

import java.io.IOException;

/* compiled from: Unknown */
public class k {
    private final byte[] a;
    private final int b;
    private int c = 0;
    private int d = 0;
    private int e = 0;

    public k(int i) {
        this.b = i;
        this.a = new byte[i];
    }

    public int a() {
        return this.e;
    }

    public int a(byte[] bArr) {
        int i;
        int min = Math.min(bArr.length, this.e);
        int min2 = Math.min(this.b - this.c, min);
        if (min2 <= 0) {
            i = 0;
        } else {
            System.arraycopy(this.a, this.c, bArr, 0, min2);
            i = min2 + 0;
            min -= min2;
            this.c = min2 + this.c;
        }
        if (this.c == this.b) {
            this.c = 0;
        }
        if (min > 0) {
            System.arraycopy(this.a, this.c, bArr, i, min);
            i += min;
            this.c += min;
        }
        this.e -= i;
        return i;
    }

    public void a(byte[] bArr, int i, int i2) {
        if (i2 <= b()) {
            int i3;
            int min = Math.min(this.b - this.d, i2);
            if (min <= 0) {
                i3 = 0;
            } else {
                System.arraycopy(bArr, i + 0, this.a, this.d, min);
                i3 = min + 0;
                i2 -= min;
                this.d = min + this.d;
            }
            if (this.d == this.b) {
                this.d = 0;
            }
            if (i2 > 0) {
                System.arraycopy(bArr, i3 + i, this.a, this.d, i2);
                i3 += i2;
                this.d += i2;
            }
            this.e = i3 + this.e;
            return;
        }
        throw new IOException("RingBuffer overflow (len " + i2 + ", c " + this.b + ", r " + this.c + ", w " + this.d + ", v " + this.e + ").");
    }

    public int b() {
        return this.b - this.e;
    }

    public int c() {
        return this.b;
    }
}
