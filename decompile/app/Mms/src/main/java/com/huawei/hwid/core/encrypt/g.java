package com.huawei.hwid.core.encrypt;

import com.huawei.hwid.core.c.b.a;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/* compiled from: SHA256 */
public class g {
    public static String a(String str) {
        try {
            MessageDigest instance = MessageDigest.getInstance("SHA-256");
            instance.update(str.getBytes("UTF-8"));
            byte[] digest = instance.digest();
            StringBuffer stringBuffer = new StringBuffer(40);
            for (byte b : digest) {
                int i = b & 255;
                if (i < 16) {
                    stringBuffer.append('0');
                }
                stringBuffer.append(Integer.toHexString(i));
            }
            return stringBuffer.toString();
        } catch (NoSuchAlgorithmException e) {
            a.d("SHA256", e.toString());
            return null;
        } catch (UnsupportedEncodingException e2) {
            a.d("SHA256", e2.toString());
            return null;
        } catch (Exception e3) {
            a.d("SHA256", e3.toString());
            return null;
        }
    }
}
