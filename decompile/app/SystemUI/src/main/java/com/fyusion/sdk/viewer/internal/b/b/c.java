package com.fyusion.sdk.viewer.internal.b.b;

import android.support.v4.util.Pools$Pool;
import android.util.Log;
import com.fyusion.sdk.viewer.FyuseException;
import com.fyusion.sdk.viewer.d;
import com.fyusion.sdk.viewer.internal.b.b;
import com.fyusion.sdk.viewer.internal.b.e;

/* compiled from: Unknown */
public class c extends a<c> implements com.fyusion.sdk.viewer.internal.b.b.a, a {
    private final b c = new b();
    private int d;
    private String e;
    private int f;
    private int g;
    private a h;
    private long i;

    /* compiled from: Unknown */
    public interface a {
        void a(int i, int i2, com.fyusion.sdk.core.a.b bVar);

        void a(c cVar, int i, int i2);
    }

    public c(Pools$Pool<c> pools$Pool) {
        super(pools$Pool);
    }

    public c a(e eVar, int i, String str, int i2, int i3, d dVar, a aVar, int i4) {
        super.a(eVar, dVar, this, i4);
        this.d = i;
        this.e = str;
        this.f = i2;
        this.g = i3;
        this.h = aVar;
        return this;
    }

    public void a(int i) {
        Log.d("DecodeJob", "decode job complete: " + f() + " in " + com.fyusion.sdk.core.util.d.a(this.i));
        this.h.a(this, this.d, i);
        g();
    }

    public void a(int i, com.fyusion.sdk.core.a.b bVar) {
        this.h.a(this.d, i, bVar);
    }

    public void a(FyuseException fyuseException) {
        Log.e("DecodeJob", "onJobFailed: ", fyuseException);
    }

    protected void a(RuntimeException runtimeException) {
        Log.e("DecodeJob", "handleException: ", runtimeException);
    }

    protected void c() {
        Log.d("DecodeJob", "doCancel: " + f());
        this.c.a();
    }

    protected void d() {
        Log.d("DecodeJob", "decode job start: " + f());
        this.i = com.fyusion.sdk.core.util.d.a();
        this.c.a(this.e, this.f, this.g, (com.fyusion.sdk.viewer.internal.b.b.a) this);
    }

    protected void e() {
        this.d = -1;
        this.e = null;
        this.f = -1;
        this.g = -1;
        this.c.b();
    }
}
