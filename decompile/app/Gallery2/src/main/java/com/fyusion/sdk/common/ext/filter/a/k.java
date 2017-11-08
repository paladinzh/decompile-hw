package com.fyusion.sdk.common.ext.filter.a;

import android.opengl.GLES20;
import android.support.annotation.NonNull;
import com.fyusion.sdk.common.ext.filter.ExposureFilter;
import com.fyusion.sdk.common.ext.filter.FilterControl;

/* compiled from: Unknown */
public class k extends a<ExposureFilter> {
    private float c = 0.5f;
    private int d = -1;

    public k() {
        super(m.EXPOSURE.a());
    }

    public void a(float f) {
        this.c = f;
    }

    public void a(int i) {
        this.d = GLES20.glGetUniformLocation(i, "exposure");
    }

    public void a(@NonNull ExposureFilter exposureFilter) {
        super.a((FilterControl) exposureFilter);
        a(exposureFilter.getValue());
    }

    public String f() {
        return "uniform highp float exposure;";
    }

    public String g() {
        return "return vec4 (input_color.rgb * pow (2.0, exposure), input_color.a);";
    }

    public void h() {
        GLES20.glUniform1f(this.d, (this.c * 4.0f) - 2.0f);
    }
}
