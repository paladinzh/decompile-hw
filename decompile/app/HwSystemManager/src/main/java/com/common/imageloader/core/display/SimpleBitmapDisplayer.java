package com.common.imageloader.core.display;

import android.graphics.Bitmap;
import com.common.imageloader.core.assist.LoadedFrom;
import com.common.imageloader.core.imageaware.ImageAware;

public final class SimpleBitmapDisplayer implements BitmapDisplayer {
    public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
        imageAware.setImageBitmap(bitmap);
    }
}
