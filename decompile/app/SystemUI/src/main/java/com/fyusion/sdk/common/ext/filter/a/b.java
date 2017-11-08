package com.fyusion.sdk.common.ext.filter.a;

import android.opengl.GLES20;
import android.support.annotation.NonNull;
import com.fyusion.sdk.common.ext.filter.BlockFilter;
import com.fyusion.sdk.common.ext.filter.BlurFilter;
import com.fyusion.sdk.common.ext.filter.FilterControl;
import com.fyusion.sdk.common.t;
import com.fyusion.sdk.core.util.c;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

/* compiled from: Unknown */
public class b extends a<BlurFilter> implements BlockFilter {
    private float c = 0.05f;
    private float d;
    private float e = 1.0f;
    private float f = 1.0f;
    private t g = new t();
    private int h = -1;
    private int i = -1;
    private int j = -1;
    private final String k = "#version 100\nattribute vec2 quad_vertex;\nvarying vec2 texture_coordinate;\nvoid main ()\n{\n  highp float x = quad_vertex.x * 2.0 - 1.0;\n  highp float y = quad_vertex.y * 2.0 - 1.0;\n  gl_Position = vec4 (x, y, 0.0, 1.0);\n  texture_coordinate = quad_vertex;\n}";
    private final String l = "#version 100 \nuniform sampler2D texture; \nvarying highp vec2 texture_coordinate; \nvoid main () \n{ \n    highp vec4 color = texture2D (texture, texture_coordinate ); \n    gl_FragColor = color; \n}";
    private final String m = "#version 100 \nuniform sampler2D texture; \nvarying highp vec2 texture_coordinate; \nuniform highp vec2 singleStepOffset; \nuniform highp int subsamplingNum; \n// the number of the vector is fixed, we set a maximum length = (maximum radiusPixelNum) / 2 + 1\nuniform highp float gaussian_subsampling_weights[8]; \nuniform highp float gaussian_subsampling_offsets[8]; \nvoid main () \n{ \n    highp vec4 curPixelColor = texture2D(texture, texture_coordinate).rgba;\n    highp vec3 sum = curPixelColor.rgb * gaussian_subsampling_weights[0];\n    for (int i = 1; i < subsamplingNum; i++) {\n        sum += texture2D(texture, texture_coordinate + singleStepOffset * gaussian_subsampling_offsets[i]).rgb * gaussian_subsampling_weights[i];\n        sum += texture2D(texture, texture_coordinate - singleStepOffset * gaussian_subsampling_offsets[i]).rgb * gaussian_subsampling_weights[i];\n    }\n    gl_FragColor = vec4(sum, curPixelColor.a);\n}";

    public b() {
        super(m.BLUR.a());
    }

    public ArrayList<Double> a(double d) {
        int i = 1;
        ArrayList<Double> arrayList = new ArrayList();
        double[] dArr = new double[]{1.0d, 0.5d, 0.25d, 0.125d, 0.0625d, 0.03125d, 0.015d, 0.008d};
        double[] dArr2 = new double[]{1.0d, 0.45d, 0.225d, 0.1125d, 0.05625d, 0.028125d, 0.0135d, 0.007200000000000001d};
        while (d < dArr2[i]) {
            i++;
        }
        if (d >= 1.0d) {
            i = 0;
        }
        for (i = Math.max(0, (i - 2) + 1); d < dArr2[i]; i++) {
            arrayList.add(Double.valueOf(dArr[i]));
        }
        if (d < 1.0d) {
            arrayList.add(Double.valueOf(d));
        }
        return arrayList;
    }

    public void a(float f) {
        this.d = f;
    }

    void a(int i) {
    }

    public void a(@NonNull BlurFilter blurFilter) {
        super.a((FilterControl) blurFilter);
        a(blurFilter.getValue() > 0.01f);
        a(blurFilter.getValue());
    }

    public void a(o oVar, o oVar2, int i, boolean z, s sVar) {
        int i2 = -99;
        if (!z) {
            i2 = oVar.b;
        }
        int i3 = i2;
        int i4 = oVar2.c;
        int i5 = oVar2.d;
        i2 = (i / 2) - 1;
        int i6 = (i / 2) + 1;
        if (i2 >= 0) {
            Float f;
            int size = i2 <= sVar.k.size() + -1 ? i2 : sVar.k.size() - 1;
            Vector vector = (Vector) ((Vector) sVar.k.get(size)).get(0);
            float[] fArr = new float[8];
            for (int i7 = 0; i7 < vector.size(); i7++) {
                f = (Float) vector.get(i7);
                fArr[i7] = f == null ? Float.NaN : f.floatValue();
            }
            vector = (Vector) ((Vector) sVar.k.get(size)).get(1);
            float[] fArr2 = new float[8];
            for (size = 0; size < vector.size(); size++) {
                f = (Float) vector.get(size);
                fArr2[size] = f == null ? Float.NaN : f.floatValue();
            }
            sVar.a(this.k, this.m);
            GLES20.glUniform1i(GLES20.glGetUniformLocation(sVar.f, "texture"), 0);
            GLES20.glActiveTexture(33984);
            GLES20.glBindTexture(3553, oVar.b);
            i2 = GLES20.glGetAttribLocation(sVar.f, "quad_vertex");
            GLES20.glEnableVertexAttribArray(i2);
            GLES20.glVertexAttribPointer(i2, 2, 5126, false, 0, sVar.g);
            float[] fArr3 = new float[]{1.0f / ((float) i4), 0.0f};
            GLES20.glUniform2f(GLES20.glGetUniformLocation(sVar.f, "singleStepOffset"), fArr3[0], fArr3[1]);
            GLES20.glUniform1i(GLES20.glGetUniformLocation(sVar.f, "subsamplingNum"), i6);
            GLES20.glUniform1fv(GLES20.glGetUniformLocation(sVar.f, "gaussian_subsampling_weights"), 8, FloatBuffer.wrap(fArr));
            GLES20.glUniform1fv(GLES20.glGetUniformLocation(sVar.f, "gaussian_subsampling_offsets"), 8, FloatBuffer.wrap(fArr2));
            GLES20.glViewport(0, 0, oVar2.c, oVar2.d);
            GLES20.glBindFramebuffer(36160, oVar2.a);
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GLES20.glDrawArrays(6, 0, 4);
            c.a();
            o oVar3;
            if (z) {
                oVar3 = new o(oVar2);
                oVar2.a(oVar);
                oVar.a(oVar3);
                GLES20.glBindTexture(3553, oVar.b);
            } else {
                oVar3 = new o(i4, i5);
                oVar.a(oVar2);
                oVar2.a(oVar3);
                GLES20.glBindTexture(3553, oVar.b);
            }
            fArr3 = new float[]{0.0f, 1.0f / ((float) i5)};
            GLES20.glUniform2f(GLES20.glGetUniformLocation(sVar.f, "singleStepOffset"), fArr3[0], fArr3[1]);
            GLES20.glBindFramebuffer(36160, oVar2.a);
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GLES20.glDrawArrays(6, 0, 4);
            GLES20.glDisableVertexAttribArray(i2);
            GLES20.glActiveTexture(33984);
            GLES20.glBindTexture(3553, 0);
            c.a();
            oVar.a(new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(i3), Integer.valueOf(oVar2.b)})), new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(oVar2.a)})));
            return;
        }
        if (!(oVar2 == null || oVar2.b == i3)) {
            oVar2.b();
        }
        oVar2.a(oVar);
    }

    public void a(o[] oVarArr, boolean z, s sVar) {
        int i = -99;
        if (!z) {
            i = oVarArr[0].b;
        }
        int i2 = i;
        if (!(oVarArr[1] == null || oVarArr[1].b == i2)) {
            oVarArr[1].b();
        }
        o oVar = new o(oVarArr[0]);
        long[] jArr = new long[20];
        for (i = 0; i < 20; i++) {
            jArr[i] = 0;
        }
        double sqrt = ((double) this.d) * (Math.sqrt((double) ((this.e * this.e) + (this.f * this.f))) * ((double) this.c));
        if (sqrt < 2.0d) {
            oVarArr[1] = new o(oVarArr[0]);
            return;
        }
        double d = sqrt <= 10.0d ? 1.0d : 10.0d / sqrt;
        double d2 = sqrt * d;
        ArrayList arrayList = new ArrayList();
        ArrayList a = a(d);
        for (int i3 = 0; i3 < a.size(); i3++) {
            sqrt = ((Double) a.get(i3)).doubleValue();
            oVarArr[1] = new o((int) (((double) this.e) * sqrt), (int) (sqrt * ((double) this.f)));
            sVar.a(oVar, oVarArr[1], this.k, this.l);
            oVar.a(new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(i2), Integer.valueOf(oVarArr[1].b)})), new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(oVarArr[1].a)})));
            oVar.a(oVarArr[1]);
        }
        oVarArr[1] = new o(oVar.c, oVar.d);
        a(oVar, oVarArr[1], (int) d2, oVar.b != i2, sVar);
        oVar.a(new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(i2), Integer.valueOf(oVarArr[1].b)})), new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(oVarArr[1].a)})));
        oVar.a(oVarArr[1]);
        oVarArr[1] = new o((int) this.e, (int) this.f);
        sVar.a(oVar, oVarArr[1], this.k, this.l);
        oVar.a(new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(i2), Integer.valueOf(oVarArr[1].b)})), new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(oVarArr[1].a)})));
        sVar.a();
    }

    public String f() {
        return " ";
    }

    String g() {
        return "return input_color;";
    }

    void h() {
    }

    public void setImageSize(int i, int i2) {
        this.e = (float) i;
        this.f = (float) i2;
    }

    public void setTextureContainer(t tVar) {
        this.g = tVar;
        this.e = (float) this.g.e();
        this.f = (float) this.g.f();
    }
}
