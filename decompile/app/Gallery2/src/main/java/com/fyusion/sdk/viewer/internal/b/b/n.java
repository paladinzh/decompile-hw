package com.fyusion.sdk.viewer.internal.b.b;

import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.core.util.d;
import java.io.IOException;

/* compiled from: Unknown */
class n implements com.fyusion.sdk.viewer.internal.b.a.a.a<com.fyusion.sdk.viewer.internal.b.c.a>, b {
    private final m a;
    private final com.fyusion.sdk.viewer.internal.b.b.b.a b;
    private final a c;
    private volatile com.fyusion.sdk.viewer.internal.b.c.g.a<com.fyusion.sdk.viewer.internal.b.c.a> d;
    private s e;
    private long f;
    private int g = 0;

    /* compiled from: Unknown */
    interface a {
        void a(com.fyusion.sdk.viewer.internal.b.c.a aVar, com.fyusion.sdk.viewer.internal.b.a aVar2);
    }

    public n(m mVar, com.fyusion.sdk.viewer.internal.b.b.b.a aVar, a aVar2) {
        this.a = mVar;
        this.b = aVar;
        this.c = aVar2;
    }

    public void a(com.fyusion.sdk.viewer.internal.b.c.a aVar) {
        if (aVar == null) {
            DLog.w("MetaDataGenerator", "Data should never be null. This is unexpected.");
            return;
        }
        DLog.d("MetaDataGenerator", "Load metadata for " + aVar.d() + " from " + this.d.c.c() + " in " + d.a(this.f) + "ms");
        this.c.a(aVar, this.d.c.c());
        if (aVar.m().hasTweens()) {
            this.e = new s(this.a, aVar);
            this.e.a();
        }
    }

    public void a(Exception exception) {
        if ((exception instanceof IOException) && this.g < 3) {
            DLog.d("MetaDataGenerator", "Retry loading data for " + this.d.a + ", attempt: " + this.g);
            this.g++;
            this.d.c.a(this.a.i(), this);
            return;
        }
        this.b.a(this.d.a, exception, this.d.c, this.d.c.c());
    }

    public boolean a() {
        this.f = d.a();
        this.d = this.a.d();
        this.d.c.a(this.a.i(), this);
        return true;
    }

    public void b() {
        com.fyusion.sdk.viewer.internal.b.c.g.a aVar = this.d;
        if (aVar != null) {
            aVar.c.b();
        }
        if (this.e != null) {
            this.e.b();
        }
    }
}
