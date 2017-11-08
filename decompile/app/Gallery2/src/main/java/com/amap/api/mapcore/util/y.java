package com.amap.api.mapcore.util;

/* compiled from: ADGLAnimationParam1V */
public class y extends x {
    private float i;
    private float j;

    public y() {
        a();
    }

    public void a() {
        super.a();
        this.i = 0.0f;
        this.j = 0.0f;
    }

    public void c(float f) {
        this.i = f;
        this.g = true;
        this.e = false;
    }

    public void d(float f) {
        this.j = f;
        this.h = true;
        this.e = false;
    }

    public float e() {
        return this.i;
    }

    public float f() {
        return this.j;
    }

    public float g() {
        return this.i + ((this.j - this.i) * this.d);
    }

    public void d() {
        this.f = false;
        if (this.g && this.h && ((double) Math.abs(this.j - this.i)) > 1.0E-4d) {
            this.f = true;
        }
        this.e = true;
    }
}
