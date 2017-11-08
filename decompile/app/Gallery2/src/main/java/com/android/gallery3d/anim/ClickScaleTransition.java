package com.android.gallery3d.anim;

import android.graphics.Rect;
import com.huawei.watermark.manager.parse.WMElement;

public class ClickScaleTransition extends BaseTransition {
    public boolean transform(float scrollProgress, Rect targetRect) {
        this.mTransformationInfo.mScaleX = (-0.3f * scrollProgress) + WMElement.CAMERASIZEVALUE1B1;
        this.mTransformationInfo.mScaleY = this.mTransformationInfo.mScaleX;
        this.mTransformationInfo.mTranslationX = (((float) (targetRect.left + (targetRect.width() / 2))) * 0.3f) * scrollProgress;
        this.mTransformationInfo.mTranslationY = (((float) (targetRect.top + (targetRect.height() / 2))) * 0.3f) * scrollProgress;
        this.mTransformationInfo.mMatrixDirty = true;
        return true;
    }
}
