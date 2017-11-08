package com.huawei.hwid.core.encrypt;

import java.util.Locale;

public abstract class HEX {
    public static String encode(byte[] bArr, int i) {
        StringBuffer stringBuffer = new StringBuffer();
        if (bArr == null) {
            return null;
        }
        if (i <= 0 || i > bArr.length) {
            i = bArr.length;
        }
        for (int i2 = 0; i2 < i; i2++) {
            String toHexString = Integer.toHexString(bArr[i2] & 255);
            if (toHexString.length() == 1) {
                toHexString = "0" + toHexString;
            }
            stringBuffer.append(toHexString.toUpperCase(Locale.ENGLISH));
        }
        return stringBuffer.toString();
    }

    public static String encode(byte[] bArr) {
        return encode(bArr, 0);
    }

    public static byte[] decode(String str) {
        if (str == null) {
            return new byte[0];
        }
        int length = str.length();
        if (length % 2 != 0) {
            return new byte[0];
        }
        int i;
        String toUpperCase = str.toUpperCase(Locale.ENGLISH);
        for (i = 0; i < length; i++) {
            char charAt = toUpperCase.charAt(i);
            if ('0' > charAt || charAt > '9') {
                if ('A' > charAt || charAt > 'F') {
                    return new byte[0];
                }
            }
        }
        int i2 = length / 2;
        byte[] bArr = new byte[i2];
        byte[] bArr2 = new byte[2];
        i = 0;
        int i3 = 0;
        while (i3 < i2) {
            int i4 = i + 1;
            bArr2[0] = (byte) ((byte) toUpperCase.charAt(i));
            length = i4 + 1;
            bArr2[1] = (byte) ((byte) toUpperCase.charAt(i4));
            i = 0;
            while (i < 2) {
                if ((byte) 65 <= bArr2[i] && bArr2[i] <= (byte) 70) {
                    bArr2[i] = (byte) ((byte) (bArr2[i] - 55));
                } else {
                    bArr2[i] = (byte) ((byte) (bArr2[i] - 48));
                }
                i++;
            }
            bArr[i3] = (byte) ((byte) ((bArr2[0] << 4) | bArr2[1]));
            i3++;
            i = length;
        }
        return bArr;
    }
}
