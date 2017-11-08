package com.fyusion.sdk.viewer.view;

import android.graphics.Matrix;
import android.opengl.GLES20;
import android.util.Log;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.fyusion.sdk.common.c.b;
import com.fyusion.sdk.common.e;
import com.fyusion.sdk.common.p;
import com.fyusion.sdk.core.a.d;
import com.fyusion.sdk.core.util.a;
import com.fyusion.sdk.core.util.c;
import com.huawei.watermark.manager.parse.WMElement;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/* compiled from: Unknown */
public class i {
    private int A = -1;
    private int B = -1;
    private int C = -1;
    private int D = -1;
    private int E = -1;
    private int F = -1;
    private int G = -1;
    private int H = -1;
    private int I = -1;
    private int J = -1;
    private int K = -1;
    private int L = -1;
    private int M = -1;
    private int N = -1;
    private int O = -1;
    private int P = -1;
    private int Q = -1;
    private int R = -1;
    private int S = -1;
    private int T = -1;
    private int U = -1;
    private int V = -1;
    private int W = -1;
    private volatile d X = null;
    private volatile d Y = null;
    private volatile int Z = -1;
    private float a = WMElement.CAMERASIZEVALUE1B1;
    private int aA = 0;
    private p aB = new p();
    private volatile int aa = -1;
    private String ab = null;
    private volatile d ac = null;
    private volatile d ad = null;
    private volatile int ae = -1;
    private volatile int af = -1;
    private volatile String ag = null;
    private volatile b ah = null;
    private volatile b ai = null;
    private volatile Matrix aj = null;
    private volatile float ak = 0.0f;
    private float[] al = new float[]{0.0f, WMElement.CAMERASIZEVALUE1B1};
    private float[] am = new float[]{0.0f, WMElement.CAMERASIZEVALUE1B1};
    private float[] an = new float[]{0.0f, WMElement.CAMERASIZEVALUE1B1};
    private float[] ao = new float[]{0.0f, WMElement.CAMERASIZEVALUE1B1};
    private float[] ap = new float[]{WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1};
    private float[] aq = new float[]{2.0f, 0.0f, 0.0f, GroundOverlayOptions.NO_DIMENSION, 0.0f, 2.0f, 0.0f, GroundOverlayOptions.NO_DIMENSION, 0.0f, 0.0f, GroundOverlayOptions.NO_DIMENSION, -0.0f, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1};
    private boolean ar = true;
    private float[] as = new float[16];
    private float[] at = new float[]{WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1};
    private float[] au = new float[]{WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1};
    private float[] av = new float[]{WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1};
    private float[] aw = new float[]{WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1};
    private boolean ax = true;
    private boolean ay = true;
    private int az = 0;
    private float b = WMElement.CAMERASIZEVALUE1B1;
    private boolean c = true;
    private float[] d = new float[]{WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1};
    private float[] e = new float[]{WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1};
    private float[] f = new float[]{WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1};
    private boolean g = true;
    private float[] h = new float[]{WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1};
    private boolean i = true;
    private float j = 0.0f;
    private boolean k = true;
    private FloatBuffer l;
    private int m = -1;
    private int n = -1;
    private int o = -1;
    private int p = -1;
    private int q = -1;
    private int r = -1;
    private int s = -1;
    private int t = -1;
    private int u = -1;
    private int v = -1;
    private int w = -1;
    private int x = -1;
    private int y = -1;
    private int z = -1;

    private void a(Matrix matrix) {
        matrix.getValues(this.e);
        this.f[0] = this.e[0];
        this.f[4] = this.e[1];
        this.f[12] = this.e[2];
        this.f[1] = this.e[3];
        this.f[5] = this.e[4];
        this.f[13] = this.e[5];
        this.f[3] = this.e[6];
        this.f[7] = this.e[7];
        this.f[15] = this.e[8];
        this.g = true;
    }

    private void a(p pVar) {
        this.aB = pVar;
    }

    private void a(d dVar, int i) {
        dVar.a(this.o, this.p);
        this.aa = i;
        this.Y = dVar;
    }

    private void a(float[] fArr, float[] fArr2) {
        float f = ((fArr2[0] * fArr[3]) + (fArr2[1] * fArr[4])) + fArr[7];
        fArr2[0] = (((fArr2[0] * fArr[0]) + (fArr2[1] * fArr[1])) + fArr[6]) / this.a;
        fArr2[1] = f / this.b;
    }

    private void a(float[] fArr, float[] fArr2, float[] fArr3) {
        float[] fArr4 = new float[]{0.0f, 0.0f};
        int b = this.X == null ? 0 : this.X.b();
        int c = this.X == null ? 0 : this.X.c();
        float[] fArr5 = new float[]{(float) b, (float) c};
        a(fArr, fArr4);
        a(fArr, fArr5);
        fArr2[0] = Math.min(fArr4[0], fArr5[0]);
        fArr2[1] = Math.max(fArr4[0], fArr5[0]);
        fArr3[0] = Math.min(fArr4[1], fArr5[1]);
        fArr3[1] = Math.max(fArr4[1], fArr5[1]);
    }

    private void b(float f) {
        this.j = f;
        this.k = true;
    }

    private void b(d dVar, int i, d dVar2, int i2, String str) {
        if (dVar != null && dVar2 != null) {
            if (i == this.aa || i2 == this.Z) {
                int i3 = this.m;
                this.m = this.o;
                this.o = i3;
                i3 = this.n;
                this.n = this.p;
                this.p = i3;
                i3 = this.Z;
                this.Z = this.aa;
                this.aa = i3;
            }
            b(dVar, i, str);
            a(dVar2, i2);
        }
    }

    private void b(d dVar, int i, String str) {
        this.ab = str;
        dVar.a(this.m, this.n);
        this.a = (float) dVar.b();
        this.b = (float) dVar.c();
        this.Z = i;
        this.X = dVar;
        this.d[0] = this.a;
        this.d[5] = this.b;
        this.c = true;
    }

    private void d() {
        r4 = new float[4];
        float[] fArr = new float[]{Math.min(0.0f, this.h[12]), Math.min(0.0f, this.h[13]), this.a, this.b};
        android.opengl.Matrix.multiplyMV(fArr, 0, this.f, 0, r4, 0);
        float ceil = (float) Math.ceil((double) Math.abs(fArr[0]));
        float ceil2 = (float) Math.ceil((double) Math.abs(fArr[1]));
        if ((this.h[0] * ceil) + this.h[12] < ((float) this.az)) {
            this.h[12] = ((float) this.az) - (this.h[0] * ceil);
        }
        if ((this.h[5] * ceil2) + this.h[13] < ((float) this.aA)) {
            this.h[13] = ((float) this.aA) - (ceil2 * this.h[5]);
        }
    }

    private void e() {
        String str = "#version 100\nuniform int orderVU;uniform int layout_;uniform int colorspace;uniform int frameWidth;uniform int frameHeight;precision highp int;precision highp float;precision highp sampler2D;vec4 semiplanarYUV(sampler2D plane0, sampler2D plane1, highp vec2 coordinate){   float Y = texture2D(plane0, coordinate).r;   vec4 UV = texture2D(plane1, coordinate);   return vec4(Y, UV.r, UV.a, 1.0);}vec4 planarYUV(sampler2D plane0, sampler2D plane1, vec2 coordinate){   float Y = texture2D(plane0, coordinate).r;   float U = texture2D(plane1, coordinate * vec2(1.0, 0.5)).r;   float V = texture2D(plane1, coordinate * vec2(1.0, 0.5) + vec2(0.0, 0.5)).r;   return vec4(Y, U, V, 1.0);}vec4 YUV(sampler2D plane0, sampler2D plane1, vec2 coordinate){   vec4 yuv;   if(layout_ == 0) yuv = planarYUV(plane0, plane1, coordinate);   if(layout_ == 1) yuv = semiplanarYUV(plane0, plane1, coordinate);   if(orderVU == 0) return yuv;   return vec4(yuv.x, yuv.z, yuv.y, yuv.w);}vec4 RGBA(sampler2D plane0, sampler2D plane1, vec2 coordinate){   if(colorspace == 0) return vec4(texture2D(plane0, coordinate).rgb, 1.0);   vec4 yuv = YUV(plane0, plane1, coordinate);   if (colorspace == 1){      float R = (1.1643835616 * (yuv.x - 0.0625) + 1.5958 * (yuv.z - 0.5));      float G = (1.1643835616 * (yuv.x - 0.0625) - 0.8129 * (yuv.z - 0.5) - 0.39173 * (yuv.y - 0.5));      float B = (1.1643835616 * (yuv.x - 0.0625) + 2.017 * (yuv.y - 0.5));      return vec4(R, G, B, 1.0);   }   if (colorspace == 2) {      float R = yuv.x + 1.402 * (yuv.z - 0.5);      float G = yuv.x - 0.344136 * (yuv.y - 0.5) - 0.741135 * (yuv.z - 0.5);      float B = yuv.x + 1.772 * (yuv.y - 0.5);      return vec4(R, G, B, 1.0);   }   return vec4(0.333, 0.333, 0.333, 1.0);}";
        int glCreateShader = GLES20.glCreateShader(35633);
        int glCreateShader2 = GLES20.glCreateShader(35632);
        int glCreateShader3 = GLES20.glCreateShader(35632);
        GLES20.glShaderSource(glCreateShader, "#version 100\nuniform mat4 model_view_projection;attribute vec2 quad_vertex;varying vec2 texture_coordinate;void main (){    gl_Position = model_view_projection * vec4 (quad_vertex, 0.0, 1.0);    texture_coordinate = quad_vertex;}");
        GLES20.glShaderSource(glCreateShader2, str + "uniform sampler2D image_plane0_texture;uniform sampler2D image_plane1_texture;varying vec2 texture_coordinate;void main (){    gl_FragColor = RGBA(image_plane0_texture, image_plane1_texture, texture_coordinate);}");
        GLES20.glShaderSource(glCreateShader3, str + "uniform sampler2D source_plane0_texture;uniform sampler2D source_plane1_texture;uniform sampler2D target_plane0_texture;uniform sampler2D target_plane1_texture;uniform mat3 intermediate_to_source;uniform mat3 intermediate_to_target;uniform vec2 validSourceXRange;uniform vec2 validSourceYRange;uniform vec2 validTargetXRange;uniform vec2 validTargetYRange;uniform float imagewidth;uniform float imageheight;uniform float alpha;varying vec2 texture_coordinate;vec2 transformTextureCoordinates (vec2 tex_coord, mat3 transformation, vec2 wh){    return (transformation * vec3 ((tex_coord - vec2 (0.5, 0.5)) * wh, 1.0)).xy / wh + vec2 (0.5, 0.5);}void main (){    vec2 wh = vec2 (imagewidth, imageheight);    vec2 source_coordinate = transformTextureCoordinates (texture_coordinate, intermediate_to_source, wh);    vec2 target_coordinate = transformTextureCoordinates (texture_coordinate, intermediate_to_target, wh);    vec4 source_color;    bool validSource = (source_coordinate.x >= validSourceXRange.x && source_coordinate.x <= validSourceXRange.y &&        source_coordinate.y >= validSourceYRange.x && source_coordinate.y <= validSourceYRange.y);    if(validSource) source_color = RGBA(source_plane0_texture, source_plane1_texture, source_coordinate);    vec4 target_color;    bool validTarget = (target_coordinate.x >= validTargetXRange.x && target_coordinate.x <= validTargetXRange.y &&        target_coordinate.y >= validTargetYRange.x && target_coordinate.y <= validTargetYRange.y);    if(validTarget) target_color = RGBA(target_plane0_texture, target_plane1_texture, target_coordinate);    float alpha_with_border_handling = alpha;    if(source_coordinate.x <= 0.005 || source_coordinate.y <= 0.005 ||        source_coordinate.x >= 0.995 || source_coordinate.y >= 0.995){        alpha_with_border_handling = 1.0;    }    if(target_coordinate.x <= 0.005 || target_coordinate.y <= 0.005 ||        target_coordinate.x >= 0.995 || target_coordinate.y >= 0.995){        alpha_with_border_handling = 0.0;    }    if(validSource && !validTarget) gl_FragColor = vec4(source_color.rgb, 1.0);    if(!validSource && validTarget) gl_FragColor = vec4(target_color.rgb, 1.0);    if(validSource && validTarget) gl_FragColor = mix(source_color, target_color, alpha_with_border_handling);}");
        GLES20.glCompileShader(glCreateShader);
        String glGetShaderInfoLog = GLES20.glGetShaderInfoLog(glCreateShader);
        GLES20.glCompileShader(glCreateShader2);
        str = GLES20.glGetShaderInfoLog(glCreateShader2);
        GLES20.glCompileShader(glCreateShader3);
        String glGetShaderInfoLog2 = GLES20.glGetShaderInfoLog(glCreateShader3);
        if ((glGetShaderInfoLog.length() + str.length()) + glGetShaderInfoLog2.length() > 0) {
            Log.e("TweeningRenderer", "errors in vertex shader: " + glGetShaderInfoLog);
            Log.e("TweeningRenderer", "errors in nontweening fragment shader: " + str);
            Log.e("TweeningRenderer", "errors in tweening fragment shader: " + glGetShaderInfoLog2);
            Log.e("TweeningRenderer", "shader compilation failure -- expect a black screen");
        }
        this.q = GLES20.glCreateProgram();
        GLES20.glAttachShader(this.q, glCreateShader);
        GLES20.glAttachShader(this.q, glCreateShader2);
        GLES20.glLinkProgram(this.q);
        c.a(this.q);
        GLES20.glDetachShader(this.q, glCreateShader2);
        GLES20.glDetachShader(this.q, glCreateShader);
        c.a();
        this.z = GLES20.glCreateProgram();
        GLES20.glAttachShader(this.z, glCreateShader);
        GLES20.glAttachShader(this.z, glCreateShader3);
        GLES20.glLinkProgram(this.z);
        c.a(this.z);
        GLES20.glDetachShader(this.z, glCreateShader3);
        GLES20.glDetachShader(this.z, glCreateShader);
        c.a();
        GLES20.glDeleteShader(glCreateShader);
        GLES20.glDeleteShader(glCreateShader2);
        GLES20.glDeleteShader(glCreateShader3);
        this.r = GLES20.glGetUniformLocation(this.q, "model_view_projection");
        this.s = GLES20.glGetAttribLocation(this.q, "quad_vertex");
        this.L = GLES20.glGetUniformLocation(this.q, "colorspace");
        this.M = GLES20.glGetUniformLocation(this.q, "layout_");
        this.N = GLES20.glGetUniformLocation(this.q, "orderVU");
        this.t = GLES20.glGetUniformLocation(this.q, "image_plane0_texture");
        this.u = GLES20.glGetUniformLocation(this.q, "image_plane1_texture");
        this.v = GLES20.glGetUniformLocation(this.q, "numTilesY_Y");
        this.w = GLES20.glGetUniformLocation(this.q, "numTilesY_UV");
        this.x = GLES20.glGetUniformLocation(this.q, "frameWidth");
        this.y = GLES20.glGetUniformLocation(this.q, "frameHeight");
        GLES20.glUseProgram(this.q);
        GLES20.glUniform1i(this.t, 0);
        GLES20.glUniform1i(this.u, 1);
        GLES20.glUseProgram(0);
        this.A = GLES20.glGetUniformLocation(this.z, "model_view_projection");
        this.B = GLES20.glGetAttribLocation(this.z, "quad_vertex");
        this.O = GLES20.glGetUniformLocation(this.z, "colorspace");
        this.P = GLES20.glGetUniformLocation(this.z, "layout_");
        this.Q = GLES20.glGetUniformLocation(this.z, "orderVU");
        this.C = GLES20.glGetUniformLocation(this.z, "source_plane0_texture");
        this.D = GLES20.glGetUniformLocation(this.z, "source_plane1_texture");
        this.E = GLES20.glGetUniformLocation(this.z, "target_plane0_texture");
        this.F = GLES20.glGetUniformLocation(this.z, "target_plane1_texture");
        this.G = GLES20.glGetUniformLocation(this.z, "intermediate_to_source");
        this.H = GLES20.glGetUniformLocation(this.z, "intermediate_to_target");
        this.I = GLES20.glGetUniformLocation(this.z, "alpha");
        this.J = GLES20.glGetUniformLocation(this.z, "imagewidth");
        this.K = GLES20.glGetUniformLocation(this.z, "imageheight");
        this.R = GLES20.glGetUniformLocation(this.z, "frameWidth");
        this.S = GLES20.glGetUniformLocation(this.z, "frameHeight");
        this.T = GLES20.glGetUniformLocation(this.z, "validSourceXRange");
        this.U = GLES20.glGetUniformLocation(this.z, "validSourceYRange");
        this.V = GLES20.glGetUniformLocation(this.z, "validTargetXRange");
        this.W = GLES20.glGetUniformLocation(this.z, "validTargetYRange");
        GLES20.glUseProgram(this.z);
        GLES20.glUniform1i(this.C, 0);
        GLES20.glUniform1i(this.D, 1);
        GLES20.glUniform1i(this.E, 2);
        GLES20.glUniform1i(this.F, 3);
        GLES20.glUseProgram(0);
        c.a();
    }

    private void f() {
        float[] fArr = new float[]{0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, 0.0f, WMElement.CAMERASIZEVALUE1B1};
        ByteBuffer allocateDirect = ByteBuffer.allocateDirect(fArr.length * 4);
        allocateDirect.order(ByteOrder.nativeOrder());
        this.l = allocateDirect.asFloatBuffer();
        this.l.put(fArr);
        this.l.position(0);
        c.a();
    }

    private void g() {
        this.m = i();
        this.n = i();
        c.a();
    }

    private void h() {
        this.o = i();
        this.p = i();
        c.a();
    }

    private int i() {
        int[] iArr = new int[1];
        GLES20.glGenTextures(1, iArr, 0);
        GLES20.glBindTexture(3553, iArr[0]);
        GLES20.glTexParameteri(3553, 10241, 9729);
        GLES20.glTexParameteri(3553, 10240, 9729);
        GLES20.glTexParameteri(3553, 10242, 33071);
        GLES20.glTexParameteri(3553, 10243, 33071);
        GLES20.glPixelStorei(3317, 1);
        GLES20.glBindTexture(3553, 0);
        return iArr[0];
    }

    private void j() {
        GLES20.glUseProgram(this.q);
        c.a();
        l();
        GLES20.glActiveTexture(33984);
        GLES20.glBindTexture(3553, this.m);
        GLES20.glActiveTexture(33985);
        GLES20.glBindTexture(3553, this.n);
        GLES20.glEnableVertexAttribArray(this.s);
        GLES20.glVertexAttribPointer(this.s, 2, 5126, false, 0, this.l);
        GLES20.glDrawArrays(6, 0, 4);
        GLES20.glDisableVertexAttribArray(this.s);
        GLES20.glActiveTexture(33984);
        GLES20.glBindTexture(3553, 0);
        GLES20.glUseProgram(0);
        GLES20.glFinish();
        c.a();
    }

    private void k() {
        GLES20.glUseProgram(this.z);
        c.a();
        m();
        GLES20.glActiveTexture(33984);
        GLES20.glBindTexture(3553, this.m);
        GLES20.glActiveTexture(33985);
        GLES20.glBindTexture(3553, this.n);
        GLES20.glActiveTexture(33986);
        GLES20.glBindTexture(3553, this.o);
        GLES20.glActiveTexture(33987);
        GLES20.glBindTexture(3553, this.p);
        GLES20.glEnableVertexAttribArray(this.B);
        GLES20.glVertexAttribPointer(this.B, 2, 5126, false, 0, this.l);
        GLES20.glDrawArrays(6, 0, 4);
        GLES20.glDisableVertexAttribArray(this.B);
        GLES20.glActiveTexture(33987);
        GLES20.glBindTexture(3553, 0);
        GLES20.glActiveTexture(33986);
        GLES20.glBindTexture(3553, 0);
        GLES20.glActiveTexture(33985);
        GLES20.glBindTexture(3553, 0);
        GLES20.glActiveTexture(33984);
        GLES20.glBindTexture(3553, 0);
        GLES20.glUseProgram(0);
        GLES20.glFinish();
        c.a();
    }

    private void l() {
        if (this.c || this.g || this.ar || this.i) {
            float[] fArr = new float[16];
            android.opengl.Matrix.multiplyMM(this.ap, 0, this.f, 0, this.d, 0);
            android.opengl.Matrix.multiplyMM(fArr, 0, this.h, 0, this.ap, 0);
            android.opengl.Matrix.multiplyMM(this.as, 0, this.aq, 0, fArr, 0);
            GLES20.glUniformMatrix4fv(this.r, 1, false, this.as, 0);
            c.a();
            this.c = false;
            this.g = false;
            this.ar = false;
            this.i = false;
        }
        if (!(this.X == null || this.X.f())) {
            this.X.a(this.L, this.M, this.N, this.x, this.y);
        }
        c.a();
    }

    private void m() {
        if (this.c || this.g || this.ar || this.i) {
            float[] fArr = new float[16];
            android.opengl.Matrix.multiplyMM(this.ap, 0, this.f, 0, this.d, 0);
            android.opengl.Matrix.multiplyMM(fArr, 0, this.h, 0, this.ap, 0);
            android.opengl.Matrix.multiplyMM(this.as, 0, this.aq, 0, fArr, 0);
            GLES20.glUniform1f(this.J, this.a);
            GLES20.glUniform1f(this.K, this.b);
            GLES20.glUniformMatrix4fv(this.A, 1, false, this.as, 0);
            this.c = false;
            this.g = false;
            this.ar = false;
            this.i = false;
        }
        if (!(this.X == null || this.X.f())) {
            this.X.a(this.O, this.P, this.Q, this.R, this.S);
        }
        if (this.ax) {
            GLES20.glUniformMatrix3fv(this.G, 1, false, this.au, 0);
            GLES20.glUniform2f(this.T, this.al[0], this.al[1]);
            GLES20.glUniform2f(this.U, this.am[0], this.am[1]);
            this.ax = false;
        }
        if (this.ay) {
            GLES20.glUniformMatrix3fv(this.H, 1, false, this.aw, 0);
            GLES20.glUniform2f(this.V, this.an[0], this.an[1]);
            GLES20.glUniform2f(this.W, this.ao[0], this.ao[1]);
            this.ay = false;
        }
        if (this.k) {
            GLES20.glUniform1f(this.I, this.j);
            this.k = false;
        }
        c.a();
    }

    public p a() {
        return this.aB;
    }

    public void a(float f) {
        this.ak = f;
    }

    public void a(int i, int i2) {
        int i3;
        this.az = i;
        this.aA = i2;
        for (i3 = 0; i3 < 16; i3++) {
            this.h[i3] = 0.0f;
        }
        for (i3 = 0; i3 < 16; i3 += 5) {
            this.h[i3] = WMElement.CAMERASIZEVALUE1B1;
        }
        android.opengl.Matrix.orthoM(this.aq, 0, 0.0f, (float) i, (float) i2, 0.0f, GroundOverlayOptions.NO_DIMENSION, WMElement.CAMERASIZEVALUE1B1);
        this.ar = true;
        Log.d("RenderPath", "width=" + i + " height=" + i2);
    }

    public void a(b bVar) {
        this.at[0] = (float) bVar.a;
        this.at[3] = (float) bVar.c;
        this.at[6] = (float) bVar.e;
        this.at[1] = (float) bVar.b;
        this.at[4] = (float) bVar.d;
        this.at[7] = (float) bVar.f;
        a.a(this.au, this.at);
        this.ax = true;
        a(this.at, this.al, this.am);
    }

    public void a(b bVar, b bVar2) {
        synchronized (this) {
            this.ah = bVar;
            this.ai = bVar2;
        }
    }

    public void a(d dVar, int i, d dVar2, int i2, String str) {
        synchronized (this) {
            this.ac = dVar;
            this.ae = i;
            this.ad = dVar2;
            this.af = i2;
            this.ag = str;
        }
    }

    public void a(d dVar, int i, String str) {
        synchronized (this) {
            this.ac = dVar;
            this.ae = i;
            this.ag = str;
        }
    }

    public void applyViewPan(float f, float f2) {
        float[] fArr = this.h;
        fArr[12] = fArr[12] + f;
        fArr = this.h;
        fArr[13] = fArr[13] + f2;
        d();
        this.i = true;
    }

    public void applyViewScale(float f, float f2, float f3) {
        float f4 = this.h[0];
        float min = (((Math.min(WMElement.CAMERASIZEVALUE1B1, Math.max(GroundOverlayOptions.NO_DIMENSION, ((2.0f * f2) / ((float) this.az)) - WMElement.CAMERASIZEVALUE1B1)) - this.aq[12]) / this.aq[0]) - this.h[12]) / this.h[0];
        float f5 = ((((-Math.min(WMElement.CAMERASIZEVALUE1B1, Math.max(GroundOverlayOptions.NO_DIMENSION, ((2.0f * f3) / ((float) this.aA)) - WMElement.CAMERASIZEVALUE1B1))) - this.aq[13]) / this.aq[5]) - this.h[13]) / this.h[5];
        this.h[0] = f;
        this.h[5] = f;
        this.h[12] = (min * (f4 - f)) + this.h[12];
        this.h[13] = ((f4 - f) * f5) + this.h[13];
        Log.d("TweeningRenderer", "tx=" + this.h[12] + " ty=" + this.h[13]);
        d();
        this.i = true;
    }

    void b() {
        this.Z = -1;
        this.aa = -1;
        e();
        f();
        g();
        h();
    }

    public void b(b bVar) {
        this.av[0] = (float) bVar.a;
        this.av[3] = (float) bVar.c;
        this.av[6] = (float) bVar.e;
        this.av[1] = (float) bVar.b;
        this.av[4] = (float) bVar.d;
        this.av[7] = (float) bVar.f;
        a.a(this.aw, this.av);
        this.ay = true;
        a(this.av, this.an, this.ao);
    }

    public void c(b bVar) {
        synchronized (this) {
            this.ah = bVar;
        }
    }

    public boolean c() {
        float f;
        Matrix matrix;
        b bVar;
        String str;
        int i;
        boolean z;
        d dVar;
        b bVar2;
        d dVar2;
        int i2 = -1;
        p a = a();
        a.b(this.az);
        a.c(this.aA);
        a.a(new e((double) this.a, (double) this.b));
        a.a(a.a(this.as, this.d));
        a(a);
        synchronized (this) {
            if (this.ac == null) {
                f = 0.0f;
                matrix = null;
                bVar = null;
                str = null;
                i = -1;
                z = false;
                dVar = null;
            } else {
                d dVar3 = this.ac;
                this.ac = null;
                int i3 = this.ae;
                this.ae = -1;
                b bVar3 = this.ah;
                this.ah = null;
                str = this.ag;
                this.ag = null;
                Matrix matrix2 = this.aj;
                this.aj = null;
                matrix = matrix2;
                bVar = bVar3;
                z = true;
                i = i3;
                d dVar4 = dVar3;
                f = this.ak;
                dVar = dVar4;
            }
            boolean z2;
            if (this.ad == null) {
                bVar2 = null;
                dVar2 = null;
                z2 = false;
            } else {
                dVar2 = this.ad;
                this.ad = null;
                i2 = this.af;
                this.af = -1;
                b bVar4 = this.ai;
                this.ai = null;
                bVar2 = bVar4;
                z2 = true;
            }
        }
        if (z && r0) {
            b(dVar, i, dVar2, i2, str);
            if (bVar != null) {
                a(bVar);
            }
            if (bVar2 != null) {
                b(bVar2);
            }
            if (matrix != null) {
                a(matrix);
            }
            b(f);
        } else if (z) {
            b(dVar, i, str);
            if (bVar != null) {
                a(bVar);
            }
            if (matrix != null) {
                a(matrix);
            }
            b(f);
        }
        if (this.X == null || this.X.f()) {
            return false;
        }
        if (this.j < 0.01f) {
            j();
        } else {
            k();
        }
        return true;
    }

    public void recycle() {
        int i = 0;
        this.ab = null;
        this.ag = null;
        this.X = null;
        this.ac = null;
        this.Y = null;
        this.ad = null;
        this.aj = null;
        this.j = 0.0f;
        this.k = true;
        this.ar = true;
        this.i = true;
        for (int i2 = 0; i2 < 16; i2++) {
            this.h[i2] = 0.0f;
        }
        while (i < 16) {
            this.h[i] = WMElement.CAMERASIZEVALUE1B1;
            i += 5;
        }
    }

    public void setImageMatrixPending(Matrix matrix) {
        synchronized (this) {
            this.aj = matrix;
        }
    }
}
