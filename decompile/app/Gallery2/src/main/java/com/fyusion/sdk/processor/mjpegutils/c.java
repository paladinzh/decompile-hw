package com.fyusion.sdk.processor.mjpegutils;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.Matrix;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.fyusion.sdk.common.DLog;
import com.huawei.watermark.manager.parse.WMElement;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/* compiled from: Unknown */
public class c {
    private static final Boolean a = Boolean.valueOf(false);
    private final float[] b = new float[]{GroundOverlayOptions.NO_DIMENSION, GroundOverlayOptions.NO_DIMENSION, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, GroundOverlayOptions.NO_DIMENSION, 0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f, GroundOverlayOptions.NO_DIMENSION, WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, 0.0f, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1};
    private FloatBuffer c = ByteBuffer.allocateDirect(this.b.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    private float[] d = new float[16];
    private float[] e = new float[16];
    private int f;
    private int g = -12345;
    private int h;
    private int i;
    private int j;
    private int k;

    public c() {
        this.c.put(this.b).position(0);
        Matrix.setIdentityM(this.e, 0);
    }

    private int a(String str, String str2) {
        int b = b(35633, str);
        if (b == 0) {
            return 0;
        }
        int b2 = b(35632, str2);
        if (b2 == 0) {
            return 0;
        }
        int glCreateProgram = GLES20.glCreateProgram();
        if (glCreateProgram == 0) {
            DLog.e("STextureRender", "Could not create program");
        }
        GLES20.glAttachShader(glCreateProgram, b);
        a("glAttachShader");
        GLES20.glAttachShader(glCreateProgram, b2);
        a("glAttachShader");
        GLES20.glLinkProgram(glCreateProgram);
        int[] iArr = new int[1];
        GLES20.glGetProgramiv(glCreateProgram, 35714, iArr, 0);
        if (iArr[0] != 1) {
            DLog.e("STextureRender", "Could not link program: ");
            DLog.e("STextureRender", GLES20.glGetProgramInfoLog(glCreateProgram));
            GLES20.glDeleteProgram(glCreateProgram);
            glCreateProgram = 0;
        }
        return glCreateProgram;
    }

    public static void a(int i, String str) {
        if (i < 0) {
            throw new RuntimeException("Unable to locate '" + str + "' in program");
        }
    }

    private void a(String str, float[] fArr) {
        String str2 = (((str + ":" + "\n") + "[ " + fArr[0] + ", " + fArr[1] + ", " + fArr[2] + ", " + fArr[3] + " ]\n") + "[ " + fArr[4] + ", " + fArr[5] + ", " + fArr[6] + ", " + fArr[7] + " ]\n") + "[ " + fArr[8] + ", " + fArr[9] + ", " + fArr[10] + ", " + fArr[11] + " ]\n";
        DLog.i("STextureRender", str2 + "[ " + fArr[12] + ", " + fArr[13] + ", " + fArr[14] + ", " + fArr[15] + " ]");
    }

    private int b(int i, String str) {
        int glCreateShader = GLES20.glCreateShader(i);
        a("glCreateShader type=" + i);
        GLES20.glShaderSource(glCreateShader, str);
        a("glShaderSource");
        GLES20.glCompileShader(glCreateShader);
        a("glCompileShader");
        int[] iArr = new int[1];
        GLES20.glGetShaderiv(glCreateShader, 35713, iArr, 0);
        if (iArr[0] != 0) {
            return glCreateShader;
        }
        DLog.e("STextureRender", "Could not compile shader " + i + ":");
        DLog.e("STextureRender", " " + GLES20.glGetShaderInfoLog(glCreateShader));
        GLES20.glDeleteShader(glCreateShader);
        return 0;
    }

    public int a() {
        return this.g;
    }

    public void a(SurfaceTexture surfaceTexture, boolean z) {
        a("onDrawFrame start");
        float[] fArr = new float[16];
        float[] fArr2 = new float[16];
        surfaceTexture.getTransformMatrix(fArr2);
        if (z) {
            fArr2[5] = -fArr2[5];
            fArr2[13] = WMElement.CAMERASIZEVALUE1B1 - fArr2[13];
        }
        Matrix.multiplyMM(fArr, 0, this.e, 0, fArr2, 0);
        if (a.booleanValue()) {
            a("mSTMatrix", fArr);
        }
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(16384);
        GLES20.glUseProgram(this.f);
        a("glUseProgram");
        GLES20.glActiveTexture(33984);
        GLES20.glBindTexture(36197, this.g);
        this.c.position(0);
        GLES20.glVertexAttribPointer(this.j, 3, 5126, false, 20, this.c);
        a("glVertexAttribPointer maPosition");
        GLES20.glEnableVertexAttribArray(this.j);
        a("glEnableVertexAttribArray maPositionHandle");
        this.c.position(3);
        GLES20.glVertexAttribPointer(this.k, 2, 5126, false, 20, this.c);
        a("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(this.k);
        a("glEnableVertexAttribArray maTextureHandle");
        GLES20.glUniformMatrix4fv(this.h, 1, false, this.d, 0);
        GLES20.glUniformMatrix4fv(this.i, 1, false, fArr, 0);
        GLES20.glDrawArrays(5, 0, 4);
        a("glDrawArrays");
        GLES20.glBindTexture(36197, 0);
    }

    public void a(String str) {
        while (true) {
            int glGetError = GLES20.glGetError();
            if (glGetError != 0) {
                DLog.e("STextureRender", str + ": glError " + glGetError);
            } else {
                return;
            }
        }
    }

    public void a(float[] fArr) {
        System.arraycopy(fArr, 0, this.e, 0, 16);
    }

    public void b() {
        this.f = a("uniform mat4 uMVPMatrix;\nuniform mat4 uSTMatrix;\nattribute vec4 aPosition;\nattribute vec4 aTextureCoord;\nvarying vec2 vTextureCoord;\nvoid main() {\n    gl_Position = uMVPMatrix * vec4(aPosition.xyz,1);\n    vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n}\n", "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nvarying vec2 vTextureCoord;\nuniform samplerExternalOES sTexture;\nvoid main() {\n    gl_FragColor = texture2D(sTexture, vTextureCoord);\n}\n");
        if (this.f != 0) {
            this.j = GLES20.glGetAttribLocation(this.f, "aPosition");
            a(this.j, "aPosition");
            this.k = GLES20.glGetAttribLocation(this.f, "aTextureCoord");
            a(this.k, "aTextureCoord");
            this.h = GLES20.glGetUniformLocation(this.f, "uMVPMatrix");
            a(this.h, "uMVPMatrix");
            Matrix.setIdentityM(this.d, 0);
            this.i = GLES20.glGetUniformLocation(this.f, "uSTMatrix");
            a(this.i, "uSTMatrix");
            Matrix.setIdentityM(this.e, 0);
            int[] iArr = new int[1];
            GLES20.glGenTextures(1, iArr, 0);
            this.g = iArr[0];
            GLES20.glBindTexture(36197, this.g);
            a("glBindTexture mTextureID");
            GLES20.glTexParameterf(36197, 10241, 9729.0f);
            GLES20.glTexParameterf(36197, 10240, 9729.0f);
            GLES20.glTexParameteri(36197, 10242, 33071);
            GLES20.glTexParameteri(36197, 10243, 33071);
            a("glTexParameter");
            return;
        }
        throw new RuntimeException("failed creating program");
    }
}
