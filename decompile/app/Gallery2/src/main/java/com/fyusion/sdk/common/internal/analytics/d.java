package com.fyusion.sdk.common.internal.analytics;

import java.util.Collection;

/* compiled from: Unknown */
public class d {
    private final e a;

    d(e eVar) {
        this.a = eVar;
    }

    int a() {
        return this.a.a();
    }

    void a(Event event) {
        this.a.a(event);
    }

    String b() {
        Collection b = this.a.b();
        String stringBuilder = e.a(b, ",").insert(0, '[').append(']').toString();
        this.a.a(b);
        return stringBuilder;
    }
}
