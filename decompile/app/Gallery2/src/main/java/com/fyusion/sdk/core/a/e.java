package com.fyusion.sdk.core.a;

import android.support.v4.util.LruCache;
import com.fyusion.sdk.common.DLog;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/* compiled from: Unknown */
public class e<T> {
    public static final e a = new e(40960, 153600);
    private LruCache<String, d<T>> b;
    private Set<a> c = Collections.synchronizedSet(new HashSet());
    private int d = 0;
    private int e = 0;

    /* compiled from: Unknown */
    class a {
        SoftReference<d<T>> a;
        int b = 0;
        final /* synthetic */ e c;

        a(e eVar, d<T> dVar) {
            this.c = eVar;
            if (dVar == null) {
                this.a = new SoftReference(null);
                return;
            }
            this.a = new SoftReference(dVar);
            this.b = dVar.d();
        }
    }

    private e(int i, int i2) {
        b(i);
        a(i2);
    }

    private void a(int i) {
        this.d = i;
    }

    private void b(int i) {
        DLog.i("ImageCache", "Image cache size: " + i + " kB");
        this.b = new LruCache<String, d<T>>(this, i) {
            final /* synthetic */ e a;

            protected int a(String str, d<T> dVar) {
                return dVar.d() / 1024;
            }

            protected void a(boolean z, String str, d<T> dVar, d<T> dVar2) {
                if (size() + this.a.e >= this.a.d) {
                    dVar.e();
                    return;
                }
                synchronized (this.a.c) {
                    this.a.c.add(new a(this.a, dVar));
                    this.a.e = this.a.e + (dVar.d() / 1024);
                }
            }

            protected /* synthetic */ void entryRemoved(boolean z, Object obj, Object obj2, Object obj3) {
                a(z, (String) obj, (d) obj2, (d) obj3);
            }

            protected /* synthetic */ int sizeOf(Object obj, Object obj2) {
                return a((String) obj, (d) obj2);
            }
        };
    }

    public d<T> a(int i, int i2, int i3, Class<?> cls) {
        if (this.c == null || this.c.isEmpty()) {
            return null;
        }
        d<T> dVar;
        synchronized (this.c) {
            Iterator it = this.c.iterator();
            while (it.hasNext()) {
                a aVar = (a) it.next();
                d<T> dVar2 = (d) aVar.a.get();
                if (dVar2 != null && dVar2.g()) {
                    if (dVar2.a(i, i2, i3) && dVar2.a(cls)) {
                        it.remove();
                        this.e -= dVar2.d() / 1024;
                        dVar = dVar2;
                        break;
                    }
                }
                this.e -= aVar.b;
                it.remove();
            }
            dVar = null;
        }
        return dVar;
    }

    public d<T> a(String str) {
        return (d) this.b.get(str);
    }

    public void a() {
        this.b.evictAll();
        synchronized (this.c) {
            for (a aVar : this.c) {
                d dVar = (d) aVar.a.get();
                if (dVar != null) {
                    dVar.e();
                }
            }
            this.c.clear();
            this.e = 0;
        }
    }

    public void a(String str, d dVar) {
        if (dVar != null) {
            this.b.put(str, dVar);
        }
    }
}
