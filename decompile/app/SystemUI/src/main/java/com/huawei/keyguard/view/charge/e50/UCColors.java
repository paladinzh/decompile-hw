package com.huawei.keyguard.view.charge.e50;

public class UCColors {
    float a;
    float b;
    float ca;
    float cb;
    float cg;
    float cr;
    int dTimes;
    float da;
    float db;
    float dg;
    float dr;
    float g;
    int oTimes;
    float oa;
    float ob;
    float og;
    float or;
    float r;

    public UCColors(int o, int d) {
        this.a = 0.0f;
        this.b = 0.0f;
        this.g = 0.0f;
        this.r = 0.0f;
        this.cr = 0.0f;
        this.cb = 0.0f;
        this.cg = 0.0f;
        this.cr = 0.0f;
        setODtimes(o, d);
    }

    public void setColor(float rv, float gv, float bv, float av) {
        this.cr = rv;
        this.r = rv;
        this.cg = gv;
        this.g = gv;
        this.cb = bv;
        this.b = bv;
        this.ca = av;
        this.a = av;
        this.or = this.r / ((float) this.oTimes);
        this.og = this.g / ((float) this.oTimes);
        this.ob = this.b / ((float) this.oTimes);
        this.oa = this.a / ((float) this.oTimes);
        this.dr = this.r / ((float) this.dTimes);
        this.dg = this.g / ((float) this.dTimes);
        this.db = this.b / ((float) this.dTimes);
        this.da = this.a / ((float) this.dTimes);
        beginColorOflip();
    }

    public void setColor(int argb8888) {
        float floatFrom255 = getFloatFrom255((argb8888 >> 24) & 255);
        this.ca = floatFrom255;
        this.a = floatFrom255;
        floatFrom255 = getFloatFrom255((argb8888 >> 16) & 255);
        this.cr = floatFrom255;
        this.r = floatFrom255;
        floatFrom255 = getFloatFrom255((argb8888 >> 8) & 255);
        this.cg = floatFrom255;
        this.g = floatFrom255;
        floatFrom255 = getFloatFrom255(argb8888 & 255);
        this.cb = floatFrom255;
        this.b = floatFrom255;
        this.or = this.r / ((float) this.oTimes);
        this.og = this.g / ((float) this.oTimes);
        this.ob = this.b / ((float) this.oTimes);
        this.oa = this.a / ((float) this.oTimes);
        this.dr = this.r / ((float) this.dTimes);
        this.dg = this.g / ((float) this.dTimes);
        this.db = this.b / ((float) this.dTimes);
        this.da = this.a / ((float) this.dTimes);
        beginColorOflip();
    }

    private static float getFloatFrom255(int b8) {
        return ((float) b8) * 0.003921569f;
    }

    public void beginColorOflip() {
        this.cr = 0.0f;
        this.cb = 0.0f;
        this.cg = 0.0f;
        this.cr = 0.0f;
    }

    public void setColorOFlip() {
        this.cr += this.or;
        this.cg += this.og;
        this.cb += this.ob;
        this.ca += this.oa;
    }

    public void beginColorDflip() {
        this.cr = this.r;
        this.cg = this.g;
        this.cb = this.b;
        this.ca = this.a;
    }

    public void setColorDFlip() {
        this.cr -= this.dr;
        this.cg -= this.dg;
        this.cb -= this.db;
        this.ca -= this.da;
    }

    public void setODtimes(int o, int d) {
        this.oTimes = o;
        this.dTimes = d;
    }

    public float getA() {
        return this.a;
    }

    public void setA(float av) {
        this.a = av;
    }
}
