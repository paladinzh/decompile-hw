package com.amap.api.mapcore.util;

/* compiled from: EarClippingTriangulator */
public class az {
    private final bi a = new bi();
    private short[] b;
    private float[] c;
    private int d;
    private final be e = new be();
    private final bi f = new bi();

    public bi a(float[] fArr) {
        return a(fArr, 0, fArr.length);
    }

    public bi a(float[] fArr, int i, int i2) {
        int i3;
        this.c = fArr;
        int i4 = i2 / 2;
        this.d = i4;
        int i5 = i / 2;
        bi biVar = this.a;
        biVar.a();
        biVar.c(i4);
        biVar.b = i4;
        short[] sArr = biVar.a;
        this.b = sArr;
        if (b(fArr, i, i2)) {
            for (i3 = 0; i3 < i4; i3 = (short) (i3 + 1)) {
                sArr[i3] = (short) ((short) (i5 + i3));
            }
        } else {
            int i6 = i4 - 1;
            for (i3 = 0; i3 < i4; i3++) {
                sArr[i3] = (short) ((short) ((i5 + i6) - i3));
            }
        }
        be beVar = this.e;
        beVar.a();
        beVar.c(i4);
        for (i3 = 0; i3 < i4; i3++) {
            beVar.a(a(i3));
        }
        biVar = this.f;
        biVar.a();
        biVar.c(Math.max(0, i4 - 2) * 3);
        a();
        return biVar;
    }

    private void a() {
        int[] iArr = this.e.a;
        while (this.d > 3) {
            int b = b();
            c(b);
            int d = d(b);
            if (b == this.d) {
                b = 0;
            }
            iArr[d] = a(d);
            iArr[b] = a(b);
        }
        if (this.d == 3) {
            bi biVar = this.f;
            short[] sArr = this.b;
            biVar.a(sArr[0]);
            biVar.a(sArr[1]);
            biVar.a(sArr[2]);
        }
    }

    private int a(int i) {
        short[] sArr = this.b;
        int i2 = sArr[d(i)] * 2;
        int i3 = sArr[i] * 2;
        int i4 = sArr[e(i)] * 2;
        float[] fArr = this.c;
        return a(fArr[i2], fArr[i2 + 1], fArr[i3], fArr[i3 + 1], fArr[i4], fArr[i4 + 1]);
    }

    private int b() {
        int i;
        int i2 = this.d;
        for (i = 0; i < i2; i++) {
            if (b(i)) {
                return i;
            }
        }
        int[] iArr = this.e.a;
        for (i = 0; i < i2; i++) {
            if (iArr[i] != -1) {
                return i;
            }
        }
        return 0;
    }

    private boolean b(int i) {
        int[] iArr = this.e.a;
        if (iArr[i] == -1) {
            return false;
        }
        int d = d(i);
        int e = e(i);
        short[] sArr = this.b;
        int i2 = sArr[d] * 2;
        int i3 = sArr[i] * 2;
        int i4 = sArr[e] * 2;
        float[] fArr = this.c;
        float f = fArr[i2];
        float f2 = fArr[i2 + 1];
        float f3 = fArr[i3];
        float f4 = fArr[i3 + 1];
        float f5 = fArr[i4];
        float f6 = fArr[i4 + 1];
        e = e(e);
        while (true) {
            int i5 = e;
            if (i5 == d) {
                return true;
            }
            if (iArr[i5] != 1) {
                i4 = sArr[i5] * 2;
                float f7 = fArr[i4];
                float f8 = fArr[i4 + 1];
                if (a(f5, f6, f, f2, f7, f8) >= 0 && a(f, f2, f3, f4, f7, f8) >= 0 && a(f3, f4, f5, f6, f7, f8) >= 0) {
                    return false;
                }
            }
            e = e(i5);
        }
    }

    private void c(int i) {
        short[] sArr = this.b;
        bi biVar = this.f;
        biVar.a(sArr[d(i)]);
        biVar.a(sArr[i]);
        biVar.a(sArr[e(i)]);
        this.a.b(i);
        this.e.b(i);
        this.d--;
    }

    private int d(int i) {
        if (i == 0) {
            i = this.d;
        }
        return i - 1;
    }

    private int e(int i) {
        return (i + 1) % this.d;
    }

    private static boolean b(float[] fArr, int i, int i2) {
        boolean z = false;
        if (i2 <= 2) {
            return false;
        }
        int i3 = (i + i2) - 3;
        float f = 0.0f;
        for (int i4 = i; i4 < i3; i4 += 2) {
            f += (fArr[i4] * fArr[i4 + 3]) - (fArr[i4 + 1] * fArr[i4 + 2]);
        }
        float f2 = fArr[(i + i2) - 2];
        float f3 = fArr[(i + i2) - 1];
        if (((f2 * fArr[i + 1]) + f) - (fArr[i] * f3) < 0.0f) {
            z = true;
        }
        return z;
    }

    private static int a(float f, float f2, float f3, float f4, float f5, float f6) {
        return (int) Math.signum((((f6 - f4) * f) + ((f2 - f6) * f3)) + ((f4 - f2) * f5));
    }
}
