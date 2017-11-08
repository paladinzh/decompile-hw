package com.fyusion.sdk.viewer.internal.f;

import android.os.Looper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/* compiled from: Unknown */
public class e {
    private static final char[] a = "0123456789abcdef".toCharArray();
    private static final char[] b = new char[32];

    public static String a(byte[] bArr) {
        String a;
        synchronized (b) {
            a = a(bArr, b);
        }
        return a;
    }

    private static String a(byte[] bArr, char[] cArr) {
        for (int i = 0; i < bArr.length; i++) {
            int i2 = bArr[i] & 255;
            cArr[i * 2] = (char) a[i2 >>> 4];
            cArr[(i * 2) + 1] = (char) a[i2 & 15];
        }
        return new String(cArr);
    }

    public static <T> List<T> a(Collection<T> collection) {
        List<T> arrayList = new ArrayList(collection.size());
        for (T add : collection) {
            arrayList.add(add);
        }
        return arrayList;
    }

    public static void a() {
        if (!b()) {
            throw new IllegalArgumentException("You must call this method on the main thread");
        }
    }

    private static boolean a(int i) {
        return i > 0 || i == Integer.MIN_VALUE;
    }

    public static boolean a(int i, int i2) {
        return a(i) && a(i2);
    }

    public static boolean b() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static boolean c() {
        return !b();
    }
}
