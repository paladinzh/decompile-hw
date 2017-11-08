package com.fyusion.sdk.viewer.internal.b.b.a;

import android.util.Log;
import com.fyusion.sdk.viewer.internal.b.b.a.a.a;
import com.fyusion.sdk.viewer.internal.b.b.a.a.c;
import com.fyusion.sdk.viewer.internal.b.b.a.d.b;
import com.fyusion.sdk.viewer.internal.b.b.a.d.d;
import com.fyusion.sdk.viewer.internal.b.e;
import java.io.File;
import java.io.IOException;

/* compiled from: Unknown */
public class f implements a {
    private static f a = null;
    private final m b;
    private final File c;
    private final int d;
    private final c e = new c();
    private d f;

    protected f(File file, int i) {
        this.c = file;
        this.d = i;
        this.b = new m();
    }

    public static synchronized a a(File file, int i) {
        a aVar;
        synchronized (f.class) {
            if (a == null) {
                a = new f(file, i);
            }
            aVar = a;
        }
        return aVar;
    }

    private synchronized d b() throws IOException {
        if (this.f == null) {
            this.f = d.a(this.c, 1, 1, (long) this.d);
        }
        return this.f;
    }

    private synchronized void c() {
        this.f = null;
    }

    public File a(e eVar) {
        File file = null;
        String a = this.b.a(eVar);
        if (Log.isLoggable("DiskLruCacheWrapper", 2)) {
            Log.v("DiskLruCacheWrapper", "Get: Obtained: " + a + " for for Key: " + eVar);
        }
        try {
            d a2 = b().a(a);
            if (a2 != null) {
                file = a2.a(0);
            }
        } catch (Throwable e) {
            if (Log.isLoggable("DiskLruCacheWrapper", 5)) {
                Log.w("DiskLruCacheWrapper", "Unable to get from disk cache", e);
            }
        }
        return file;
    }

    public synchronized void a() {
        try {
            b().a();
            c();
        } catch (Throwable e) {
            if (Log.isLoggable("DiskLruCacheWrapper", 5)) {
                Log.w("DiskLruCacheWrapper", "Unable to clear disk cache", e);
            }
        }
    }

    public void a(a aVar) {
    }

    public void a(e eVar, c cVar) {
        b b;
        this.e.a(eVar);
        try {
            String a = this.b.a(eVar);
            if (Log.isLoggable("DiskLruCacheWrapper", 2)) {
                Log.v("DiskLruCacheWrapper", "Put: Obtained: " + a + " for for Key: " + eVar);
            }
            d b2 = b();
            if (b2.a(a) == null) {
                b = b2.b(a);
                if (b != null) {
                    if (cVar.a(b.a(0))) {
                        b.a();
                    }
                    b.c();
                    this.e.b(eVar);
                    return;
                }
                throw new IllegalStateException("Had two simultaneous puts for: " + a);
            }
            this.e.b(eVar);
        } catch (Throwable e) {
            Log.w("DiskLruCacheWrapper", "Unable to put to disk cache", e);
        } catch (Throwable th) {
            this.e.b(eVar);
        }
    }

    public void b(e eVar) {
        try {
            b().c(this.b.a(eVar));
        } catch (Throwable e) {
            if (Log.isLoggable("DiskLruCacheWrapper", 5)) {
                Log.w("DiskLruCacheWrapper", "Unable to delete from disk cache", e);
            }
        }
    }
}
