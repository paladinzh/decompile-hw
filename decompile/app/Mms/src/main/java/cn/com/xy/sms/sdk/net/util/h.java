package cn.com.xy.sms.sdk.net.util;

import com.google.android.gms.location.places.Place;
import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;

/* compiled from: Unknown */
public final class h {
    private static String a = "RSA";
    private static String b = "MD5withRSA";
    private static final String c = "RSAPublicKey";
    private static final String d = "RSAPrivateKey";
    private static final int e = 117;
    private static final int f = 128;
    private static final int g = 256;
    private static final String h = "RSA/ECB/PKCS1Padding";

    private static String a(Map<String, Object> map) {
        return b.c(((Key) map.get(d)).getEncoded());
    }

    public static String a(byte[] bArr) {
        StringBuffer stringBuffer = new StringBuffer();
        for (byte b : bArr) {
            String toHexString = Integer.toHexString(b & 255);
            if (toHexString.length() == 1) {
                toHexString = "0" + toHexString;
            }
            stringBuffer.append(toHexString.toUpperCase());
        }
        return stringBuffer.toString();
    }

    private static Map<String, Object> a() {
        KeyPairGenerator instance = KeyPairGenerator.getInstance("RSA");
        instance.initialize(Place.TYPE_SUBLOCALITY_LEVEL_2);
        KeyPair generateKeyPair = instance.generateKeyPair();
        RSAPublicKey rSAPublicKey = (RSAPublicKey) generateKeyPair.getPublic();
        RSAPrivateKey rSAPrivateKey = (RSAPrivateKey) generateKeyPair.getPrivate();
        Map<String, Object> hashMap = new HashMap(2);
        hashMap.put(c, rSAPublicKey);
        hashMap.put(d, rSAPrivateKey);
        return hashMap;
    }

    private static boolean a(byte[] bArr, String str, String str2) {
        PublicKey generatePublic = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(b.a(str)));
        Signature instance = Signature.getInstance("MD5withRSA");
        instance.initVerify(generatePublic);
        instance.update(bArr);
        return instance.verify(b.a(str2));
    }

    public static byte[] a(String str) {
        if (str.length() <= 0) {
            return null;
        }
        byte[] bArr = new byte[(str.length() / 2)];
        for (int i = 0; i < str.length() / 2; i++) {
            bArr[i] = (byte) ((byte) ((Integer.parseInt(str.substring(i << 1, (i << 1) + 1), 16) << 4) + Integer.parseInt(str.substring((i << 1) + 1, (i << 1) + 2), 16)));
        }
        return bArr;
    }

    public static byte[] a(byte[] bArr, String str) {
        byte[] doFinal;
        Key generatePrivate = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(b.a(str)));
        Cipher instance = Cipher.getInstance(h);
        instance.init(1, generatePrivate);
        int length = bArr.length;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i = 0;
        int i2 = 0;
        while (length - i2 > 0) {
            doFinal = length - i2 <= e ? instance.doFinal(bArr, i2, length - i2) : instance.doFinal(bArr, i2, e);
            byteArrayOutputStream.write(doFinal, 0, doFinal.length);
            i2 = i + 1;
            int i3 = i2;
            i2 *= e;
            i = i3;
        }
        doFinal = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        return doFinal;
    }

    private static String b(Map<String, Object> map) {
        return b.c(((Key) map.get(c)).getEncoded());
    }

    public static String b(byte[] bArr) {
        StringBuffer stringBuffer = new StringBuffer();
        for (byte b : bArr) {
            String toHexString = Integer.toHexString(b & 255);
            if (toHexString.length() == 1) {
                toHexString = "0" + toHexString;
            }
            stringBuffer.append(toHexString);
        }
        return stringBuffer.toString();
    }

    private static Map<String, Object> b() {
        KeyPairGenerator instance = KeyPairGenerator.getInstance("RSA");
        instance.initialize(2048);
        KeyPair generateKeyPair = instance.generateKeyPair();
        RSAPublicKey rSAPublicKey = (RSAPublicKey) generateKeyPair.getPublic();
        RSAPrivateKey rSAPrivateKey = (RSAPrivateKey) generateKeyPair.getPrivate();
        Map<String, Object> hashMap = new HashMap(2);
        hashMap.put(c, rSAPublicKey);
        hashMap.put(d, rSAPrivateKey);
        return hashMap;
    }

    public static byte[] b(byte[] bArr, String str) {
        byte[] doFinal;
        Key generatePrivate = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(b.a(str)));
        Cipher instance = Cipher.getInstance(h);
        instance.init(2, generatePrivate);
        int length = bArr.length;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i = 0;
        int i2 = 0;
        while (length - i2 > 0) {
            doFinal = length - i2 <= g ? instance.doFinal(bArr, i2, length - i2) : instance.doFinal(bArr, i2, g);
            byteArrayOutputStream.write(doFinal, 0, doFinal.length);
            i2 = i + 1;
            int i3 = i2;
            i2 <<= 8;
            i = i3;
        }
        doFinal = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        return doFinal;
    }

    private static String c(byte[] bArr, String str) {
        PrivateKey generatePrivate = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(b.a(str)));
        Signature instance = Signature.getInstance("MD5withRSA");
        instance.initSign(generatePrivate);
        instance.update(bArr);
        return b.c(instance.sign());
    }

    private static byte[] d(byte[] bArr, String str) {
        byte[] doFinal;
        Key generatePrivate = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(b.a(str)));
        Cipher instance = Cipher.getInstance(h);
        instance.init(2, generatePrivate);
        int length = bArr.length;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i = 0;
        int i2 = 0;
        while (length - i2 > 0) {
            doFinal = length - i2 <= f ? instance.doFinal(bArr, i2, length - i2) : instance.doFinal(bArr, i2, f);
            byteArrayOutputStream.write(doFinal, 0, doFinal.length);
            i2 = i + 1;
            int i3 = i2;
            i2 <<= 7;
            i = i3;
        }
        doFinal = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        return doFinal;
    }

    private static byte[] e(byte[] bArr, String str) {
        byte[] doFinal;
        Key generatePublic = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(b.a(str)));
        Cipher instance = Cipher.getInstance(h);
        instance.init(2, generatePublic);
        int length = bArr.length;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i = 0;
        int i2 = 0;
        while (length - i2 > 0) {
            doFinal = length - i2 <= f ? instance.doFinal(bArr, i2, length - i2) : instance.doFinal(bArr, i2, f);
            byteArrayOutputStream.write(doFinal, 0, doFinal.length);
            i2 = i + 1;
            int i3 = i2;
            i2 <<= 7;
            i = i3;
        }
        doFinal = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        return doFinal;
    }

    private static byte[] f(byte[] bArr, String str) {
        byte[] doFinal;
        Key generatePublic = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(b.a(str)));
        Cipher instance = Cipher.getInstance(h);
        instance.init(1, generatePublic);
        int length = bArr.length;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i = 0;
        int i2 = 0;
        while (length - i2 > 0) {
            doFinal = length - i2 <= e ? instance.doFinal(bArr, i2, length - i2) : instance.doFinal(bArr, i2, e);
            byteArrayOutputStream.write(doFinal, 0, doFinal.length);
            i2 = i + 1;
            int i3 = i2;
            i2 *= e;
            i = i3;
        }
        doFinal = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        return doFinal;
    }

    private static byte[] g(byte[] bArr, String str) {
        byte[] doFinal;
        Key generatePublic = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(b.a(str)));
        Cipher instance = Cipher.getInstance(h);
        instance.init(2, generatePublic);
        int length = bArr.length;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i = 0;
        int i2 = 0;
        while (length - i2 > 0) {
            doFinal = length - i2 <= g ? instance.doFinal(bArr, i2, length - i2) : instance.doFinal(bArr, i2, g);
            byteArrayOutputStream.write(doFinal, 0, doFinal.length);
            i2 = i + 1;
            int i3 = i2;
            i2 <<= 8;
            i = i3;
        }
        doFinal = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        return doFinal;
    }
}
