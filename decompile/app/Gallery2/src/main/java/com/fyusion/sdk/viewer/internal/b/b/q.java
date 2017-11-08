package com.fyusion.sdk.viewer.internal.b.b;

import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.core.util.d;
import com.fyusion.sdk.viewer.internal.b.a.a.a;
import com.fyusion.sdk.viewer.internal.b.c.g;
import java.io.File;
import java.io.IOException;

/* compiled from: Unknown */
class q implements a<File>, b {
    private int a;
    private final m b;
    private final b.a c;
    private volatile g.a<File> d;
    private long e;
    private int f = 0;

    q(int i, m mVar, b.a aVar) {
        this.a = i;
        this.b = mVar;
        this.c = aVar;
    }

    public void a(File file) {
        if (file != null) {
            DLog.d("SourceGenerator", "Fetch " + file.getName() + " from " + this.d.c.c() + " in " + d.a(this.e) + "ms");
            this.c.a(this.d.a, (Object) file, this.d.c, this.d.c.c());
            return;
        }
        DLog.w("SourceGenerator", "Data should never be null here, this is unexpected.");
    }

    public void a(Exception exception) {
        if (exception instanceof IOException) {
            Object obj;
            int i;
            if (this.f >= 3) {
                obj = null;
            } else {
                i = 1;
            }
            if (obj != null && (exception instanceof com.fyusion.sdk.viewer.internal.b.d)) {
                if (((com.fyusion.sdk.viewer.internal.b.d) exception).a() == 403) {
                    obj = null;
                } else {
                    i = 1;
                }
            }
            if (obj != null) {
                DLog.d("SourceGenerator", "Retry loading data for " + this.d.a + ", attempt: " + this.f);
                this.f++;
                this.d.c.a(this.b.i(), this);
                return;
            }
        }
        this.c.a(this.d.a, exception, this.d.c, this.d.c.c());
    }

    public boolean a() {
        this.e = d.a();
        this.d = this.b.a(this.a);
        this.d.c.a(this.b.i(), this);
        return true;
    }

    public void b() {
        g.a aVar = this.d;
        if (aVar != null) {
            aVar.c.b();
        }
    }
}
