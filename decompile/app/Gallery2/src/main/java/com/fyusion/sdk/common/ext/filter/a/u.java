package com.fyusion.sdk.common.ext.filter.a;

import android.opengl.GLES20;
import android.support.annotation.NonNull;
import com.fyusion.sdk.common.ext.filter.BlockFilter;
import com.fyusion.sdk.common.ext.filter.FilterControl;
import com.fyusion.sdk.common.ext.filter.SharpenFilter;
import com.fyusion.sdk.common.p;
import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: Unknown */
public class u extends a<SharpenFilter> implements BlockFilter {
    private float c = 0.0f;
    private float d = WMElement.CAMERASIZEVALUE1B1;
    private float e = WMElement.CAMERASIZEVALUE1B1;
    private int f = -1;
    private int g = -1;
    private int h = -1;
    private p i = new p();

    public u() {
        super(m.SHARPEN.a());
    }

    public void a(float f) {
        this.c = f;
    }

    public void a(int i) {
        this.f = GLES20.glGetUniformLocation(i, "sharpness");
        this.g = GLES20.glGetUniformLocation(i, "pixel_size_x");
        this.h = GLES20.glGetUniformLocation(i, "pixel_size_y");
    }

    public void a(@NonNull SharpenFilter sharpenFilter) {
        super.a((FilterControl) sharpenFilter);
        a(sharpenFilter.getValue());
    }

    public String d() {
        return "uniform float pixel_size_x;uniform float pixel_size_y;uniform float sharpness;varying vec2 texture_coordinate_left;varying vec2 texture_coordinate_right;varying vec2 texture_coordinate_bottom;varying vec2 texture_coordinate_top;varying float center_multiplier;varying float edge_multiplier;";
    }

    public String e() {
        return "    vec2 step_x = vec2 (pixel_size_x, 0.0);    vec2 step_y = vec2 (0.0, pixel_size_y);    texture_coordinate_left = quad_vertex - step_x;    texture_coordinate_right = quad_vertex + step_x;    texture_coordinate_bottom = quad_vertex - step_y;    texture_coordinate_top = quad_vertex + step_y;    center_multiplier = 1.0 + 4.0 * sharpness;    edge_multiplier = sharpness;";
    }

    public String f() {
        return "uniform highp float sharpness;varying highp vec2 texture_coordinate_left;varying highp vec2 texture_coordinate_right;varying highp vec2 texture_coordinate_bottom;varying highp vec2 texture_coordinate_top;varying highp float center_multiplier;varying highp float edge_multiplier;";
    }

    public String g() {
        return "highp vec3 left_color = texture2D (texture, texture_coordinate_left).rgb;highp vec3 right_color = texture2D (texture, texture_coordinate_right).rgb;highp vec3 bottom_color = texture2D (texture, texture_coordinate_bottom).rgb;highp vec3 top_color = texture2D (texture, texture_coordinate_top).rgb;highp vec3 newRGB = center_multiplier * input_color.rgb - edge_multiplier * (left_color + right_color + bottom_color + top_color);newRGB = clamp(newRGB, 0.0, 1.0);return vec4 (newRGB, input_color.a);";
    }

    public int getHeight() {
        return (int) this.e;
    }

    public int getWidth() {
        return (int) this.d;
    }

    public void h() {
        GLES20.glUniform1f(this.f, this.c * 4.0f);
        GLES20.glUniform1f(this.g, WMElement.CAMERASIZEVALUE1B1 / this.d);
        GLES20.glUniform1f(this.h, WMElement.CAMERASIZEVALUE1B1 / this.e);
    }

    public void setImageSize(int i, int i2) {
        this.d = (float) i;
        this.e = (float) i2;
    }

    public void setTextureContainer(p pVar) {
        this.i = pVar;
        this.d = (float) this.i.e();
        this.e = (float) this.i.f();
    }
}
