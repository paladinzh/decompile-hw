package com.avast.android.shepherd.obfuscated;

import java.util.concurrent.atomic.AtomicReference;

/* compiled from: Unknown */
public class t implements s {
    private final AtomicReference<n> a = new AtomicReference();

    public n a() {
        n nVar = (n) this.a.get();
        return (nVar == null || nVar.d()) ? null : nVar;
    }

    public void a(n nVar) {
        if (!nVar.d()) {
            this.a.set(nVar);
        }
    }
}
