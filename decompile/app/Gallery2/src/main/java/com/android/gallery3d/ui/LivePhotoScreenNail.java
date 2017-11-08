package com.android.gallery3d.ui;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import com.huawei.gallery.util.MyPrinter;

public class LivePhotoScreenNail extends SurfaceTextureScreenNail {
    private MyPrinter LOG = new MyPrinter("LivePhotoScreenNail");
    private OnLivePhotoChangedListener mListener;
    private Surface mSurface;

    public interface OnLivePhotoChangedListener {
        void onFrameAvailable();
    }

    public void setListener(OnLivePhotoChangedListener listener) {
        this.mListener = listener;
    }

    public void noDraw() {
    }

    public void recycle() {
    }

    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if (getSurfaceTexture() != surfaceTexture) {
            this.LOG.w("invalid texture arrived !!! ");
            return;
        }
        if (this.mListener != null) {
            this.mListener.onFrameAvailable();
        }
    }

    public void acquireSurfaceTexture() {
        super.acquireSurfaceTexture();
        this.mSurface = new Surface(getSurfaceTexture());
    }

    public void releaseSurfaceTexture() {
        if (this.mSurface != null) {
            this.LOG.d("release surface and texture");
            this.mSurface.release();
            this.mSurface = null;
            super.releaseSurfaceTexture();
        }
    }

    public Surface getSurface() {
        return this.mSurface;
    }
}
