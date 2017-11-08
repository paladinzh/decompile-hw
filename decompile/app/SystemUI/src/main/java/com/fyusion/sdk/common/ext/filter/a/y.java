package com.fyusion.sdk.common.ext.filter.a;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.support.annotation.NonNull;
import com.fyusion.sdk.common.ext.filter.FilterControl;
import com.fyusion.sdk.common.ext.filter.ToneCurveFilter;
import com.fyusion.sdk.core.util.b;
import com.fyusion.sdk.core.util.c;

/* compiled from: Unknown */
public class y extends a<ToneCurveFilter> {
    private float c = 0.0f;
    private int d = -1;
    private z e = null;
    private boolean f = false;
    private Bitmap g = null;
    private int h = -1;
    private int i = -1;

    public y(z zVar) {
        super(m.TONECURVE.a());
        this.e = zVar;
        if (this.g != null) {
            this.g.recycle();
        }
        this.g = a(zVar);
        this.f = true;
    }

    private Bitmap a(z zVar) {
        short[] a = zVar.a();
        b.a(a.length, 768);
        int[] iArr = new int[256];
        for (int i = 0; i < 256; i++) {
            iArr[i] = Color.argb(255, a[(i * 3) + 0], a[(i * 3) + 1], a[(i * 3) + 2]);
        }
        return Bitmap.createBitmap(iArr, 256, 1, Config.ARGB_8888);
    }

    private void i() {
        if (this.a) {
            j();
            this.a = false;
        }
        GLES20.glBindTexture(3553, this.d);
        GLUtils.texImage2D(3553, 0, this.g, 0);
        GLES20.glBindTexture(3553, 0);
        c.a();
        this.f = false;
    }

    private void j() {
        int[] iArr = new int[]{-1};
        GLES20.glGenTextures(1, iArr, 0);
        this.d = iArr[0];
        GLES20.glBindTexture(3553, this.d);
        GLES20.glTexParameteri(3553, 10241, 9729);
        GLES20.glTexParameteri(3553, 10240, 9729);
        GLES20.glTexParameteri(3553, 10242, 33071);
        GLES20.glTexParameteri(3553, 10243, 33071);
        GLES20.glBindTexture(3553, 0);
        c.a();
        this.a = false;
    }

    public void a() {
        if (this.f || this.a) {
            if (this.e != null) {
                i();
            }
        }
        GLES20.glActiveTexture(33985);
        GLES20.glBindTexture(3553, this.d);
    }

    public void a(float f) {
        this.c = f;
    }

    public void a(int i) {
        this.i = GLES20.glGetUniformLocation(i, "tone_curve_texture");
        this.h = GLES20.glGetUniformLocation(i, "tone_curve_alpha");
    }

    public void a(@NonNull ToneCurveFilter toneCurveFilter) {
        super.a((FilterControl) toneCurveFilter);
        a(toneCurveFilter.getValue());
    }

    public void b() {
        GLES20.glActiveTexture(33985);
        GLES20.glBindTexture(3553, 0);
    }

    public String f() {
        return "uniform sampler2D tone_curve_texture;uniform highp float tone_curve_alpha;";
    }

    public String g() {
        return "highp float r = texture2D (tone_curve_texture, vec2 (input_color.r, 0.0)).r;highp float g = texture2D (tone_curve_texture, vec2 (input_color.g, 0.0)).g;highp float b = texture2D (tone_curve_texture, vec2 (input_color.b, 0.0)).b;highp vec3 filtered_color = vec3 (r, g, b);return vec4 (mix (input_color.rgb, filtered_color, tone_curve_alpha), input_color.a);";
    }

    public void h() {
        GLES20.glUniform1i(this.i, 1);
        GLES20.glUniform1f(this.h, this.c);
    }
}
