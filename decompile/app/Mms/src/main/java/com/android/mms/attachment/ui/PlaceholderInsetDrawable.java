package com.android.mms.attachment.ui;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;

public class PlaceholderInsetDrawable extends InsetDrawable {
    private final int mSourceHeight;
    private final int mSourceWidth;

    public static PlaceholderInsetDrawable fromDrawable(Drawable drawable, int sourceWidth, int sourceHeight) {
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        int insetHorizontal = (drawableWidth < 0 || drawableWidth > sourceWidth) ? 0 : (sourceWidth - drawableWidth) / 2;
        int insetVertical = (drawableHeight < 0 || drawableHeight > sourceHeight) ? 0 : (sourceHeight - drawableHeight) / 2;
        return new PlaceholderInsetDrawable(drawable, insetHorizontal, insetVertical, insetHorizontal, insetVertical, sourceWidth, sourceHeight);
    }

    private PlaceholderInsetDrawable(Drawable drawable, int insetLeft, int insetTop, int insetRight, int insetBottom, int sourceWidth, int sourceHeight) {
        super(drawable, insetLeft, insetTop, insetRight, insetBottom);
        this.mSourceWidth = sourceWidth;
        this.mSourceHeight = sourceHeight;
    }

    public int getIntrinsicWidth() {
        return this.mSourceWidth;
    }

    public int getIntrinsicHeight() {
        return this.mSourceHeight;
    }
}
