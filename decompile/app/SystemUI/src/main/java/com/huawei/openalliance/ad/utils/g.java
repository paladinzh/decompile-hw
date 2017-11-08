package com.huawei.openalliance.ad.utils;

import com.huawei.openalliance.ad.utils.b.c;
import com.huawei.openalliance.ad.utils.b.d;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/* compiled from: Unknown */
public class g {
    public static String a(File file) {
        String a;
        Throwable th;
        Closeable closeable = null;
        Closeable fileInputStream;
        try {
            MessageDigest instance = MessageDigest.getInstance("SHA-256");
            fileInputStream = new FileInputStream(file);
            try {
                byte[] bArr = new byte[8192];
                while (true) {
                    int read = fileInputStream.read(bArr);
                    if (read <= 0) {
                        break;
                    }
                    instance.update(bArr, 0, read);
                }
                a = c.a(instance.digest());
                b.a(fileInputStream);
            } catch (FileNotFoundException e) {
            } catch (IOException e2) {
            } catch (NoSuchAlgorithmException e3) {
            }
        } catch (FileNotFoundException e4) {
            fileInputStream = null;
            try {
                d.c("Sha256Util", "fail to get file sha256, ");
                b.a(fileInputStream);
                return a;
            } catch (Throwable th2) {
                Throwable th3 = th2;
                closeable = fileInputStream;
                th = th3;
                b.a(closeable);
                throw th;
            }
        } catch (IOException e5) {
            fileInputStream = null;
            d.c("Sha256Util", "fail to get file sha256, ");
            b.a(fileInputStream);
            return a;
        } catch (NoSuchAlgorithmException e6) {
            fileInputStream = null;
            d.c("Sha256Util", "fail to get file sha256, ");
            b.a(fileInputStream);
            return a;
        } catch (Throwable th4) {
            th = th4;
            b.a(closeable);
            throw th;
        }
        return a;
    }
}
