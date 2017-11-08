package com.fyusion.sdk.viewer.internal.b.b;

import com.fyusion.sdk.common.h;
import com.fyusion.sdk.viewer.internal.b.a.a.a;
import com.fyusion.sdk.viewer.internal.b.c.g;
import java.io.File;

/* compiled from: Unknown */
class s implements a<File>, b {
    private final m a;
    private com.fyusion.sdk.viewer.internal.b.c.a b;
    private volatile g.a<File> c;

    public s(m mVar, com.fyusion.sdk.viewer.internal.b.c.a aVar) {
        this.a = mVar;
        this.b = aVar;
    }

    public void a(File file) {
        if (file != null && file.exists()) {
            this.b.c(file);
        } else {
            h.a("TweeningDataGenerator", "Fetching tweening file for " + this.b.d() + " failed.");
        }
    }

    public void a(Exception exception) {
        h.a("TweeningDataGenerator", "Fetching tweening file for " + this.b.d() + " failed.", exception);
    }

    public boolean a() {
        try {
            this.c = this.a.e();
            this.c.c.a(this.a.i(), this);
        } catch (Throwable e) {
            h.a("TweeningDataGenerator", "Fetching tweening file for " + this.b.d() + " failed", e);
        }
        return true;
    }

    public void b() {
        g.a aVar = this.c;
        if (aVar != null) {
            aVar.c.b();
        }
    }
}
