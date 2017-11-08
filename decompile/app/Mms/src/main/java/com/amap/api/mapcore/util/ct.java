package com.amap.api.mapcore.util;

import java.util.HashMap;
import java.util.Map;

/* compiled from: LogInfo */
public abstract class ct {
    @cm(a = "b2", b = 2)
    protected int a = -1;
    @cm(a = "b1", b = 6)
    protected String b;
    @cm(a = "b3", b = 2)
    protected int c = 1;
    @cm(a = "a1", b = 6)
    private String d;

    public int a() {
        return this.a;
    }

    public void a(int i) {
        this.a = i;
    }

    public String b() {
        return this.b;
    }

    public void a(String str) {
        this.b = str;
    }

    public void b(String str) {
        this.d = bx.b(str);
    }

    public int c() {
        return this.c;
    }

    public void b(int i) {
        this.c = i;
    }

    public static String c(String str) {
        Map hashMap = new HashMap();
        hashMap.put("b1", str);
        return ck.a(hashMap);
    }

    public static String c(int i) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append("b2").append("=").append(i);
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return stringBuilder.toString();
    }
}
