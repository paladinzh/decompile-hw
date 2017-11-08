package com.fyusion.sdk.common.ext.filter;

import com.fyusion.sdk.common.ext.filter.a.a;
import com.fyusion.sdk.common.ext.filter.a.ah;
import com.fyusion.sdk.core.util.b;
import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: Unknown */
public class VignetteFilter extends AdjustmentFilter {
    private float[] a = new float[]{0.5f, 0.5f};
    private float[] b = new float[]{0.0f, 0.0f, 0.0f};
    private float c = 0.0f;
    private float d = WMElement.CAMERASIZEVALUE1B1;

    public VignetteFilter() {
        super(ImageFilterAbstractFactory.VIGNETTE);
    }

    float a() {
        return 0.0f;
    }

    float b() {
        return 0.0f;
    }

    float c() {
        return 0.5f;
    }

    public Class<? extends a> getImplementationClass() {
        return ah.class;
    }

    public float[] getVignetteCenter() {
        return (float[]) this.a.clone();
    }

    public float[] getVignetteColor() {
        return (float[]) this.b.clone();
    }

    public float getVignetteEnd() {
        return this.d;
    }

    public float getVignetteStart() {
        return this.c;
    }

    public void setVignetteCenter(float[] fArr) {
        b.a(fArr.length, 2);
        b.a(fArr[0], 0.0f);
        b.a(fArr[1], 0.0f);
        b.b(fArr[0], (float) WMElement.CAMERASIZEVALUE1B1);
        b.b(fArr[1], (float) WMElement.CAMERASIZEVALUE1B1);
        this.a = (float[]) fArr.clone();
    }

    public void setVignetteColor(float[] fArr) {
        b.a(fArr.length, 3);
        b.a(fArr[0], 0.0f);
        b.a(fArr[1], 0.0f);
        b.a(fArr[1], 0.0f);
        b.b(fArr[0], (float) WMElement.CAMERASIZEVALUE1B1);
        b.b(fArr[1], (float) WMElement.CAMERASIZEVALUE1B1);
        b.b(fArr[1], (float) WMElement.CAMERASIZEVALUE1B1);
        this.b = (float[]) fArr.clone();
    }

    public void setVignetteRange(float f, float f2) {
        b.a(f, 0.0f);
        b.b(f, (float) WMElement.CAMERASIZEVALUE1B1);
        b.a(f2, 0.0f);
        b.b(f2, (float) WMElement.CAMERASIZEVALUE1B1);
        b.b(f, f2);
        this.c = f;
        this.d = f2;
    }
}
