package com.common.imageloader.core.display;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import com.common.imageloader.core.assist.LoadedFrom;
import com.common.imageloader.core.imageaware.ImageAware;

public class DrawableDisplayer implements BitmapDisplayer {
    private Drawable drawable;

    public DrawableDisplayer(Drawable drawable) {
        this.drawable = drawable;
    }

    public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
        imageAware.setImageDrawable(this.drawable);
    }
}
