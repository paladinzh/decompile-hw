package com.amap.api.services.core;

/* compiled from: XXTEA */
public class ah {
    private static int a = 4;

    private ah() {
    }

    public static byte[] a(byte[] bArr, byte[] bArr2) {
        if (bArr.length == 0) {
            return bArr;
        }
        int length = bArr.length;
        int i = a - (length % a);
        byte[] bArr3 = new byte[(((length / a) + 1) * a)];
        System.arraycopy(bArr, 0, bArr3, 0, bArr.length);
        while (length < bArr3.length) {
            bArr3[length] = (byte) ((byte) i);
            length++;
        }
        return a(a(a(bArr3, true), a(bArr2, false)), false);
    }

    public static int[] a(int[] iArr, int[] iArr2) {
        int length = iArr.length - 1;
        if (length < 1) {
            return iArr;
        }
        if (iArr2.length < 4) {
            Object obj = new int[4];
            System.arraycopy(iArr2, 0, obj, 0, iArr2.length);
            Object obj2 = obj;
        }
        int i = iArr[length];
        int i2 = iArr[0];
        int i3 = 0;
        i2 = i;
        i = (52 / (length + 1)) + 6;
        while (true) {
            int i4 = i - 1;
            if (i <= 0) {
                return iArr;
            }
            int i5;
            i3 -= 1640531527;
            int i6 = (i3 >>> 2) & 3;
            i = i2;
            i2 = 0;
            while (i2 < length) {
                i5 = iArr[i2 + 1];
                i = (((i ^ iArr2[(i2 & 3) ^ i6]) + (i5 ^ i3)) ^ (((i >>> 5) ^ (i5 << 2)) + ((i5 >>> 3) ^ (i << 4)))) + iArr[i2];
                iArr[i2] = i;
                i2++;
            }
            i5 = iArr[0];
            i2 = (((iArr2[(i2 & 3) ^ i6] ^ i) + (i5 ^ i3)) ^ (((i >>> 5) ^ (i5 << 2)) + ((i5 >>> 3) ^ (i << 4)))) + iArr[length];
            iArr[length] = i2;
            i = i4;
        }
    }

    private static int[] a(byte[] bArr, boolean z) {
        int[] iArr = new int[(bArr.length >>> 2)];
        int length = bArr.length;
        for (int i = 0; i < length; i++) {
            int i2 = i >>> 2;
            iArr[i2] = iArr[i2] | ((bArr[i] & 255) << ((i & 3) << 3));
        }
        return iArr;
    }

    private static byte[] a(int[] iArr, boolean z) {
        int i;
        int length = iArr.length << 2;
        if (z) {
            i = iArr[iArr.length - 1];
            if (i > length) {
                return null;
            }
            length = i;
        }
        byte[] bArr = new byte[length];
        for (i = 0; i < length; i++) {
            bArr[i] = (byte) ((byte) ((iArr[i >>> 2] >>> ((i & 3) << 3)) & 255));
        }
        return bArr;
    }
}
