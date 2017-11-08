package com.fyusion.sdk.viewer.internal.b.a;

import android.support.annotation.Nullable;
import android.util.Log;
import com.fyusion.sdk.viewer.internal.b.a.a.a;
import com.fyusion.sdk.viewer.internal.b.c.d;
import com.fyusion.sdk.viewer.internal.b.c.l;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/* compiled from: Unknown */
public class b implements a<File>, a {
    private final d a;
    private final com.fyusion.sdk.viewer.internal.b.b.a.a b;
    private final int c;
    private a<InputStream> d;
    private com.fyusion.sdk.viewer.internal.b.a e;
    private a<? super File> f;

    public b(d dVar, com.fyusion.sdk.viewer.internal.b.b.a.a aVar, int i) {
        this.a = dVar;
        this.b = aVar;
        this.c = i;
    }

    public void a() {
    }

    public void a(com.fyusion.sdk.viewer.d dVar, a<? super File> aVar) {
        Object a = this.b.a(this.a);
        if (a != null && a.exists()) {
            this.e = com.fyusion.sdk.viewer.internal.b.a.DATA_DISK_CACHE;
            aVar.a(a);
            return;
        }
        this.e = com.fyusion.sdk.viewer.internal.b.a.REMOTE;
        this.f = aVar;
        if (this.d == null) {
            this.d = new d(this.a, this.c);
        }
        this.d.a(dVar, this);
    }

    public void a(Exception exception) {
        if (exception instanceof RuntimeException) {
            Log.d("FileFetcher", "Fetching file from " + this.a + " is cancelled.");
        } else {
            Log.w("FileFetcher", "Fetching file from " + this.a + " failed.", exception);
        }
        this.f.a(exception);
    }

    public void a(@Nullable Object obj) {
        Object obj2 = null;
        if (obj != null) {
            try {
                this.b.a(this.a, new l((InputStream) obj));
                obj2 = this.b.a(this.a);
            } catch (Throwable th) {
                this.d.a();
            }
        }
        this.d.a();
        if (obj2 != null) {
            this.f.a(obj2);
        } else if (obj != null) {
            a(new IOException("Failed to fetch file from " + this.a));
        } else {
            a(new RuntimeException("Data returned is null, probably request is cancelled. "));
        }
    }

    public void b() {
        if (this.d != null) {
            this.d.b();
        }
    }

    public com.fyusion.sdk.viewer.internal.b.a c() {
        return this.e;
    }
}
