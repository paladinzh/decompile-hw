package com.huawei.gallery.ui;

import android.graphics.Bitmap;
import android.graphics.Rect;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.TileImageView.Model;

public abstract class BaseCropView extends GLView {
    protected int mImageHeight = -1;
    protected int mImageRotation;
    protected int mImageWidth = -1;

    public abstract void detectFaces(Bitmap bitmap);

    public abstract Rect getCropRectangle();

    public abstract void pause();

    public abstract void resume();

    public abstract void setGLRoot(GLRoot gLRoot);

    public int getImageWidth() {
        return this.mImageWidth;
    }

    public int getImageHeight() {
        return this.mImageHeight;
    }

    public void setAspectRatio(float ratio) {
    }

    public void setSpotlightRatio(float ratioX, float ratioY) {
    }

    public void setDataModel(Model dataModel, int rotation) {
        if (((rotation / 90) & 1) != 0) {
            this.mImageWidth = dataModel.getImageHeight();
            this.mImageHeight = dataModel.getImageWidth();
        } else {
            this.mImageWidth = dataModel.getImageWidth();
            this.mImageHeight = dataModel.getImageHeight();
        }
        this.mImageRotation = rotation;
        onDataModelChanged(dataModel);
    }

    protected void onDataModelChanged(Model dataModel) {
    }

    public void initializeHighlightRectangle() {
    }

    public void setWallpaperSize(int w, int h) {
    }

    public void setScrollableWallper(boolean scrollable) {
    }
}
