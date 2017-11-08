package com.loc;

import java.util.HashMap;
import java.util.Map;

/* compiled from: AuthRequest */
class x extends bs {
    private Map<String, String> d = new HashMap();
    private String e;
    private Map<String, String> f = new HashMap();

    x() {
    }

    public Map<String, String> a() {
        return this.d;
    }

    void a(String str) {
        this.e = str;
    }

    void a(Map<String, String> map) {
        this.d.clear();
        this.d.putAll(map);
    }

    public Map<String, String> b() {
        return this.f;
    }

    void b(Map<String, String> map) {
        this.f.clear();
        this.f.putAll(map);
    }

    public String c() {
        return this.e;
    }
}
