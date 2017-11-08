package com.common.imageloader.core.display;

import android.graphics.Bitmap;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import com.common.imageloader.core.assist.LoadedFrom;
import com.common.imageloader.core.imageaware.ImageAware;
import com.huawei.systemmanager.comm.misc.Utility;

public class FadeInBitmapDisplayer implements BitmapDisplayer {
    private final boolean animateFromDisk;
    private final boolean animateFromMemory;
    private final boolean animateFromNetwork;
    private final int durationMillis;

    public FadeInBitmapDisplayer(int durationMillis) {
        this(durationMillis, true, true, true);
    }

    public FadeInBitmapDisplayer(int durationMillis, boolean animateFromNetwork, boolean animateFromDisk, boolean animateFromMemory) {
        this.durationMillis = durationMillis;
        this.animateFromNetwork = animateFromNetwork;
        this.animateFromDisk = animateFromDisk;
        this.animateFromMemory = animateFromMemory;
    }

    public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
        imageAware.setImageBitmap(bitmap);
        if (!((this.animateFromNetwork && loadedFrom == LoadedFrom.NETWORK) || (this.animateFromDisk && loadedFrom == LoadedFrom.DISC_CACHE))) {
            if (!this.animateFromMemory || loadedFrom != LoadedFrom.MEMORY_CACHE) {
                return;
            }
        }
        animate(imageAware.getWrappedView(), this.durationMillis);
    }

    public static void animate(View imageView, int durationMillis) {
        if (imageView != null) {
            AlphaAnimation fadeImage = new AlphaAnimation(0.0f, Utility.ALPHA_MAX);
            fadeImage.setDuration((long) durationMillis);
            fadeImage.setInterpolator(new DecelerateInterpolator());
            imageView.startAnimation(fadeImage);
        }
    }
}
