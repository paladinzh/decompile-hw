package com.fyusion.sdk.common.a.a;

import java.util.Collection;

/* compiled from: Unknown */
public class e {
    private final g a;

    e(g gVar) {
        this.a = gVar;
    }

    int a() {
        return this.a.a();
    }

    void a(d dVar) {
        this.a.a(dVar);
    }

    String b() {
        Collection b = this.a.b();
        String stringBuilder = g.a(b, ",").insert(0, '[').append(']').toString();
        this.a.a(b);
        return stringBuilder;
    }
}
