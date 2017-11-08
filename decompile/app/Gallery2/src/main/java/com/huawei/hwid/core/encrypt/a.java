package com.huawei.hwid.core.encrypt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import com.android.gallery3d.gadget.XmlUtils;
import com.huawei.hwid.core.constants.HwAccountConstants;
import com.huawei.hwid.core.d.b;
import com.huawei.hwid.core.d.b.e;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class a {
    @SuppressLint({"TrulyRandom"})
    public static String a(Context context, String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        try {
            Key secretKeySpec = new SecretKeySpec(a(context), "AES");
            Cipher instance = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] bArr = new byte[16];
            new SecureRandom().nextBytes(bArr);
            instance.init(1, secretKeySpec, new IvParameterSpec(bArr));
            return a(b.a(bArr), b.a(instance.doFinal(str.getBytes(XmlUtils.INPUT_ENCODING))));
        } catch (Throwable e) {
            e.d("AES128_CBC", "aes cbc encrypter data error", e);
            return "";
        } catch (Throwable e2) {
            e.d("AES128_CBC", "aes cbc encrypter data error", e2);
            return "";
        }
    }

    public static String b(Context context, String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        try {
            return a(context, str, a(context));
        } catch (Throwable e) {
            e.d("AES128_CBC", "aes cbc decrypter data error", e);
            return c(context, str);
        }
    }

    private static String c(Context context, String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        try {
            return a(context, str, b(context));
        } catch (Throwable e) {
            e.d("AES128_CBC", "aes cbc decrypter data error again", e);
            return "";
        }
    }

    private static String a(Context context, String str, byte[] bArr) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        Key secretKeySpec = new SecretKeySpec(bArr, "AES");
        Cipher instance = Cipher.getInstance("AES/CBC/PKCS5Padding");
        String a = a(str);
        String b = b(str);
        if (TextUtils.isEmpty(a) || TextUtils.isEmpty(b)) {
            e.b("AES128_CBC", "ivParameter or encrypedWord is null");
            return "";
        }
        instance.init(2, secretKeySpec, new IvParameterSpec(b.a(a)));
        return new String(instance.doFinal(b.a(b)), XmlUtils.INPUT_ENCODING);
    }

    public static byte[] a(Context context) {
        byte[] a = b.a(HwAccountConstants.a());
        byte[] a2 = b.a(b.c());
        return a(a(a(a(a, -4), a2), 6), b.a(com.huawei.hwid.core.d.a.a()));
    }

    private static byte[] b(Context context) {
        byte[] a = b.a(HwAccountConstants.a());
        byte[] a2 = b.a(b.c());
        return a(a(a(a, a2), b.a(com.huawei.hwid.core.d.a.a())));
    }

    private static byte[] a(byte[] bArr) {
        if (bArr == null) {
            return bArr;
        }
        for (int i = 0; i < bArr.length; i++) {
            bArr[i] = (byte) ((byte) (bArr[i] >> 2));
        }
        return bArr;
    }

    private static byte[] a(byte[] bArr, byte[] bArr2) {
        if (bArr == null || bArr2 == null) {
            return null;
        }
        int length = bArr.length;
        if (length != bArr2.length) {
            return null;
        }
        byte[] bArr3 = new byte[length];
        for (int i = 0; i < length; i++) {
            bArr3[i] = (byte) ((byte) (bArr[i] ^ bArr2[i]));
        }
        return bArr3;
    }

    private static byte[] a(byte[] bArr, int i) {
        if (bArr == null) {
            return bArr;
        }
        for (int i2 = 0; i2 < bArr.length; i2++) {
            if (i >= 0) {
                bArr[i2] = (byte) ((byte) (bArr[i2] >> i));
            } else {
                bArr[i2] = (byte) ((byte) (bArr[i2] << (-i)));
            }
        }
        return bArr;
    }

    private static String a(String str, String str2) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            return "";
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(str2.substring(0, 6));
            stringBuffer.append(str.substring(0, 6));
            stringBuffer.append(str2.substring(6, 10));
            stringBuffer.append(str.substring(6, 16));
            stringBuffer.append(str2.substring(10, 16));
            stringBuffer.append(str.substring(16));
            stringBuffer.append(str2.substring(16));
            return stringBuffer.toString();
        } catch (Exception e) {
            e.d("AES128_CBC", e.getMessage());
            return "";
        }
    }

    private static String a(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(str.substring(6, 12));
            stringBuffer.append(str.substring(16, 26));
            stringBuffer.append(str.substring(32, 48));
            return stringBuffer.toString();
        } catch (Exception e) {
            e.d("AES128_CBC", e.getMessage());
            return "";
        }
    }

    private static String b(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(str.substring(0, 6));
            stringBuffer.append(str.substring(12, 16));
            stringBuffer.append(str.substring(26, 32));
            stringBuffer.append(str.substring(48));
            return stringBuffer.toString();
        } catch (Exception e) {
            e.d("AES128_CBC", e.getMessage());
            return "";
        }
    }
}
