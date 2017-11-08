package com.android.gallery3d.ui;

import com.huawei.watermark.manager.parse.WMElement;

public class FlingScroller {
    private double mCosAngle;
    private double mCurrV;
    private int mCurrX;
    private int mCurrY;
    private int mDistance;
    private int mDuration;
    private int mFinalX;
    private int mFinalY;
    private int mMaxX;
    private int mMaxY;
    private int mMinX;
    private int mMinY;
    private double mSinAngle;
    private int mStartX;
    private int mStartY;

    public int getFinalX() {
        return this.mFinalX;
    }

    public int getFinalY() {
        return this.mFinalY;
    }

    public int getDuration() {
        return this.mDuration;
    }

    public int getCurrX() {
        return this.mCurrX;
    }

    public int getCurrY() {
        return this.mCurrY;
    }

    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
        this.mStartX = startX;
        this.mStartY = startY;
        this.mMinX = minX;
        this.mMinY = minY;
        this.mMaxX = maxX;
        this.mMaxY = maxY;
        double velocity = Math.hypot((double) velocityX, (double) velocityY);
        this.mSinAngle = ((double) velocityY) / velocity;
        this.mCosAngle = ((double) velocityX) / velocity;
        this.mDuration = (int) Math.round(Math.pow(Math.abs(velocity), 0.3333333333333333d) * 50.0d);
        this.mDistance = (int) Math.round(((((double) this.mDuration) * velocity) / 4.0d) / 1000.0d);
        this.mFinalX = getX(WMElement.CAMERASIZEVALUE1B1);
        this.mFinalY = getY(WMElement.CAMERASIZEVALUE1B1);
    }

    public void computeScrollOffset(float progress) {
        progress = Math.min(progress, WMElement.CAMERASIZEVALUE1B1);
        float f = WMElement.CAMERASIZEVALUE1B1 - ((float) Math.pow((double) (WMElement.CAMERASIZEVALUE1B1 - progress), 4.0d));
        this.mCurrX = getX(f);
        this.mCurrY = getY(f);
        this.mCurrV = getV(progress);
    }

    private int getX(float f) {
        int r = (int) Math.round(((double) this.mStartX) + (((double) (((float) this.mDistance) * f)) * this.mCosAngle));
        if (this.mCosAngle > 0.0d && this.mStartX <= this.mMaxX) {
            return Math.min(r, this.mMaxX);
        }
        if (this.mCosAngle >= 0.0d || this.mStartX < this.mMinX) {
            return r;
        }
        return Math.max(r, this.mMinX);
    }

    private int getY(float f) {
        int r = (int) Math.round(((double) this.mStartY) + (((double) (((float) this.mDistance) * f)) * this.mSinAngle));
        if (this.mSinAngle > 0.0d && this.mStartY <= this.mMaxY) {
            return Math.min(r, this.mMaxY);
        }
        if (this.mSinAngle >= 0.0d || this.mStartY < this.mMinY) {
            return r;
        }
        return Math.max(r, this.mMinY);
    }

    private double getV(float progress) {
        return (((double) ((this.mDistance * 4) * 1000)) * Math.pow((double) (WMElement.CAMERASIZEVALUE1B1 - progress), 3.0d)) / ((double) this.mDuration);
    }
}
