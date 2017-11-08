package com.fyusion.sdk.common;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.fyusion.sdk.core.util.b;
import com.fyusion.sdk.core.util.c;
import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: Unknown */
public class p {
    private int a = -1;
    private int b = -1;
    private int c = -1;
    private e d = new e(-1.0d, -1.0d);
    private float[] e = new float[16];

    private void i() {
        GLES20.glBindTexture(3553, d());
        GLES20.glTexParameteri(3553, 10241, 9729);
        GLES20.glTexParameteri(3553, 10240, 9729);
        GLES20.glTexParameteri(3553, 10242, 33071);
        GLES20.glTexParameteri(3553, 10243, 33071);
        GLES20.glPixelStorei(3317, 1);
        GLES20.glBindTexture(3553, 0);
        c.a();
    }

    public void a() {
        int[] iArr = new int[]{-1};
        GLES20.glGenTextures(1, iArr, 0);
        this.a = iArr[0];
        b.c(this.a, 0);
        c.a();
        i();
    }

    public void a(int i) {
        this.a = i;
    }

    public void a(Bitmap bitmap) {
        b.a(c());
        GLES20.glBindTexture(3553, this.a);
        GLUtils.texImage2D(3553, 0, bitmap, 0);
        GLES20.glBindTexture(3553, 0);
        c.a();
        this.b = bitmap.getWidth();
        this.c = bitmap.getHeight();
        this.d = new e((double) this.b, (double) this.c);
        float[] fArr = new float[]{0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.5f, 0.5f, 0.0f, WMElement.CAMERASIZEVALUE1B1};
        r0 = new float[16];
        Matrix.multiplyMM(r0, 0, fArr, 0, new float[]{-2.0f, 0.0f, 0.0f, 0.0f, 0.0f, -2.0f, 0.0f, 0.0f, 0.0f, 0.0f, GroundOverlayOptions.NO_DIMENSION, 0.0f, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, 0.0f, WMElement.CAMERASIZEVALUE1B1}, 0);
        r4 = new float[16];
        Matrix.invertM(r4, 0, r0, 0);
        Matrix.multiplyMM(this.e, 0, new float[]{(float) this.b, 0.0f, 0.0f, 0.0f, 0.0f, (float) this.c, 0.0f, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1}, 0, r4, 0);
    }

    public void a(e eVar) {
        this.d = eVar;
    }

    public void a(float[] fArr) {
        this.e = fArr;
    }

    public float[] a(float[] fArr, float[] fArr2) {
        float[] fArr3 = new float[16];
        Matrix.multiplyMM(fArr3, 0, new float[]{0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.5f, 0.5f, 0.0f, WMElement.CAMERASIZEVALUE1B1}, 0, fArr, 0);
        float[] fArr4 = new float[16];
        Matrix.invertM(fArr4, 0, fArr3, 0);
        fArr3 = new float[16];
        Matrix.multiplyMM(fArr3, 0, fArr2, 0, fArr4, 0);
        return fArr3;
    }

    public void b() {
        if (c()) {
            GLES20.glDeleteTextures(1, new int[]{this.a}, 0);
            this.a = -1;
            this.b = -1;
            this.c = -1;
        }
    }

    public void b(int i) {
        this.b = i;
    }

    public void c(int i) {
        this.c = i;
    }

    public boolean c() {
        return this.a > 0;
    }

    public int d() {
        b.a(c());
        return this.a;
    }

    public int e() {
        return this.b;
    }

    public int f() {
        return this.c;
    }

    public e g() {
        return this.d;
    }

    public float[] h() {
        return this.e;
    }
}
