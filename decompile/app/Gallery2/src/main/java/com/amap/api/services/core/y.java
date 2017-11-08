package com.amap.api.services.core;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import java.util.Map.Entry;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;

/* compiled from: ClientInfo */
public class y {
    public static String a(Context context, ad adVar, Map<String, String> map) {
        String b;
        try {
            b = b(context, adVar, map);
            if ("".equals(b)) {
                return null;
            }
            b = b(context, b.getBytes("utf-8"));
            return b;
        } catch (Throwable e) {
            ay.a(e, "CInfo", "rsaInfo");
            e.printStackTrace();
            b = null;
        }
    }

    private static String c(Context context, byte[] bArr) throws InvalidKeyException, IOException, InvalidKeySpecException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, CertificateException {
        KeyGenerator instance = KeyGenerator.getInstance("AES");
        if (instance == null) {
            return null;
        }
        instance.init(256);
        byte[] encoded = instance.generateKey().getEncoded();
        Key a = ae.a(context);
        if (a == null) {
            return null;
        }
        Object a2 = aa.a(encoded, a);
        Object a3 = aa.a(encoded, bArr);
        byte[] bArr2 = new byte[(a2.length + a3.length)];
        System.arraycopy(a2, 0, bArr2, 0, a2.length);
        System.arraycopy(a3, 0, bArr2, a2.length, a3.length);
        encoded = ae.a(bArr2);
        if (encoded == null) {
            return "";
        }
        return aa.a(encoded);
    }

    public static String a(Context context, byte[] bArr) {
        try {
            return c(context, bArr);
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

    public static String b(Context context, byte[] bArr) {
        try {
            return c(context, bArr);
        } catch (Throwable e) {
            ay.a(e, "CInfo", "AESData");
            e.printStackTrace();
            return "";
        } catch (Throwable e2) {
            ay.a(e2, "CInfo", "AESData");
            e2.printStackTrace();
            return "";
        } catch (Throwable e22) {
            ay.a(e22, "CInfo", "AESData");
            e22.printStackTrace();
            return "";
        } catch (Throwable e222) {
            ay.a(e222, "CInfo", "AESData");
            e222.printStackTrace();
            return "";
        } catch (Throwable e2222) {
            ay.a(e2222, "CInfo", "AESData");
            e2222.printStackTrace();
            return "";
        } catch (Throwable e22222) {
            ay.a(e22222, "CInfo", "AESData");
            e22222.printStackTrace();
            return "";
        } catch (Throwable e222222) {
            ay.a(e222222, "CInfo", "AESData");
            e222222.printStackTrace();
            return "";
        } catch (Throwable e2222222) {
            ay.a(e2222222, "CInfo", "AESData");
            e2222222.printStackTrace();
            return "";
        } catch (Throwable e22222222) {
            ay.a(e22222222, "CInfo", "AESData");
            e22222222.printStackTrace();
            return "";
        }
    }

    public static String a(Context context, ad adVar) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append("\"sim\":\"").append(z.q(context)).append("\",\"av\":\"").append(adVar.a).append("\",\"pro\":\"").append(adVar.c).append("\",\"ed\":\"").append(adVar.d()).append("\",\"nt\":\"").append(z.k(context)).append("\",\"np\":\"").append(z.p(context)).append("\",\"mnc\":\"").append(z.e(context)).append("\",\"lnt\":\"").append(z.f(context)).append("\",\"ant\":\"").append(z.h(context)).append("\"");
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static String a(Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append("\"key\":\"").append(w.f(context)).append("\",\"ct\":\"android\",\"ime\":\"").append(z.m(context)).append("\",\"pkg\":\"").append(w.b(context)).append("\",\"mod\":\"").append(Build.MODEL).append("\",\"apn\":\"").append(w.a(context)).append("\",\"apv\":\"").append(w.c(context)).append("\",\"sv\":\"").append(VERSION.RELEASE).append("\",");
        } catch (Throwable th) {
            ay.a(th, "CInfo", "getPublicJSONInfo");
            th.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private static String b(Context context, ad adVar, Map<String, String> map) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            String m = z.m(context);
            stringBuilder.append("ct=android");
            stringBuilder.append("&ime=").append(m);
            stringBuilder.append("&pkg=").append(w.b(context));
            stringBuilder.append("&mod=");
            stringBuilder.append(Build.MODEL);
            stringBuilder.append("&apn=").append(w.a(context));
            stringBuilder.append("&apv=").append(w.c(context));
            stringBuilder.append("&sv=");
            stringBuilder.append(VERSION.RELEASE);
            stringBuilder.append("&sim=").append(z.n(context));
            stringBuilder.append("&av=").append(adVar.a);
            stringBuilder.append("&pro=").append(adVar.c);
            stringBuilder.append("&cid=").append(z.d(context));
            stringBuilder.append("&dmac=").append(z.c(context));
            stringBuilder.append("&wmac=").append(z.b(context));
            stringBuilder.append("&nt=");
            stringBuilder.append(z.l(context));
            m = z.o(context);
            stringBuilder.append("&np=");
            stringBuilder.append(m);
            stringBuilder.append("&ia=1&");
            m = z.a(context);
            if (m == null) {
                m = "";
            }
            stringBuilder.append("utd=").append(m).append("&");
            m = w.f(context);
            if (m != null && m.length() > 0) {
                stringBuilder.append("key=");
                stringBuilder.append(m);
                stringBuilder.append("&");
            }
            stringBuilder.append("ctm=" + System.currentTimeMillis());
            stringBuilder.append("&re=" + z.j(context));
            if (map != null) {
                for (Entry entry : map.entrySet()) {
                    stringBuilder.append("&").append((String) entry.getKey()).append("=").append((String) entry.getValue());
                }
            }
        } catch (Throwable th) {
            ay.a(th, "CInfo", "InitXInfo");
            th.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static String a() {
        String str = null;
        try {
            str = String.valueOf(System.currentTimeMillis());
            int length = str.length();
            str = str.substring(0, length - 2) + "1" + str.substring(length - 1);
        } catch (Throwable th) {
            ay.a(th, "CInfo", "getTS");
            th.printStackTrace();
        }
        return str;
    }

    public static String a(Context context, String str, String str2) {
        try {
            return ab.a(w.d(context) + ":" + str.substring(0, str.length() - 3) + ":" + str2);
        } catch (Throwable th) {
            ay.a(th, "CInfo", "Scode");
            th.printStackTrace();
            return null;
        }
    }
}
