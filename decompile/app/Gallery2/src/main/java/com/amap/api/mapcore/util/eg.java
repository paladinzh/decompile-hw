package com.amap.api.mapcore.util;

/* compiled from: ShortArray */
public class eg {
    public short[] a;
    public int b;
    public boolean c;

    public eg() {
        this(true, 16);
    }

    public eg(boolean z, int i) {
        this.c = z;
        this.a = new short[i];
    }

    public void a(short s) {
        short[] sArr = this.a;
        if (this.b == sArr.length) {
            sArr = d(Math.max(8, (int) (((float) this.b) * 1.75f)));
        }
        int i = this.b;
        this.b = i + 1;
        sArr[i] = (short) s;
    }

    public short a(int i) {
        if (i < this.b) {
            return this.a[i];
        }
        throw new IndexOutOfBoundsException("index can't be >= size: " + i + " >= " + this.b);
    }

    public short b(int i) {
        if (i < this.b) {
            Object obj = this.a;
            short s = obj[i];
            this.b--;
            if (this.c) {
                System.arraycopy(obj, i + 1, obj, i, this.b - i);
            } else {
                obj[i] = (short) obj[this.b];
            }
            return s;
        }
        throw new IndexOutOfBoundsException("index can't be >= size: " + i + " >= " + this.b);
    }

    public void a() {
        this.b = 0;
    }

    public short[] c(int i) {
        int i2 = this.b + i;
        if (i2 > this.a.length) {
            d(Math.max(8, i2));
        }
        return this.a;
    }

    protected short[] d(int i) {
        Object obj = new short[i];
        System.arraycopy(this.a, 0, obj, 0, Math.min(this.b, obj.length));
        this.a = obj;
        return obj;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof eg)) {
            return false;
        }
        eg egVar = (eg) obj;
        int i = this.b;
        if (i != egVar.b) {
            return false;
        }
        for (int i2 = 0; i2 < i; i2++) {
            if (this.a[i2] != egVar.a[i2]) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        if (this.b == 0) {
            return "[]";
        }
        short[] sArr = this.a;
        StringBuilder stringBuilder = new StringBuilder(32);
        stringBuilder.append('[');
        stringBuilder.append(sArr[0]);
        for (int i = 1; i < this.b; i++) {
            stringBuilder.append(", ");
            stringBuilder.append(sArr[i]);
        }
        stringBuilder.append(']');
        return stringBuilder.toString();
    }
}
