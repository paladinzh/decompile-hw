package com.fyusion.sdk.viewer.view;

import android.opengl.GLES20;
import android.os.Process;
import android.support.annotation.NonNull;
import android.util.Log;
import com.fyusion.sdk.common.j;
import com.fyusion.sdk.common.j.a;
import com.fyusion.sdk.common.o;
import com.fyusion.sdk.common.p;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: Unknown */
public class k extends i implements j {
    private final Object a = new Object();
    private a b = null;
    private boolean c = false;
    private volatile boolean d = false;
    private com.fyusion.sdk.common.internal.a e = null;
    private int f = 0;
    private int g = 0;

    public void addOverlay(@NonNull o oVar) {
        if (this.e == null) {
            throw new IllegalStateException("No overlay compositor set in view");
        }
        this.e.a(oVar);
    }

    public p getRenderedTextureContainer() {
        return super.a();
    }

    public j getRenderer() {
        return this;
    }

    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(16384);
        if (super.c()) {
            if (this.e != null) {
                this.e.a(gl10);
            }
            if (this.b != null && !this.d) {
                synchronized (this.a) {
                    if (this.b != null) {
                        if (!this.d) {
                            this.d = true;
                            this.b.b();
                        }
                    }
                }
                return;
            }
            return;
        }
        Log.d("TweeningViewRenderer", "thread " + Process.myTid() + " TweeningViewRenderer render failed");
    }

    public void onSurfaceChanged(GL10 gl10, int i, int i2) {
        super.a(i, i2);
        GLES20.glViewport(0, 0, i, i2);
        this.f = i;
        this.g = i2;
        if (this.e != null) {
            this.e.a(i, i2);
        }
        if (this.b != null) {
            this.b.a(i, i2);
        }
    }

    public void onSurfaceCreated(GL10 gl10, EGLConfig eGLConfig) {
        super.b();
        if (this.b != null) {
            this.b.a();
        }
        synchronized (this.a) {
            this.c = true;
        }
    }

    public void recycle() {
        super.recycle();
        synchronized (this.a) {
            this.d = false;
        }
    }

    public void setObserver(a aVar) {
        this.b = aVar;
        if (aVar != null) {
            synchronized (this.a) {
                if (this.c) {
                    aVar.a();
                }
            }
        }
    }

    public void setOverlayCompositor(@NonNull com.fyusion.sdk.common.internal.a aVar) {
        aVar.a(this.f, this.g);
        this.e = aVar;
    }
}
