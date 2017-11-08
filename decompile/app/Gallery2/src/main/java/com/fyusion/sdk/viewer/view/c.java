package com.fyusion.sdk.viewer.view;

import android.graphics.Matrix;
import com.fyusion.sdk.common.c.a;
import com.fyusion.sdk.common.d;
import com.fyusion.sdk.common.e;
import com.fyusion.sdk.common.h;
import com.fyusion.sdk.viewer.g;
import com.fyusion.sdk.viewer.i;
import com.fyusion.sdk.viewer.internal.b.c.a.b;
import com.huawei.watermark.manager.parse.WMElement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* compiled from: Unknown */
public class c implements b, com.fyusion.sdk.viewer.internal.request.target.b {
    private static float n = 1.75f;
    private static float o = WMElement.CAMERASIZEVALUE1B1;
    private static ExecutorService p = Executors.newSingleThreadExecutor();
    private g a = null;
    private h b = null;
    private float c = 0.0f;
    private float d = 0.0f;
    private d e = new d(0.0f, 0.0f);
    private h f = null;
    private g g = null;
    private b h;
    private final Object i = new Object();
    private volatile boolean j;
    private volatile boolean k;
    private e l;
    private float m = WMElement.CAMERASIZEVALUE1B1;

    public c(h hVar, boolean z) {
        this.b = hVar;
        this.a = new g(z);
        this.h = new b();
    }

    private void a(float f, a aVar, b bVar) {
        if (f >= 0.0f) {
            d b = this.a.b(f);
            if (b != null) {
                a(b, aVar);
            }
        } else if (bVar.b && this.a.f()) {
            a(null, aVar);
        }
    }

    private void a(d dVar, a aVar) {
        String str = null;
        if (dVar != null) {
            if (dVar.a() == null) {
                if (dVar.b() != null) {
                }
            }
            if (this.l != null) {
                Matrix matrix = new Matrix();
                e c = dVar.c();
                float a = this.a.a(c, this.l);
                float[] fArr = new float[9];
                com.fyusion.sdk.common.c.b(com.fyusion.sdk.common.c.a(aVar, com.fyusion.sdk.common.c.b(com.fyusion.sdk.common.c.a((double) a, (double) a))), fArr);
                matrix.setValues(fArr);
                matrix.postRotate((float) this.a.d());
                matrix.preTranslate((-((float) c.a)) / 2.0f, (-((float) c.b)) / 2.0f);
                matrix.postTranslate(((float) this.l.a) / 2.0f, ((float) this.l.b) / 2.0f);
                if (this.b.e()) {
                    i tweeningRenderer = this.b.getTweeningRenderer();
                    if (this.f != null) {
                        str = this.f.fyuseId;
                    }
                    dVar.a(tweeningRenderer, str);
                    this.b.setImageMatrixPending(matrix);
                    this.b.d();
                }
            }
        }
    }

    private void b(boolean z) {
        if (this.f != null && this.g != null) {
            if (z) {
                this.g.b();
            } else {
                this.g.a();
            }
        }
    }

    private void d() {
        d b = this.a.b((float) Math.max(this.f.getMagic().getThumbnailIndex(), 0));
        if (b == null) {
            b = this.a.b(0.0f);
        }
        this.c = 0.0f;
        this.d = 0.0f;
        a(b, this.a.a(this.c, this.d));
    }

    public void a() {
        if (!this.k) {
            synchronized (this.i) {
                this.k = true;
                if (this.j) {
                    b(true);
                }
            }
        } else if (this.j) {
            b(true);
        } else {
            synchronized (this.i) {
                if (this.j) {
                    b(true);
                }
            }
        }
    }

    void a(float f, float f2) {
        if (this.m > o) {
            this.b.a(f, f2);
        }
    }

    void a(float f, float f2, float f3) {
        float f4 = this.m;
        this.m *= f;
        if (this.m < o) {
            this.m = o;
        }
        if (this.m > n) {
            this.m = n;
        }
        if (f4 != this.m) {
            this.b.a(this.m, f2, f3);
        }
    }

    void a(float f, float f2, com.fyusion.sdk.viewer.a.b bVar) {
        boolean z = false;
        if (this.a.l()) {
            if (bVar != com.fyusion.sdk.viewer.a.b.HAS_ENDED) {
                this.a.b(false);
            }
            d dVar = new d(0.0f, 0.0f);
            d dVar2 = new d(0.0f, 0.0f);
            dVar2.a = f;
            dVar2.b = f2;
            if (Math.abs(dVar2.a) > 250.0f) {
                z = true;
            }
            if (z || Math.abs(dVar2.b) > 250.0f) {
                dVar2.b = 0.0f;
                dVar2.a = 0.0f;
            }
            float a = this.a.a(this.b, bVar, dVar2, dVar, this.h);
            this.c = dVar.a;
            this.d = dVar.b;
            a(a, this.a.a(this.c, this.d), this.h);
            if (bVar == com.fyusion.sdk.viewer.a.b.HAS_ENDED) {
                this.a.b(true);
            }
        }
    }

    void a(int i) {
        this.a.a(i);
    }

    public void a(int i, int i2) {
        this.l = new e((double) i, (double) i2);
        synchronized (this.i) {
            this.j = true;
            if (this.k) {
                b(true);
            }
        }
    }

    public void a(com.fyusion.sdk.viewer.c.a aVar) {
        d dVar = null;
        boolean z = false;
        if (this.a.k() || this.a.g()) {
            this.a.c(false);
            float a = this.a.a(aVar, this.e, this.h);
            if (a < 0.0f) {
                this.a.c(true);
                return;
            }
            boolean z2;
            this.c = this.e.a;
            this.d = this.e.b;
            float sqrt = (float) Math.sqrt((double) ((this.e.a * this.e.a) + (this.e.b * this.e.b)));
            if (sqrt > i.a) {
                sqrt = i.a / sqrt;
                d dVar2 = this.e;
                dVar2.a *= sqrt;
                dVar2 = this.e;
                dVar2.b = sqrt * dVar2.b;
            }
            if (a >= 0.0f) {
                dVar = this.a.b(a);
                z2 = dVar != null ? true : true;
            } else {
                z2 = this.a.f();
            }
            if (z2) {
                if (a >= 0.0f) {
                    z = true;
                }
                if (z || this.h.b) {
                    a(dVar, this.a.a(this.c, this.d));
                }
            }
            this.a.c(true);
        }
    }

    public void a(g gVar) {
        this.g = gVar;
    }

    public void a(com.fyusion.sdk.viewer.internal.b.c.a aVar) {
        this.a.a(aVar);
    }

    void a(boolean z) {
        this.a.a(z);
    }

    public void b() {
        if (this.f != null) {
            if (com.fyusion.sdk.viewer.internal.f.e.b()) {
                p.execute(new Runnable(this) {
                    final /* synthetic */ c a;

                    {
                        this.a = r1;
                    }

                    public void run() {
                        this.a.d();
                    }
                });
            } else {
                d();
            }
        }
    }

    public void b(com.fyusion.sdk.viewer.internal.b.c.a aVar) {
        this.f = aVar.n();
        this.j = false;
        this.k = false;
        b(false);
        this.c = 0.0f;
        this.d = 0.0f;
        this.a.a();
        this.a.b();
        int displayRotation = this.b.getDisplayRotation();
        this.a.a(aVar);
        this.a.a(displayRotation);
        this.a.b(true);
        this.a.a((float) Math.max(this.f.getMagic().getThumbnailIndex(), 0));
        n = aVar.m().getWidth() < 3500 ? n : n * 4.0f;
    }

    public void c() {
        this.a.h();
    }
}
