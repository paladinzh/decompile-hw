package com.huawei.hwid.core.encrypt;

import com.android.gallery3d.gadget.XmlUtils;
import com.huawei.hwid.core.d.b.e;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;

public class d {
    public static String a(String str, String str2) {
        try {
            Key secretKeySpec = new SecretKeySpec(str2.getBytes(XmlUtils.INPUT_ENCODING), "HmacSHA256");
            Mac instance = Mac.getInstance(secretKeySpec.getAlgorithm());
            instance.init(secretKeySpec);
            return new String(new Hex().encode(instance.doFinal(str.getBytes(XmlUtils.INPUT_ENCODING))), XmlUtils.INPUT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            e.d("HMACSHA256", e.getMessage());
            return null;
        } catch (InvalidKeyException e2) {
            e.d("HMACSHA256", e2.getMessage());
            return null;
        } catch (NoSuchAlgorithmException e3) {
            e.d("HMACSHA256", e3.getMessage());
            return null;
        }
    }
}
