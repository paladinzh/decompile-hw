package com.avast.android.sdk.engine.obfuscated;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/* compiled from: Unknown */
public class an {

    /* compiled from: Unknown */
    public enum a {
        SHA1,
        SHA256,
        MD5
    }

    public static String a(a aVar, File file, int i) throws NoSuchAlgorithmException {
        byte[] a = a(aVar, file);
        return a != null ? a(a(a), i) : null;
    }

    public static String a(a aVar, InputStream inputStream, int i) throws NoSuchAlgorithmException {
        byte[] a = a(aVar, inputStream);
        return a != null ? a(a(a), i) : null;
    }

    public static String a(a aVar, String str, int i) throws NoSuchAlgorithmException {
        byte[] a = a(aVar, str);
        return a != null ? a(a(a), i) : null;
    }

    private static String a(String str, int i) {
        if (str == null) {
            return null;
        }
        while (str.length() < i) {
            str = "0" + str;
        }
        return str;
    }

    public static String a(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        String str = "";
        for (byte b : bArr) {
            str = str + Integer.toString((b & 255) + 256, 16).substring(1);
        }
        return str;
    }

    public static byte[] a(a aVar, File file) throws NoSuchAlgorithmException {
        if (file == null) {
            return null;
        }
        try {
            MessageDigest instance = MessageDigest.getInstance(aVar.toString());
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] bArr = new byte[1024];
            while (true) {
                int read = fileInputStream.read(bArr);
                if (read == -1) {
                    return instance.digest();
                }
                instance.update(bArr, 0, read);
            }
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e2) {
            return null;
        }
    }

    public static byte[] a(a aVar, InputStream inputStream) throws NoSuchAlgorithmException {
        if (inputStream == null) {
            return null;
        }
        try {
            MessageDigest instance = MessageDigest.getInstance(aVar.toString());
            byte[] bArr = new byte[1024];
            while (true) {
                int read = inputStream.read(bArr);
                if (read == -1) {
                    return instance.digest();
                }
                instance.update(bArr, 0, read);
            }
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e2) {
            return null;
        }
    }

    public static byte[] a(a aVar, String str) throws NoSuchAlgorithmException {
        if (str == null) {
            return null;
        }
        MessageDigest instance = MessageDigest.getInstance(aVar.toString());
        instance.update(str.getBytes());
        return instance.digest();
    }
}
