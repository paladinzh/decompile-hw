package com.huawei.hwid.core.encrypt;

import android.annotation.SuppressLint;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class c {
    @SuppressLint({"TrulyRandom"})
    private static String a() {
        byte[] generateSeed = new SecureRandom().generateSeed(16);
        return HEX.encode(generateSeed, generateSeed.length);
    }

    private static SecretKeySpec a(byte[] bArr, int i) {
        int i2;
        int i3;
        if (i > 0 && i <= bArr.length) {
            i2 = i;
        } else {
            i2 = bArr.length;
        }
        if (i2 > 16) {
            i2 = 16;
        }
        byte[] bArr2 = new byte[16];
        for (i3 = 0; i3 < 16; i3++) {
            bArr2[i3] = (byte) 0;
        }
        for (i3 = 0; i3 < i2; i3++) {
            bArr2[i3] = (byte) bArr[i3];
        }
        return new SecretKeySpec(bArr2, 0, 16, "AES/CBC/PKCS5Padding");
    }

    public static String a(byte[] bArr, byte[] bArr2) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        String a = a();
        return a + ":" + HEX.encode(b.a(bArr, HEX.decode(a), a(bArr2, 0)));
    }
}
