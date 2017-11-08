package com.android.gallery3d.ui;

import android.content.Context;
import android.graphics.Matrix;
import com.huawei.gallery.anim.CanvasAnimation;

public interface GLRoot {

    public interface OnDetachListener {
        void onDetachFromWindow();
    }

    public interface OnGLIdleListener {
        boolean onGLIdle(GLCanvas gLCanvas, boolean z);
    }

    boolean addOnGLIdleListener(OnGLIdleListener onGLIdleListener);

    void clearAnimationProxyView(boolean z);

    void freeze();

    GLCanvas getCanvas();

    int getCompensation();

    Matrix getCompensationMatrix();

    Context getContext();

    int getDisplayRotation();

    boolean getInstantTouchingState();

    boolean hasAnimationProxyView();

    boolean isDoingStateTransitionAnimation();

    void lockRenderThread();

    void registerLaunchedAnimation(CanvasAnimation canvasAnimation);

    void requestFullScreenLayout();

    void requestLayoutContentPane();

    void requestRender();

    void requestRenderForced();

    void setAnimationProxyView(GLView gLView);

    void setContentPane(GLView gLView);

    void setLightsOutMode(boolean z);

    void unfreeze();

    void unlockRenderThread();
}
