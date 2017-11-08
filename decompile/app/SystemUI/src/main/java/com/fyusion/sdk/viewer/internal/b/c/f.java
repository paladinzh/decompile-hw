package com.fyusion.sdk.viewer.internal.b.c;

import com.fyusion.sdk.viewer.internal.c;

/* compiled from: Unknown */
public class f implements g<String, a> {
    private c a;
    private b b;

    /* compiled from: Unknown */
    public static class a implements h<String, a> {
        c a;

        public a(c cVar) {
            this.a = cVar;
        }

        public g<String, a> a(j jVar) {
            return new f(this.a, new b());
        }
    }

    public f(c cVar, b bVar) {
        this.a = cVar;
        this.b = bVar;
    }

    public com.fyusion.sdk.viewer.internal.b.c.g.a<a> a(String str, boolean z) {
        return new com.fyusion.sdk.viewer.internal.b.c.g.a(new k(str), new com.fyusion.sdk.viewer.internal.b.a.c(this.a, this.b, str, z));
    }

    public boolean a(String str) {
        return true;
    }
}
