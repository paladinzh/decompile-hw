package com.fyusion.sdk.common.ext.filter.a;

import android.opengl.GLES20;
import android.support.annotation.NonNull;
import com.fyusion.sdk.common.ext.filter.BrightnessFilter;
import com.fyusion.sdk.common.ext.filter.FilterControl;

/* compiled from: Unknown */
public class d extends a<BrightnessFilter> {
    private float c;
    private float d = 0.0f;
    private int e = -1;

    public d() {
        super(m.BRIGHTNESS.a());
    }

    public void a(float f) {
        this.d = f;
    }

    public void a(int i) {
        this.e = GLES20.glGetUniformLocation(i, "brightnessCtrl_inputShader");
    }

    public void a(@NonNull BrightnessFilter brightnessFilter) {
        super.a((FilterControl) brightnessFilter);
        a(brightnessFilter.getValue());
    }

    public String f() {
        return "uniform highp float brightnessCtrl_inputShader;";
    }

    public String g() {
        return "return vec4 ((input_color.rgb + vec3 (brightnessCtrl_inputShader)), input_color.a);";
    }

    public void h() {
        this.c = this.d - 0.5f;
        GLES20.glUniform1f(this.e, this.c);
    }
}
