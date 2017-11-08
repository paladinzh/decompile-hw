package com.loc;

import android.text.TextUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/* compiled from: MD5 */
public class s {
    public static String a(String str) {
        Throwable th;
        FileInputStream fileInputStream = null;
        FileInputStream fileInputStream2;
        try {
            if (TextUtils.isEmpty(str)) {
                return null;
            }
            File file = new File(str);
            if (!file.isFile() || !file.exists()) {
                return null;
            }
            byte[] bArr = new byte[2048];
            MessageDigest instance = MessageDigest.getInstance("MD5");
            fileInputStream2 = new FileInputStream(file);
            while (true) {
                try {
                    int read = fileInputStream2.read(bArr);
                    if (read == -1) {
                        break;
                    }
                    instance.update(bArr, 0, read);
                } catch (Throwable th2) {
                    th = th2;
                }
            }
            String c = w.c(instance.digest());
            if (fileInputStream2 != null) {
                try {
                    fileInputStream2.close();
                } catch (Throwable e) {
                    aa.a(e, "MD5", "getMd5FromFile");
                }
            }
            return c;
        } catch (Throwable th3) {
            th = th3;
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            throw th;
        }
    }

    public static String a(byte[] bArr) {
        return w.c(b(bArr));
    }

    public static byte[] a(byte[] bArr, String str) {
        try {
            MessageDigest instance = MessageDigest.getInstance(str);
            instance.update(bArr);
            return instance.digest();
        } catch (Throwable e) {
            aa.a(e, "MD5", "getMd5Bytes");
            return null;
        } catch (Throwable e2) {
            aa.a(e2, "MD5", "getMd5Bytes1");
            return null;
        }
    }

    public static String b(String str) {
        return str != null ? w.c(d(str)) : null;
    }

    private static byte[] b(byte[] bArr) {
        return a(bArr, "MD5");
    }

    public static String c(String str) {
        return w.d(e(str));
    }

    public static byte[] d(String str) {
        try {
            return f(str);
        } catch (Throwable e) {
            aa.a(e, "MD5", "getMd5Bytes");
            return new byte[0];
        } catch (Throwable e2) {
            aa.a(e2, "MD5", "getMd5Bytes");
            return new byte[0];
        } catch (Throwable e22) {
            aa.a(e22, "MD5", "getMd5Bytes");
            return new byte[0];
        }
    }

    private static byte[] e(String str) {
        try {
            return f(str);
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

    private static byte[] f(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        if (str == null) {
            return null;
        }
        MessageDigest instance = MessageDigest.getInstance("MD5");
        instance.update(str.getBytes("UTF-8"));
        return instance.digest();
    }
}
