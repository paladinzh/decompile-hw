package com.android.gallery3d.ui;

import android.annotation.SuppressLint;
import com.android.gallery3d.util.Constant;
import com.android.gallery3d.util.GalleryLog;
import javax.microedition.khronos.opengles.GL10;

public class GLRootViewDebugger {
    private static final boolean DEBUG_FPS = Constant.DBG;
    private static final boolean DEBUG_FPS_MORE = Constant.VDBG;
    private int mFrameCount = 0;
    private long mFrameCountingStart = 0;
    private long mFrameTotalCount = 0;
    private long mFrameTotalDuration = 0;

    public void onRequestRender() {
    }

    public void onLayoutContentPane(int width, int height, int compensation, GLView contentView) {
        GalleryLog.i("GLRootView", "layout content pane " + width + "x" + height + " (compensation " + compensation + ")");
    }

    public void onSurfaceChanged(GL10 gl1, int width, int height) {
        GalleryLog.i("GLRootView", "onSurfaceChanged: " + width + "x" + height + ", gl10: " + gl1.toString());
    }

    public long onDrawFrameStart(GL10 gl) {
        return 0;
    }

    @SuppressLint({"DefaultLocale"})
    public void outputFps() {
        if (DEBUG_FPS) {
            long now = System.nanoTime();
            if (this.mFrameCountingStart == 0) {
                this.mFrameCountingStart = now;
            } else if (now - this.mFrameCountingStart >= 400000000) {
                GalleryLog.d("GLRootView", "Reset, start to count fps");
                this.mFrameCountingStart = now;
                this.mFrameCount = 0;
                this.mFrameTotalDuration = 0;
                this.mFrameTotalCount = 0;
            } else if (now - this.mFrameCountingStart > 200000000) {
                this.mFrameTotalDuration += now - this.mFrameCountingStart;
                double fps = (((double) this.mFrameCount) * 1.0E9d) / ((double) (now - this.mFrameCountingStart));
                double averageFps = (((double) this.mFrameTotalCount) * 1.0E9d) / ((double) this.mFrameTotalDuration);
                String message = String.format("Now the fps is %f, average fps is %f", new Object[]{Double.valueOf(fps), Double.valueOf(averageFps)});
                if (DEBUG_FPS_MORE) {
                    message = message + String.format("%n%d frames in the lastest %d ms. Averagely, %d frames in total %d ms", new Object[]{Integer.valueOf(this.mFrameCount), Long.valueOf((now - this.mFrameCountingStart) / 1000000), Long.valueOf(this.mFrameTotalCount), Long.valueOf(this.mFrameTotalDuration / 1000000)});
                }
                GalleryLog.d("GLRootView", message);
                this.mFrameCountingStart = System.nanoTime();
                this.mFrameCount = 0;
            }
            this.mFrameCount++;
            this.mFrameTotalCount++;
        }
    }

    public void onDrawFrameEnd(long startTime) {
    }

    @SuppressLint({"SdCardPath"})
    public void onPause() {
    }
}
