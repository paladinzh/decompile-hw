package com.fyusion.sdk.common.util;

import android.os.Build;
import com.fyusion.sdk.common.FyuseSDK;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.UUID;

/* compiled from: Unknown */
public class a {
    public static File a(String str) {
        return new File(FyuseSDK.getContext().getCacheDir(), "workspace/" + str);
    }

    public static String a() {
        return com.fyusion.sdk.common.a.i() + "~" + UUID.randomUUID().toString();
    }

    public static void a(File file) {
        if (file.isDirectory()) {
            for (File a : file.listFiles()) {
                a(a);
            }
        }
        file.delete();
    }

    public static void a(File file, File file2) throws IOException {
        a(new FileInputStream(file), new FileOutputStream(file2));
    }

    public static void a(File file, File file2, String str) throws IOException {
        a(new File(file, str), new File(file2, str));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void a(FileInputStream fileInputStream, FileOutputStream fileOutputStream) throws IOException {
        Throwable th;
        Throwable th2 = null;
        FileChannel channel = fileInputStream.getChannel();
        Throwable th3;
        try {
            FileChannel channel2 = fileOutputStream.getChannel();
            try {
                channel.transferTo(0, channel.size(), channel2);
                if (channel2 != null) {
                    channel2.close();
                }
                if (channel != null) {
                    channel.close();
                    return;
                }
                return;
            } catch (Throwable th4) {
                Throwable th5 = th4;
                th4 = th3;
                th3 = th5;
            }
            if (channel2 != null) {
                if (th4 == null) {
                    channel2.close();
                } else {
                    channel2.close();
                }
            }
            throw th3;
            throw th3;
        } catch (Throwable th6) {
            th3 = th6;
            if (channel != null) {
                if (th2 == null) {
                    channel.close();
                } else {
                    try {
                        channel.close();
                    } catch (Throwable th7) {
                        th2.addSuppressed(th7);
                    }
                }
            }
            throw th3;
        }
    }

    public static String b() {
        String str = Build.MANUFACTURER;
        String str2 = Build.MODEL;
        return !str2.startsWith(str) ? b(str) + "." + str2 : b(str2);
    }

    private static String b(String str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        char charAt = str.charAt(0);
        return !Character.isUpperCase(charAt) ? Character.toUpperCase(charAt) + str.substring(1) : str;
    }
}
