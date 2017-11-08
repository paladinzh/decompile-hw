package com.fyusion.sdk.common.ext.filter;

import android.graphics.Matrix;
import android.opengl.GLES20;
import android.support.annotation.NonNull;
import com.fyusion.sdk.common.ext.e;
import com.fyusion.sdk.common.ext.e.a;
import com.fyusion.sdk.common.ext.filter.a.l;
import com.fyusion.sdk.common.ext.filter.a.n;
import com.fyusion.sdk.common.k;
import com.fyusion.sdk.common.t;
import com.fyusion.sdk.core.util.b;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: Unknown */
public class FilterRenderer implements k {
    private k a = null;
    private a b = new a();
    private e c = new e();
    private t d = new t();
    private int e = 1;
    private int f = 1;
    public n filteredTextureRenderer = new n();
    private com.fyusion.sdk.common.a.a g = null;

    public FilterRenderer(k kVar) {
        this.a = kVar;
    }

    private void a() {
        if (this.b.a != this.e || this.b.b != this.f) {
            if (this.c.b()) {
                this.c.a();
            }
            this.b.a = this.e;
            this.b.b = this.f;
            this.b.c = false;
            this.c.a(this.b);
        }
        b.a(this.c.b());
    }

    private void a(GL10 gl10) {
        if (this.a != null) {
            this.c.c();
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GLES20.glClear(16384);
            GLES20.glViewport(0, 0, this.b.a, this.b.b);
            this.a.onDrawFrame(gl10);
            this.c.d();
            t renderedTextureContainer = this.a.getRenderedTextureContainer();
            renderedTextureContainer.a(this.c.e());
            setRenderedTextureContainer(renderedTextureContainer);
        }
    }

    private void b() {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(16384);
        GLES20.glViewport(0, 0, this.e, this.f);
        this.filteredTextureRenderer.b(this.c.e());
        this.filteredTextureRenderer.a(this.d);
        this.filteredTextureRenderer.j();
    }

    public void applyViewPan(float f, float f2) {
        this.a.applyViewPan(f, f2);
    }

    public void applyViewScale(float f, float f2, float f3) {
        this.a.applyViewScale(f, f2, f3);
    }

    public t getRenderedTextureContainer() {
        return this.d;
    }

    public k getRenderer() {
        return this.a;
    }

    public void onDrawFrame(GL10 gl10) {
        a();
        a(gl10);
        b();
        if (this.g != null) {
            this.g.a(gl10);
        }
    }

    public void onSurfaceChanged(GL10 gl10, int i, int i2) {
        GLES20.glViewport(0, 0, i, i2);
        this.e = i;
        this.f = i2;
        if (this.g != null) {
            this.g.a(i, i2);
        }
        if (this.a != null) {
            this.a.onSurfaceChanged(gl10, i, i2);
        }
    }

    public void onSurfaceCreated(GL10 gl10, EGLConfig eGLConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        this.filteredTextureRenderer.b();
        if (this.a != null) {
            this.a.onSurfaceCreated(gl10, eGLConfig);
        }
    }

    public void recycle() {
        this.a.recycle();
        this.b.a = -1;
        this.b.b = -1;
        this.filteredTextureRenderer.a.b = true;
    }

    public void setFilters(l lVar) {
        if (this.filteredTextureRenderer != null) {
            this.filteredTextureRenderer.a(lVar);
        }
    }

    public void setImageMatrixPending(Matrix matrix) {
        this.a.setImageMatrixPending(matrix);
    }

    public void setObserver(k.a aVar) {
        this.a.setObserver(aVar);
    }

    public void setOverlayCompositor(@NonNull com.fyusion.sdk.common.a.a aVar) {
        aVar.a(this.e, this.f);
        this.g = aVar;
    }

    public void setRenderedTextureContainer(t tVar) {
        this.d = tVar;
    }
}
