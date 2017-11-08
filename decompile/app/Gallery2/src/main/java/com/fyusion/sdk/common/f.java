package com.fyusion.sdk.common;

import java.util.HashMap;

/* compiled from: Unknown */
class f {
    private String a;
    private boolean b = false;
    private g c;
    private HashMap<String, Boolean> d;

    f(String str) {
        this.a = str;
    }

    public void a(String str, Boolean bool) {
        if (this.d == null) {
            this.d = new HashMap();
        }
        this.d.put(str, bool);
    }

    public void a(HashMap<String, Boolean> hashMap) {
        this.d = hashMap;
    }

    void a(boolean z) {
        this.b = z;
        if (this.c != null) {
            if (z) {
                this.c.a();
            } else {
                this.c.b();
            }
        }
    }

    boolean a() {
        return this.b;
    }

    boolean a(String str) {
        return this.d != null ? ((Boolean) this.d.get(str)).booleanValue() : false;
    }

    String b() {
        return this.a;
    }

    public HashMap<String, Boolean> c() {
        return this.d;
    }
}
