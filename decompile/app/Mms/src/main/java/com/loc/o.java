package com.loc;

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
public class o {
    public static String a() {
        String str = null;
        try {
            str = String.valueOf(System.currentTimeMillis());
            int length = str.length();
            str = str.substring(0, length - 2) + "1" + str.substring(length - 1);
        } catch (Throwable th) {
            aa.a(th, "CInfo", "getTS");
        }
        return str;
    }

    public static String a(Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append("\"key\":\"").append(m.f(context)).append("\",\"platform\":\"android\",\"diu\":\"").append(q.q(context)).append("\",\"pkg\":\"").append(m.c(context)).append("\",\"model\":\"").append(Build.MODEL).append("\",\"appname\":\"").append(m.b(context)).append("\",\"appversion\":\"").append(m.d(context)).append("\",\"sysversion\":\"").append(VERSION.RELEASE).append("\",");
        } catch (Throwable th) {
            aa.a(th, "CInfo", "getPublicJSONInfo");
        }
        return stringBuilder.toString();
    }

    public static String a(Context context, v vVar) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append("\"sim\":\"").append(q.e(context)).append("\",\"sdkversion\":\"").append(vVar.a).append("\",\"product\":\"").append(vVar.c).append("\",\"ed\":\"").append(vVar.d()).append("\",\"nt\":\"").append(q.c(context)).append("\",\"np\":\"").append(q.a(context)).append("\",\"mnc\":\"").append(q.b(context)).append("\",\"ant\":\"").append(q.d(context)).append("\"");
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static String a(Context context, v vVar, Map<String, String> map, boolean z) {
        try {
            byte[] a;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            a(byteArrayOutputStream, q.q(context));
            a(byteArrayOutputStream, q.i(context));
            String f = q.f(context);
            if (f == null) {
                f = "";
            }
            a(byteArrayOutputStream, f);
            a(byteArrayOutputStream, m.c(context));
            a(byteArrayOutputStream, Build.MODEL);
            a(byteArrayOutputStream, Build.MANUFACTURER);
            a(byteArrayOutputStream, Build.DEVICE);
            a(byteArrayOutputStream, m.b(context));
            a(byteArrayOutputStream, m.d(context));
            a(byteArrayOutputStream, String.valueOf(VERSION.SDK_INT));
            a(byteArrayOutputStream, q.r(context));
            a(byteArrayOutputStream, q.p(context));
            a(byteArrayOutputStream, q.m(context) + "");
            a(byteArrayOutputStream, q.l(context) + "");
            a(byteArrayOutputStream, q.s(context));
            a(byteArrayOutputStream, q.k(context));
            if (z) {
                a(byteArrayOutputStream, "");
            } else {
                a(byteArrayOutputStream, q.h(context));
            }
            if (z) {
                a(byteArrayOutputStream, "");
            } else {
                a(byteArrayOutputStream, q.g(context));
            }
            if (z) {
                a(byteArrayOutputStream, "");
                a(byteArrayOutputStream, "");
            } else {
                String[] j = q.j(context);
                a(byteArrayOutputStream, j[0]);
                a(byteArrayOutputStream, j[1]);
            }
            byte[] a2 = w.a(byteArrayOutputStream.toByteArray());
            Key a3 = w.a(context);
            if (a2.length <= 117) {
                a = r.a(a2, a3);
            } else {
                byte[] bArr = new byte[117];
                System.arraycopy(a2, 0, bArr, 0, 117);
                Object a4 = r.a(bArr, a3);
                a = new byte[((a2.length + 128) - 117)];
                System.arraycopy(a4, 0, a, 0, 128);
                System.arraycopy(a2, 117, a, 128, a2.length - 117);
            }
            return r.b(a);
        } catch (Throwable th) {
            aa.a(th, "CInfo", "InitXInfo");
            return null;
        }
    }

    public static String a(Context context, String str, String str2) {
        try {
            return s.b(m.e(context) + ":" + str.substring(0, str.length() - 3) + ":" + str2);
        } catch (Throwable th) {
            aa.a(th, "CInfo", "Scode");
            return null;
        }
    }

    public static String a(Context context, byte[] bArr) {
        try {
            return b(context, bArr);
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
            aa.a(e, "CInfo", "writeField");
        }
    }

    private static void a(ByteArrayOutputStream byteArrayOutputStream, String str) {
        if (TextUtils.isEmpty(str)) {
            a(byteArrayOutputStream, (byte) 0, new byte[0]);
            return;
        }
        byte length = str.getBytes().length <= 255 ? (byte) str.getBytes().length : (byte) -1;
        try {
            a(byteArrayOutputStream, length, str.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            a(byteArrayOutputStream, length, str.getBytes());
        }
    }

    static String b(Context context, byte[] bArr) throws InvalidKeyException, IOException, InvalidKeySpecException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, CertificateException {
        KeyGenerator instance = KeyGenerator.getInstance("AES");
        if (instance == null) {
            return null;
        }
        instance.init(256);
        byte[] encoded = instance.generateKey().getEncoded();
        Key a = w.a(context);
        if (a == null) {
            return null;
        }
        Object a2 = r.a(encoded, a);
        Object a3 = r.a(encoded, bArr);
        byte[] bArr2 = new byte[(a2.length + a3.length)];
        System.arraycopy(a2, 0, bArr2, 0, a2.length);
        System.arraycopy(a3, 0, bArr2, a2.length, a3.length);
        encoded = w.a(bArr2);
        return encoded == null ? "" : r.b(encoded);
    }

    public static String c(Context context, byte[] bArr) {
        try {
            return b(context, bArr);
        } catch (Throwable e) {
            aa.a(e, "CInfo", "AESData");
            return "";
        } catch (Throwable e2) {
            aa.a(e2, "CInfo", "AESData");
            return "";
        } catch (Throwable e22) {
            aa.a(e22, "CInfo", "AESData");
            return "";
        } catch (Throwable e222) {
            aa.a(e222, "CInfo", "AESData");
            return "";
        } catch (Throwable e2222) {
            aa.a(e2222, "CInfo", "AESData");
            return "";
        } catch (Throwable e22222) {
            aa.a(e22222, "CInfo", "AESData");
            return "";
        } catch (Throwable e222222) {
            aa.a(e222222, "CInfo", "AESData");
            return "";
        } catch (Throwable e2222222) {
            aa.a(e2222222, "CInfo", "AESData");
            return "";
        } catch (Throwable e22222222) {
            aa.a(e22222222, "CInfo", "AESData");
            return "";
        }
    }
}
