package com.amap.api.mapcore.util;

import java.util.HashMap;
import java.util.Map;

/* compiled from: AuthRequest */
class by extends dj {
    private Map<String, String> a = new HashMap();
    private String b;
    private Map<String, String> c = new HashMap();

    by() {
    }

    void a(Map<String, String> map) {
        this.a.clear();
        this.a.putAll(map);
    }

    void a(String str) {
        this.b = str;
    }

    void b(Map<String, String> map) {
        this.c.clear();
        this.c.putAll(map);
    }

    public String a() {
        return this.b;
    }

    public Map<String, String> c() {
        return this.a;
    }

    public Map<String, String> b() {
        return this.c;
    }
}
