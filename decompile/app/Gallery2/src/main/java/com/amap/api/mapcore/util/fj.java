package com.amap.api.mapcore.util;

import java.util.HashMap;
import java.util.Map;

@Deprecated
/* compiled from: AuthRequest */
class fj extends hd {
    private Map<String, String> a = new HashMap();
    private String b;
    private Map<String, String> c = new HashMap();

    fj() {
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

    public String c() {
        return this.b;
    }

    public Map<String, String> a() {
        return this.a;
    }

    public Map<String, String> b() {
        return this.c;
    }
}
