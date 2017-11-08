package com.huawei.hwid.core.encrypt;

import android.content.Context;
import com.android.gallery3d.gadget.XmlUtils;
import com.huawei.hwid.core.d.b;
import com.huawei.hwid.core.d.k;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class e {
    public static String a(Context context, String str) {
        Throwable e;
        Throwable th;
        String stringBuffer = new StringBuffer().append("PkmJy").append("gVfr").append("Dxs").append(k.e("BccB")).toString();
        String str2 = "";
        try {
            byte[] decode = AES128_ECB_HEX.decode(str, b.c(stringBuffer), 0);
            if (decode != null) {
                stringBuffer = new String(decode, XmlUtils.INPUT_ENCODING);
                try {
                    Arrays.fill(decode, (byte) 0);
                    str2 = stringBuffer;
                } catch (InvalidKeyException e2) {
                    e = e2;
                    com.huawei.hwid.core.d.b.e.d("HwIDEncrypter", "InvalidKeyException  ", e);
                    return stringBuffer;
                } catch (BadPaddingException e3) {
                    e = e3;
                    com.huawei.hwid.core.d.b.e.d("HwIDEncrypter", "BadPaddingException  ", e);
                    return stringBuffer;
                } catch (IllegalBlockSizeException e4) {
                    e = e4;
                    com.huawei.hwid.core.d.b.e.d("HwIDEncrypter", "IllegalBlockSizeException ", e);
                    return stringBuffer;
                } catch (NoSuchAlgorithmException e5) {
                    e = e5;
                    com.huawei.hwid.core.d.b.e.d("HwIDEncrypter", "NoSuchAlgorithmException ", e);
                    return stringBuffer;
                } catch (NoSuchPaddingException e6) {
                    e = e6;
                    com.huawei.hwid.core.d.b.e.d("HwIDEncrypter", "NoSuchPaddingException ", e);
                    return stringBuffer;
                } catch (UnsupportedEncodingException e7) {
                    e = e7;
                    com.huawei.hwid.core.d.b.e.d("HwIDEncrypter", "UnsupportedEncodingException ", e);
                    return stringBuffer;
                }
            }
            return str2;
        } catch (Throwable e8) {
            th = e8;
            stringBuffer = str2;
            e = th;
            com.huawei.hwid.core.d.b.e.d("HwIDEncrypter", "InvalidKeyException  ", e);
            return stringBuffer;
        } catch (Throwable e82) {
            th = e82;
            stringBuffer = str2;
            e = th;
            com.huawei.hwid.core.d.b.e.d("HwIDEncrypter", "BadPaddingException  ", e);
            return stringBuffer;
        } catch (Throwable e822) {
            th = e822;
            stringBuffer = str2;
            e = th;
            com.huawei.hwid.core.d.b.e.d("HwIDEncrypter", "IllegalBlockSizeException ", e);
            return stringBuffer;
        } catch (Throwable e8222) {
            th = e8222;
            stringBuffer = str2;
            e = th;
            com.huawei.hwid.core.d.b.e.d("HwIDEncrypter", "NoSuchAlgorithmException ", e);
            return stringBuffer;
        } catch (Throwable e82222) {
            th = e82222;
            stringBuffer = str2;
            e = th;
            com.huawei.hwid.core.d.b.e.d("HwIDEncrypter", "NoSuchPaddingException ", e);
            return stringBuffer;
        } catch (Throwable e822222) {
            th = e822222;
            stringBuffer = str2;
            e = th;
            com.huawei.hwid.core.d.b.e.d("HwIDEncrypter", "UnsupportedEncodingException ", e);
            return stringBuffer;
        }
    }

    public static String b(Context context, String str) {
        return a.a(context, str);
    }

    public static String c(Context context, String str) {
        return a.b(context, str);
    }

    public static String d(Context context, String str) {
        String stringBuffer = new StringBuffer().append("PkmJy").append("gVfr").append("Dxs").append(k.e("BccB")).toString();
        String str2 = null;
        try {
            str2 = c.a(b.c(str), b.c(stringBuffer));
        } catch (InvalidKeyException e) {
            com.huawei.hwid.core.d.b.e.d("HwIDEncrypter", "InvalidKeyException ");
        } catch (BadPaddingException e2) {
            com.huawei.hwid.core.d.b.e.d("HwIDEncrypter", "BadPaddingException ");
        } catch (IllegalBlockSizeException e3) {
            com.huawei.hwid.core.d.b.e.d("HwIDEncrypter", "IllegalBlockSizeException ");
        } catch (NoSuchAlgorithmException e4) {
            com.huawei.hwid.core.d.b.e.d("HwIDEncrypter", "NoSuchAlgorithmException ");
        } catch (NoSuchPaddingException e5) {
            com.huawei.hwid.core.d.b.e.d("HwIDEncrypter", "NoSuchPaddingException ");
        } catch (InvalidAlgorithmParameterException e6) {
            com.huawei.hwid.core.d.b.e.d("HwIDEncrypter", "NoSuchPaddingException ");
        }
        return str2;
    }
}
