package com.android.gallery3d.ui;

import android.annotation.TargetApi;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.android.gallery3d.common.ApiHelper;
import com.huawei.watermark.manager.parse.WMElement;

@TargetApi(11)
public abstract class SurfaceTextureScreenNail implements ScreenNail, OnFrameAvailableListener {
    protected ExtTexture mExtTexture;
    private boolean mHasTexture = false;
    private int mHeight;
    private SurfaceTexture mSurfaceTexture;
    private float[] mTransform = new float[16];
    private int mWidth;

    public abstract void noDraw();

    public abstract void onFrameAvailable(SurfaceTexture surfaceTexture);

    public abstract void recycle();

    public void acquireSurfaceTexture() {
        this.mExtTexture = new ExtTexture(36197);
        this.mExtTexture.setSize(this.mWidth, this.mHeight);
        this.mSurfaceTexture = new SurfaceTexture(this.mExtTexture.getId());
        setDefaultBufferSize(this.mSurfaceTexture, this.mWidth, this.mHeight);
        this.mSurfaceTexture.setOnFrameAvailableListener(this);
        synchronized (this) {
            this.mHasTexture = true;
        }
    }

    @TargetApi(15)
    private static void setDefaultBufferSize(SurfaceTexture st, int width, int height) {
        if (ApiHelper.HAS_SET_DEFALT_BUFFER_SIZE) {
            st.setDefaultBufferSize(width, height);
        }
    }

    @TargetApi(14)
    private static void releaseSurfaceTexture(SurfaceTexture st) {
        st.setOnFrameAvailableListener(null);
        if (ApiHelper.HAS_RELEASE_SURFACE_TEXTURE) {
            st.release();
        }
    }

    public SurfaceTexture getSurfaceTexture() {
        return this.mSurfaceTexture;
    }

    public void releaseSurfaceTexture() {
        synchronized (this) {
            this.mHasTexture = false;
        }
        this.mExtTexture.recycle();
        this.mExtTexture = null;
        releaseSurfaceTexture(this.mSurfaceTexture);
        this.mSurfaceTexture = null;
    }

    public void setSize(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }

    public void resizeTexture() {
        if (this.mExtTexture != null) {
            this.mExtTexture.setSize(this.mWidth, this.mHeight);
            setDefaultBufferSize(this.mSurfaceTexture, this.mWidth, this.mHeight);
        }
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public void draw(GLCanvas canvas, int x, int y, int width, int height) {
        draw(canvas, x, y, width, height, canvas.getAlpha());
    }

    public void draw(GLCanvas canvas, int x, int y, int width, int height, float alpha) {
        synchronized (this) {
            if (this.mHasTexture) {
                this.mSurfaceTexture.updateTexImage();
                this.mSurfaceTexture.getTransformMatrix(this.mTransform);
                canvas.save(2);
                int cx = x + (width / 2);
                int cy = y + (height / 2);
                canvas.translate((float) cx, (float) cy);
                canvas.scale(WMElement.CAMERASIZEVALUE1B1, GroundOverlayOptions.NO_DIMENSION, WMElement.CAMERASIZEVALUE1B1);
                canvas.translate((float) (-cx), (float) (-cy));
                updateTransformMatrix(this.mTransform);
                canvas.setAlpha(alpha);
                canvas.drawTexture(this.mExtTexture, this.mTransform, x, y, width, height);
                canvas.restore();
                return;
            }
        }
    }

    public void draw(GLCanvas canvas, RectF source, RectF dest) {
        throw new UnsupportedOperationException();
    }

    protected void updateTransformMatrix(float[] matrix) {
    }
}
