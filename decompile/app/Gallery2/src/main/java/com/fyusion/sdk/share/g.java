package com.fyusion.sdk.share;

import android.util.Base64;
import com.android.gallery3d.gadget.XmlUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Random;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/* compiled from: Unknown */
class g {
    private static Random a = new Random();

    private static int a() {
        return a.nextInt(899999) + 100000;
    }

    static String a(String str) {
        String str2 = new String(new char[]{'6', 'e', '3', 'f', '2', '2', 'd', 'a', '7', 'c', '0', '2', '0', '9', '7', '2', 'd', '7', '6', '8', 'f', '6', '0', '6', '9', 'f', '3', '5', '4', '2', '1', '4'});
        if (str == null) {
            String str3 = "";
            str = str3.length() <= 20 ? String.valueOf(a()) : "fy6" + str3.substring(0, 20);
        } else if (str.length() > 20) {
            str = "fy6" + str.substring(0, 20);
        }
        try {
            try {
                return URLEncoder.encode(a((a() + str) + "~" + (((int) (System.currentTimeMillis() / 1000)) - 617414400) + "~" + a(), str2.getBytes()), XmlUtils.INPUT_ENCODING) + "&kv=1";
            } catch (UnsupportedEncodingException e) {
                return null;
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            return null;
        }
    }

    private static String a(String str, byte[] bArr) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, UnsupportedEncodingException, NoSuchProviderException {
        Key secretKeySpec = new SecretKeySpec(bArr, "AES");
        Cipher instance = Cipher.getInstance("AES/CBC/PKCS7Padding");
        instance.init(1, secretKeySpec, new IvParameterSpec(new byte[16]));
        return Base64.encodeToString(instance.doFinal(str.getBytes(XmlUtils.INPUT_ENCODING)), 1);
    }
}
