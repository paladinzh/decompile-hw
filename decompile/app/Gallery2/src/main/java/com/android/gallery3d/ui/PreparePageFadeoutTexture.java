package com.android.gallery3d.ui;

import android.graphics.Rect;
import android.os.ConditionVariable;
import com.android.gallery3d.ui.GLRoot.OnGLIdleListener;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.app.GLHost;

public class PreparePageFadeoutTexture implements OnGLIdleListener {
    public Rect mAnimRect;
    private boolean mCancelled = false;
    private ConditionVariable mResultReady = new ConditionVariable(false);
    private GLView mRootPane;
    private RawTexture mTexture;

    public PreparePageFadeoutTexture(GLView rootPane) {
        if (rootPane == null) {
            this.mCancelled = true;
            return;
        }
        int w = rootPane.getWidth();
        int h = rootPane.getHeight();
        if (w == 0 || h == 0) {
            this.mCancelled = true;
            return;
        }
        this.mTexture = new RawTexture(w, h, true);
        this.mAnimRect = rootPane.getAnimRect();
        this.mRootPane = rootPane;
    }

    public boolean isCancelled() {
        return this.mCancelled;
    }

    public synchronized RawTexture get() {
        if (this.mCancelled) {
            return null;
        }
        if (this.mResultReady.block(200)) {
            return this.mTexture;
        }
        this.mCancelled = true;
        return null;
    }

    public boolean onGLIdle(GLCanvas canvas, boolean renderRequested) {
        TraceController.traceBegin("PreparePageFadeoutTexture onGLIdle");
        long start = System.currentTimeMillis();
        if (this.mCancelled) {
            this.mTexture = null;
        } else {
            try {
                canvas.beginRenderTarget(this.mTexture);
                canvas.setCustomFlag(2);
                this.mRootPane.render(canvas);
                canvas.setCustomFlag(1);
                canvas.endRenderTarget();
            } catch (RuntimeException e) {
                this.mTexture = null;
            }
        }
        this.mResultReady.open();
        GalleryLog.d("PreparePageFadeoutTexture", "PreparePageFadeoutTexture onGLIdle " + (System.currentTimeMillis() - start) + "ms");
        TraceController.traceEnd();
        return false;
    }

    public static void prepareFadeOutTexture(GLHost glHost, GLView rootPane) {
        PreparePageFadeoutTexture task = new PreparePageFadeoutTexture(rootPane);
        if (!task.isCancelled()) {
            TraceController.traceBegin("PreparePageFadeoutTexture prepareFadeOutTexture");
            GLRoot root = glHost.getGLRoot();
            RawTexture rawTexture = null;
            root.unlockRenderThread();
            try {
                root.addOnGLIdleListener(task);
                rawTexture = task.get();
                if (rawTexture != null) {
                    glHost.getTransitionStore().put("fade_texture", rawTexture);
                }
                glHost.getTransitionStore().put("start_pos", task.mAnimRect);
            } finally {
                root.lockRenderThread();
                TraceController.traceEnd();
            }
        }
    }
}
