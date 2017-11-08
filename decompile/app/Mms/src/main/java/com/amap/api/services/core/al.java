package com.amap.api.services.core;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.TextUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;

/* compiled from: ClientInfo */
public class al {
    public static String a(Context context, ar arVar, Map<String, String> map, boolean z) {
        try {
            byte[] a;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            a(byteArrayOutputStream, an.q(context));
            a(byteArrayOutputStream, an.i(context));
            String f = an.f(context);
            if (f == null) {
                f = "";
            }
            a(byteArrayOutputStream, f);
            a(byteArrayOutputStream, aj.c(context));
            a(byteArrayOutputStream, Build.MODEL);
            a(byteArrayOutputStream, Build.MANUFACTURER);
            a(byteArrayOutputStream, Build.DEVICE);
            a(byteArrayOutputStream, aj.b(context));
            a(byteArrayOutputStream, aj.d(context));
            a(byteArrayOutputStream, String.valueOf(VERSION.SDK_INT));
            a(byteArrayOutputStream, an.r(context));
            a(byteArrayOutputStream, an.p(context));
            a(byteArrayOutputStream, an.m(context) + "");
            a(byteArrayOutputStream, an.l(context) + "");
            a(byteArrayOutputStream, an.s(context));
            a(byteArrayOutputStream, an.k(context));
            if (z) {
                a(byteArrayOutputStream, "");
            } else {
                a(byteArrayOutputStream, an.h(context));
            }
            if (z) {
                a(byteArrayOutputStream, "");
            } else {
                a(byteArrayOutputStream, an.g(context));
            }
            if (z) {
                a(byteArrayOutputStream, "");
                a(byteArrayOutputStream, "");
            } else {
                String[] j = an.j(context);
                a(byteArrayOutputStream, j[0]);
                a(byteArrayOutputStream, j[1]);
            }
            byte[] a2 = as.a(byteArrayOutputStream.toByteArray());
            Key a3 = as.a(context);
            if (a2.length <= 117) {
                a = ao.a(a2, a3);
            } else {
                byte[] bArr = new byte[117];
                System.arraycopy(a2, 0, bArr, 0, 117);
                Object a4 = ao.a(bArr, a3);
                a = new byte[((a2.length + 128) - 117)];
                System.arraycopy(a4, 0, a, 0, 128);
                System.arraycopy(a2, 117, a, 128, a2.length - 117);
            }
            return ao.b(a);
        } catch (Throwable th) {
            ay.a(th, "CInfo", "InitXInfo");
            return null;
        }
    }

    static String a(Context context, byte[] bArr) throws InvalidKeyException, IOException, InvalidKeySpecException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, CertificateException {
        KeyGenerator instance = KeyGenerator.getInstance("AES");
        if (instance == null) {
            return null;
        }
        instance.init(256);
        byte[] encoded = instance.generateKey().getEncoded();
        Key a = as.a(context);
        if (a == null) {
            return null;
        }
        Object a2 = ao.a(encoded, a);
        Object a3 = ao.a(encoded, bArr);
        byte[] bArr2 = new byte[(a2.length + a3.length)];
        System.arraycopy(a2, 0, bArr2, 0, a2.length);
        System.arraycopy(a3, 0, bArr2, a2.length, a3.length);
        encoded = as.a(bArr2);
        if (encoded == null) {
            return "";
        }
        return ao.b(encoded);
    }

    public static String b(Context context, byte[] bArr) {
        try {
            return a(context, bArr);
        } catch (Throwable e) {
            ay.a(e, "CInfo", "AESData");
            return "";
        } catch (Throwable e2) {
            ay.a(e2, "CInfo", "AESData");
            return "";
        } catch (Throwable e22) {
            ay.a(e22, "CInfo", "AESData");
            return "";
        } catch (Throwable e222) {
            ay.a(e222, "CInfo", "AESData");
            return "";
        } catch (Throwable e2222) {
            ay.a(e2222, "CInfo", "AESData");
            return "";
        } catch (Throwable e22222) {
            ay.a(e22222, "CInfo", "AESData");
            return "";
        } catch (Throwable e222222) {
            ay.a(e222222, "CInfo", "AESData");
            return "";
        } catch (Throwable e2222222) {
            ay.a(e2222222, "CInfo", "AESData");
            return "";
        } catch (Throwable e22222222) {
            ay.a(e22222222, "CInfo", "AESData");
            return "";
        }
    }

    public static String a(Context context, ar arVar) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append("\"sim\":\"").append(an.e(context)).append("\",\"sdkversion\":\"").append(arVar.a).append("\",\"product\":\"").append(arVar.c).append("\",\"ed\":\"").append(arVar.d()).append("\",\"nt\":\"").append(an.c(context)).append("\",\"np\":\"").append(an.a(context)).append("\",\"mnc\":\"").append(an.b(context)).append("\",\"ant\":\"").append(an.d(context)).append("\"");
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private static void a(ByteArrayOutputStream byteArrayOutputStream, String str) {
        if (TextUtils.isEmpty(str)) {
            a(byteArrayOutputStream, (byte) 0, new byte[0]);
            return;
        }
        byte length;
        if (str.getBytes().length <= 255) {
            length = (byte) str.getBytes().length;
        } else {
            length = (byte) -1;
        }
        try {
            a(byteArrayOutputStream, length, str.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            a(byteArrayOutputStream, length, str.getBytes());
        }
    }

    private static void a(ByteArrayOutputStream byteArrayOutputStream, byte b, byte[] bArr) {
        int i = 0;
        try {
            byteArrayOutputStream.write(new byte[]{(byte) b});
            int i2 = b <= (byte) 0 ? 0 : 1;
            if ((b & 255) < 255) {
                i = 1;
            }
            if ((i & i2) != 0) {
                byteArrayOutputStream.write(bArr);
            } else if ((b & 255) == 255) {
                byteArrayOutputStream.write(bArr, 0, 255);
            }
        } catch (Throwable e) {
            ay.a(e, "CInfo", "writeField");
        }
    }

    public static String a() {
        String str = null;
        try {
            str = String.valueOf(System.currentTimeMillis());
            int length = str.length();
            str = str.substring(0, length - 2) + "1" + str.substring(length - 1);
        } catch (Throwable th) {
            ay.a(th, "CInfo", "getTS");
        }
        return str;
    }

    public static String a(Context context, String str, String str2) {
        try {
            return ap.a(aj.e(context) + ":" + str.substring(0, str.length() - 3) + ":" + str2);
        } catch (Throwable th) {
            ay.a(th, "CInfo", "Scode");
            return null;
        }
    }
}
