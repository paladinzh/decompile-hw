package com.a.a;

import java.util.Collections;
import java.util.Map;

/* compiled from: Unknown */
public interface b {

    /* compiled from: Unknown */
    public static class a {
        public byte[] a;
        public String b;
        public long c;
        public long d;
        public long e;
        public long f;
        public Map<String, String> g = Collections.emptyMap();

        public boolean a() {
            return !((this.e > System.currentTimeMillis() ? 1 : (this.e == System.currentTimeMillis() ? 0 : -1)) >= 0);
        }

        public boolean b() {
            return !((this.f > System.currentTimeMillis() ? 1 : (this.f == System.currentTimeMillis() ? 0 : -1)) >= 0);
        }
    }

    a a(String str);

    void a();

    void a(String str, a aVar);
}
