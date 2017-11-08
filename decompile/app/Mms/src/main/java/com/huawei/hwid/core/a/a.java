package com.huawei.hwid.core.a;

import com.huawei.hwid.ui.common.f;

/* compiled from: DataAnalyseUtil */
public class a {
    private static boolean a = false;
    private static boolean b = false;
    private static String c = "";
    private static f d = f.Default;

    public static boolean a() {
        com.huawei.hwid.core.c.b.a.a("DataAnalyseUtil", "overSeaUniversalFlag is " + b);
        return b;
    }

    public static synchronized boolean b() {
        boolean z;
        synchronized (a.class) {
            z = a;
        }
        return z;
    }

    public static void a(String str) {
        c = str;
    }

    public static String c() {
        return c;
    }
}
