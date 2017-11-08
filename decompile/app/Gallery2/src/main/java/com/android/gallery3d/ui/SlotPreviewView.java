package com.android.gallery3d.ui;

import com.android.gallery3d.anim.FloatAnimation;
import com.android.gallery3d.app.GalleryContext;

public class SlotPreviewView extends GLView {
    private int mPhotoHeight;
    private int mPhotoWidth;
    private SlotPreviewRender mRender;
    private FloatAnimation mTransitionAnimation = null;

    public interface SlotPreviewRender {
        int renderSlotPreview(GLCanvas gLCanvas, int i, int i2);
    }

    public SlotPreviewView(GalleryContext activity) {
        setVisibility(1);
    }

    public void setSlotRender(SlotPreviewRender slotRender) {
        this.mRender = slotRender;
    }

    public void setAnimation(FloatAnimation anim) {
        this.mTransitionAnimation = anim;
    }

    protected void render(GLCanvas canvas) {
        boolean requestRender = false;
        super.render(canvas);
        if (this.mRender != null) {
            if (this.mTransitionAnimation != null) {
                requestRender = this.mTransitionAnimation.calculate(AnimationTime.get());
            }
            this.mRender.renderSlotPreview(canvas, this.mPhotoWidth, this.mPhotoHeight);
            if (requestRender) {
                invalidate();
            }
        }
    }

    protected void onLayout(boolean changeSize, int left, int top, int right, int bottom) {
        super.onLayout(changeSize, left, top, right, bottom);
        this.mPhotoWidth = right - left;
        this.mPhotoHeight = bottom - top;
    }
}
