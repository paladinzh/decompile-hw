package com.amap.api.mapcore.util;

import android.graphics.RectF;
import android.os.Handler;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.amap.api.maps.model.animation.Animation.AnimationListener;
import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: GLAnimation */
public class di implements Cloneable {
    private float a = WMElement.CAMERASIZEVALUE1B1;
    private boolean b = false;
    private boolean c = true;
    boolean d = false;
    boolean e = false;
    boolean f = false;
    boolean g = false;
    boolean h = true;
    boolean i = false;
    boolean j = false;
    long k = -1;
    long l;
    long m = 500;
    int n = 0;
    int o = 0;
    int p = 1;
    Interpolator q;
    AnimationListener r;
    RectF s = new RectF();
    RectF t = new RectF();
    dn u = new dn();
    dn v = new dn();
    private boolean w = true;
    private Handler x;
    private Runnable y;
    private Runnable z;

    public /* synthetic */ Object clone() throws CloneNotSupportedException {
        return a();
    }

    public di() {
        j();
    }

    public di a() throws CloneNotSupportedException {
        di diVar = (di) super.clone();
        diVar.s = new RectF();
        diVar.t = new RectF();
        diVar.u = new dn();
        diVar.v = new dn();
        return diVar;
    }

    public void b() {
        if (this.e && !this.d) {
            o();
            this.d = true;
        }
        this.k = Long.MIN_VALUE;
        this.w = false;
        this.c = false;
    }

    public void a(Interpolator interpolator) {
        this.q = interpolator;
    }

    public void a(long j) {
        if ((j >= 0 ? 1 : null) == null) {
            j = 0;
        }
        this.m = j;
    }

    public void b(long j) {
        this.k = j;
        this.d = false;
        this.e = false;
        this.f = false;
        this.o = 0;
        this.c = true;
    }

    public void c() {
        b(-1);
    }

    public void d() {
        b(AnimationUtils.currentAnimationTimeMillis());
    }

    protected float e() {
        return this.a;
    }

    public long f() {
        return this.m;
    }

    public long g() {
        return this.l;
    }

    public boolean h() {
        return true;
    }

    public boolean i() {
        return true;
    }

    public void a(AnimationListener animationListener) {
        this.r = animationListener;
    }

    protected void j() {
        if (this.q == null) {
            this.q = new AccelerateDecelerateInterpolator();
        }
    }

    public boolean a(long j, dn dnVar) {
        float f;
        Object obj;
        boolean z;
        float f2;
        Object obj2;
        if (this.k == -1) {
            this.k = j;
        }
        long g = g();
        long j2 = this.m;
        if (j2 != 0) {
            f = ((float) (j - (g + this.k))) / ((float) j2);
        } else {
            f = ((j > this.k ? 1 : (j == this.k ? 0 : -1)) >= 0 ? 1 : null) == null ? 0.0f : WMElement.CAMERASIZEVALUE1B1;
        }
        if (f >= WMElement.CAMERASIZEVALUE1B1) {
            obj = 1;
        } else {
            obj = null;
        }
        if (obj != null) {
            z = false;
        } else {
            z = true;
        }
        this.c = z;
        if (this.j) {
            f2 = f;
        } else {
            f2 = Math.max(Math.min(f, WMElement.CAMERASIZEVALUE1B1), 0.0f);
        }
        if (f2 >= 0.0f) {
            obj2 = 1;
        } else {
            obj2 = null;
        }
        if (obj2 != null || this.h) {
            if ((f2 <= WMElement.CAMERASIZEVALUE1B1 ? 1 : null) != null || this.i) {
                if (!this.e) {
                    m();
                    this.e = true;
                }
                if (this.j) {
                    f2 = Math.max(Math.min(f2, WMElement.CAMERASIZEVALUE1B1), 0.0f);
                }
                if (this.f) {
                    f2 = WMElement.CAMERASIZEVALUE1B1 - f2;
                }
                a(this.q.getInterpolation(f2), dnVar);
            }
        }
        if (obj != null) {
            if (this.n != this.o) {
                if (this.n > 0) {
                    this.o++;
                }
                if (this.p == 2) {
                    this.f = !this.f;
                }
                this.k = -1;
                this.c = true;
                n();
            } else if (!this.d) {
                this.d = true;
                o();
            }
        }
        if (this.c || !this.w) {
            return this.c;
        }
        this.w = false;
        return true;
    }

    private void m() {
        if (this.r != null) {
            if (this.x != null) {
                this.x.postAtFrontOfQueue(this.y);
            } else {
                this.r.onAnimationStart();
            }
        }
    }

    private void n() {
    }

    private void o() {
        if (this.r != null) {
            if (this.x != null) {
                this.x.postAtFrontOfQueue(this.z);
            } else {
                this.r.onAnimationEnd();
            }
        }
    }

    public boolean a(long j, dn dnVar, float f) {
        this.a = f;
        return a(j, dnVar);
    }

    protected boolean k() {
        return this.e;
    }

    public boolean l() {
        return this.d;
    }

    protected void a(float f, dn dnVar) {
    }
}
