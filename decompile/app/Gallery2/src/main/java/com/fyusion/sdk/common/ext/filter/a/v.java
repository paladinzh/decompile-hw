package com.fyusion.sdk.common.ext.filter.a;

import android.opengl.GLES20;
import android.support.annotation.NonNull;
import com.fyusion.sdk.common.ext.filter.FilterControl;
import com.fyusion.sdk.common.ext.filter.SkinTonePreservingSaturationFilter;

/* compiled from: Unknown */
public class v extends a<SkinTonePreservingSaturationFilter> {
    private float c = 0.0f;
    private int d = -1;

    public v() {
        super(m.SKINTONESATURATION.a());
    }

    public void a(float f) {
        this.c = f;
    }

    public void a(int i) {
        this.d = GLES20.glGetUniformLocation(i, "stpsf_intensity");
    }

    public void a(@NonNull SkinTonePreservingSaturationFilter skinTonePreservingSaturationFilter) {
        super.a((FilterControl) skinTonePreservingSaturationFilter);
        a(skinTonePreservingSaturationFilter.getValue());
    }

    public String f() {
        return "uniform highp float stpsf_intensity;";
    }

    public String g() {
        return "highp vec3 pixel = input_color.rgb;highp float grayLevel = 0.33333 * (pixel.r + pixel.g + pixel.b);highp vec3 saturation = max((grayLevel - pixel) / grayLevel, (pixel - grayLevel) / (1.0 - grayLevel));highp float maxSaturation = max(max(saturation.b, saturation.g), saturation.r);highp float isSkinTone = 4.0 * (1.0 - saturation.r) * min(2.0 * pixel.g - pixel.b, pixel.r - pixel.g) / grayLevel;highp vec3 maxSatV = vec3(1.0, maxSaturation, maxSaturation * maxSaturation);highp vec3 intensityV = vec3(3.0 * stpsf_intensity,                              -4.5 * stpsf_intensity * stpsf_intensity - 1.5 * stpsf_intensity,                              4.5 * stpsf_intensity * stpsf_intensity * stpsf_intensity - 0.5 * stpsf_intensity);highp float increase = dot(maxSatV, intensityV) * (1.0 - isSkinTone);highp vec3 grayLevel3 = vec3 (grayLevel, grayLevel, grayLevel);pixel = clamp(pixel + (pixel - grayLevel3) * increase, 0.0, 1.0);return vec4 (pixel, input_color.a);";
    }

    public void h() {
        GLES20.glUniform1f(this.d, this.c);
    }
}
