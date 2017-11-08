package com.android.gallery3d.ui;

import android.graphics.Rect;
import android.opengl.Matrix;
import com.huawei.watermark.manager.parse.WMElement;

public class Paper {
    private EdgeAnimation mAnimationLeft = new EdgeAnimation();
    private EdgeAnimation mAnimationRight = new EdgeAnimation();
    private int mHeight;
    private boolean mIsWide = true;
    private float[] mMatrix = new float[16];
    private int mWidth;

    public Paper(boolean bWide) {
        this.mIsWide = bWide;
    }

    public void overScroll(float distance) {
        if (this.mIsWide) {
            distance /= (float) this.mWidth;
        } else {
            distance /= (float) this.mHeight;
        }
        if (distance < 0.0f) {
            this.mAnimationLeft.onPull(-distance);
        } else {
            this.mAnimationRight.onPull(distance);
        }
    }

    public void edgeReached(float velocity) {
        if (this.mIsWide) {
            velocity /= (float) this.mWidth;
        } else {
            velocity /= (float) this.mHeight;
        }
        if (velocity < 0.0f) {
            this.mAnimationRight.onAbsorb(-velocity);
        } else {
            this.mAnimationLeft.onAbsorb(velocity);
        }
    }

    public void onRelease() {
        this.mAnimationLeft.onRelease();
        this.mAnimationRight.onRelease();
    }

    public boolean advanceAnimation() {
        return this.mAnimationLeft.update() | this.mAnimationRight.update();
    }

    public void setSize(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }

    public float[] getTransform(Rect rect, float scroll) {
        float x = (((float) (this.mIsWide ? rect.centerX() : rect.centerY())) - scroll) + ((float) ((this.mIsWide ? this.mWidth : this.mHeight) / 4));
        int range = ((this.mIsWide ? this.mWidth : this.mHeight) * 3) / 2;
        float degrees = (((WMElement.CAMERASIZEVALUE1B1 / (((float) Math.exp((double) ((-((((((float) range) - x) * this.mAnimationLeft.getValue()) - (x * this.mAnimationRight.getValue())) / ((float) range))) * 4.0f))) + WMElement.CAMERASIZEVALUE1B1)) - 0.5f) * 2.0f) * -45.0f;
        Matrix.setIdentityM(this.mMatrix, 0);
        Matrix.translateM(this.mMatrix, 0, this.mMatrix, 0, (float) rect.centerX(), (float) rect.centerY(), 0.0f);
        if (this.mIsWide) {
            Matrix.rotateM(this.mMatrix, 0, degrees, 0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f);
        } else {
            Matrix.rotateM(this.mMatrix, 0, -degrees, WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f);
        }
        Matrix.translateM(this.mMatrix, 0, this.mMatrix, 0, (float) ((-rect.width()) / 2), (float) ((-rect.height()) / 2), 0.0f);
        return this.mMatrix;
    }
}
