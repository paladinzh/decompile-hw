package com.fyusion.sdk.viewer.internal.b.a;

import android.support.annotation.Nullable;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.viewer.d;
import com.fyusion.sdk.viewer.internal.b.c.a;
import com.fyusion.sdk.viewer.internal.b.c.b;
import java.io.InputStream;

/* compiled from: Unknown */
public class c implements a<a>, a.a<InputStream> {
    private com.fyusion.sdk.viewer.internal.c a;
    private String b;
    private boolean c;
    private a<InputStream> d;
    private a.a<? super a> e;
    private com.fyusion.sdk.viewer.internal.b.a f;
    private b g;

    public c(com.fyusion.sdk.viewer.internal.c cVar, b bVar, String str, boolean z) {
        this.a = cVar;
        this.g = bVar;
        this.b = str;
        this.c = z;
    }

    public void a() {
    }

    public void a(d dVar, a.a<? super a> aVar) {
        Object a = this.a.a(this.b, this.c, this.g);
        if (a != null) {
            this.f = com.fyusion.sdk.viewer.internal.b.a.MEMORY_CACHE;
            aVar.a(a);
            return;
        }
        this.e = aVar;
        this.f = com.fyusion.sdk.viewer.internal.b.a.REMOTE;
        com.fyusion.sdk.viewer.internal.b.c.d dVar2 = new com.fyusion.sdk.viewer.internal.b.c.d(com.fyusion.sdk.common.internal.a.a.b(this.b));
        if (this.d == null) {
            this.d = new d(dVar2);
        }
        this.d.a(dVar, this);
    }

    public void a(@Nullable InputStream inputStream) {
        if (inputStream != null) {
            try {
                this.e.a(this.a.a(this.b, inputStream, this.c, this.g));
            } catch (Exception e) {
                DLog.e("FyuseDataFetcher", "Failed processing Fyuse Data", e);
                a(e);
                return;
            } catch (Throwable th) {
                this.d.a();
            }
        }
        a(new RuntimeException("Data returned is null, probably request is cancelled"));
        this.d.a();
    }

    public void a(Exception exception) {
        if (exception instanceof RuntimeException) {
            DLog.d("FyuseDataFetcher", "Fetching metadata for " + this.b + " is cancelled.");
        } else {
            DLog.w("FyuseDataFetcher", "Fetching metadata for " + this.b + " failed.", exception);
        }
        this.e.a(exception);
    }

    public void b() {
        if (this.d != null) {
            this.d.b();
        }
    }

    public com.fyusion.sdk.viewer.internal.b.a c() {
        return this.f;
    }
}
