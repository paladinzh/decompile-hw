package com.fyusion.sdk.common;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;
import com.android.gallery3d.gadget.XmlUtils;
import com.fyusion.sdk.common.ext.util.exif.ExifInterface.GpsLongitudeRef;
import java.math.BigInteger;
import java.net.NetworkInterface;
import java.net.URLEncoder;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/* compiled from: Unknown */
public class b {
    static final Long a = b;
    private static final Long b = Long.valueOf(86400000);
    private static String c = null;
    private static String d = null;
    private static final String[] e = new String[]{"HTC One_M8", "Fyusion.UltraPhone"};

    private static String a(int i) {
        return g(String.valueOf(i));
    }

    static String a(String str) {
        Random secureRandom = new SecureRandom();
        String str2 = new BigInteger(130, secureRandom).toString(10) + ((int) (System.currentTimeMillis() / 1000)) + new BigInteger(130, secureRandom).toString(10);
        try {
            long b = b(16);
            Key secretKeySpec = new SecretKeySpec(new StringBuilder(String.valueOf(b)).reverse().toString().getBytes(XmlUtils.INPUT_ENCODING), "HmacSHA256");
            Mac instance = Mac.getInstance(secretKeySpec.getAlgorithm());
            instance.init(secretKeySpec);
            String substring = b(instance.doFinal(str.getBytes(XmlUtils.INPUT_ENCODING))).substring(0, 32);
            Cipher instance2 = Cipher.getInstance("AES/CBC/PKCS7Padding");
            instance2.init(1, new SecretKeySpec(substring.getBytes(XmlUtils.INPUT_ENCODING), "AES"), new IvParameterSpec(String.valueOf(b).getBytes(XmlUtils.INPUT_ENCODING)));
            return URLEncoder.encode(Base64.encodeToString(instance2.doFinal(str2.getBytes(XmlUtils.INPUT_ENCODING)), 1), XmlUtils.INPUT_ENCODING) + "&n=" + b;
        } catch (Throwable e) {
            DLog.e("Auth", "Unable to generate key.", e);
            return null;
        }
    }

    private static String a(byte[] bArr) {
        return Base64.encodeToString(bArr, 0);
    }

    static void a(Long l) {
        Editor edit = FyuseSDK.getContext().getSharedPreferences(g("A_P"), 0).edit();
        edit.putString(g("A_E"), g(l.toString()));
        edit.apply();
    }

    static void a(String str, boolean z) {
        Editor edit = FyuseSDK.getContext().getSharedPreferences(g("A_P"), 0).edit();
        int nextInt = new Random().nextInt(100);
        edit.putString(g(str), !z ? a(nextInt) : a(nextInt * 101));
        f e = e(str);
        if (!(e == null || e.c() == null || e.c().size() <= 0)) {
            for (Entry entry : e.c().entrySet()) {
                edit.putString(g(str + "-" + ((String) entry.getKey())), !((Boolean) entry.getValue()).booleanValue() ? a(nextInt) : a(nextInt * 101));
            }
        }
        edit.apply();
    }

    private static byte[] a(byte[] bArr, byte[] bArr2) {
        byte[] bArr3 = new byte[bArr.length];
        for (int i = 0; i < bArr.length; i++) {
            bArr3[i] = (byte) ((byte) (bArr[i] ^ bArr2[i % bArr2.length]));
        }
        return bArr3;
    }

    public static String[] a() {
        return e;
    }

    private static long b(int i) {
        SecureRandom secureRandom = new SecureRandom();
        long nextInt = (long) (secureRandom.nextInt(9) + 1);
        for (int i2 = 0; i2 < i - 1; i2++) {
            nextInt = (nextInt * 10) + ((long) secureRandom.nextInt(10));
        }
        return nextInt;
    }

    public static String b() {
        return "mo7P9wNPEzv32TnmYqa7V7";
    }

    private static String b(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder(bArr.length * 2);
        for (byte b : bArr) {
            stringBuilder.append("0123456789abcdef".charAt((b & 240) >> 4)).append("0123456789abcdef".charAt(b & 15));
        }
        return stringBuilder.toString();
    }

    static void b(String str) {
        Editor edit = FyuseSDK.getContext().getSharedPreferences(g("A_P"), 0).edit();
        edit.putString(g("A_T"), g(str));
        edit.apply();
    }

    static String c() {
        return h(FyuseSDK.getContext().getSharedPreferences(g("A_P"), 0).getString(g("A_T"), null));
    }

    static void c(String str) {
        Editor edit = FyuseSDK.getContext().getSharedPreferences(g("A_P"), 0).edit();
        edit.putString(g("U_A_T"), g(str));
        edit.apply();
    }

    static void d() {
        Editor edit = FyuseSDK.getContext().getSharedPreferences(g("A_P"), 0).edit();
        edit.remove(g("U_A_T"));
        edit.apply();
    }

    static boolean d(String str) {
        return Long.parseLong(h(FyuseSDK.getContext().getSharedPreferences(g("A_P"), 0).getString(g(str), a(101)))) % 101 == 0;
    }

    static f e(String str) {
        return a.d(str);
    }

    static String e() {
        return h(FyuseSDK.getContext().getSharedPreferences(g("A_P"), 0).getString(g("U_A_T"), null));
    }

    static Long f() {
        String h = h(FyuseSDK.getContext().getSharedPreferences(g("A_P"), 0).getString(g("A_E"), null));
        return Long.valueOf(h != null ? Long.parseLong(h) : 0);
    }

    static void f(String str) {
        Editor edit = FyuseSDK.getContext().getSharedPreferences(g("A_P"), 0).edit();
        edit.putString(g(GpsLongitudeRef.EAST), g(str));
        edit.apply();
    }

    static String g() {
        return h(FyuseSDK.getContext().getSharedPreferences(g("A_P"), 0).getString(g(GpsLongitudeRef.EAST), null));
    }

    private static String g(String str) {
        return str != null ? a(a(str.getBytes(), a.h().getBytes())) : null;
    }

    private static String h(String str) {
        return str != null ? new String(a(i(str), a.h().getBytes())) : null;
    }

    static boolean h() {
        return FyuseSDK.getContext().getSharedPreferences("ACC", 0).getBoolean("ACC", false);
    }

    static void i() {
        Editor edit = FyuseSDK.getContext().getSharedPreferences("ACC", 0).edit();
        edit.putBoolean("ACC", true);
        edit.apply();
    }

    private static byte[] i(String str) {
        return Base64.decode(str, 0);
    }

    static synchronized String j() {
        String str;
        synchronized (b.class) {
            if (c == null) {
                SharedPreferences sharedPreferences = FyuseSDK.getContext().getSharedPreferences("U_I", 0);
                c = sharedPreferences.getString("U_I", null);
                if (c == null) {
                    c = UUID.randomUUID().toString();
                    Editor edit = sharedPreferences.edit();
                    edit.putString("U_I", c);
                    edit.apply();
                }
            }
            str = c;
        }
        return str;
    }

    public static String k() {
        if (d != null) {
            return d;
        }
        try {
            NetworkInterface networkInterface;
            Iterator it = Collections.list(NetworkInterface.getNetworkInterfaces()).iterator();
            do {
                if (it.hasNext()) {
                    networkInterface = (NetworkInterface) it.next();
                } else {
                    d = "02:00:00:00:00:00~" + a.a().l();
                    return d;
                }
            } while (!networkInterface.getName().equalsIgnoreCase("wlan0"));
            byte[] hardwareAddress = networkInterface.getHardwareAddress();
            if (hardwareAddress != null) {
                StringBuilder stringBuilder = new StringBuilder();
                for (byte b : hardwareAddress) {
                    stringBuilder.append(Integer.toHexString(b & 255) + ":");
                }
                if (stringBuilder.length() > 0) {
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                }
                d = stringBuilder.toString() + "~" + a.a().l();
                return d;
            }
            d = "02:00:00:00:00:00~" + a.a().l();
            return d;
        } catch (Exception e) {
        }
    }
}
