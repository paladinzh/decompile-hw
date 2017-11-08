package com.fyusion.sdk.common.ext.filter;

import com.fyusion.sdk.common.ext.filter.a.a;
import com.fyusion.sdk.common.ext.filter.a.f;
import com.fyusion.sdk.core.util.b;
import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: Unknown */
public class ClampFilter extends FilterControl {
    private float[] a = new float[]{0.0f, 0.0f, 0.0f};
    private float[] b = new float[]{WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1};

    public ClampFilter() {
        super("CLAMP");
    }

    float a() {
        return 0.0f;
    }

    float b() {
        return 0.0f;
    }

    float c() {
        return 0.0f;
    }

    public float[] getClampMaximum() {
        return (float[]) this.b.clone();
    }

    public float[] getClampMinimum() {
        return (float[]) this.a.clone();
    }

    public Class<? extends a> getImplementationClass() {
        return f.class;
    }

    public void setClampRange(float[] fArr, float[] fArr2) {
        b.a(fArr.length, 3);
        b.a(fArr[0], 0.0f);
        b.a(fArr[1], 0.0f);
        b.b(fArr[0], (float) WMElement.CAMERASIZEVALUE1B1);
        b.b(fArr[1], (float) WMElement.CAMERASIZEVALUE1B1);
        b.a(fArr2.length, 3);
        b.a(fArr2[0], 0.0f);
        b.a(fArr2[1], 0.0f);
        b.b(fArr2[0], (float) WMElement.CAMERASIZEVALUE1B1);
        b.b(fArr2[1], (float) WMElement.CAMERASIZEVALUE1B1);
        b.a(fArr2[0], fArr[0]);
        this.a = (float[]) fArr.clone();
        this.b = (float[]) fArr2.clone();
    }
}
