package com.fyusion.sdk.viewer.internal.b.b;

import android.os.Looper;
import com.fyusion.sdk.viewer.internal.b.e;
import com.fyusion.sdk.viewer.internal.f.d;

/* compiled from: Unknown */
class k<Z> implements o<Z> {
    private a a;
    private e b;
    private int c;
    private boolean d;
    private final o<Z> e;

    /* compiled from: Unknown */
    interface a {
        void b(e eVar, k<?> kVar);
    }

    k(o<Z> oVar) {
        this.e = (o) d.a((Object) oVar);
    }

    public Z a() {
        return this.e.a();
    }

    void a(e eVar, a aVar) {
        this.b = eVar;
        this.a = aVar;
    }

    public int b() {
        return this.e.b();
    }

    public void c() {
        if (this.c > 0) {
            throw new IllegalStateException("Cannot recycle a resource while it is still acquired");
        } else if (this.d) {
            throw new IllegalStateException("Cannot recycle a resource that has already been recycled");
        } else {
            this.d = true;
            this.e.c();
        }
    }

    void d() {
        if (this.d) {
            throw new IllegalStateException("Cannot acquire a recycled resource");
        } else if (Looper.getMainLooper().equals(Looper.myLooper())) {
            this.c++;
        } else {
            throw new IllegalThreadStateException("Must call acquire on the main thread");
        }
    }

    void e() {
        if (this.c <= 0) {
            throw new IllegalStateException("Cannot release a recycled or not yet acquired resource");
        } else if (Looper.getMainLooper().equals(Looper.myLooper())) {
            int i = this.c - 1;
            this.c = i;
            if (i == 0 && this.a != null) {
                this.a.b(this.b, this);
            }
        } else {
            throw new IllegalThreadStateException("Must call release on the main thread");
        }
    }

    public boolean f() {
        return this.e.f();
    }

    public String toString() {
        return "EngineResource{, listener=" + this.a + ", key=" + this.b + ", acquired=" + this.c + ", isRecycled=" + this.d + ", resource=" + this.e + '}';
    }
}
