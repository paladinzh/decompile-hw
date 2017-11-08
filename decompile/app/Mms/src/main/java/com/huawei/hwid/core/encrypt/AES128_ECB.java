package com.huawei.hwid.core.encrypt;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public abstract class AES128_ECB {
    public static byte[] decode(byte[] bArr, int i, byte[] bArr2, int i2) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        return a(bArr, i, bArr2, i2, 1);
    }

    private static byte[] a(byte[] bArr, int i, byte[] bArr2, int i2, int i3) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        if (bArr == null || bArr2 == null) {
            return new byte[0];
        }
        int i4;
        int i5;
        if (i <= 0 || i > bArr.length) {
            i = bArr.length;
        }
        if (i2 > 0 && i2 <= bArr2.length) {
            i4 = i2;
        } else {
            i4 = bArr2.length;
        }
        if (i4 > 16) {
            i4 = 16;
        }
        byte[] bArr3 = new byte[16];
        for (i5 = 0; i5 < 16; i5++) {
            bArr3[i5] = (byte) 0;
        }
        for (i5 = 0; i5 < i4; i5++) {
            bArr3[i5] = (byte) bArr2[i5];
        }
        Cipher instance = Cipher.getInstance("AES/ECB/PKCS5Padding");
        if (i3 != 0) {
            i4 = 2;
        } else {
            i4 = 1;
        }
        instance.init(i4, new SecretKeySpec(bArr3, 0, 16, "AES"));
        return instance.doFinal(bArr, 0, i);
    }
}
