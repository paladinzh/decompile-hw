package com.huawei.openalliance.ad.utils.b;

/* compiled from: Unknown */
public abstract class d {
    private static e a = null;

    public static void a(String str, String str2, Throwable th) {
        if (c()) {
            a.c(str, str2);
        }
    }

    public static void a(String str, String... strArr) {
        if (a() && strArr != null) {
            StringBuffer stringBuffer = new StringBuffer();
            for (String append : strArr) {
                stringBuffer.append(append);
            }
            a.a(str, stringBuffer.toString());
        }
    }

    public static boolean a() {
        return e() && a.c(f.DEBUG);
    }

    public static void b(String str, String... strArr) {
        if (b() && strArr != null) {
            StringBuffer stringBuffer = new StringBuffer();
            for (String append : strArr) {
                stringBuffer.append(append);
            }
            a.b(str, stringBuffer.toString());
        }
    }

    public static boolean b() {
        return e() && a.c(f.INFO);
    }

    public static void c(String str, String... strArr) {
        if (c() && strArr != null) {
            StringBuffer stringBuffer = new StringBuffer();
            for (String append : strArr) {
                stringBuffer.append(append);
            }
            a.c(str, stringBuffer.toString());
        }
    }

    public static boolean c() {
        return e() && a.c(f.WARN);
    }

    public static void d(String str, String... strArr) {
        if (d() && strArr != null) {
            StringBuffer stringBuffer = new StringBuffer();
            for (String append : strArr) {
                stringBuffer.append(append);
            }
            a.d(str, stringBuffer.toString());
        }
    }

    public static boolean d() {
        return e() && a.c(f.ERROR);
    }

    private static boolean e() {
        return a != null;
    }
}
