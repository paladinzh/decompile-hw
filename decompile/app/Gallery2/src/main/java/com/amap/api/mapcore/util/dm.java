package com.amap.api.mapcore.util;

import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: GLScaleAnimation */
public class dm extends di {
    private int A = 0;
    private float B;
    private float C;
    private float a;
    private float b;
    private float c;
    private float w;
    private int x = 0;
    private int y = 0;
    private int z = 0;

    public dm(float f, float f2, float f3, float f4) {
        this.a = f;
        this.b = f2;
        this.c = f3;
        this.w = f4;
        this.B = 0.0f;
        this.C = 0.0f;
    }

    protected void a(float f, dn dnVar) {
        float f2 = WMElement.CAMERASIZEVALUE1B1;
        e();
        float f3 = (this.a == WMElement.CAMERASIZEVALUE1B1 && this.b == WMElement.CAMERASIZEVALUE1B1) ? WMElement.CAMERASIZEVALUE1B1 : this.a + ((this.b - this.a) * f);
        if (!(this.c == WMElement.CAMERASIZEVALUE1B1 && this.w == WMElement.CAMERASIZEVALUE1B1)) {
            f2 = this.c + ((this.w - this.c) * f);
        }
        if (this.B == 0.0f && this.C == 0.0f) {
            dnVar.e = (double) f3;
            dnVar.f = (double) f2;
            return;
        }
        dnVar.e = (double) f3;
        dnVar.f = (double) f2;
    }
}
