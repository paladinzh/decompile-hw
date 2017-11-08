package com.avast.android.sdk.engine.obfuscated;

import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/* compiled from: Unknown */
public class e {
    static final /* synthetic */ boolean a;

    static {
        boolean z = false;
        if (!e.class.desiredAssertionStatus()) {
            z = true;
        }
        a = z;
    }

    private e() {
    }

    public static OutputStream a(OutputStream outputStream, byte[] bArr) throws IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        return new m(outputStream, bArr);
    }

    public static Cipher a(byte[] bArr, byte[] bArr2, int i) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        Cipher instance;
        try {
            instance = Cipher.getInstance("AES/CBC/PKCS5Padding", "BC");
        } catch (NoSuchProviderException e) {
            instance = Cipher.getInstance("AES/CBC/PKCS5Padding");
        }
        instance.init(i, new SecretKeySpec(bArr, "AES"), new IvParameterSpec(bArr2));
        return instance;
    }

    public static Mac a(byte[] bArr) throws NoSuchAlgorithmException, InvalidKeyException {
        String str = "HmacSHA1";
        Key secretKeySpec = new SecretKeySpec(bArr, str);
        Mac instance = Mac.getInstance(str);
        instance.init(secretKeySpec);
        return instance;
    }

    private static byte[] a(byte[] bArr, int i) {
        if (!a && i > bArr.length) {
            throw new AssertionError("Trimmed size is bigger that original array (length: " + bArr.length + ", needed: " + i + ").");
        } else if (bArr.length == i) {
            return bArr;
        } else {
            Object obj = new byte[i];
            System.arraycopy(bArr, 0, obj, 0, i);
            return obj;
        }
    }

    public static byte[] a(byte[] bArr, long j) throws g {
        return j.a(bArr, j);
    }

    public static byte[] a(byte[] bArr, byte[] bArr2) throws i, InvalidKeyException, NoSuchAlgorithmException {
        byte[] bArr3 = new byte[(h.a(bArr2, bArr, bArr.length, null) + 20)];
        int a = h.a(bArr2, bArr, bArr.length, bArr3);
        Mac a2 = a(bArr2);
        a2.update(bArr, 0, bArr.length);
        Object doFinal = a2.doFinal();
        if (a || doFinal.length == 20) {
            System.arraycopy(doFinal, 0, bArr3, a, 20);
            return a(bArr3, a + 20);
        }
        throw new AssertionError("Invalid HMac length");
    }
}
