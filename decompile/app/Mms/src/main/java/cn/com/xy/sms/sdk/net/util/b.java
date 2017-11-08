package cn.com.xy.sms.sdk.net.util;

import java.util.Arrays;

/* compiled from: Unknown */
public final class b {
    private static final boolean a = true;
    private static final char[] b = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    private static final int[] c;

    static {
        int[] iArr = new int[256];
        c = iArr;
        Arrays.fill(iArr, -1);
        int length = b.length;
        for (int i = 0; i < length; i++) {
            c[b[i]] = i;
        }
        c[61] = 0;
    }

    public static final byte[] a(String str) {
        int length = str == null ? 0 : str.length();
        if (length == 0) {
            return new byte[0];
        }
        int i;
        int i2 = 0;
        for (i = 0; i < length; i++) {
            if (c[str.charAt(i)] < 0) {
                i2++;
            }
        }
        if ((length - i2) % 4 != 0) {
            return null;
        }
        i = length;
        int i3 = 0;
        while (i > 1) {
            i--;
            if (c[str.charAt(i)] > 0) {
                break;
            } else if (str.charAt(i) == '=') {
                i3++;
            }
        }
        int i4 = (((length - i2) * 6) >> 3) - i3;
        byte[] bArr = new byte[i4];
        length = 0;
        i2 = 0;
        while (length < i4) {
            i = 0;
            i3 = i2;
            i2 = 0;
            while (i2 < 4) {
                int i5 = i3 + 1;
                i3 = c[str.charAt(i3)];
                if (i3 < 0) {
                    i2--;
                } else {
                    i |= i3 << (18 - (i2 * 6));
                }
                i2++;
                i3 = i5;
            }
            i2 = length + 1;
            bArr[length] = (byte) ((byte) (i >> 16));
            if (i2 < i4) {
                length = i2 + 1;
                bArr[i2] = (byte) ((byte) (i >> 8));
                if (length >= i4) {
                    i2 = i3;
                } else {
                    i2 = length + 1;
                    bArr[length] = (byte) ((byte) i);
                }
            }
            length = i2;
            i2 = i3;
        }
        return bArr;
    }

    public static final byte[] a(byte[] bArr) {
        int i = 0;
        int length = bArr == null ? 0 : bArr.length;
        if (length == 0) {
            return new byte[0];
        }
        int i2 = (length / 3) * 3;
        int i3 = (((length - 1) / 3) + 1) << 2;
        int i4 = i3 + (((i3 - 1) / 76) << 1);
        byte[] bArr2 = new byte[i4];
        i3 = 0;
        int i5 = 0;
        int i6 = 0;
        while (i6 < i2) {
            int i7 = i6 + 1;
            int i8 = i7 + 1;
            i7 = ((bArr[i7] & 255) << 8) | ((bArr[i6] & 255) << 16);
            i6 = i8 + 1;
            i7 |= bArr[i8] & 255;
            i8 = i5 + 1;
            bArr2[i5] = (byte) ((byte) b[(i7 >>> 18) & 63]);
            i5 = i8 + 1;
            bArr2[i8] = (byte) ((byte) b[(i7 >>> 12) & 63]);
            i8 = i5 + 1;
            bArr2[i5] = (byte) ((byte) b[(i7 >>> 6) & 63]);
            i5 = i8 + 1;
            bArr2[i8] = (byte) ((byte) b[i7 & 63]);
            i3++;
            if (i3 == 19 && i5 < i4 - 2) {
                i7 = i5 + 1;
                bArr2[i5] = (byte) 13;
                i3 = i7 + 1;
                bArr2[i7] = (byte) 10;
                i5 = i3;
                i3 = 0;
            }
        }
        i3 = length - i2;
        if (i3 > 0) {
            i5 = (bArr[i2] & 255) << 10;
            if (i3 == 2) {
                i = (bArr[length - 1] & 255) << 2;
            }
            i |= i5;
            bArr2[i4 - 4] = (byte) ((byte) b[i >> 12]);
            bArr2[i4 - 3] = (byte) ((byte) b[(i >>> 6) & 63]);
            bArr2[i4 - 2] = (byte) (i3 != 2 ? 61 : (byte) b[i & 63]);
            bArr2[i4 - 1] = (byte) 61;
        }
        return bArr2;
    }

    private static byte[] a(byte[] bArr, boolean z) {
        int i = 0;
        int length = bArr == null ? 0 : bArr.length;
        if (length == 0) {
            return new byte[0];
        }
        int i2 = (length / 3) * 3;
        int i3 = (((length - 1) / 3) + 1) << 2;
        int i4 = i3 + (((i3 - 1) / 76) << 1);
        byte[] bArr2 = new byte[i4];
        i3 = 0;
        int i5 = 0;
        int i6 = 0;
        while (i6 < i2) {
            int i7 = i6 + 1;
            int i8 = i7 + 1;
            i7 = ((bArr[i7] & 255) << 8) | ((bArr[i6] & 255) << 16);
            i6 = i8 + 1;
            i7 |= bArr[i8] & 255;
            i8 = i5 + 1;
            bArr2[i5] = (byte) ((byte) b[(i7 >>> 18) & 63]);
            i5 = i8 + 1;
            bArr2[i8] = (byte) ((byte) b[(i7 >>> 12) & 63]);
            i8 = i5 + 1;
            bArr2[i5] = (byte) ((byte) b[(i7 >>> 6) & 63]);
            i5 = i8 + 1;
            bArr2[i8] = (byte) ((byte) b[i7 & 63]);
            i3++;
            if (i3 == 19 && i5 < i4 - 2) {
                i7 = i5 + 1;
                bArr2[i5] = (byte) 13;
                i3 = i7 + 1;
                bArr2[i7] = (byte) 10;
                i5 = i3;
                i3 = 0;
            }
        }
        i3 = length - i2;
        if (i3 > 0) {
            i5 = (bArr[i2] & 255) << 10;
            if (i3 == 2) {
                i = (bArr[length - 1] & 255) << 2;
            }
            i |= i5;
            bArr2[i4 - 4] = (byte) ((byte) b[i >> 12]);
            bArr2[i4 - 3] = (byte) ((byte) b[(i >>> 6) & 63]);
            bArr2[i4 - 2] = (byte) (i3 != 2 ? 61 : (byte) b[i & 63]);
            bArr2[i4 - 1] = (byte) 61;
        }
        return bArr2;
    }

    public static final byte[] b(byte[] bArr) {
        int i;
        int i2;
        int i3 = 0;
        for (byte b : bArr) {
            if (c[b & 255] < 0) {
                i3++;
            }
        }
        if ((i2 - i3) % 4 != 0) {
            return null;
        }
        i = i2;
        int i4 = 0;
        while (i > 1) {
            i--;
            if (c[bArr[i] & 255] > 0) {
                break;
            } else if (bArr[i] == (byte) 61) {
                i4++;
            }
        }
        int i5 = (((i2 - i3) * 6) >> 3) - i4;
        byte[] bArr2 = new byte[i5];
        i2 = 0;
        i3 = 0;
        while (i2 < i5) {
            i = 0;
            i4 = i3;
            i3 = 0;
            while (i3 < 4) {
                int i6 = i4 + 1;
                i4 = c[bArr[i4] & 255];
                if (i4 < 0) {
                    i3--;
                } else {
                    i |= i4 << (18 - (i3 * 6));
                }
                i3++;
                i4 = i6;
            }
            i3 = i2 + 1;
            bArr2[i2] = (byte) ((byte) (i >> 16));
            if (i3 < i5) {
                i2 = i3 + 1;
                bArr2[i3] = (byte) ((byte) (i >> 8));
                if (i2 >= i5) {
                    i3 = i4;
                } else {
                    i3 = i2 + 1;
                    bArr2[i2] = (byte) ((byte) i);
                }
            }
            i2 = i3;
            i3 = i4;
        }
        return bArr2;
    }

    public static String c(byte[] bArr) {
        int length = bArr.length;
        StringBuffer stringBuffer = new StringBuffer((bArr.length * 3) / 2);
        int i = length - 3;
        int i2 = 0;
        int i3 = 0;
        while (i3 <= i) {
            int i4 = (((bArr[i3] & 255) << 16) | ((bArr[i3 + 1] & 255) << 8)) | (bArr[i3 + 2] & 255);
            stringBuffer.append(b[(i4 >> 18) & 63]);
            stringBuffer.append(b[(i4 >> 12) & 63]);
            stringBuffer.append(b[(i4 >> 6) & 63]);
            stringBuffer.append(b[i4 & 63]);
            i4 = i3 + 3;
            i3 = i2 + 1;
            if (i2 < 14) {
                i2 = i3;
                i3 = i4;
            } else {
                stringBuffer.append(" ");
                i2 = 0;
                i3 = i4;
            }
        }
        if (i3 == (length + 0) - 2) {
            i2 = ((bArr[i3] & 255) << 16) | ((bArr[i3 + 1] & 255) << 8);
            stringBuffer.append(b[(i2 >> 18) & 63]);
            stringBuffer.append(b[(i2 >> 12) & 63]);
            stringBuffer.append(b[(i2 >> 6) & 63]);
            stringBuffer.append("=");
        } else if (i3 == (length + 0) - 1) {
            i2 = (bArr[i3] & 255) << 16;
            stringBuffer.append(b[(i2 >> 18) & 63]);
            stringBuffer.append(b[(i2 >> 12) & 63]);
            stringBuffer.append("==");
        }
        return stringBuffer.toString();
    }
}
