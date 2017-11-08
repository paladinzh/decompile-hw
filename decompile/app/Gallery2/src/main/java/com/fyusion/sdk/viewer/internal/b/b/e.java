package com.fyusion.sdk.viewer.internal.b.b;

import android.support.v4.util.Pools$Pool;
import android.util.Log;
import com.fyusion.sdk.common.i;
import com.fyusion.sdk.core.a.b;
import com.fyusion.sdk.core.a.h;
import com.fyusion.sdk.core.util.pool.ByteBufferPool;
import com.fyusion.sdk.viewer.FyuseException;
import com.fyusion.sdk.viewer.d;
import com.fyusion.sdk.viewer.internal.c;

/* compiled from: Unknown */
public class e extends a<e> implements a, com.fyusion.sdk.viewer.internal.b.c.a, com.fyusion.sdk.viewer.internal.c.a {
    private int c;
    private int d;
    private int e;
    private int f;
    private int g;
    private b h;
    private c i;
    private a j;
    private com.fyusion.sdk.viewer.internal.b.c k = new com.fyusion.sdk.viewer.internal.b.c();

    /* compiled from: Unknown */
    public interface a {
        void a(e eVar, int i, int i2);

        void a(e eVar, FyuseException fyuseException);
    }

    public e(Pools$Pool<e> pools$Pool, c cVar) {
        super(pools$Pool);
        this.i = cVar;
    }

    public int a() {
        return this.e;
    }

    public h a(i iVar) {
        h a = iVar.a(this.f, this.g, this.e);
        if (a != null) {
            this.k.a(this.h, a, this);
        }
        return a;
    }

    public e a(com.fyusion.sdk.viewer.internal.b.e eVar, int i, int i2, int i3, int i4, int i5, b bVar, d dVar, a aVar, int i6) {
        super.a(eVar, dVar, this, i6);
        this.c = i;
        this.d = i2;
        this.e = i3;
        this.f = i4;
        this.g = i5;
        this.h = bVar;
        this.j = aVar;
        return this;
    }

    public void a(FyuseException fyuseException) {
        Log.e("EncodeJob", "EncodeJob failed", fyuseException);
        this.j.a(this, fyuseException);
    }

    protected void a(RuntimeException runtimeException) {
        Log.e("EncodeJob", "handleException: ", runtimeException);
        this.j.a(this, new FyuseException(runtimeException.getMessage()));
    }

    public void a(String str) {
        j();
    }

    public void b() {
        this.j.a(this, this.c, this.d);
        g();
    }

    protected void c() {
        this.k.a();
    }

    protected void d() {
        this.i.a(f(), this);
    }

    protected void e() {
        ByteBufferPool.INSTANCE.release(this.h.a());
        this.c = -1;
        this.d = -1;
        this.e = -1;
        this.f = -1;
        this.g = -1;
        this.k.b();
    }
}
