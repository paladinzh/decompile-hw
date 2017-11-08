package com.fyusion.sdk.common.ext.filter.a;

import android.opengl.GLES20;
import android.support.annotation.NonNull;
import com.fyusion.sdk.common.ext.filter.FilterControl;
import com.fyusion.sdk.common.ext.filter.SaturationFilter;
import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: Unknown */
public class t extends a<SaturationFilter> {
    float c = ((this.g - WMElement.CAMERASIZEVALUE1B1) * 2.0f);
    float d = (WMElement.CAMERASIZEVALUE1B1 - (this.c * 0.5f));
    private float e = WMElement.CAMERASIZEVALUE1B1;
    private float f;
    private float g = 2.0f;
    private int h = -1;

    public t() {
        super(m.SATURATION.a());
    }

    public void a(float f) {
        this.e = f;
    }

    public void a(int i) {
        this.h = GLES20.glGetUniformLocation(i, "saturationCtrl_inputShader");
    }

    public void a(@NonNull SaturationFilter saturationFilter) {
        super.a((FilterControl) saturationFilter);
        a(saturationFilter.getValue());
    }

    public String f() {
        return "uniform highp float saturationCtrl_inputShader;";
    }

    public String g() {
        return "highp vec3 hsv = rgb2hsv( clamp(input_color.rgb, 0.0, 1.0)  );hsv.y = hsv.y * saturationCtrl_inputShader;hsv.y = clamp(hsv.y, 0.0, 1.0);return vec4( hsv2rgb(hsv), input_color.a );";
    }

    public void h() {
        this.f = this.e <= 0.5f ? this.e * 2.0f : (this.c * this.e) + this.d;
        GLES20.glUniform1f(this.h, this.f);
    }
}
