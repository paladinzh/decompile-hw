package com.fyusion.sdk.common.ext;

import android.graphics.Bitmap;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import com.fyusion.sdk.common.ext.filter.ImageFilter;
import com.fyusion.sdk.common.ext.filter.a.l;
import com.fyusion.sdk.common.ext.filter.a.n;
import com.fyusion.sdk.common.t;
import java.util.Collection;

/* compiled from: Unknown */
public class d {
    private EGLContext a = null;
    private EGLDisplay b = null;
    private EGLSurface c = null;
    private n d = new n();
    private t e = new t();

    public d(EGLContext eGLContext, EGLDisplay eGLDisplay, EGLSurface eGLSurface, int i, int i2) {
        this.a = eGLContext;
        this.b = eGLDisplay;
        this.c = eGLSurface;
        a();
        this.e.a();
        this.d.a();
        GLES20.glViewport(0, 0, i, i2);
    }

    private void a() {
        if (!EGL14.eglMakeCurrent(this.b, this.c, this.c, this.a)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }

    public void a(Bitmap bitmap) {
        a();
        this.e.a(bitmap);
        this.d.b(this.e.d());
        this.d.a(this.e);
        this.d.j();
    }

    public void a(Collection<ImageFilter> collection) {
        a();
        l lVar = new l();
        lVar.a((Collection) collection);
        this.d.a(lVar);
    }
}
