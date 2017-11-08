package com.avast.android.sdk.engine.obfuscated;

import com.huawei.rcs.common.HwRcsCommonObject;

/* compiled from: Unknown */
public class w {
    private byte[] a;
    private byte[] b = null;

    public w(byte[] bArr) {
        this.a = bArr;
    }

    private int a(byte b) {
        return b & 255;
    }

    private int a(byte[] bArr) {
        return (bArr[1] & 255) < 128 ? 1 : ((bArr[1] & 255) - 128) + 1;
    }

    private int a(byte[] bArr, int i) {
        int i2 = 1;
        if (i < 1) {
            return -1;
        }
        int i3 = 0;
        if (i > 1) {
            i2 = 2;
        }
        while (i2 <= i) {
            i3 = (i3 << 8) + (bArr[i2] & 255);
            i2++;
        }
        return i3;
    }

    private int a(byte[] bArr, String str) {
        int a = a(bArr);
        int a2 = a(bArr, a);
        int i = a + 1;
        int a3 = a(bArr[0]);
        byte[] bArr2 = new byte[a2];
        System.arraycopy(bArr, i, bArr2, 0, a2);
        int i2 = 0;
        while (i < bArr.length) {
            switch (a3) {
                case HwRcsCommonObject.BLACKLIST_MSG_VIDEO_TYPE /*48*/:
                case 49:
                case 160:
                    StringBuilder append = new StringBuilder().append(str);
                    a = a(bArr2, append.append(i2).append("/").toString()) + i;
                    i2++;
                    break;
                default:
                    if ("/1/0/4/0/4".equals(str + i2)) {
                        if (a3 == 4) {
                            this.b = new byte[a2];
                            System.arraycopy(bArr, i, this.b, 0, a2);
                        } else {
                            throw new SecurityException();
                        }
                    }
                    i2++;
                    a = i + a2;
                    break;
            }
            if (a >= bArr.length) {
                return a;
            }
            byte[] bArr3 = new byte[(bArr.length - a)];
            System.arraycopy(bArr, a, bArr3, 0, bArr.length - a);
            a3 = a(bArr3[0]);
            int a4 = a(bArr3);
            a2 = a(bArr3, a4);
            i = (a4 + 1) + a;
            bArr2 = new byte[a2];
            System.arraycopy(bArr, i, bArr2, 0, a2);
        }
        return i;
    }

    public byte[] a() {
        int a = a(this.a);
        int a2 = a(this.a, a);
        byte[] bArr = new byte[a2];
        System.arraycopy(this.a, a + 1, bArr, 0, a2);
        int i = 0;
        while (i < a2) {
            int a3;
            switch (a(this.a[0])) {
                case HwRcsCommonObject.BLACKLIST_MSG_VIDEO_TYPE /*48*/:
                case 49:
                case 160:
                    a3 = a(bArr, "/") + i;
                    break;
                default:
                    a3 = a2;
                    break;
            }
            Object obj = new byte[(this.a.length - ((a + 1) + a3))];
            System.arraycopy(this.a, (a + 1) + a3, obj, 0, this.a.length - ((a + 1) + a3));
            Object obj2 = obj;
            i = a3;
            Object obj3 = obj2;
        }
        return this.b;
    }
}
