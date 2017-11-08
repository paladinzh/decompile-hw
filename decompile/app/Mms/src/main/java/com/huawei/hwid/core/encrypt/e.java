package com.huawei.hwid.core.encrypt;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.p;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/* compiled from: HwIDEncrypter */
public class e {
    public static String a(Context context, String str) {
        Throwable e;
        Throwable th;
        String stringBuffer = new StringBuffer().append("PkmJy").append("gVfr").append("Dxs").append(p.g("BccB")).toString();
        String str2 = "";
        try {
            byte[] decode = AES128_ECB_HEX.decode(str, d.e(stringBuffer), 0);
            if (decode != null) {
                stringBuffer = new String(decode, "UTF-8");
                try {
                    Arrays.fill(decode, (byte) 0);
                    str2 = stringBuffer;
                } catch (InvalidKeyException e2) {
                    e = e2;
                    a.d("HwIDEncrypter", "InvalidKeyException  ", e);
                    return stringBuffer;
                } catch (BadPaddingException e3) {
                    e = e3;
                    a.d("HwIDEncrypter", "BadPaddingException  ", e);
                    return stringBuffer;
                } catch (IllegalBlockSizeException e4) {
                    e = e4;
                    a.d("HwIDEncrypter", "IllegalBlockSizeException ", e);
                    return stringBuffer;
                } catch (NoSuchAlgorithmException e5) {
                    e = e5;
                    a.d("HwIDEncrypter", "NoSuchAlgorithmException ", e);
                    return stringBuffer;
                } catch (NoSuchPaddingException e6) {
                    e = e6;
                    a.d("HwIDEncrypter", "NoSuchPaddingException ", e);
                    return stringBuffer;
                } catch (UnsupportedEncodingException e7) {
                    e = e7;
                    a.d("HwIDEncrypter", "UnsupportedEncodingException ", e);
                    return stringBuffer;
                }
            }
            return str2;
        } catch (Throwable e8) {
            th = e8;
            stringBuffer = str2;
            e = th;
            a.d("HwIDEncrypter", "InvalidKeyException  ", e);
            return stringBuffer;
        } catch (Throwable e82) {
            th = e82;
            stringBuffer = str2;
            e = th;
            a.d("HwIDEncrypter", "BadPaddingException  ", e);
            return stringBuffer;
        } catch (Throwable e822) {
            th = e822;
            stringBuffer = str2;
            e = th;
            a.d("HwIDEncrypter", "IllegalBlockSizeException ", e);
            return stringBuffer;
        } catch (Throwable e8222) {
            th = e8222;
            stringBuffer = str2;
            e = th;
            a.d("HwIDEncrypter", "NoSuchAlgorithmException ", e);
            return stringBuffer;
        } catch (Throwable e82222) {
            th = e82222;
            stringBuffer = str2;
            e = th;
            a.d("HwIDEncrypter", "NoSuchPaddingException ", e);
            return stringBuffer;
        } catch (Throwable e822222) {
            th = e822222;
            stringBuffer = str2;
            e = th;
            a.d("HwIDEncrypter", "UnsupportedEncodingException ", e);
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
        try {
            return c.a(d.e(str), d.e(new StringBuffer().append("PkmJy").append("gVfr").append("Dxs").append(p.g("BccB")).toString()));
        } catch (Throwable e) {
            a.d("HwIDEncrypter", "InvalidKeyException ", e);
            return null;
        } catch (Throwable e2) {
            a.d("HwIDEncrypter", "BadPaddingException ", e2);
            return null;
        } catch (Throwable e22) {
            a.d("HwIDEncrypter", "IllegalBlockSizeException ", e22);
            return null;
        } catch (Throwable e222) {
            a.d("HwIDEncrypter", "NoSuchAlgorithmException ", e222);
            return null;
        } catch (Throwable e2222) {
            a.d("HwIDEncrypter", "NoSuchPaddingException ", e2222);
            return null;
        } catch (Throwable e22222) {
            a.d("HwIDEncrypter", "NoSuchPaddingException ", e22222);
            return null;
        }
    }

    public static String e(Context context, String str) {
        Throwable e;
        Throwable th;
        String stringBuffer = new StringBuffer().append("PkmJy").append("gVfr").append("Dxs").append(p.g("BccB")).toString();
        String str2 = "";
        try {
            String[] split = str.trim().split(":");
            if (split == null || split.length != 2 || TextUtils.isEmpty(split[0]) || TextUtils.isEmpty(split[1])) {
                return str2;
            }
            byte[] a = c.a(split[1], split[0], d.e(stringBuffer));
            if (a != null) {
                stringBuffer = new String(a, "UTF-8");
                try {
                    Arrays.fill(a, (byte) 0);
                    str2 = stringBuffer;
                } catch (InvalidKeyException e2) {
                    e = e2;
                    a.d("HwIDEncrypter", "InvalidKeyException  ", e);
                    return stringBuffer;
                } catch (BadPaddingException e3) {
                    e = e3;
                    a.d("HwIDEncrypter", "BadPaddingException  ", e);
                    return stringBuffer;
                } catch (IllegalBlockSizeException e4) {
                    e = e4;
                    a.d("HwIDEncrypter", "IllegalBlockSizeException ", e);
                    return stringBuffer;
                } catch (NoSuchAlgorithmException e5) {
                    e = e5;
                    a.d("HwIDEncrypter", "NoSuchAlgorithmException ", e);
                    return stringBuffer;
                } catch (NoSuchPaddingException e6) {
                    e = e6;
                    a.d("HwIDEncrypter", "NoSuchPaddingException ", e);
                    return stringBuffer;
                } catch (UnsupportedEncodingException e7) {
                    e = e7;
                    a.d("HwIDEncrypter", "UnsupportedEncodingException ", e);
                    return stringBuffer;
                } catch (InvalidAlgorithmParameterException e8) {
                    e = e8;
                    a.d("HwIDEncrypter", "UnsupportedEncodingException ", e);
                    return stringBuffer;
                }
            }
            stringBuffer = str2;
            return stringBuffer;
        } catch (Throwable e9) {
            th = e9;
            stringBuffer = str2;
            e = th;
            a.d("HwIDEncrypter", "InvalidKeyException  ", e);
            return stringBuffer;
        } catch (Throwable e92) {
            th = e92;
            stringBuffer = str2;
            e = th;
            a.d("HwIDEncrypter", "BadPaddingException  ", e);
            return stringBuffer;
        } catch (Throwable e922) {
            th = e922;
            stringBuffer = str2;
            e = th;
            a.d("HwIDEncrypter", "IllegalBlockSizeException ", e);
            return stringBuffer;
        } catch (Throwable e9222) {
            th = e9222;
            stringBuffer = str2;
            e = th;
            a.d("HwIDEncrypter", "NoSuchAlgorithmException ", e);
            return stringBuffer;
        } catch (Throwable e92222) {
            th = e92222;
            stringBuffer = str2;
            e = th;
            a.d("HwIDEncrypter", "NoSuchPaddingException ", e);
            return stringBuffer;
        } catch (Throwable e922222) {
            th = e922222;
            stringBuffer = str2;
            e = th;
            a.d("HwIDEncrypter", "UnsupportedEncodingException ", e);
            return stringBuffer;
        } catch (Throwable e9222222) {
            th = e9222222;
            stringBuffer = str2;
            e = th;
            a.d("HwIDEncrypter", "UnsupportedEncodingException ", e);
            return stringBuffer;
        }
    }
}
