package com.huawei.gallery.refocus.wideaperture.photo3dview.app;

import android.content.Context;
import com.huawei.gallery.refocus.app.AbsRefocusController;
import com.huawei.gallery.refocus.app.AbsRefocusDelegate;
import com.huawei.gallery.refocus.wideaperture.app.WideAperturePhotoImpl;
import com.huawei.gallery.refocus.wideaperture.app.WideAperturePhotoImpl.WideAperturePhotoListener;

public class WideAperturePhoto3DViewController extends AbsRefocusController implements WideAperturePhotoListener {
    private WideAperturePhotoImpl mWideAperturePhoto = new WideAperturePhotoImpl(this.mDelegate.getFilePath(), this.mPhotoWidth, this.mPhotoHeight);

    public WideAperturePhoto3DViewController(Context context, AbsRefocusDelegate delegate) {
        super(context, delegate);
        this.mWideAperturePhoto.setWideAperturePhotoListener(this);
    }

    public boolean prepare() {
        return this.mWideAperturePhoto.prepare();
    }

    public void setViewMode(int viewMode) {
        this.mWideAperturePhoto.setViewMode(viewMode);
    }

    public void init3DViewDisplayParams(float[] angle, int viewOrientation, int viewWidth, int viewHeight) {
        this.mWideAperturePhoto.init3DViewDisplayParams(angle, viewOrientation, viewWidth, viewHeight);
    }

    public void create3DView() {
        this.mWideAperturePhoto.create3DView();
    }

    public void destroy3DView() {
        this.mWideAperturePhoto.destroy3DView();
    }

    public void set3DViewProperty(int propertyType, int viewOrientation, int viewWidth, int viewHeight) {
        this.mWideAperturePhoto.set3DViewProperty(propertyType, viewOrientation, viewWidth, viewHeight);
    }

    public void cleanUp() {
        if (this.mWideAperturePhoto != null) {
            this.mWideAperturePhoto.cleanupResource();
        }
    }

    public void onPrepareComplete() {
        this.mDelegate.preparePhotoComplete();
        this.mPrepareComplete = true;
    }

    public boolean isRefocusPhoto() {
        return this.mWideAperturePhoto.isRefocusPhoto();
    }

    public void invalidate3DView(float[] angle) {
        this.mWideAperturePhoto.invalidate3DView(angle);
    }

    public void onGotFocusPoint() {
    }

    public void onSaveAsComplete(int saveState, String filePath) {
    }

    public void onSaveFileComplete(int saveState) {
    }

    public void onRefocusComplete() {
    }

    public void finishRefocus() {
    }
}
