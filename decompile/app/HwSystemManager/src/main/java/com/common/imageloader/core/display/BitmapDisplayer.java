package com.common.imageloader.core.display;

import android.graphics.Bitmap;
import com.common.imageloader.core.assist.LoadedFrom;
import com.common.imageloader.core.imageaware.ImageAware;

public interface BitmapDisplayer {
    void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom);
}
