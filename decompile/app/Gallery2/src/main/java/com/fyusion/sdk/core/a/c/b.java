package com.fyusion.sdk.core.a.c;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import com.fyusion.sdk.common.OpenGLUtils;
import com.fyusion.sdk.core.a.i;
import com.huawei.watermark.manager.parse.WMElement;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/* compiled from: Unknown */
public class b implements i {
    float[] a = new float[16];
    private int b = -1;
    private int c = -1;
    private int d = -1;
    private int e = -1;
    private int f = -1;
    private int g = -1;
    private int h = -1;
    private int i = -1;
    private int j = -1;
    private int k = -1;
    private int l = -1;
    private int m = -1;
    private int n = -1;
    private int o = -1;
    private int p = -1;
    private int q = -1;
    private int r = -1;
    private int s = -1;
    private int t = -1;
    private int u = -1;
    private FloatBuffer v;
    private FloatBuffer w;
    private FloatBuffer x;
    private boolean y = true;
    private a z;

    /* compiled from: Unknown */
    /* renamed from: com.fyusion.sdk.core.a.c.b$1 */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] a = new int[a.values().length];

        static {
            try {
                a[a.BT_601_STUDIO.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                a[a.BT_709_STUDIO.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                a[a.BT_601_FULL.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                a[a.JPEG.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                a[a.BT_709_FULL.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    /* compiled from: Unknown */
    public enum a {
        BT_601_STUDIO,
        BT_601_FULL,
        BT_709_STUDIO,
        BT_709_FULL,
        JPEG
    }

    public b(int i, int i2, int i3, int i4) {
        this.r = i;
        this.s = i2;
        this.t = i3;
        this.u = i4;
        this.z = a.BT_601_STUDIO;
    }

    private void c() {
        float[] fArr = new float[]{0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f, WMElement.CAMERASIZEVALUE1B1, 0.66666f, 0.0f, 0.66666f};
        float[] fArr2 = new float[]{0.0f, 0.66666f, WMElement.CAMERASIZEVALUE1B1, 0.66666f, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, 0.0f, WMElement.CAMERASIZEVALUE1B1};
        float[] fArr3 = new float[]{0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, 0.0f, WMElement.CAMERASIZEVALUE1B1};
        if (this.t * 4 > this.r) {
            float f = ((float) this.r) / ((float) (this.t * 4));
            fArr[4] = f;
            fArr[2] = f;
            fArr2[4] = f;
            fArr2[2] = f;
        }
        this.v = ByteBuffer.allocateDirect(fArr.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.v.put(fArr).position(0);
        this.w = ByteBuffer.allocateDirect(fArr2.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.w.put(fArr2).position(0);
        this.x = ByteBuffer.allocateDirect(fArr3.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.x.put(fArr3).position(0);
        OpenGLUtils.checkErrors();
    }

    private void d() {
        int[] iArr = new int[1];
        GLES20.glGenTextures(1, iArr, 0);
        this.q = iArr[0];
        GLES20.glBindTexture(36197, this.q);
        GLES20.glTexParameterf(36197, 10241, 9729.0f);
        GLES20.glTexParameterf(36197, 10240, 9729.0f);
        GLES20.glTexParameteri(36197, 10242, 33071);
        GLES20.glTexParameteri(36197, 10243, 33071);
        OpenGLUtils.checkErrors();
    }

    private void e() {
        try {
            g();
            f();
        } catch (IOException e) {
            if (this.b != -1) {
                GLES20.glDeleteProgram(this.b);
            }
            if (this.i != -1) {
                GLES20.glDeleteProgram(this.i);
            }
            this.b = -1;
            this.i = -1;
            e.printStackTrace();
        }
    }

    private void f() throws IOException {
        this.i = OpenGLUtils.createProgram("#version 100\nuniform mat4 stMatrix;\nattribute vec4 vertex;\nattribute vec4 texCoords;\nvarying vec2 textureCoordinate;\nvoid main() {\n    gl_Position = vec4(vertex.xy * 2.0 - 1.0, 0, 1);\n    textureCoordinate = (stMatrix * texCoords).st;\n}", "#version 100\n#extension GL_OES_EGL_image_external : require\nuniform highp vec4 uscale;\nuniform highp vec4 vscale;\nuniform highp samplerExternalOES texture;\nuniform highp float texPixelSpacing;\nvarying highp vec2 textureCoordinate;\nvoid main() {\n    highp mat4 matrix;\n    highp vec4 rgb1 = vec4(texture2D(texture,textureCoordinate).rgb,1.0);\n    highp vec4 rgb2 = vec4(texture2D(texture,textureCoordinate+vec2(texPixelSpacing,0.0)).rgb,1.0);\n    highp float u1 = dot(rgb1,uscale);\n    highp float u2 = dot(rgb2,uscale);\n    highp float v1 = dot(rgb1,vscale);\n    highp float v2 = dot(rgb2,vscale);\n    gl_FragColor = vec4(u1,v1,u2,v2);\n}");
        if (this.i != -1) {
            this.l = GLES20.glGetAttribLocation(this.i, "vertex");
            OpenGLUtils.checkLocation(this.l, "vertex");
            this.m = GLES20.glGetUniformLocation(this.i, "stMatrix");
            OpenGLUtils.checkLocation(this.m, "stMatrix");
            this.o = GLES20.glGetUniformLocation(this.i, "texture");
            OpenGLUtils.checkLocation(this.o, "texture");
            this.j = GLES20.glGetUniformLocation(this.i, "uscale");
            OpenGLUtils.checkLocation(this.j, "uscale");
            this.k = GLES20.glGetUniformLocation(this.i, "vscale");
            OpenGLUtils.checkLocation(this.k, "vscale");
            this.n = GLES20.glGetUniformLocation(this.i, "texPixelSpacing");
            OpenGLUtils.checkLocation(this.n, "texPixelSpacing");
            this.p = GLES20.glGetAttribLocation(this.i, "texCoords");
            OpenGLUtils.checkLocation(this.p, "texCoords");
            OpenGLUtils.checkErrors();
        }
    }

    private void g() throws IOException {
        this.b = OpenGLUtils.createProgram("#version 100\nuniform mat4 stMatrix;\nattribute vec4 vertex;\nattribute vec4 texCoords;\nvarying vec2 textureCoordinate;\nvoid main() {\n    gl_Position = vec4(vertex.xy * 2.0 - 1.0, 0, 1);\n    textureCoordinate = (stMatrix * texCoords).st;\n}", "#version 100\n#extension GL_OES_EGL_image_external : require\nuniform highp vec4 lumscale;\nuniform samplerExternalOES texture;\nuniform highp float texPixelSpacing;\nvarying highp vec2 textureCoordinate;\nvoid main () {\n    highp mat4 matrix;\n    matrix[0] = vec4(texture2D(texture,textureCoordinate).rgb,1.0);\n    matrix[1] = vec4(texture2D(texture,textureCoordinate+vec2(texPixelSpacing,0.0)).rgb,1.0);\n    matrix[2] = vec4(texture2D(texture,textureCoordinate+vec2(texPixelSpacing*2.0,0.0)).rgb,1.0);\n    matrix[3] = vec4(texture2D(texture,textureCoordinate+vec2(texPixelSpacing*3.0,0.0)).rgb,1.0);\n    highp vec4 lumdata = lumscale*matrix;\n    gl_FragColor = lumdata;\n}\n");
        if (this.b != -1) {
            this.d = GLES20.glGetAttribLocation(this.b, "vertex");
            OpenGLUtils.checkLocation(this.d, "vertex");
            this.e = GLES20.glGetUniformLocation(this.b, "stMatrix");
            OpenGLUtils.checkLocation(this.e, "stMatrix");
            this.g = GLES20.glGetUniformLocation(this.b, "texture");
            OpenGLUtils.checkLocation(this.g, "texture");
            this.c = GLES20.glGetUniformLocation(this.b, "lumscale");
            OpenGLUtils.checkLocation(this.c, "lumscale");
            this.f = GLES20.glGetUniformLocation(this.b, "texPixelSpacing");
            OpenGLUtils.checkLocation(this.f, "texPixelSpacing");
            this.h = GLES20.glGetAttribLocation(this.b, "texCoords");
            OpenGLUtils.checkLocation(this.h, "texCoords");
            OpenGLUtils.checkErrors();
        }
    }

    public void a() {
        d();
        c();
        e();
    }

    public void a(SurfaceTexture surfaceTexture, boolean z) {
        surfaceTexture.getTransformMatrix(this.a);
        if (z) {
            this.a[5] = -this.a[5];
            this.a[13] = WMElement.CAMERASIZEVALUE1B1 - this.a[13];
        }
        GLES20.glActiveTexture(33984);
        GLES20.glBindTexture(36197, this.q);
        OpenGLUtils.checkErrors();
        if (this.y) {
            GLES20.glClear(16384);
        }
        this.y = false;
        GLES20.glVertexAttribPointer(this.d, 2, 5126, false, 0, this.v);
        GLES20.glVertexAttribPointer(this.h, 2, 5126, false, 0, this.x);
        GLES20.glEnableVertexAttribArray(this.d);
        GLES20.glEnableVertexAttribArray(this.h);
        GLES20.glUseProgram(this.b);
        GLES20.glUniformMatrix4fv(this.e, 1, false, this.a, 0);
        GLES20.glUniform1i(this.g, 0);
        GLES20.glUniform1f(this.f, WMElement.CAMERASIZEVALUE1B1 / ((float) this.r));
        switch (AnonymousClass1.a[this.z.ordinal()]) {
            case 1:
                GLES20.glUniform4f(this.c, 0.257f, 0.504f, 0.098f, 0.0625f);
                break;
            case 2:
                GLES20.glUniform4f(this.c, 0.183f, 0.614f, 0.062f, 0.0625f);
                break;
            case 3:
            case 4:
            case 5:
                GLES20.glUniform4f(this.c, 0.299f, 0.587f, 0.114f, 0.0f);
                break;
        }
        GLES20.glDrawArrays(6, 0, 4);
        OpenGLUtils.checkErrors();
        GLES20.glDisableVertexAttribArray(this.d);
        GLES20.glDisableVertexAttribArray(this.h);
        GLES20.glVertexAttribPointer(this.l, 2, 5126, false, 0, this.w);
        GLES20.glVertexAttribPointer(this.p, 2, 5126, false, 0, this.x);
        GLES20.glEnableVertexAttribArray(this.l);
        GLES20.glEnableVertexAttribArray(this.p);
        GLES20.glUseProgram(this.i);
        GLES20.glUniformMatrix4fv(this.m, 1, false, this.a, 0);
        GLES20.glUniform1i(this.o, 0);
        GLES20.glUniform1f(this.n, 2.0f / ((float) this.r));
        switch (AnonymousClass1.a[this.z.ordinal()]) {
            case 1:
                GLES20.glUniform4f(this.j, -0.148f, -0.291f, 0.439f, 0.5f);
                GLES20.glUniform4f(this.k, 0.439f, -0.368f, -0.071f, 0.5f);
                break;
            case 2:
                GLES20.glUniform4f(this.j, -0.101f, -0.339f, 0.439f, 0.5f);
                GLES20.glUniform4f(this.k, 0.439f, -0.399f, -0.04f, 0.5f);
                break;
            case 3:
            case 4:
            case 5:
                GLES20.glUniform4f(this.j, -0.168736f, -0.331264f, 0.5f, 0.5f);
                GLES20.glUniform4f(this.k, 0.5f, -0.418688f, -0.081312f, 0.5f);
                break;
        }
        GLES20.glDrawArrays(6, 0, 4);
        OpenGLUtils.checkErrors();
        GLES20.glDisableVertexAttribArray(this.l);
        GLES20.glDisableVertexAttribArray(this.p);
    }

    public int[] a(int i, int i2) {
        return new int[]{12375, i, 12374, (i2 * 3) / 2, 12344};
    }

    public int b() {
        return this.q;
    }
}
