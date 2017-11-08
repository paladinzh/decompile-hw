package com.android.gallery3d.ui;

import android.support.v4.app.FragmentTransaction;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryLog;

public abstract class BasicTexture implements Texture {
    private static ThreadLocal sInFinalizer = new ThreadLocal();
    protected GLCanvas mCanvasRef;
    private boolean mHasBorder;
    protected int mHeight;
    protected int mId;
    protected int mState;
    protected int mTextureHeight;
    protected int mTextureWidth;
    protected int mWidth;

    protected abstract int getTarget();

    protected abstract boolean onBind(GLCanvas gLCanvas);

    protected BasicTexture(GLCanvas canvas, int id, int state) {
        this.mWidth = -1;
        this.mHeight = -1;
        this.mCanvasRef = null;
        setAssociatedCanvas(canvas);
        this.mId = id;
        this.mState = state;
    }

    protected BasicTexture() {
        this(null, 0, 0);
    }

    protected void setAssociatedCanvas(GLCanvas canvas) {
        this.mCanvasRef = canvas;
        if (canvas != null) {
            canvas.putTexture(this);
        }
    }

    protected void setSize(int width, int height) {
        GalleryLog.d("BasicTexture", "setSize, width = " + width + ", height = " + height);
        this.mWidth = width;
        this.mHeight = height;
        this.mTextureWidth = Utils.nextPowerOf2(width);
        this.mTextureHeight = Utils.nextPowerOf2(height);
        if (this.mTextureWidth > FragmentTransaction.TRANSIT_ENTER_MASK || this.mTextureHeight > FragmentTransaction.TRANSIT_ENTER_MASK) {
            GalleryLog.w("BasicTexture", String.format("texture is too large: %d x %d", new Object[]{Integer.valueOf(this.mTextureWidth), Integer.valueOf(this.mTextureHeight)}));
        }
    }

    public int getId() {
        return this.mId;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public int getTextureWidth() {
        return this.mTextureWidth;
    }

    public int getTextureHeight() {
        return this.mTextureHeight;
    }

    public boolean hasBorder() {
        return this.mHasBorder;
    }

    protected void setBorder(boolean hasBorder) {
        this.mHasBorder = hasBorder;
    }

    public void draw(GLCanvas canvas, int x, int y) {
        canvas.drawTexture(this, x, y, getWidth(), getHeight());
    }

    public void draw(GLCanvas canvas, int x, int y, int w, int h) {
        canvas.drawTexture(this, x, y, w, h);
    }

    public boolean isLoaded() {
        return this.mState == 1;
    }

    public void recycle() {
        freeResource();
    }

    public void yield() {
        freeResource();
    }

    private void freeResource() {
        GLCanvas canvas = this.mCanvasRef;
        if (canvas != null && isLoaded()) {
            canvas.unloadTexture(this);
        }
        this.mState = 0;
        setAssociatedCanvas(null);
    }

    protected void finalize() {
        sInFinalizer.set(BasicTexture.class);
        recycle();
        sInFinalizer.set(null);
    }

    public static boolean inFinalizer() {
        return sInFinalizer.get() != null;
    }
}
