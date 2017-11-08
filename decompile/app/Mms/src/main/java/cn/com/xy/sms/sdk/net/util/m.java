package cn.com.xy.sms.sdk.net.util;

import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/* compiled from: Unknown */
public final class m {
    public static String a(String str) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(str.getBytes("UTF-8"));
            StringBuffer stringBuffer = new StringBuffer();
            for (byte b : digest) {
                int i = b & 255;
                if (i < 16) {
                    stringBuffer.append("0");
                }
                stringBuffer.append(Integer.toHexString(i));
            }
            return stringBuffer.toString();
        } catch (Throwable th) {
            return "";
        }
    }

    public static String a(String str, String str2) {
        return new String(a(str.getBytes(), str2)).replaceAll("\r\n", "").replaceAll("\n", "");
    }

    private static boolean a(String str, String str2, String str3) {
        return a(str.getBytes(), str2, str3.getBytes());
    }

    private static boolean a(byte[] bArr, String str, byte[] bArr2) {
        try {
            PublicKey generatePublic = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(b.b(bArr)));
            byte[] b = b.b(bArr2);
            Signature instance = Signature.getInstance("SHA256WithRSA");
            instance.initVerify(generatePublic);
            instance.update(str.getBytes());
            return instance.verify(b);
        } catch (Throwable th) {
            return false;
        }
    }

    private static byte[] a(byte[] bArr, String str) {
        try {
            PrivateKey generatePrivate = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(b.b(bArr)));
            Signature instance = Signature.getInstance("SHA256WithRSA");
            instance.initSign(generatePrivate);
            instance.update(str.getBytes());
            return b.a(instance.sign());
        } catch (Throwable th) {
            return null;
        }
    }

    private static byte[] a(byte[] bArr, String str, String str2) {
        try {
            PrivateKey generatePrivate = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(b.b(bArr)));
            Signature instance = Signature.getInstance(str2);
            instance.initSign(generatePrivate);
            instance.update(str.getBytes());
            return b.a(instance.sign());
        } catch (Throwable th) {
            return null;
        }
    }
}
