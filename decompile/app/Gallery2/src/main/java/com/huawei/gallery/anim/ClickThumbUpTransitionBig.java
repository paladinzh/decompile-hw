package com.huawei.gallery.anim;

import android.graphics.Rect;
import com.android.gallery3d.anim.BaseTransition;
import com.huawei.watermark.manager.parse.WMElement;

public class ClickThumbUpTransitionBig extends BaseTransition {
    private float mTransitionScale = 0.3f;

    public boolean transform(float scrollProgress, Rect targetRect) {
        Rect thumbnailRect = this.mRectOld;
        if (this.mRectOld == null) {
            return false;
        }
        float tw = (float) targetRect.width();
        float th = (float) targetRect.height();
        float cxOld = (float) thumbnailRect.centerX();
        float cyOld = (float) thumbnailRect.centerY();
        if (!(tw == 0.0f || th == 0.0f)) {
            this.mTransitionScale = Math.max(((float) thumbnailRect.width()) / tw, ((float) thumbnailRect.height()) / th);
        }
        this.mTransformationInfo.mScaleX = this.mTransitionScale + ((WMElement.CAMERASIZEVALUE1B1 - this.mTransitionScale) * scrollProgress);
        this.mTransformationInfo.mScaleY = this.mTransformationInfo.mScaleX;
        this.mTransformationInfo.mTranslationX = ((((tw / 2.0f) - cxOld) * scrollProgress) + cxOld) - ((this.mTransformationInfo.mScaleX * tw) / 2.0f);
        this.mTransformationInfo.mTranslationY = ((((th / 2.0f) - cyOld) * scrollProgress) + cyOld) - ((this.mTransformationInfo.mScaleY * th) / 2.0f);
        this.mTransformationInfo.mMatrixDirty = true;
        return true;
    }
}
