package com.fyusion.sdk.viewer.internal.b.d;

import com.fyusion.sdk.viewer.internal.b.c.a;

/* compiled from: Unknown */
public class b extends c<a> {
    private boolean b = false;

    public b(a aVar) {
        super(aVar);
    }

    public synchronized void c() {
        if (!this.b) {
            this.b = true;
            ((a) this.a).h();
        }
    }

    public boolean f() {
        return !((a) this.a).e();
    }
}
