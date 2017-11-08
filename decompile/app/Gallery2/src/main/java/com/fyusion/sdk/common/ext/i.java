package com.fyusion.sdk.common.ext;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import com.fyusion.sdk.common.ext.d.a;
import com.fyusion.sdk.common.ext.filter.a.l;
import com.fyusion.sdk.common.ext.filter.a.n;
import com.fyusion.sdk.common.p;
import com.huawei.watermark.manager.parse.WMElement;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/* compiled from: Unknown */
public class i {
    private EGLContext a;
    private EGLDisplay b;
    private EGLSurface c;
    private d d = new d();
    private a e = new a();
    private n f = new n();
    private p g = new p();
    private ByteBuffer h;

    private void b() {
        if (!EGL14.eglMakeCurrent(this.b, this.c, this.c, this.a)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }

    private void b(int i, int i2) {
        this.b = EGL14.eglGetDisplay(0);
        EGLConfig[] eGLConfigArr = new EGLConfig[1];
        if (EGL14.eglChooseConfig(this.b, new int[]{12324, 8, 12323, 8, 12322, 8, 12321, 8, 12339, 1, 12352, 4, 12344}, 0, eGLConfigArr, 0, eGLConfigArr.length, new int[1], 0)) {
            this.a = EGL14.eglCreateContext(this.b, eGLConfigArr[0], EGL14.EGL_NO_CONTEXT, new int[]{12440, 2, 12344}, 0);
            this.c = EGL14.eglCreatePbufferSurface(this.b, eGLConfigArr[0], new int[]{12375, i, 12374, i2, 12344}, 0);
            return;
        }
        throw new RuntimeException("unable to find RGBA8888+recordable EGL config");
    }

    Bitmap a(Bitmap bitmap, l lVar) {
        if (this.h != null) {
            this.h.rewind();
            this.f.a = lVar;
            int i = this.e.a;
            int i2 = this.e.b;
            this.d.c();
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
            GLES20.glClear(16384);
            GLES20.glViewport(0, 0, i, i2);
            this.g.a(bitmap);
            this.f.b();
            this.f.b(this.g.d());
            this.f.a(this.g);
            this.f.j();
            GLES20.glReadPixels(0, 0, i, i2, 6408, 5121, this.h);
            this.d.d();
            Bitmap createBitmap = Bitmap.createBitmap(i, i2, Config.ARGB_8888);
            createBitmap.copyPixelsFromBuffer(this.h);
            return createBitmap;
        }
        throw new IllegalStateException("Please call setSize() first before applyFilter");
    }

    void a() {
        if (!(this.b == null || this.b == EGL14.EGL_NO_DISPLAY)) {
            EGL14.eglMakeCurrent(this.b, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroySurface(this.b, this.c);
            EGL14.eglDestroyContext(this.b, this.a);
            EGL14.eglReleaseThread();
            EGL14.eglTerminate(this.b);
        }
        this.b = EGL14.EGL_NO_DISPLAY;
        this.a = EGL14.EGL_NO_CONTEXT;
        this.c = EGL14.EGL_NO_SURFACE;
    }

    void a(int i, int i2) {
        if (this.e.a != i || this.e.b != i2) {
            a();
            this.h = ByteBuffer.allocateDirect((i * i2) * 4);
            this.h.order(ByteOrder.nativeOrder());
            b(i, i2);
            b();
            this.e.a = i;
            this.e.b = i2;
            if (this.d.b()) {
                this.d.a();
                this.g.b();
            }
            this.d.a(this.e);
            this.g.a();
            this.f.b();
        }
    }
}
