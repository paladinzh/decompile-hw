package com.fyusion.sdk.viewer.internal.b.b;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.Pools$Pool;
import android.util.Log;
import com.fyusion.sdk.viewer.FyuseException;
import com.fyusion.sdk.viewer.internal.b.e;
import com.fyusion.sdk.viewer.internal.request.d;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/* compiled from: Unknown */
class g<R> implements com.fyusion.sdk.viewer.internal.b.b.c.a, com.fyusion.sdk.viewer.internal.b.b.e.a, a<com.fyusion.sdk.viewer.internal.b.c.a, File>, com.fyusion.sdk.viewer.internal.f.a.a.c {
    private static final a a = new a();
    private static final Handler b = new Handler(Looper.getMainLooper(), new c());
    private Iterator<Integer> A;
    private final List<d> c;
    private final com.fyusion.sdk.viewer.internal.f.a.b d;
    private final Pools$Pool<g<?>> e;
    private final a f;
    private final h g;
    private final l h;
    private final l i;
    private final l j;
    private final l k;
    private final com.fyusion.sdk.viewer.internal.b.b.f.b l;
    private m m;
    private e n;
    private o<com.fyusion.sdk.viewer.internal.b.c.a> o;
    private o<File> p;
    private com.fyusion.sdk.viewer.internal.b.a q;
    private boolean r;
    private FyuseException s;
    private boolean t;
    private List<d> u;
    private k<?> v;
    private r w;
    private List<a> x;
    private List<a> y;
    private volatile boolean z;

    /* compiled from: Unknown */
    static class a {
        a() {
        }

        public <R> k<R> a(o<R> oVar) {
            return new k(oVar);
        }
    }

    /* compiled from: Unknown */
    static class b {
        g a;
        a b;

        public b(g gVar, a aVar) {
            this.a = gVar;
            this.b = aVar;
        }
    }

    /* compiled from: Unknown */
    private static class c implements Callback {
        private c() {
        }

        public boolean handleMessage(Message message) {
            g gVar;
            a aVar = null;
            if (message.obj instanceof b) {
                b bVar = (b) message.obj;
                g gVar2 = bVar.a;
                aVar = bVar.b;
                gVar = gVar2;
            } else {
                gVar = (g) message.obj;
            }
            switch (message.what) {
                case 1:
                    gVar.d();
                    break;
                case 2:
                    gVar.a(message.arg1);
                    break;
                case 3:
                    gVar.a(message.arg1, message.arg2, aVar);
                    break;
                case 4:
                    gVar.b(message.arg1, message.arg2, aVar);
                    break;
                case 5:
                    gVar.g();
                    break;
                case 6:
                    gVar.h();
                    break;
                default:
                    throw new IllegalStateException("Unrecognized message: " + message.what);
            }
            return true;
        }
    }

    g(l lVar, l lVar2, l lVar3, l lVar4, com.fyusion.sdk.viewer.internal.b.b.f.b bVar, h hVar, Pools$Pool<g<?>> pools$Pool) {
        this(lVar, lVar2, lVar3, lVar4, bVar, hVar, pools$Pool, a);
    }

    g(l lVar, l lVar2, l lVar3, l lVar4, com.fyusion.sdk.viewer.internal.b.b.f.b bVar, h hVar, Pools$Pool<g<?>> pools$Pool, a aVar) {
        this.c = new ArrayList(2);
        this.d = com.fyusion.sdk.viewer.internal.f.a.b.a();
        this.m = new m();
        this.x = Collections.synchronizedList(new ArrayList());
        this.y = Collections.synchronizedList(new ArrayList());
        this.h = lVar;
        this.i = lVar2;
        this.j = lVar3;
        this.k = lVar4;
        this.l = bVar;
        this.g = hVar;
        this.e = pools$Pool;
        this.f = aVar;
    }

    private void a(int i) {
        if (this.o != null) {
            this.d.b();
            if (this.z) {
                Log.d("EngineJob", "cancelled after downloading mp4: " + this.n);
                this.o.c();
                a(false);
                return;
            }
            if (this.A.hasNext()) {
                this.g.a(this, this.n, ((Integer) this.A.next()).intValue());
            }
            this.g.a(this, this.n, i, (File) this.p.a());
        }
    }

    private void a(int i, int i2, a aVar) {
        if (this.x.remove(aVar)) {
            this.d.b();
            aVar.a(false);
            if (this.z) {
                this.o.c();
                a(false);
                return;
            }
            this.m.a(i, i2);
            return;
        }
        Log.d("EngineJob", "handleDecodeCompletedOnMainThread called when decodeJob is no longer in the list: " + this.n);
    }

    private void a(int i, int i2, Object obj) {
        for (d a : this.c) {
            a.a(0, i, i2, obj);
        }
    }

    private void a(List<a> list, l lVar) {
        synchronized (list) {
            Iterator it = list.iterator();
            while (it.hasNext()) {
                a aVar = (a) it.next();
                aVar.i();
                if (lVar.remove(aVar)) {
                    it.remove();
                    aVar.a(true);
                }
            }
        }
    }

    private void a(boolean z) {
        com.fyusion.sdk.viewer.internal.f.e.a();
        if (this.x.isEmpty() && this.y.isEmpty()) {
            Log.d("EngineJob", "release: " + this.n);
            this.c.clear();
            this.n = null;
            this.v = null;
            this.o = null;
            this.p = null;
            if (this.u != null) {
                this.u.clear();
            }
            this.t = false;
            this.z = false;
            this.r = false;
            this.w.a(z);
            this.w = null;
            this.m.c();
            this.s = null;
            this.q = null;
            this.A = null;
            this.e.release(this);
        }
    }

    private void b(int i, int i2, a aVar) {
        if (this.y.remove(aVar)) {
            this.d.b();
            aVar.a(false);
            if (this.z) {
                this.o.c();
                a(false);
                return;
            } else if (this.c.isEmpty()) {
                throw new IllegalStateException("Received a resource without any callbacks to notify");
            } else if (i >= 0 && i2 >= 0) {
                boolean c = this.m.c(i, i2);
                boolean z = this.m.c(i) && this.y.size() == 0;
                for (d dVar : this.c) {
                    if (!d(dVar)) {
                        dVar.a(i, this.m.b(i, i2), this.m.h(), null);
                    }
                }
                if (c && z && !this.r) {
                    g();
                }
                return;
            } else {
                return;
            }
        }
        Log.d("EngineJob", "handleSliceProgressOnMainThread called when encodeJob is no longer in the list: " + this.n);
    }

    private void c(d dVar) {
        if (this.u == null) {
            this.u = new ArrayList(2);
        }
        if (!this.u.contains(dVar)) {
            this.u.add(dVar);
        }
    }

    private void d() {
        this.d.b();
        if (this.z) {
            this.o.c();
            a(false);
        } else if (this.c.isEmpty()) {
            throw new IllegalStateException("Received a resource without any callbacks to notify");
        } else if (((com.fyusion.sdk.viewer.internal.b.c.a) this.o.a()).e()) {
            e();
        } else {
            if (this.A.hasNext()) {
                Log.d("EngineJob", "source location available: " + this.n);
                this.g.a(this, this.n, ((Integer) this.A.next()).intValue());
            }
            for (d dVar : this.c) {
                if (!d(dVar)) {
                    dVar.a(this.o);
                }
            }
        }
    }

    private boolean d(d dVar) {
        return this.u != null && this.u.contains(dVar);
    }

    private void e() {
        f();
        this.m.l().a((com.fyusion.sdk.viewer.internal.b.c.a) this.o.a(), (File) this.m.a(), new com.fyusion.sdk.viewer.e.a(this) {
            final /* synthetic */ g a;

            {
                this.a = r1;
            }

            public void a() {
                g.b.obtainMessage(5, this.a).sendToTarget();
            }

            public void a(int i, int i2, Object obj) {
                this.a.a(i, i2, obj);
            }

            public void a(String str) {
                this.a.s = new FyuseException(str);
                g.b.obtainMessage(6, this.a).sendToTarget();
            }
        });
    }

    private void f() {
        for (d dVar : this.c) {
            if (!d(dVar)) {
                dVar.a(this.o);
            }
        }
    }

    private void g() {
        this.d.b();
        if (this.z) {
            this.o.c();
            a(false);
        } else if (this.c.isEmpty()) {
            throw new IllegalStateException("Received a resource without any callbacks to notify");
        } else {
            Log.d("EngineJob", "engine job completed: " + this.n);
            this.v = this.f.a(this.o);
            this.r = true;
            this.v.d();
            this.g.a(this.n, this.v);
            for (d dVar : this.c) {
                if (!d(dVar)) {
                    this.v.d();
                    dVar.a(this.v, this.q);
                }
            }
            this.v.e();
            a(false);
        }
    }

    private void h() {
        this.d.b();
        if (this.z) {
            a(false);
        } else if (this.c.isEmpty()) {
            throw new IllegalStateException("Received an exception without any callbacks to notify");
        } else if (this.t) {
            throw new IllegalStateException("Already failed once");
        } else {
            this.t = true;
            this.g.a(this.n, null);
            for (d dVar : this.c) {
                if (!d(dVar)) {
                    dVar.a(this.s);
                }
            }
            a(false);
        }
    }

    g<R> a(com.fyusion.sdk.viewer.internal.b bVar, e eVar, Object obj, d dVar, boolean z, com.fyusion.sdk.viewer.d dVar2) {
        this.n = eVar;
        this.m.a(bVar, obj, z, dVar, dVar2, this.l);
        return this;
    }

    public m a() {
        return this.m;
    }

    public void a(int i, int i2, com.fyusion.sdk.core.a.b bVar) {
        this.g.a(this, this.n, i, i2, bVar);
    }

    public void a(int i, o<File> oVar, com.fyusion.sdk.viewer.internal.b.a aVar) {
        this.p = oVar;
        b.obtainMessage(2, i, 0, this).sendToTarget();
    }

    public void a(FyuseException fyuseException) {
        this.s = fyuseException;
        b.obtainMessage(6, this).sendToTarget();
    }

    public void a(c cVar) {
        if (this.n != null && this.n.equals(cVar.f())) {
            this.x.add(cVar);
            this.j.execute(cVar);
            return;
        }
        Log.d("EngineJob", "releasing decode job: " + this.n + " not equal to " + cVar.f());
        cVar.a(true);
    }

    public void a(c cVar, int i, int i2) {
        b.obtainMessage(3, i, i2, new b(this, cVar)).sendToTarget();
    }

    public void a(e eVar) {
        if (this.n != null && this.n.equals(eVar.f())) {
            this.y.add(eVar);
            this.k.execute(eVar);
            return;
        }
        Log.d("EngineJob", "releasing encode job: " + this.n + " not equal to " + eVar.f());
        eVar.a(true);
    }

    public void a(e eVar, int i, int i2) {
        b.obtainMessage(4, i, i2, new b(this, eVar)).sendToTarget();
    }

    public void a(e eVar, FyuseException fyuseException) {
        Log.e("EngineJob", "encode job failed: " + eVar.f());
        b.obtainMessage(4, -1, -1, new b(this, eVar)).sendToTarget();
    }

    public void a(o<com.fyusion.sdk.viewer.internal.b.c.a> oVar, com.fyusion.sdk.viewer.internal.b.a aVar) {
        this.o = oVar;
        this.q = aVar;
        this.A = this.m.g().iterator();
        b.obtainMessage(1, this).sendToTarget();
    }

    public void a(r rVar) {
        if (this.w != null) {
            this.w.a(false);
        }
        this.w = rVar;
        (!rVar.a() ? this.i : this.h).execute(rVar);
    }

    public void a(d dVar) {
        com.fyusion.sdk.viewer.internal.f.e.a();
        this.d.b();
        if (this.r) {
            dVar.a(this.v, this.q);
        } else if (this.t) {
            dVar.a(this.s);
        } else {
            this.c.add(dVar);
        }
    }

    void b() {
        boolean z = false;
        if (!this.t && !this.r && !this.z) {
            Log.d("EngineJob", "cancel: " + this.n);
            this.z = true;
            this.w.i();
            if (this.h.remove(this.w) || this.i.remove(this.w)) {
                z = true;
            }
            a(this.x, this.j);
            a(this.y, this.k);
            this.g.a(this, this.n);
            if (z) {
                a(true);
            }
            if (this.m.l() != null) {
                this.m.l().a();
            }
        }
    }

    public void b(d dVar) {
        com.fyusion.sdk.viewer.internal.f.e.a();
        this.d.b();
        if (this.r || this.t) {
            c(dVar);
            return;
        }
        this.c.remove(dVar);
        if (this.c.isEmpty()) {
            b();
        }
    }

    public com.fyusion.sdk.viewer.internal.f.a.b k() {
        return this.d;
    }
}
