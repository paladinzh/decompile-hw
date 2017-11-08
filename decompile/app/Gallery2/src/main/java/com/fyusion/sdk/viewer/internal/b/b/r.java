package com.fyusion.sdk.viewer.internal.b.b;

import android.support.v4.util.Pools$Pool;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.viewer.FyuseException;
import com.fyusion.sdk.viewer.d;
import com.fyusion.sdk.viewer.internal.b.e;
import java.io.File;

/* compiled from: Unknown */
class r extends a<r> implements a, com.fyusion.sdk.viewer.internal.b.b.b.a, a {
    private m c;
    private a<com.fyusion.sdk.viewer.internal.b.c.a, File> d;
    private b e;
    private volatile b f;
    private int g;

    /* compiled from: Unknown */
    interface a<R, F> {
        void a(int i, o<F> oVar, com.fyusion.sdk.viewer.internal.b.a aVar);

        void a(FyuseException fyuseException);

        void a(o<R> oVar, com.fyusion.sdk.viewer.internal.b.a aVar);
    }

    /* compiled from: Unknown */
    /* renamed from: com.fyusion.sdk.viewer.internal.b.b.r$1 */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] a = new int[b.values().length];

        static {
            try {
                a[b.INITIALIZE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                a[b.METADATA.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                a[b.SOURCE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                a[b.FINISHED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    /* compiled from: Unknown */
    private enum b {
        INITIALIZE,
        DATA_CACHE,
        SOURCE,
        METADATA,
        FINISHED
    }

    r(Pools$Pool<r> pools$Pool) {
        super(pools$Pool);
    }

    private b a(b bVar) {
        switch (AnonymousClass1.a[bVar.ordinal()]) {
            case 1:
                return this.c.f() != null ? a(b.METADATA) : b.METADATA;
            case 2:
                return b.SOURCE;
            case 3:
            case 4:
                return b.FINISHED;
            default:
                throw new IllegalArgumentException("Unrecognized stage: " + bVar);
        }
    }

    private void b() {
        boolean z = false;
        while (!this.a && this.f != null) {
            z = this.f.a();
            if (z) {
                break;
            }
            this.e = a(this.e);
            this.f = l();
        }
        if (this.e != b.FINISHED && !this.a) {
            return;
        }
        if (!z) {
            j();
        }
    }

    private b l() {
        switch (AnonymousClass1.a[this.e.ordinal()]) {
            case 2:
                return new n(this.c, this, this);
            case 3:
                return new q(this.g, this.c, this);
            case 4:
                return null;
            default:
                throw new IllegalStateException("Unrecognized stage: " + this.e);
        }
    }

    r a(e eVar, int i, m mVar, d dVar, a<com.fyusion.sdk.viewer.internal.b.c.a, File> aVar, int i2) {
        super.a(eVar, dVar, this, i2);
        this.g = i;
        this.c = mVar;
        this.d = aVar;
        return this;
    }

    public void a(FyuseException fyuseException) {
        this.d.a(fyuseException);
    }

    public void a(com.fyusion.sdk.viewer.internal.b.c.a aVar, com.fyusion.sdk.viewer.internal.b.a aVar2) {
        g();
        this.c.a(aVar.n());
        this.d.a(new com.fyusion.sdk.viewer.internal.b.d.b(aVar), aVar2);
    }

    public void a(e eVar, Exception exception, com.fyusion.sdk.viewer.internal.b.a.a<?> aVar, com.fyusion.sdk.viewer.internal.b.a aVar2) {
        if (this.e == b.SOURCE && this.c.b() && (exception instanceof com.fyusion.sdk.viewer.internal.b.d) && ((com.fyusion.sdk.viewer.internal.b.d) exception).a() == 403) {
            DLog.d("SourceJob", "High profile does not exist for " + eVar.d() + " fallback to base profile");
            this.c.a(true);
            b();
            return;
        }
        this.b.add(exception);
        DLog.d("SourceJob", "Fetching data failed: " + eVar);
        j();
    }

    public void a(e eVar, Object obj, com.fyusion.sdk.viewer.internal.b.a.a<?> aVar, com.fyusion.sdk.viewer.internal.b.a aVar2) {
        g();
        if (this.d != null) {
            this.d.a(this.g, new com.fyusion.sdk.viewer.internal.b.d.a((File) obj), aVar2);
        }
    }

    protected void a(RuntimeException runtimeException) {
        DLog.w("SourceJob", "SourceJob threw unexpectedly, isCancelled: " + this.a + ", stage: " + this.e, runtimeException);
        j();
        if (!this.a) {
            throw runtimeException;
        }
    }

    boolean a() {
        return a(b.INITIALIZE) == b.DATA_CACHE;
    }

    protected void c() {
        b bVar = this.f;
        if (bVar != null) {
            bVar.b();
        }
    }

    protected void d() {
        this.e = a(b.INITIALIZE);
        this.f = l();
        b();
    }

    protected void e() {
        this.g = -1;
        this.d = null;
        this.e = null;
        this.f = null;
    }
}
