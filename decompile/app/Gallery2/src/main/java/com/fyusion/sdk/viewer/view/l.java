package com.fyusion.sdk.viewer.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.opengl.GLSurfaceView.Renderer;
import android.support.annotation.UiThread;
import android.util.AttributeSet;
import android.util.Log;
import com.fyusion.sdk.common.o;
import com.fyusion.sdk.viewer.b;
import com.fyusion.sdk.viewer.c;
import com.fyusion.sdk.viewer.g;
import com.fyusion.sdk.viewer.internal.b.e;

/* compiled from: Unknown */
public class l extends j implements com.fyusion.sdk.common.j.a {
    private com.fyusion.sdk.viewer.a a;
    private com.fyusion.sdk.viewer.a.a b;
    private c d;
    private b e;
    private c f;
    private com.fyusion.sdk.viewer.internal.b.c.a g;
    private a h;
    private volatile boolean i = false;
    private volatile boolean j = false;
    private boolean k = true;
    private boolean l = true;
    private boolean m = true;

    /* compiled from: Unknown */
    interface a {
        void a();
    }

    public l(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.a = new com.fyusion.sdk.viewer.a(context);
        this.b = new com.fyusion.sdk.viewer.a.a(this) {
            final /* synthetic */ l a;

            {
                this.a = r1;
            }

            public void a(float f, float f2) {
                if (this.a.f != null) {
                    this.a.f.a(f, f2);
                }
            }

            public void a(float f, float f2, float f3) {
                if (this.a.f != null) {
                    this.a.f.a(f, f2, f3);
                }
            }

            public void a(float f, float f2, com.fyusion.sdk.viewer.a.b bVar) {
                if (bVar == com.fyusion.sdk.viewer.a.b.HAS_STARTED) {
                    this.a.d.a(true);
                } else if (bVar == com.fyusion.sdk.viewer.a.b.HAS_ENDED) {
                    this.a.d.a(false);
                }
                if (this.a.f != null) {
                    this.a.f.a(f, f2, bVar);
                }
            }
        };
        this.d = c.a();
        this.e = new b(this) {
            final /* synthetic */ l a;

            {
                this.a = r1;
            }

            public void a(com.fyusion.sdk.viewer.c.a aVar) {
                if (this.a.f != null) {
                    this.a.f.a(aVar);
                }
            }
        };
        this.f = new c(this, true);
        this.f.a(new g(this) {
            final /* synthetic */ l a;

            {
                this.a = r1;
            }

            public void a() {
            }

            public void b() {
                this.a.l();
            }
        });
        this.c.setObserver(this);
    }

    private void l() {
        Object obj = null;
        synchronized (this) {
            if (!(this.g == null || this.i || !this.j)) {
                obj = 1;
            }
        }
        if (obj != null) {
            this.f.b();
        }
    }

    private synchronized void m() {
        if (this.k) {
            this.d.a(this.e);
        }
    }

    private synchronized void n() {
        if (this.l) {
            this.a.a(this, this.b);
        }
    }

    public void a() {
        synchronized (this) {
            this.j = true;
            this.i = false;
            Log.d("TweeningViewWrapper", "surfaceCreated: " + (this.g != null ? this.g.d() : "null"));
            l();
        }
    }

    public /* bridge */ /* synthetic */ void a(float f, float f2, float f3) {
        super.a(f, f2, f3);
    }

    public void a(int i, int i2) {
    }

    public void a(o oVar) {
        this.c.addOverlay(oVar);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    synchronized void a(boolean z) {
        if (this.k != z) {
            Log.d("TweeningViewWrapper", "enableMotion: " + z);
            this.k = z;
            if (z) {
                this.d.a(this.e);
            } else {
                this.d.b(this.e);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void b() {
        synchronized (this) {
            if (this.g != null) {
                this.i = true;
            } else {
                this.c.recycle();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    synchronized void b(boolean z) {
        if (this.l != z) {
            Log.d("TweeningViewWrapper", "enableGesture: " + z);
            this.l = z;
            if (z) {
                this.a.a(this, this.b);
            } else {
                this.a.a();
            }
        }
    }

    public /* bridge */ /* synthetic */ boolean e() {
        return super.e();
    }

    public /* bridge */ /* synthetic */ int getDisplayRotation() {
        return super.getDisplayRotation();
    }

    e getKey() {
        return this.g;
    }

    public /* bridge */ /* synthetic */ Renderer getRenderer() {
        return super.getRenderer();
    }

    com.fyusion.sdk.viewer.internal.request.target.b getSizeReadyCallback() {
        return this.f;
    }

    @UiThread
    void i() {
        Log.d("TweeningViewWrapper", "recycle: " + this.i + " " + (this.g != null ? this.g.d() : "null"));
        if (this.i) {
            setVisibility(8);
            this.i = false;
            this.d.b(this.e);
            this.a.a();
            this.f.c();
            com.fyusion.sdk.viewer.internal.a.b.a(this.g);
        }
        if (this.g != null) {
            this.g.b(this.f);
            this.g = null;
        }
        this.c.recycle();
    }

    void j() {
        f();
        this.m = false;
        if (this.g != null) {
            setData(this.g);
        }
        l();
    }

    @UiThread
    void k() {
        synchronized (this) {
            this.i = false;
            this.j = false;
        }
        this.d.b(this.e);
        this.a.a();
        this.f.c();
        this.c.recycle();
        com.fyusion.sdk.viewer.internal.a.b.a(this.g);
        a_();
        this.m = true;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        m();
        n();
    }

    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.f.a(getDisplay().getRotation());
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.d.b(this.e);
        this.a.a();
    }

    @UiThread
    void setData(com.fyusion.sdk.viewer.internal.b.c.a aVar) {
        if (!(getVisibility() == 0 || aVar == null)) {
            setVisibility(0);
        }
        this.g = aVar;
        this.c.recycle();
        if (this.m) {
            f();
        }
        this.i = false;
        this.f.b(aVar);
        this.g.a(this.f);
    }

    public /* bridge */ /* synthetic */ void setImageMatrixPending(Matrix matrix) {
        super.setImageMatrixPending(matrix);
    }

    void setListener(a aVar) {
        this.h = aVar;
    }

    void setRotateWithGravity(boolean z) {
        this.f.a(z);
    }
}
