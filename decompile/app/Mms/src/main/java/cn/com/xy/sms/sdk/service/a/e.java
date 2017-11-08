package cn.com.xy.sms.sdk.service.a;

import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.util.DataEnCipher;
import cn.com.xy.sms.sdk.util.StringUtils;
import java.io.File;

/* compiled from: Unknown */
public final class e {
    public static byte[] a;
    public static String b;
    public static String c;
    public static String d = "ERROR";
    private static boolean e = false;
    private static DataEnCipher f = null;

    public static String a(String str) {
        try {
            if (!d()) {
                return d;
            }
            if (StringUtils.isNull(str)) {
                return "";
            }
            byte[] xyBase64Decode2 = f.xyBase64Decode2(str);
            xyBase64Decode2 = f.xyDecrypt(xyBase64Decode2, xyBase64Decode2.length, a, a.length);
            return xyBase64Decode2 != null ? new String(xyBase64Decode2, "UTF-8") : "";
        } catch (Throwable th) {
            return "";
        }
    }

    private static String a(byte[] bArr) {
        return bArr != null ? f.xyBase64Encode2(bArr, bArr.length) : "";
    }

    private static void a() {
        try {
            System.loadLibrary("xy-algorithm");
            e = true;
        } catch (Exception e) {
        }
    }

    public static String b(String str) {
        try {
            if (!d()) {
                return d;
            }
            if (StringUtils.isNull(str)) {
                return "";
            }
            if (a != null) {
                if (!StringUtils.isNull(str)) {
                    byte[] bytes = str.getBytes("UTF-8");
                    bytes = f.xyEncrypt(bytes, bytes.length, a, a.length);
                    return bytes != null ? f.xyBase64Encode2(bytes, bytes.length) : "";
                }
            }
            return null;
        } catch (Throwable th) {
            return null;
        }
    }

    private static void b() {
        if (!e) {
            try {
                System.load(new File(Constant.getPARSE_PATH(), "libxy-algorithm.so").getCanonicalPath());
                e = true;
            } catch (Throwable th) {
                try {
                    System.loadLibrary("xy-algorithm");
                    e = true;
                } catch (Exception e) {
                }
            }
        }
    }

    private static synchronized DataEnCipher c() {
        DataEnCipher dataEnCipher;
        synchronized (e.class) {
            if (f == null) {
                f = new DataEnCipher();
            }
            dataEnCipher = f;
        }
        return dataEnCipher;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static synchronized boolean d() {
        synchronized (e.class) {
            try {
                if (a == null) {
                    c();
                    if (!e) {
                        System.load(new File(Constant.getPARSE_PATH(), "libxy-algorithm.so").getCanonicalPath());
                        e = true;
                    }
                    if (a == null) {
                        f.getKeyData(1);
                    }
                    f.getChannelData(1);
                    f.getChannelData(2);
                    if (a != null) {
                        return true;
                    }
                }
                return true;
            } catch (Throwable th) {
            }
        }
        return false;
    }
}
