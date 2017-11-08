package com.amap.api.mapcore.util;

/* compiled from: ADGLAnimationParam2V */
public class z extends x {
    public float i;
    public float j;
    public float k;
    public float l;

    public z() {
        a();
    }

    public void a() {
        super.a();
        this.i = 0.0f;
        this.j = 0.0f;
        this.k = 0.0f;
        this.l = 0.0f;
    }

    public void a(float f, float f2) {
        this.i = f;
        this.k = f2;
        this.g = true;
        this.e = false;
    }

    public void b(float f, float f2) {
        this.j = f;
        this.l = f2;
        this.h = true;
        this.e = false;
    }

    public float e() {
        return this.i;
    }

    public float f() {
        return this.k;
    }

    public float g() {
        return this.j;
    }

    public float h() {
        return this.l;
    }

    public float i() {
        return this.i + ((this.j - this.i) * this.d);
    }

    public float j() {
        return this.k + ((this.l - this.k) * this.d);
    }

    public void d() {
        boolean z = false;
        this.f = false;
        if (this.g && this.h) {
            float f = this.l - this.k;
            if (((double) Math.abs(this.j - this.i)) > 1.0E-4d) {
                z = true;
            }
            if (z || ((double) Math.abs(f)) > 1.0E-4d) {
                this.f = true;
            }
        }
        this.e = true;
    }
}
