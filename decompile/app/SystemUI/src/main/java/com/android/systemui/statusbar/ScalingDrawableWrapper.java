package com.android.systemui.statusbar;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;

class ScalingDrawableWrapper extends DrawableWrapper {
    private float mScaleFactor;

    public ScalingDrawableWrapper(Drawable drawable, float scaleFactor) {
        super(drawable);
        this.mScaleFactor = scaleFactor;
    }

    public int getIntrinsicWidth() {
        return (int) (((float) super.getIntrinsicWidth()) * this.mScaleFactor);
    }

    public int getIntrinsicHeight() {
        return (int) (((float) super.getIntrinsicHeight()) * this.mScaleFactor);
    }
}
