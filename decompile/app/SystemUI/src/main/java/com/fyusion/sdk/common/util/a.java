package com.fyusion.sdk.common.util;

import android.os.Build;
import com.fyusion.sdk.common.FyuseSDK;
import fyusion.vislib.BuildConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/* compiled from: Unknown */
public class a {
    public static File a(String str) {
        return new File(FyuseSDK.getContext().getCacheDir(), "workspace/" + str);
    }

    public static String a() {
        String str = Build.MANUFACTURER;
        String str2 = Build.MODEL;
        return !str2.startsWith(str) ? b(str) + "." + str2 : b(str2);
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
        Throwable th2;
        Throwable th3 = null;
        FileChannel channel = fileInputStream.getChannel();
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
            } catch (Throwable th22) {
                Throwable th4 = th22;
                th22 = th;
                th = th4;
            }
            if (channel2 != null) {
                if (th22 == null) {
                    channel2.close();
                } else {
                    channel2.close();
                }
            }
            throw th;
            throw th;
        } catch (Throwable th5) {
            th = th5;
            if (channel != null) {
                if (th3 == null) {
                    channel.close();
                } else {
                    try {
                        channel.close();
                    } catch (Throwable th6) {
                        th3.addSuppressed(th6);
                    }
                }
            }
            throw th;
        }
    }

    private static String b(String str) {
        if (str == null || str.length() == 0) {
            return BuildConfig.FLAVOR;
        }
        char charAt = str.charAt(0);
        return !Character.isUpperCase(charAt) ? Character.toUpperCase(charAt) + str.substring(1) : str;
    }
}
