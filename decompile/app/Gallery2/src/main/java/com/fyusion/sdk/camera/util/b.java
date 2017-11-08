package com.fyusion.sdk.camera.util;

/* compiled from: Unknown */
public class b {
    public static int a = 0;
    public static int b = 1;
    private static int c = 0;
    private static int d = 0;
    private static byte[] e = null;
    private static int f = 0;
    private static byte[] g = null;
    private static int h = 0;
    private static int i = 0;
    private static byte[] j = null;
    private static int k = 0;
    private static byte[] l = null;
    private static b m = null;
    private static final b n = new b();

    public static b a() {
        return n;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized byte[] a(int i, int i2) {
        int i3 = 0;
        synchronized (this) {
            int i4;
            byte[] bArr;
            if (i2 != b) {
                if (c == 0) {
                    i3 = 1;
                }
                c = i3;
                i4 = c != 0 ? f : d;
                bArr = c != 0 ? g : e;
                if (i > i4 || bArr == null) {
                    if (c != 0) {
                        g = new byte[i];
                        f = i;
                        bArr = g;
                    } else {
                        e = new byte[i];
                        d = i;
                        bArr = g;
                    }
                }
                bArr = c != 0 ? g : e;
            } else {
                if (h == 0) {
                    i3 = 1;
                }
                h = i3;
                i4 = h != 0 ? k : i;
                bArr = h != 0 ? l : j;
                if (i > i4 || bArr == null) {
                    if (h != 0) {
                        l = new byte[i];
                        k = i;
                        bArr = l;
                    } else {
                        j = new byte[i];
                        i = i;
                        bArr = j;
                    }
                }
                bArr = h != 0 ? l : j;
            }
        }
    }
}
