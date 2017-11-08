package com.fyusion.sdk.common.ext.filter;

import com.fyusion.sdk.common.ext.filter.a.a;
import com.fyusion.sdk.common.ext.filter.a.ah;

/* compiled from: Unknown */
public class VignetteFilter extends AdjustmentFilter {
    private float[] a = new float[]{0.5f, 0.5f};
    private float[] b = new float[]{0.0f, 0.0f, 0.0f};
    private float c = 0.0f;
    private float d = 1.0f;

    public VignetteFilter() {
        super("VIGNETTE");
    }

    float a() {
        return 0.0f;
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

    public float getVignetteStart() {
        return this.c;
    }
}
