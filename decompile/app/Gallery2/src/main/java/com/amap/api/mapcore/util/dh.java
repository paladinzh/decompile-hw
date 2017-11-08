package com.amap.api.mapcore.util;

import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: GLAlphaAnimation */
public class dh extends di {
    public float a = 0.0f;
    public float b = WMElement.CAMERASIZEVALUE1B1;
    public float c = 0.0f;

    public dh(float f, float f2) {
        this.a = f;
        this.b = f2;
    }

    protected void a(float f, dn dnVar) {
        float f2 = this.a;
        this.c = f2 + ((this.b - f2) * f);
        dnVar.c = (double) this.c;
    }
}
