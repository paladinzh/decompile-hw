package com.avast.android.sdk.engine.obfuscated;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/* compiled from: Unknown */
public class u implements t {
    private final AtomicReference<o> a = new AtomicReference();

    public o a() throws IOException {
        o oVar = (o) this.a.get();
        return (oVar == null || oVar.d()) ? null : oVar;
    }

    public void a(o oVar) throws IOException {
        if (!oVar.d()) {
            this.a.set(oVar);
        }
    }
}
