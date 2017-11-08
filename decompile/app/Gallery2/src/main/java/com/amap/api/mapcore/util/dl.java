package com.amap.api.mapcore.util;

import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: GLRotateAnimation */
public class dl extends di {
    public float a = 0.0f;
    public float b = WMElement.CAMERASIZEVALUE1B1;
    public float c = 0.0f;
    private float w = 0.0f;
    private float x = 0.0f;
    private float y;
    private float z;

    public dl(float f, float f2, float f3, float f4, float f5) {
        this.a = f;
        this.b = f2;
    }

    protected void a(float f, dn dnVar) {
        float f2 = this.a + ((this.b - this.a) * f);
        e();
        this.c = f2;
        if (this.y == 0.0f && this.z == 0.0f) {
            dnVar.d = (double) f2;
        } else {
            dnVar.d = (double) f2;
        }
    }
}
