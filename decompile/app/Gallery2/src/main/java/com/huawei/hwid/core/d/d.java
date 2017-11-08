package com.huawei.hwid.core.d;

import com.huawei.hwid.core.d.b.e;

public class d {
    private static boolean a = false;
    private static boolean b = false;
    private static String c = "";

    public static boolean a() {
        e.a("DataAnalyseUtil", "overSeaUniversalFlag is " + b);
        return b;
    }

    public static synchronized boolean b() {
        boolean z;
        synchronized (d.class) {
            z = a;
        }
        return z;
    }

    public static String c() {
        return c;
    }
}
