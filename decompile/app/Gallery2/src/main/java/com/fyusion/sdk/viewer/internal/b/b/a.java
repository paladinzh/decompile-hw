package com.fyusion.sdk.viewer.internal.b.b;

import android.support.v4.util.Pools$Pool;
import android.util.Log;
import com.fyusion.sdk.viewer.FyuseException;
import com.fyusion.sdk.viewer.d;
import com.fyusion.sdk.viewer.internal.b.e;
import com.fyusion.sdk.viewer.internal.f.a.a.c;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public abstract class a<T extends a<T>> implements c, Comparable<a>, Runnable {
    protected volatile boolean a;
    protected final List<Exception> b = new ArrayList();
    private final com.fyusion.sdk.viewer.internal.f.a.b c = com.fyusion.sdk.viewer.internal.f.a.b.a();
    private final Pools$Pool<T> d;
    private final b e = new b();
    private e f;
    private d g;
    private a h;
    private int i;
    private volatile boolean j;

    /* compiled from: Unknown */
    interface a {
        void a(FyuseException fyuseException);
    }

    /* compiled from: Unknown */
    private static class b {
        private boolean a;
        private boolean b;
        private boolean c;

        private b() {
        }

        private boolean b(boolean z) {
            return (this.c || z || this.b) ? this.a : false;
        }

        synchronized boolean a() {
            this.b = true;
            return b(false);
        }

        synchronized boolean a(boolean z) {
            this.a = true;
            return b(z);
        }

        synchronized boolean b() {
            this.c = true;
            return b(false);
        }

        synchronized void c() {
            this.b = false;
            this.a = false;
            this.c = false;
        }
    }

    public a(Pools$Pool<T> pools$Pool) {
        this.d = pools$Pool;
    }

    private void a() {
        e();
        this.e.c();
        this.j = false;
        this.f = null;
        this.g = null;
        this.h = null;
        this.a = false;
        this.b.clear();
        this.d.release(this);
    }

    private void b() {
        this.c.b();
        if (this.j) {
            throw new IllegalStateException("Already notified");
        }
        this.j = true;
    }

    private int l() {
        return this.g.ordinal();
    }

    public int a(a aVar) {
        int l = l() - aVar.l();
        return l != 0 ? l : this.i - aVar.i;
    }

    void a(e eVar, d dVar, a aVar, int i) {
        this.f = eVar;
        this.g = dVar;
        this.h = aVar;
        this.i = i;
    }

    protected abstract void a(RuntimeException runtimeException);

    void a(boolean z) {
        if (this.e.a(z)) {
            a();
        }
    }

    protected abstract void c();

    public /* synthetic */ int compareTo(Object obj) {
        return a((a) obj);
    }

    protected abstract void d();

    protected abstract void e();

    public e f() {
        return this.f;
    }

    protected void g() {
        if (this.e.a()) {
            a();
        }
    }

    protected void h() {
        if (this.e.b()) {
            a();
        }
    }

    public void i() {
        this.a = true;
        c();
    }

    protected void j() {
        b();
        this.h.a(new FyuseException("Failed to execute job " + this.a, new ArrayList(this.b)));
        h();
    }

    public com.fyusion.sdk.viewer.internal.f.a.b k() {
        return this.c;
    }

    public void run() {
        try {
            if (this.a) {
                Log.d("BasePoolableJob", "run is cancelled: " + this.f);
                j();
                return;
            }
            if (this.f != null) {
                d();
            }
        } catch (RuntimeException e) {
            a(e);
        }
    }
}
