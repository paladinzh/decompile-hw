package com.huawei.hwid.core.encrypt;

import com.huawei.hwid.core.c.b.a;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;

/* compiled from: HMACSHA256 */
public class d {
    public static String a(String str, String str2) {
        try {
            Key secretKeySpec = new SecretKeySpec(str2.getBytes("UTF-8"), "HmacSHA256");
            Mac instance = Mac.getInstance(secretKeySpec.getAlgorithm());
            instance.init(secretKeySpec);
            return new String(new Hex().encode(instance.doFinal(str.getBytes("UTF-8"))), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            a.d("HMACSHA256", e.toString());
            return null;
        } catch (InvalidKeyException e2) {
            a.d("HMACSHA256", e2.toString());
            return null;
        } catch (NoSuchAlgorithmException e3) {
            a.d("HMACSHA256", e3.toString());
            return null;
        }
    }
}
