package com.fyusion.sdk.common;

import android.graphics.Matrix;
import android.opengl.GLSurfaceView.Renderer;

/* compiled from: Unknown */
public interface k extends Renderer {

    /* compiled from: Unknown */
    public interface a {
        void a();

        void a(int i, int i2);

        void b();
    }

    void applyViewPan(float f, float f2);

    void applyViewScale(float f, float f2, float f3);

    t getRenderedTextureContainer();

    k getRenderer();

    void recycle();

    void setImageMatrixPending(Matrix matrix);

    void setObserver(a aVar);

    void setOverlayCompositor(com.fyusion.sdk.common.a.a aVar);
}
