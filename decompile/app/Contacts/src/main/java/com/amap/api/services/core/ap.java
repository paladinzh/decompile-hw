package com.amap.api.services.core;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/* compiled from: MD5 */
public class ap {
    public static String a(String str) {
        if (str != null) {
            return as.c(c(str));
        }
        return null;
    }

    public static String a(byte[] bArr) {
        return as.c(b(bArr));
    }

    public static String b(String str) {
        return as.d(d(str));
    }

    public static byte[] a(byte[] bArr, String str) {
        try {
            MessageDigest instance = MessageDigest.getInstance(str);
            instance.update(bArr);
            return instance.digest();
        } catch (Throwable e) {
            ay.a(e, "MD5", "getMd5Bytes");
            return null;
        } catch (Throwable e2) {
            ay.a(e2, "MD5", "getMd5Bytes1");
            return null;
        }
    }

    private static byte[] b(byte[] bArr) {
        return a(bArr, "MD5");
    }

    public static byte[] c(String str) {
        try {
            return e(str);
        } catch (Throwable e) {
            ay.a(e, "MD5", "getMd5Bytes");
            return new byte[0];
        } catch (Throwable e2) {
            ay.a(e2, "MD5", "getMd5Bytes");
            return new byte[0];
        } catch (Throwable e22) {
            ay.a(e22, "MD5", "getMd5Bytes");
            return new byte[0];
        }
    }

    private static byte[] d(String str) {
        try {
            return e(str);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return new byte[0];
        } catch (UnsupportedEncodingException e2) {
            e2.printStackTrace();
            return new byte[0];
        } catch (Throwable th) {
            th.printStackTrace();
            return new byte[0];
        }
    }

    private static byte[] e(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        if (str == null) {
            return null;
        }
        MessageDigest instance = MessageDigest.getInstance("MD5");
        instance.update(str.getBytes("UTF-8"));
        return instance.digest();
    }
}
