package com.fyusion.sdk.viewer.internal.b.b;

import android.os.Looper;
import android.os.MessageQueue.IdleHandler;
import android.support.v4.util.Pools$Pool;
import android.util.Log;
import com.fyusion.sdk.viewer.internal.b.b.a.k;
import java.io.File;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public class f implements com.fyusion.sdk.viewer.internal.b.b.a.k.a, h, a {
    private final Map<com.fyusion.sdk.viewer.internal.b.e, g> a;
    private final j b;
    private final k c;
    private final com.fyusion.sdk.viewer.internal.c d;
    private final d e;
    private final Map<com.fyusion.sdk.viewer.internal.b.e, WeakReference<k<?>>> f;
    private final p g;
    private final e h;
    private final i i;
    private final a j;
    private final c k;
    private ReferenceQueue<k<?>> l;

    /* compiled from: Unknown */
    static class a {
        private final Pools$Pool<c> a = com.fyusion.sdk.viewer.internal.f.a.a.a(150, new com.fyusion.sdk.viewer.internal.f.a.a.a<c>(this) {
            final /* synthetic */ a a;

            {
                this.a = r1;
            }

            public c a() {
                return new c(this.a.a);
            }

            public /* synthetic */ Object b() {
                return a();
            }
        });
        private int b;

        a() {
        }

        c a(com.fyusion.sdk.viewer.internal.b.e eVar, int i, String str, int i2, int i3, com.fyusion.sdk.viewer.d dVar, com.fyusion.sdk.viewer.internal.b.b.c.a aVar) {
            c cVar = (c) this.a.acquire();
            int i4 = this.b;
            this.b = i4 + 1;
            return cVar.a(eVar, i, str, i2, i3, dVar, aVar, i4);
        }
    }

    /* compiled from: Unknown */
    public interface b {
        com.fyusion.sdk.viewer.internal.b.b.a.a a();
    }

    /* compiled from: Unknown */
    static class c {
        private com.fyusion.sdk.viewer.internal.c a;
        private final Pools$Pool<e> b = com.fyusion.sdk.viewer.internal.f.a.a.a(150, new com.fyusion.sdk.viewer.internal.f.a.a.a<e>(this) {
            final /* synthetic */ c a;

            {
                this.a = r1;
            }

            public e a() {
                return new e(this.a.b, this.a.a);
            }

            public /* synthetic */ Object b() {
                return a();
            }
        });
        private int c;

        c(com.fyusion.sdk.viewer.internal.c cVar) {
            this.a = cVar;
        }

        e a(com.fyusion.sdk.viewer.internal.b.e eVar, int i, int i2, int i3, int i4, int i5, com.fyusion.sdk.core.a.b bVar, com.fyusion.sdk.viewer.d dVar, com.fyusion.sdk.viewer.internal.b.b.e.a aVar) {
            e eVar2 = (e) this.b.acquire();
            int i6 = this.c;
            this.c = i6 + 1;
            return eVar2.a(eVar, i, i2, i3, i4, i5, bVar, dVar, aVar, i6);
        }
    }

    /* compiled from: Unknown */
    static class d {
        private final l a;
        private final l b;
        private final l c;
        private final l d;
        private final b e;
        private final h f;
        private final Pools$Pool<g<?>> g = com.fyusion.sdk.viewer.internal.f.a.a.a(150, new com.fyusion.sdk.viewer.internal.f.a.a.a<g<?>>(this) {
            final /* synthetic */ d a;

            {
                this.a = r1;
            }

            public g<?> a() {
                return new g(this.a.a, this.a.b, this.a.c, this.a.d, this.a.e, this.a.f, this.a.g);
            }

            public /* synthetic */ Object b() {
                return a();
            }
        });

        d(l lVar, l lVar2, l lVar3, l lVar4, b bVar, h hVar) {
            this.a = lVar;
            this.b = lVar2;
            this.c = lVar3;
            this.d = lVar4;
            this.e = bVar;
            this.f = hVar;
        }

        <R> g<R> a(com.fyusion.sdk.viewer.internal.b bVar, com.fyusion.sdk.viewer.internal.b.e eVar, Object obj, d dVar, boolean z, com.fyusion.sdk.viewer.d dVar2) {
            return ((g) this.g.acquire()).a(bVar, eVar, obj, dVar, z, dVar2);
        }
    }

    /* compiled from: Unknown */
    private static class e implements b {
        private final com.fyusion.sdk.viewer.internal.b.b.a.a.b a;
        private final boolean b;
        private volatile com.fyusion.sdk.viewer.internal.b.b.a.a c;

        public e(com.fyusion.sdk.viewer.internal.b.b.a.a.b bVar) {
            this(bVar, false);
        }

        public e(com.fyusion.sdk.viewer.internal.b.b.a.a.b bVar, boolean z) {
            this.a = bVar;
            this.b = z;
        }

        public com.fyusion.sdk.viewer.internal.b.b.a.a a() {
            if (this.c == null) {
                synchronized (this) {
                    if (this.c == null) {
                        this.c = this.a.a();
                        if (this.b && this.c != null) {
                            this.c.a();
                        }
                    }
                    if (this.c == null) {
                        this.c = new com.fyusion.sdk.viewer.internal.b.b.a.b();
                    }
                }
            }
            return this.c;
        }
    }

    /* compiled from: Unknown */
    public static class f {
        private final g a;
        private final com.fyusion.sdk.viewer.internal.request.d b;

        public f(com.fyusion.sdk.viewer.internal.request.d dVar, g gVar) {
            this.b = dVar;
            this.a = gVar;
        }

        public void a() {
            this.a.b(this.b);
        }
    }

    /* compiled from: Unknown */
    private static class g implements IdleHandler {
        private final Map<com.fyusion.sdk.viewer.internal.b.e, WeakReference<k<?>>> a;
        private final ReferenceQueue<k<?>> b;

        public g(Map<com.fyusion.sdk.viewer.internal.b.e, WeakReference<k<?>>> map, ReferenceQueue<k<?>> referenceQueue) {
            this.a = map;
            this.b = referenceQueue;
        }

        public boolean queueIdle() {
            h hVar = (h) this.b.poll();
            if (hVar != null) {
                this.a.remove(hVar.a);
            }
            return true;
        }
    }

    /* compiled from: Unknown */
    private static class h extends WeakReference<k<?>> {
        private final com.fyusion.sdk.viewer.internal.b.e a;

        public h(com.fyusion.sdk.viewer.internal.b.e eVar, k<?> kVar, ReferenceQueue<? super k<?>> referenceQueue) {
            super(kVar, referenceQueue);
            this.a = eVar;
        }
    }

    /* compiled from: Unknown */
    static class i {
        private final Pools$Pool<r> a = com.fyusion.sdk.viewer.internal.f.a.a.a(150, new com.fyusion.sdk.viewer.internal.f.a.a.a<r>(this) {
            final /* synthetic */ i a;

            {
                this.a = r1;
            }

            public r a() {
                return new r(this.a.a);
            }

            public /* synthetic */ Object b() {
                return a();
            }
        });
        private int b;

        i() {
        }

        <R, F> r a(com.fyusion.sdk.viewer.internal.b.e eVar, int i, m mVar, com.fyusion.sdk.viewer.d dVar, a<com.fyusion.sdk.viewer.internal.b.c.a, File> aVar) {
            r rVar = (r) this.a.acquire();
            int i2 = this.b;
            this.b = i2 + 1;
            return rVar.a(eVar, i, mVar, dVar, aVar, i2);
        }
    }

    public f(k kVar, com.fyusion.sdk.viewer.internal.b.b.a.a.b bVar, com.fyusion.sdk.viewer.internal.b.b.a.a.b bVar2, l lVar, l lVar2, l lVar3, l lVar4) {
        this(kVar, bVar, bVar2, lVar, lVar2, lVar3, lVar4, null, null, null, null, null, null, null, null);
    }

    f(k kVar, com.fyusion.sdk.viewer.internal.b.b.a.a.b bVar, com.fyusion.sdk.viewer.internal.b.b.a.a.b bVar2, l lVar, l lVar2, l lVar3, l lVar4, Map<com.fyusion.sdk.viewer.internal.b.e, g> map, j jVar, Map<com.fyusion.sdk.viewer.internal.b.e, WeakReference<k<?>>> map2, d dVar, i iVar, a aVar, c cVar, p pVar) {
        this.c = kVar;
        this.h = new e(bVar);
        if (map2 == null) {
            map2 = new HashMap();
        }
        this.f = map2;
        if (jVar == null) {
            jVar = new j();
        }
        this.b = jVar;
        if (map == null) {
            map = new HashMap();
        }
        this.a = map;
        if (dVar == null) {
            dVar = new d(lVar, lVar2, lVar3, lVar4, this.h, this);
        }
        this.e = dVar;
        this.d = new com.fyusion.sdk.viewer.internal.c(new e(bVar2, true));
        if (iVar == null) {
            iVar = new i();
        }
        this.i = iVar;
        if (aVar == null) {
            aVar = new a();
        }
        this.j = aVar;
        if (cVar == null) {
            c cVar2 = new c(this.d);
        }
        this.k = cVar;
        if (pVar == null) {
            pVar = new p();
        }
        this.g = pVar;
        kVar.a((com.fyusion.sdk.viewer.internal.b.b.a.k.a) this);
    }

    private k<?> a(com.fyusion.sdk.viewer.internal.b.e eVar) {
        WeakReference weakReference = (WeakReference) this.f.get(eVar);
        if (weakReference == null) {
            return null;
        }
        k<?> kVar = (k) weakReference.get();
        if (kVar == null) {
            this.f.remove(eVar);
            return kVar;
        }
        kVar.d();
        return kVar;
    }

    private static void a(String str, long j, com.fyusion.sdk.viewer.internal.b.e eVar) {
        Log.v("Engine", str + " in " + com.fyusion.sdk.core.util.d.a(j) + "ms, key: " + eVar);
    }

    private k<?> b(com.fyusion.sdk.viewer.internal.b.e eVar) {
        k<?> c = c(eVar);
        if (c != null) {
            c.d();
            this.f.put(eVar, new h(eVar, c, b()));
        }
        return c;
    }

    private ReferenceQueue<k<?>> b() {
        if (this.l == null) {
            this.l = new ReferenceQueue();
            Looper.myQueue().addIdleHandler(new g(this.f, this.l));
        }
        return this.l;
    }

    private k<?> c(com.fyusion.sdk.viewer.internal.b.e eVar) {
        o a = this.c.a(eVar);
        return a != null ? !(a instanceof k) ? new k(a) : (k) a : null;
    }

    public <R> f a(com.fyusion.sdk.viewer.internal.b bVar, Object obj, int i, int i2, boolean z, com.fyusion.sdk.viewer.d dVar, d dVar2, com.fyusion.sdk.viewer.internal.request.d dVar3) {
        com.fyusion.sdk.viewer.internal.f.e.a();
        long a = com.fyusion.sdk.core.util.d.a();
        Log.d("Engine", "loading: " + obj);
        com.fyusion.sdk.viewer.internal.b.e a2 = this.b.a(obj, z);
        o b = b(a2);
        if (b == null) {
            b = a(a2);
            if (b == null) {
                g gVar = (g) this.a.get(a2);
                if (gVar == null) {
                    g a3 = this.e.a(bVar, a2, obj, dVar2, z, dVar);
                    r a4 = this.i.a(a2, -1, a3.a(), dVar, a3);
                    this.a.put(a2, a3);
                    a3.a(dVar3);
                    a3.a(a4);
                    if (Log.isLoggable("Engine", 2)) {
                        a("Started new load", a, a2);
                    }
                    return new f(dVar3, a3);
                }
                gVar.a(dVar3);
                if (Log.isLoggable("Engine", 2)) {
                    a("Added to existing load", a, a2);
                }
                return new f(dVar3, gVar);
            }
            Log.d("Engine", "fetch fyuseData from activeResource: " + a2);
            dVar3.a(b, com.fyusion.sdk.viewer.internal.b.a.MEMORY_CACHE);
            if (Log.isLoggable("Engine", 2)) {
                a("Loaded resource from active resources", a, a2);
            }
            return null;
        }
        Log.d("Engine", "fetch fyuseData from cache: " + a2);
        dVar3.a(b, com.fyusion.sdk.viewer.internal.b.a.MEMORY_CACHE);
        if (Log.isLoggable("Engine", 2)) {
            a("Loaded resource from cache", a, a2);
        }
        return null;
    }

    public com.fyusion.sdk.viewer.internal.c a() {
        return this.d;
    }

    public void a(g gVar, com.fyusion.sdk.viewer.internal.b.e eVar) {
        com.fyusion.sdk.viewer.internal.f.e.a();
        if (gVar.equals((g) this.a.get(eVar))) {
            this.a.remove(eVar);
        }
    }

    public void a(g gVar, com.fyusion.sdk.viewer.internal.b.e eVar, int i) {
        if (Log.isLoggable("Engine", 3)) {
            Log.d("Engine", "assign download : " + i);
        }
        m a = gVar.a();
        gVar.a(this.i.a(eVar, i, a, a.i(), gVar));
    }

    public void a(g gVar, com.fyusion.sdk.viewer.internal.b.e eVar, int i, int i2, com.fyusion.sdk.core.a.b bVar) {
        m a = gVar.a();
        gVar.a(this.k.a(eVar, i, i2, a.b(i, i2), a.j(), a.k(), bVar, a.i(), gVar));
    }

    public void a(g gVar, com.fyusion.sdk.viewer.internal.b.e eVar, int i, File file) {
        m a = gVar.a();
        gVar.a(this.j.a(eVar, i, file.getAbsolutePath(), a.j(), a.k(), a.i(), gVar));
    }

    public void a(o oVar) {
        com.fyusion.sdk.viewer.internal.f.e.a();
        if (oVar instanceof k) {
            ((k) oVar).e();
            return;
        }
        throw new IllegalArgumentException("Cannot release anything but an EngineResource");
    }

    public void a(com.fyusion.sdk.viewer.internal.b.e eVar, k<?> kVar) {
        com.fyusion.sdk.viewer.internal.f.e.a();
        if (kVar != null && kVar.f()) {
            kVar.a(eVar, this);
            this.f.put(eVar, new h(eVar, kVar, b()));
        }
        this.a.remove(eVar);
    }

    public void b(o<?> oVar) {
        com.fyusion.sdk.viewer.internal.f.e.a();
        this.g.a(oVar);
    }

    public void b(com.fyusion.sdk.viewer.internal.b.e eVar, k kVar) {
        com.fyusion.sdk.viewer.internal.f.e.a();
        this.f.remove(eVar);
        this.c.b(eVar, kVar);
    }
}
