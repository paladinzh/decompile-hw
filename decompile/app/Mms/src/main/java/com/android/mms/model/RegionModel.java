package com.android.mms.model;

public class RegionModel extends Model {
    private String mBackgroundColor;
    private String mFit;
    private int mHeight;
    private int mLeft;
    private final String mRegionId;
    private int mTop;
    private int mWidth;

    public RegionModel(String regionId, int left, int top, int width, int height) {
        this(regionId, "meet", left, top, width, height);
    }

    public RegionModel(String regionId, String fit, int left, int top, int width, int height) {
        this(regionId, fit, left, top, width, height, null);
    }

    public RegionModel(String regionId, String fit, int left, int top, int width, int height, String bgColor) {
        this.mRegionId = regionId;
        this.mFit = fit;
        this.mLeft = left;
        this.mTop = top;
        this.mWidth = width;
        this.mHeight = height;
        this.mBackgroundColor = bgColor;
    }

    public String getRegionId() {
        return this.mRegionId;
    }

    public String getFit() {
        return this.mFit;
    }

    public int getLeft() {
        return this.mLeft;
    }

    public void setLeft(int left) {
        this.mLeft = left;
        notifyModelChanged(true);
    }

    public int getTop() {
        return this.mTop;
    }

    public void setTop(int top) {
        this.mTop = top;
        notifyModelChanged(true);
    }

    public int getWidth() {
        return this.mWidth;
    }

    public void setWidth(int width) {
        this.mWidth = width;
        notifyModelChanged(true);
    }

    public int getHeight() {
        return this.mHeight;
    }

    public void setHeight(int height) {
        this.mHeight = height;
        notifyModelChanged(true);
    }

    public String getBackgroundColor() {
        return this.mBackgroundColor;
    }

    public String toString() {
        return "[ReginonId:" + this.mRegionId + " Left:" + this.mLeft + " Top:" + this.mTop + " Width:" + this.mWidth + " Height:" + this.mHeight + " BackgroundColor:" + this.mBackgroundColor + "]";
    }
}
