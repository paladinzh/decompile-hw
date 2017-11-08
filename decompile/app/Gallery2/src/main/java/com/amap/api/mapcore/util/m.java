package com.amap.api.mapcore.util;

import android.opengl.GLSurfaceView.Renderer;

/* compiled from: IGLSurfaceView */
public interface m {
    int getHeight();

    int getWidth();

    boolean isEnabled();

    void queueEvent(Runnable runnable);

    void requestRender();

    void setRenderMode(int i);

    void setRenderer(Renderer renderer);

    void setVisibility(int i);

    void setZOrderOnTop(boolean z);
}
