package com.fyusion.sdk.common.ext.filter.a;

import android.opengl.GLES20;
import android.support.annotation.NonNull;
import com.fyusion.sdk.common.ext.filter.BlockFilter;
import com.fyusion.sdk.common.ext.filter.FilterControl;
import com.fyusion.sdk.common.ext.filter.VignetteFilter;
import com.fyusion.sdk.common.t;

/* compiled from: Unknown */
public class ah extends a<VignetteFilter> implements BlockFilter {
    private float[] c = new float[]{1.0f, 1.0f};
    private float[] d = new float[]{0.5f, 0.5f};
    private float[] e = new float[]{0.0f, 0.0f, 0.0f};
    private float f = 0.0f;
    private float g = 1.0f;
    private int h = -1;
    private int i = -1;
    private int j = -1;
    private int k = -1;
    private int l;
    private int m;
    private t n = new t();

    public ah() {
        super(m.VIGNETTE.a());
    }

    public void a(float f, float f2) {
        this.f = f;
        this.g = f2;
    }

    public void a(int i) {
        this.h = GLES20.glGetUniformLocation(i, "texture01_to_oriImgPxl");
        this.i = GLES20.glGetUniformLocation(i, "vignette_center_oriImgPxl");
        this.j = GLES20.glGetUniformLocation(i, "vignette_start_disPxl");
        this.k = GLES20.glGetUniformLocation(i, "vignette_end_disPxl");
    }

    public void a(@NonNull VignetteFilter vignetteFilter) {
        super.a((FilterControl) vignetteFilter);
        a(vignetteFilter.getValue() > 0.01f);
        a(vignetteFilter.getVignetteCenter());
        b(vignetteFilter.getVignetteColor());
        a(vignetteFilter.getVignetteStart(), 1.0f - vignetteFilter.getValue());
    }

    public void a(float[] fArr) {
        this.d = (float[]) fArr.clone();
    }

    public void b(float[] fArr) {
        this.e = (float[]) fArr.clone();
    }

    public String f() {
        return "uniform highp vec3 vignette_color;uniform highp mat4 texture01_to_oriImgPxl;uniform highp vec2 vignette_center_oriImgPxl;uniform highp float vignette_start_disPxl;uniform highp float vignette_end_disPxl;";
    }

    public String g() {
        return "highp vec4 textureCurPxl_onOriImgPxl = texture01_to_oriImgPxl * vec4(texture_coordinate, 0.0, 1.0);highp float d = distance (textureCurPxl_onOriImgPxl.xy, vignette_center_oriImgPxl.xy);highp float percent = smoothstep (vignette_start_disPxl, vignette_end_disPxl, d);return vec4 (mix (input_color.rgb, vignette_color, percent), input_color.a);";
    }

    public float getValue() {
        return 1.0f - i();
    }

    public void h() {
        float[] fArr = new float[]{((float) this.n.g().a) / 2.0f, ((float) this.n.g().b) / 2.0f};
        GLES20.glUniformMatrix4fv(this.h, 1, false, this.n.h(), 0);
        GLES20.glUniform2fv(this.i, 1, fArr, 0);
        float min = Math.min((float) this.n.g().a, (float) this.n.g().b);
        GLES20.glUniform1f(this.j, this.f * min);
        GLES20.glUniform1f(this.k, min * (this.g * 1.5f));
    }

    public float i() {
        return this.g;
    }

    public void setImageSize(int i, int i2) {
        this.l = i;
        this.m = i2;
    }

    public void setTextureContainer(t tVar) {
        this.n = tVar;
        this.l = this.n.e();
        this.m = this.n.f();
    }
}
