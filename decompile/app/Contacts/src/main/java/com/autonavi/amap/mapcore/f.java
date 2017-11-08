package com.autonavi.amap.mapcore;

/* compiled from: VTMCDataCache */
class f {
    byte[] a;
    String b;
    int c;
    String d;
    int e;

    public f(byte[] bArr) {
        try {
            this.c = (int) (System.currentTimeMillis() / 1000);
            byte b = bArr[4];
            this.b = new String(bArr, 5, b);
            int i = b + 5;
            int i2 = i + 1;
            b = bArr[i];
            this.d = new String(bArr, i2, b);
            this.e = Convert.getInt(bArr, b + i2);
            this.a = bArr;
        } catch (Exception e) {
            this.a = null;
        }
    }

    public void a(int i) {
        if (this.a != null) {
            this.c = (int) (System.currentTimeMillis() / 1000);
            int i2 = this.a[4] + 5;
            Convert.writeInt(this.a, this.a[i2] + (i2 + 1), i);
            this.e = i;
        }
    }
}
