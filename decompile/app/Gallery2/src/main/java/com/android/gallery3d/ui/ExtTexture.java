package com.android.gallery3d.ui;

import javax.microedition.khronos.opengles.GL11;

public class ExtTexture extends BasicTexture {
    private float[] mCropRect = new float[4];
    private int mTarget;
    private int[] mTextureId = new int[1];

    public ExtTexture(int target) {
        GLId.glGenTextures(1, this.mTextureId, 0);
        this.mId = this.mTextureId[0];
        this.mTarget = target;
    }

    private void uploadToCanvas(GLCanvas canvas) {
        GL11 gl = canvas.getGLInstance();
        int width = getWidth();
        int height = getHeight();
        this.mCropRect[0] = 0.0f;
        this.mCropRect[1] = (float) height;
        this.mCropRect[2] = (float) width;
        this.mCropRect[3] = (float) (-height);
        gl.glBindTexture(this.mTarget, this.mId);
        gl.glTexParameterfv(this.mTarget, 35741, this.mCropRect, 0);
        gl.glTexParameteri(this.mTarget, 10242, 33071);
        gl.glTexParameteri(this.mTarget, 10243, 33071);
        gl.glTexParameterf(this.mTarget, 10241, 9729.0f);
        gl.glTexParameterf(this.mTarget, 10240, 9729.0f);
        setAssociatedCanvas(canvas);
        this.mState = 1;
    }

    protected boolean onBind(GLCanvas canvas) {
        if (!isLoaded()) {
            uploadToCanvas(canvas);
        }
        return true;
    }

    public int getTarget() {
        return this.mTarget;
    }

    public boolean isOpaque() {
        return true;
    }

    public void yield() {
    }
}
