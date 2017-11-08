package com.fyusion.sdk.common.ext.filter.a;

import android.opengl.GLES20;
import android.support.annotation.NonNull;
import com.fyusion.sdk.common.ext.filter.ContrastFilter;
import com.fyusion.sdk.common.ext.filter.FilterControl;

/* compiled from: Unknown */
public class g extends a<ContrastFilter> {
    private float c = 1.0f;
    private int d = -1;

    public g() {
        super(m.CONTRAST.a());
    }

    public void a(float f) {
        this.c = f;
    }

    public void a(int i) {
        this.d = GLES20.glGetUniformLocation(i, "contrast");
    }

    public void a(@NonNull ContrastFilter contrastFilter) {
        super.a((FilterControl) contrastFilter);
        a(contrastFilter.getValue());
    }

    public String f() {
        return "uniform highp float contrast;";
    }

    public String g() {
        return "return vec4 (((input_color.rgb - vec3 (0.5)) * contrast + vec3 (0.5)), input_color.a);";
    }

    public void h() {
        GLES20.glUniform1f(this.d, this.c);
    }
}
