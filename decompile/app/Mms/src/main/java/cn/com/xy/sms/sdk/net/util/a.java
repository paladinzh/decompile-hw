package cn.com.xy.sms.sdk.net.util;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/* compiled from: Unknown */
public final class a {
    private static final String a = "AES/CBC/PKCS5Padding";
    private static final String b = "AES";
    private static final int c = 128;
    private static byte[] d = new byte[]{(byte) 18, (byte) 52, (byte) 86, (byte) 120, (byte) -112, (byte) -85, (byte) -51, (byte) -17, (byte) -87, (byte) -73, (byte) -56, (byte) -42, (byte) -29, (byte) -15, (byte) 31, (byte) -2};

    private static byte[] a() {
        KeyGenerator instance = KeyGenerator.getInstance(b);
        instance.init(c);
        return instance.generateKey().getEncoded();
    }

    public static byte[] a(byte[] bArr, byte[] bArr2) {
        Key secretKeySpec = new SecretKeySpec(bArr2, b);
        Cipher instance = Cipher.getInstance(a);
        instance.init(1, secretKeySpec, new IvParameterSpec(d));
        return instance.doFinal(bArr);
    }

    public static byte[] b(byte[] bArr, byte[] bArr2) {
        Key secretKeySpec = new SecretKeySpec(bArr2, b);
        Cipher instance = Cipher.getInstance(a);
        instance.init(2, secretKeySpec, new IvParameterSpec(d));
        return instance.doFinal(bArr);
    }
}
