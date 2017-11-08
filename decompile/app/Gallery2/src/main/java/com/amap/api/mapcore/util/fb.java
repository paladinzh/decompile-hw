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
public class fb {

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

    public static String a(Context context, String str, String str2) {
        try {
            return fe.b(ey.e(context) + ":" + str.substring(0, str.length() - 3) + ":" + str2);
        } catch (Throwable th) {
            fl.a(th, "CInfo", "Scode");
            return null;
        }
    }

    public static String a() {
        String str = null;
        try {
            str = String.valueOf(System.currentTimeMillis());
            int length = str.length();
            str = str.substring(0, length - 2) + "1" + str.substring(length - 1);
        } catch (Throwable th) {
            fl.a(th, "CInfo", "getTS");
        }
        return str;
    }

    public static String a(Context context) {
        try {
            a aVar = new a();
            aVar.d = ey.c(context);
            aVar.i = ey.d(context);
            return a(context, aVar);
        } catch (Throwable th) {
            fl.a(th, "CInfo", "InitXInfo");
            return null;
        }
    }

    public static byte[] a(Context context, byte[] bArr) throws CertificateException, InvalidKeySpecException, NoSuchAlgorithmException, NullPointerException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        KeyGenerator instance = KeyGenerator.getInstance("AES");
        if (instance == null) {
            return null;
        }
        instance.init(256);
        byte[] encoded = instance.generateKey().getEncoded();
        Key a = fi.a(context);
        if (a == null) {
            return null;
        }
        Object a2 = fd.a(encoded, a);
        Object a3 = fd.a(encoded, bArr);
        Object obj = new byte[(a2.length + a3.length)];
        System.arraycopy(a2, 0, obj, 0, a2.length);
        System.arraycopy(a3, 0, obj, a2.length, a3.length);
        return obj;
    }

    public static byte[] a(Context context, boolean z) {
        try {
            return b(context, b(context, z));
        } catch (Throwable th) {
            fl.a(th, "CInfo", "getGZipXInfo");
            return null;
        }
    }

    public static String b(Context context) {
        try {
            return a(context, b(context, false));
        } catch (Throwable th) {
            fl.a(th, "CInfo", "getClientXInfo");
            return null;
        }
    }

    @Deprecated
    public static String a(Context context, fh fhVar, Map<String, String> map, boolean z) {
        try {
            return a(context, b(context, z));
        } catch (Throwable th) {
            fl.a(th, "CInfo", "rsaLocClineInfo");
            return null;
        }
    }

    public static String b(Context context, byte[] bArr) {
        try {
            return d(context, bArr);
        } catch (Throwable th) {
            fl.a(th, "CInfo", "AESData");
            return "";
        }
    }

    public static byte[] c(Context context, byte[] bArr) throws CertificateException, InvalidKeySpecException, NoSuchAlgorithmException, NullPointerException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        Key a = fi.a(context);
        if (bArr.length <= 117) {
            return fd.a(bArr, a);
        }
        byte[] bArr2 = new byte[117];
        System.arraycopy(bArr, 0, bArr2, 0, 117);
        Object a2 = fd.a(bArr2, a);
        byte[] bArr3 = new byte[((bArr.length + 128) - 117)];
        System.arraycopy(a2, 0, bArr3, 0, 128);
        System.arraycopy(bArr, 117, bArr3, 128, bArr.length - 117);
        return bArr3;
    }

    private static String a(Context context, a aVar) {
        return fd.a(b(context, aVar));
    }

    private static byte[] b(Context context, a aVar) {
        Throwable th;
        ByteArrayOutputStream byteArrayOutputStream;
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
                byte[] a = a(context, byteArrayOutputStream);
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
                    fl.a(th, "CInfo", "InitXInfo");
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

    private static byte[] a(Context context, ByteArrayOutputStream byteArrayOutputStream) throws CertificateException, NoSuchAlgorithmException, IOException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException {
        return c(context, fi.b(byteArrayOutputStream.toByteArray()));
    }

    static String d(Context context, byte[] bArr) throws InvalidKeyException, IOException, InvalidKeySpecException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, CertificateException {
        byte[] b = fi.b(a(context, bArr));
        if (b == null) {
            return "";
        }
        return fd.a(b);
    }

    public static void a(ByteArrayOutputStream byteArrayOutputStream, String str) {
        if (TextUtils.isEmpty(str)) {
            fi.a(byteArrayOutputStream, (byte) 0, new byte[0]);
            return;
        }
        byte length;
        if (str.getBytes().length <= 255) {
            length = (byte) str.getBytes().length;
        } else {
            length = (byte) -1;
        }
        fi.a(byteArrayOutputStream, length, fi.a(str));
    }

    public static String e(Context context, byte[] bArr) {
        try {
            return d(context, bArr);
        } catch (Throwable th) {
            th.printStackTrace();
            return "";
        }
    }

    private static a b(Context context, boolean z) {
        a aVar = new a();
        aVar.a = fc.q(context);
        aVar.b = fc.i(context);
        String f = fc.f(context);
        if (f == null) {
            f = "";
        }
        aVar.c = f;
        aVar.d = ey.c(context);
        aVar.e = Build.MODEL;
        aVar.f = Build.MANUFACTURER;
        aVar.g = Build.DEVICE;
        aVar.h = ey.b(context);
        aVar.i = ey.d(context);
        aVar.j = String.valueOf(VERSION.SDK_INT);
        aVar.k = fc.r(context);
        aVar.l = fc.p(context);
        aVar.m = fc.m(context) + "";
        aVar.n = fc.l(context) + "";
        aVar.o = fc.s(context);
        aVar.p = fc.k(context);
        if (z) {
            aVar.q = "";
        } else {
            aVar.q = fc.h(context);
        }
        if (z) {
            aVar.r = "";
        } else {
            aVar.r = fc.g(context);
        }
        if (z) {
            aVar.s = "";
            aVar.t = "";
        } else {
            String[] j = fc.j(context);
            aVar.s = j[0];
            aVar.t = j[1];
        }
        return aVar;
    }
}
