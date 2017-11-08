package cn.com.xy.sms.sdk.util;

import cn.com.xy.sms.sdk.service.a.e;

/* compiled from: Unknown */
public class KeyInitUtil {
    public static synchronized String dataDecrypt(String str) {
        String a;
        synchronized (KeyInitUtil.class) {
            try {
                a = e.a(str);
            } catch (Throwable th) {
                return e.d;
            }
        }
        return a;
    }

    public static synchronized String dataEncrypt(String str) {
        String b;
        synchronized (KeyInitUtil.class) {
            try {
                b = e.b(str);
            } catch (Throwable th) {
                return e.d;
            }
        }
        return b;
    }

    public static synchronized int setKeyData(byte[] bArr) {
        synchronized (KeyInitUtil.class) {
            e.a = new byte[bArr.length];
            for (int i = 0; i < bArr.length; i++) {
                e.a[i] = (byte) bArr[i];
            }
        }
        return 0;
    }

    public static synchronized int setSDKInitData(String str, int i) {
        synchronized (KeyInitUtil.class) {
        }
        return i;
    }
}
