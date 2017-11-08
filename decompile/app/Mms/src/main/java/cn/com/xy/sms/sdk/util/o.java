package cn.com.xy.sms.sdk.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.nio.channels.FileChannel.MapMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/* compiled from: Unknown */
public final class o {
    public static String a(File file) {
        Closeable fileInputStream;
        Closeable closeable;
        Throwable th;
        try {
            MessageDigest instance = MessageDigest.getInstance("MD5");
            fileInputStream = new FileInputStream(file);
            try {
                instance.update(fileInputStream.getChannel().map(MapMode.READ_ONLY, 0, file.length()));
                String a = a(instance.digest());
                f.a(fileInputStream);
                return a;
            } catch (NoSuchAlgorithmException e) {
                closeable = fileInputStream;
                f.a(closeable);
                return null;
            } catch (Throwable th2) {
                th = th2;
                f.a(fileInputStream);
                throw th;
            }
        } catch (NoSuchAlgorithmException e2) {
            closeable = null;
            f.a(closeable);
            return null;
        } catch (Throwable th3) {
            th = th3;
            fileInputStream = null;
            f.a(fileInputStream);
            throw th;
        }
    }

    private static String a(byte[] bArr) {
        int i = 0;
        if (bArr == null) {
            return null;
        }
        String str = "0123456789abcdef";
        char[] cArr = new char[(bArr.length * 2)];
        int i2 = 0;
        while (i2 < bArr.length) {
            cArr[i] = (char) str.charAt((bArr[i2] >> 4) & 15);
            i++;
            cArr[i] = (char) str.charAt(bArr[i2] & 15);
            i2++;
            i++;
        }
        return String.valueOf(cArr);
    }
}
