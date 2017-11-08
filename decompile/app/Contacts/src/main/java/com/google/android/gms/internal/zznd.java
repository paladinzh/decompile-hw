package com.google.android.gms.internal;

/* compiled from: Unknown */
public class zznd {
    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int zza(byte[] bArr, int i, int i2, int i3) {
        int i4;
        int i5 = i + (i2 & -4);
        int i6 = i3;
        while (i < i5) {
            i4 = ((((bArr[i] & 255) | ((bArr[i + 1] & 255) << 8)) | ((bArr[i + 2] & 255) << 16)) | (bArr[i + 3] << 24)) * -862048943;
            i4 = (((i4 >>> 17) | (i4 << 15)) * 461845907) ^ i6;
            i6 = -430675100 + (((i4 >>> 19) | (i4 << 13)) * 5);
            i += 4;
        }
        i4 = 0;
        switch (i2 & 3) {
            case 1:
                break;
            case 2:
                break;
            case 3:
                i4 = (bArr[i5 + 2] & 255) << 16;
                break;
            default:
                i4 = i6;
                break;
        }
    }
}
