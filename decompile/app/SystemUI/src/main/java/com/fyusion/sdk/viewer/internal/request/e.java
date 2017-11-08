package com.fyusion.sdk.viewer.internal.request;

import android.graphics.drawable.Drawable;
import android.support.v4.util.Pools$Pool;
import android.util.Log;
import com.fyusion.sdk.viewer.FyuseException;
import com.fyusion.sdk.viewer.RequestListener;
import com.fyusion.sdk.viewer.d;
import com.fyusion.sdk.viewer.internal.b.b.f;
import com.fyusion.sdk.viewer.internal.b.b.o;
import com.fyusion.sdk.viewer.internal.f.a.a.c;
import com.fyusion.sdk.viewer.internal.f.a.b;
import com.fyusion.sdk.viewer.internal.request.target.Target;

/* compiled from: Unknown */
public final class e<R> implements c, b, d {
    private static final Pools$Pool<e<?>> a = com.fyusion.sdk.viewer.internal.f.a.a.a(150, new com.fyusion.sdk.viewer.internal.f.a.a.a<e<?>>() {
        public e<?> a() {
            return new e();
        }

        public /* synthetic */ Object b() {
            return a();
        }
    });
    private final String b;
    private final b c;
    private com.fyusion.sdk.viewer.internal.b d;
    private Object e;
    private a<?> f;
    private int g;
    private int h;
    private d i;
    private Target<R> j;
    private RequestListener<R> k;
    private f l;
    private o<R> m;
    private f.f n;
    private long o;
    private a p;
    private Drawable q;
    private Drawable r;
    private int s;
    private int t;
    private int u;

    /* compiled from: Unknown */
    private enum a {
        PENDING,
        RUNNING,
        WAITING_FOR_SIZE,
        COMPLETE,
        FAILED,
        CANCELLED,
        CLEARED,
        PAUSED
    }

    private e() {
        this.b = String.valueOf(hashCode());
        this.c = b.a();
        this.u = 0;
    }

    public static <R> e<R> a(com.fyusion.sdk.viewer.internal.b bVar, Object obj, a<?> aVar, int i, int i2, d dVar, Target<R> target, RequestListener<R> requestListener, f fVar) {
        e<R> eVar = (e) a.acquire();
        if (eVar == null) {
            eVar = new e();
        }
        eVar.b(bVar, obj, aVar, i, i2, dVar, target, requestListener, fVar);
        return eVar;
    }

    private void a(o<R> oVar, R r, com.fyusion.sdk.viewer.internal.b.a aVar) {
        this.p = a.COMPLETE;
        this.m = oVar;
        if (this.k == null || !this.k.onResourceReady(this.e)) {
            this.j.onResourceReady(r);
        }
    }

    private void b(o oVar) {
        this.l.a(oVar);
        this.m = null;
    }

    private void b(com.fyusion.sdk.viewer.internal.b bVar, Object obj, a<?> aVar, int i, int i2, d dVar, Target<R> target, RequestListener<R> requestListener, f fVar) {
        this.d = bVar;
        this.e = obj;
        this.f = aVar;
        this.g = i;
        this.h = i2;
        this.i = dVar;
        this.j = target;
        this.k = requestListener;
        this.l = fVar;
        this.p = a.PENDING;
    }

    private Drawable i() {
        if (this.q == null) {
            this.q = this.f.d();
        }
        return this.q;
    }

    private Drawable j() {
        if (this.r == null) {
            this.r = this.f.e();
        }
        return this.r;
    }

    private void l() {
        Drawable i = i();
        if (i == null) {
            i = j();
        }
        this.j.onLoadFailed(i);
    }

    private void m() {
        this.c.b();
        if (this.p == a.WAITING_FOR_SIZE) {
            this.p = a.RUNNING;
            this.n = this.l.a(this.d, this.e, this.s, this.t, this.f.i(), this.i, this.f.c(), this);
        }
    }

    public void a() {
        this.c.b();
        this.o = com.fyusion.sdk.core.util.d.a();
        if (this.e != null) {
            this.p = a.WAITING_FOR_SIZE;
            m();
            if (this.p == a.RUNNING || this.p == a.WAITING_FOR_SIZE) {
                this.j.onLoadStarted(j());
            }
            return;
        }
        if (com.fyusion.sdk.viewer.internal.f.e.a(this.g, this.h)) {
            this.s = this.g;
            this.t = this.h;
        }
        a(new FyuseException("Received null model"));
    }

    public void a(int i, int i2, int i3, Object obj) {
        this.u++;
        if (this.j != null) {
            this.j.onProcessingSliceProgress(i2, i3, obj);
            if (this.k != null && i3 > 0) {
                this.k.onProgress((this.u * 100) / i3);
            }
        }
    }

    public void a(FyuseException fyuseException) {
        this.c.b();
        Log.w("Fyuse", "Load failed for " + this.e + " with size [" + this.s + "x" + this.t + "]", fyuseException);
        this.n = null;
        this.p = a.FAILED;
        if (this.k == null || !this.k.onLoadFailed(fyuseException, this.e)) {
            l();
        }
    }

    public void a(o<?> oVar) {
        this.j.onMetadataReady(oVar.a());
    }

    public void a(o<?> oVar, com.fyusion.sdk.viewer.internal.b.a aVar) {
        this.c.b();
        this.n = null;
        if (oVar != null) {
            Object a = oVar.a();
            if (a != null) {
                a(oVar, a, aVar);
                return;
            }
            b(oVar);
            a(new FyuseException("Expected to receive an object but instead got {" + a + "} inside" + " " + "Resource{" + oVar + "}." + "To indicate failure return a null Resource " + "object, rather than a Resource object containing null data."));
            return;
        }
        a(new FyuseException("Expected to receive a Resource<R>."));
    }

    public void b() {
        c();
        this.p = a.PAUSED;
    }

    public void c() {
        com.fyusion.sdk.viewer.internal.f.e.a();
        if (this.p != a.CLEARED) {
            h();
            if (this.m != null) {
                b(this.m);
            }
            this.j.onLoadCleared(j());
            this.p = a.CLEARED;
        }
    }

    public boolean d() {
        return this.p == a.RUNNING || this.p == a.WAITING_FOR_SIZE;
    }

    public boolean e() {
        return this.p == a.COMPLETE;
    }

    public boolean f() {
        return this.p == a.CANCELLED || this.p == a.CLEARED;
    }

    public void g() {
        this.e = null;
        this.g = -1;
        this.h = -1;
        this.j = null;
        this.k = null;
        this.n = null;
        this.q = null;
        this.r = null;
        this.s = -1;
        this.t = -1;
        this.u = 0;
        a.release(this);
    }

    void h() {
        this.c.b();
        this.p = a.CANCELLED;
        if (this.n != null) {
            this.n.a();
            this.n = null;
        }
    }

    public b k() {
        return this.c;
    }
}
