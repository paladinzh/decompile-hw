package com.fyusion.sdk.common.ext.filter.a;

import android.opengl.GLES20;
import android.support.annotation.NonNull;
import com.fyusion.sdk.common.ext.filter.ClampFilter;
import com.fyusion.sdk.common.ext.filter.FilterControl;
import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: Unknown */
public class f extends a<ClampFilter> {
    private float[] c = new float[]{0.0f, 0.0f, 0.0f};
    private float[] d = new float[]{WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1};
    private int e = -1;
    private int f = -1;

    public f() {
        super(m.CLAMP.a());
    }

    public void a(int i) {
        this.e = GLES20.glGetUniformLocation(i, "clamp_minimum");
        this.f = GLES20.glGetUniformLocation(i, "clamp_maximum");
    }

    public void a(@NonNull ClampFilter clampFilter) {
        super.a((FilterControl) clampFilter);
        a(clampFilter.getClampMinimum(), clampFilter.getClampMaximum());
    }

    public void a(float[] fArr, float[] fArr2) {
        this.c = (float[]) fArr.clone();
        this.d = (float[]) fArr2.clone();
    }

    public String f() {
        return "uniform highp vec3 clamp_minimum;uniform highp vec3 clamp_maximum;";
    }

    public String g() {
        return "return vec4 (clamp (input_color.rgb, clamp_minimum, clamp_maximum), input_color.a);";
    }

    public void h() {
        GLES20.glUniform3fv(this.e, 1, this.c, 0);
        GLES20.glUniform3fv(this.f, 1, this.d, 0);
    }
}
