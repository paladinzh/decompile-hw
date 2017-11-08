package com.huawei.hwid.core.encrypt;

import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.p;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/* compiled from: SHA512 */
public class h {
    public static String a(String str) {
        String str2 = str + ":" + new StringBuffer().append("7D8F98E7BB522785").append("F6A10EDFAEDEB663").append("71D3BA3BC921CD6F").append(p.g("40D.3CD3.7.?AD32")).toString();
        try {
            MessageDigest instance = MessageDigest.getInstance("SHA-512");
            instance.update(str2.getBytes("UTF-8"));
            byte[] digest = instance.digest();
            StringBuffer stringBuffer = new StringBuffer(128);
            for (byte b : digest) {
                int i = b & 255;
                if (i < 16) {
                    stringBuffer.append('0');
                }
                stringBuffer.append(Integer.toHexString(i));
            }
            return stringBuffer.toString();
        } catch (NoSuchAlgorithmException e) {
            a.d("SHA-512", e.toString());
            return null;
        } catch (UnsupportedEncodingException e2) {
            a.d("SHA-512", e2.toString());
            return null;
        } catch (Exception e3) {
            a.d("SHA-512", e3.toString());
            return null;
        }
    }
}
