package com.huawei.gallery.anim;

import android.graphics.Rect;
import com.android.gallery3d.anim.BaseTransition;
import com.android.gallery3d.common.Utils;
import com.huawei.watermark.manager.parse.WMElement;

public class ClickThumbUpTransitionSmall extends BaseTransition {
    public boolean transform(float scrollProgress, Rect targetRect) {
        Rect target = this.mRectOld;
        this.mTransformationInfo.mScaleX = (1.4000001f * scrollProgress) + WMElement.CAMERASIZEVALUE1B1;
        this.mTransformationInfo.mScaleY = this.mTransformationInfo.mScaleX;
        this.mTransformationInfo.mTranslationX = ((WMElement.CAMERASIZEVALUE1B1 - this.mTransformationInfo.mScaleX) / 2.0f) * ((float) target.width());
        this.mTransformationInfo.mTranslationY = ((WMElement.CAMERASIZEVALUE1B1 - this.mTransformationInfo.mScaleY) / 2.0f) * ((float) target.height());
        this.mTransformationInfo.mAlpha = WMElement.CAMERASIZEVALUE1B1 - Utils.clamp(scrollProgress, 0.0f, (float) WMElement.CAMERASIZEVALUE1B1);
        this.mTransformationInfo.mAlphaDirty = true;
        this.mTransformationInfo.mMatrixDirty = true;
        return true;
    }
}
