package com.android.gallery3d.ui;

import com.android.gallery3d.util.GalleryLog;
import javax.microedition.khronos.opengles.GL11;

public class RawTexture extends BasicTexture {
    private float[] mCropRect = new float[4];
    private final boolean mOpaque;
    private int[] mTextureId = new int[1];

    public RawTexture(int width, int height, boolean opaque) {
        this.mOpaque = opaque;
        setSize(width, height);
    }

    public boolean isOpaque() {
        return this.mOpaque;
    }

    protected void prepare(GLCanvas canvas) {
        GL11 gl = canvas.getGLInstance();
        this.mCropRect[0] = 0.0f;
        this.mCropRect[1] = (float) this.mHeight;
        this.mCropRect[2] = (float) this.mWidth;
        this.mCropRect[3] = (float) (-this.mHeight);
        GLId.glGenTextures(1, this.mTextureId, 0);
        gl.glBindTexture(3553, this.mTextureId[0]);
        gl.glTexParameterfv(3553, 35741, this.mCropRect, 0);
        gl.glTexParameteri(3553, 10242, 33071);
        gl.glTexParameteri(3553, 10243, 33071);
        gl.glTexParameterf(3553, 10241, 9729.0f);
        gl.glTexParameterf(3553, 10240, 9729.0f);
        gl.glTexImage2D(3553, 0, 6408, getTextureWidth(), getTextureHeight(), 0, 6408, 5121, null);
        this.mId = this.mTextureId[0];
        this.mState = 1;
        setAssociatedCanvas(canvas);
    }

    protected boolean onBind(GLCanvas canvas) {
        if (isLoaded()) {
            return true;
        }
        GalleryLog.w("RawTexture", "lost the content due to context change");
        return false;
    }

    public void yield() {
    }

    protected int getTarget() {
        return 3553;
    }
}
