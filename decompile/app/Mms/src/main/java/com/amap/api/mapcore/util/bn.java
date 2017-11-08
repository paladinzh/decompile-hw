package com.amap.api.mapcore.util;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.TextUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
public class bn {

    /* compiled from: ClientInfo */
    private static class a {
        String a;
        String b;
        String c;
        String d;
        String e;
        String f;
        String g;
        String h;
        String i;
        String j;
        String k;
        String l;
        String m;
        String n;
        String o;
        String p;
        String q;
        String r;
        String s;
        String t;

        private a() {
        }
    }

    public static String a(Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append("\"key\":\"").append(bl.f(context)).append("\",\"platform\":\"android\",\"diu\":\"").append(bq.q(context)).append("\",\"pkg\":\"").append(bl.c(context)).append("\",\"model\":\"").append(Build.MODEL).append("\",\"appname\":\"").append(bl.b(context)).append("\",\"appversion\":\"").append(bl.d(context)).append("\",\"sysversion\":\"").append(VERSION.RELEASE).append("\",");
        } catch (Throwable th) {
            cb.a(th, "CInfo", "getPublicJSONInfo");
        }
        return stringBuilder.toString();
    }

    public static String a(Context context, bv bvVar, Map<String, String> map, boolean z) {
        try {
            a aVar = new a();
            aVar.a = bq.q(context);
            aVar.b = bq.i(context);
            String f = bq.f(context);
            if (f == null) {
                f = "";
            }
            aVar.c = f;
            aVar.d = bl.c(context);
            aVar.e = Build.MODEL;
            aVar.f = Build.MANUFACTURER;
            aVar.g = Build.DEVICE;
            aVar.h = bl.b(context);
            aVar.i = bl.d(context);
            aVar.j = String.valueOf(VERSION.SDK_INT);
            aVar.k = bq.r(context);
            aVar.l = bq.p(context);
            aVar.m = bq.m(context) + "";
            aVar.n = bq.l(context) + "";
            aVar.o = bq.s(context);
            aVar.p = bq.k(context);
            if (z) {
                aVar.q = "";
            } else {
                aVar.q = bq.h(context);
            }
            if (z) {
                aVar.r = "";
            } else {
                aVar.r = bq.g(context);
            }
            if (z) {
                aVar.s = "";
                aVar.t = "";
            } else {
                String[] j = bq.j(context);
                aVar.s = j[0];
                aVar.t = j[1];
            }
            return a(context, aVar);
        } catch (Throwable th) {
            cb.a(th, "CInfo", "InitXInfo");
            return null;
        }
    }

    public static String a(Context context, byte[] bArr) {
        try {
            return e(context, bArr);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return "";
        } catch (NoSuchAlgorithmException e2) {
            e2.printStackTrace();
            return "";
        } catch (NoSuchPaddingException e3) {
            e3.printStackTrace();
            return "";
        } catch (IllegalBlockSizeException e4) {
            e4.printStackTrace();
            return "";
        } catch (BadPaddingException e5) {
            e5.printStackTrace();
            return "";
        } catch (InvalidKeySpecException e6) {
            e6.printStackTrace();
            return "";
        } catch (CertificateException e7) {
            e7.printStackTrace();
            return "";
        } catch (IOException e8) {
            e8.printStackTrace();
            return "";
        } catch (Throwable th) {
            th.printStackTrace();
            return "";
        }
    }

    public static byte[] b(Context context, byte[] bArr) throws CertificateException, InvalidKeySpecException, NoSuchAlgorithmException, NullPointerException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        Key a = bx.a(context);
        if (bArr.length <= 117) {
            return br.a(bArr, a);
        }
        byte[] bArr2 = new byte[117];
        System.arraycopy(bArr, 0, bArr2, 0, 117);
        Object a2 = br.a(bArr2, a);
        byte[] bArr3 = new byte[((bArr.length + 128) - 117)];
        System.arraycopy(a2, 0, bArr3, 0, 128);
        System.arraycopy(bArr, 117, bArr3, 128, bArr.length - 117);
        return bArr3;
    }

    public static byte[] c(Context context, byte[] bArr) throws CertificateException, InvalidKeySpecException, NoSuchAlgorithmException, NullPointerException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        KeyGenerator instance = KeyGenerator.getInstance("AES");
        if (instance == null) {
            return null;
        }
        instance.init(256);
        byte[] encoded = instance.generateKey().getEncoded();
        Key a = bx.a(context);
        if (a == null) {
            return null;
        }
        Object a2 = br.a(encoded, a);
        Object a3 = br.a(encoded, bArr);
        Object obj = new byte[(a2.length + a3.length)];
        System.arraycopy(a2, 0, obj, 0, a2.length);
        System.arraycopy(a3, 0, obj, a2.length, a3.length);
        return obj;
    }

    public static String a() {
        String str = null;
        try {
            str = String.valueOf(System.currentTimeMillis());
            int length = str.length();
            str = str.substring(0, length - 2) + "1" + str.substring(length - 1);
        } catch (Throwable th) {
            cb.a(th, "CInfo", "getTS");
        }
        return str;
    }

    public static String a(Context context, String str, String str2) {
        try {
            return bs.b(bl.e(context) + ":" + str.substring(0, str.length() - 3) + ":" + str2);
        } catch (Throwable th) {
            cb.a(th, "CInfo", "Scode");
            return null;
        }
    }

    public static String d(Context context, byte[] bArr) {
        try {
            return e(context, bArr);
        } catch (Throwable e) {
            cb.a(e, "CInfo", "AESData");
            return "";
        } catch (Throwable e2) {
            cb.a(e2, "CInfo", "AESData");
            return "";
        } catch (Throwable e22) {
            cb.a(e22, "CInfo", "AESData");
            return "";
        } catch (Throwable e222) {
            cb.a(e222, "CInfo", "AESData");
            return "";
        } catch (Throwable e2222) {
            cb.a(e2222, "CInfo", "AESData");
            return "";
        } catch (Throwable e22222) {
            cb.a(e22222, "CInfo", "AESData");
            return "";
        } catch (Throwable e222222) {
            cb.a(e222222, "CInfo", "AESData");
            return "";
        } catch (Throwable e2222222) {
            cb.a(e2222222, "CInfo", "AESData");
            return "";
        } catch (Throwable e22222222) {
            cb.a(e22222222, "CInfo", "AESData");
            return "";
        }
    }

    public static String a(Context context, bv bvVar) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append("\"sim\":\"").append(bq.e(context)).append("\",\"sdkversion\":\"").append(bvVar.a).append("\",\"product\":\"").append(bvVar.c).append("\",\"ed\":\"").append(bvVar.d()).append("\",\"nt\":\"").append(bq.c(context)).append("\",\"np\":\"").append(bq.a(context)).append("\",\"mnc\":\"").append(bq.b(context)).append("\",\"ant\":\"").append(bq.d(context)).append("\"");
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private static String a(Context context, a aVar) {
        ByteArrayOutputStream byteArrayOutputStream;
        Throwable th;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                a(byteArrayOutputStream, aVar.a);
                a(byteArrayOutputStream, aVar.b);
                a(byteArrayOutputStream, aVar.c);
                a(byteArrayOutputStream, aVar.d);
                a(byteArrayOutputStream, aVar.e);
                a(byteArrayOutputStream, aVar.f);
                a(byteArrayOutputStream, aVar.g);
                a(byteArrayOutputStream, aVar.h);
                a(byteArrayOutputStream, aVar.i);
                a(byteArrayOutputStream, aVar.j);
                a(byteArrayOutputStream, aVar.k);
                a(byteArrayOutputStream, aVar.l);
                a(byteArrayOutputStream, aVar.m);
                a(byteArrayOutputStream, aVar.n);
                a(byteArrayOutputStream, aVar.o);
                a(byteArrayOutputStream, aVar.p);
                a(byteArrayOutputStream, aVar.q);
                a(byteArrayOutputStream, aVar.r);
                a(byteArrayOutputStream, aVar.s);
                a(byteArrayOutputStream, aVar.t);
                String a = a(context, byteArrayOutputStream);
                if (byteArrayOutputStream != null) {
                    try {
                        byteArrayOutputStream.close();
                    } catch (Throwable th2) {
                        th2.printStackTrace();
                    }
                }
                return a;
            } catch (Throwable th3) {
                th = th3;
                try {
                    cb.a(th, "CInfo", "InitXInfo");
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (Throwable th4) {
                            th4.printStackTrace();
                        }
                    }
                    return null;
                } catch (Throwable th5) {
                    th4 = th5;
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (Throwable th22) {
                            th22.printStackTrace();
                        }
                    }
                    throw th4;
                }
            }
        } catch (Throwable th6) {
            th4 = th6;
            byteArrayOutputStream = null;
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            throw th4;
        }
    }

    private static String a(Context context, ByteArrayOutputStream byteArrayOutputStream) throws CertificateException, NoSuchAlgorithmException, IOException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException {
        return br.b(b(context, bx.b(byteArrayOutputStream.toByteArray())));
    }

    static String e(Context context, byte[] bArr) throws InvalidKeyException, IOException, InvalidKeySpecException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, CertificateException {
        byte[] b = bx.b(c(context, bArr));
        if (b == null) {
            return "";
        }
        return br.b(b);
    }

    private static void a(ByteArrayOutputStream byteArrayOutputStream, String str) {
        if (TextUtils.isEmpty(str)) {
            bx.a(byteArrayOutputStream, (byte) 0, new byte[0]);
            return;
        }
        byte length;
        if (str.getBytes().length <= 255) {
            length = (byte) str.getBytes().length;
        } else {
            length = (byte) -1;
        }
        bx.a(byteArrayOutputStream, length, bx.a(str));
    }
}
