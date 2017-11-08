package com.android.gallery3d.ui;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import com.huawei.watermark.manager.parse.WMElement;

public class CaptureAnimation {
    private static final Interpolator sSlideInterpolator = new AccelerateDecelerateInterpolator();
    private static final Interpolator sZoomInInterpolator = new AccelerateInterpolator();
    private static final Interpolator sZoomOutInterpolator = new DecelerateInterpolator();

    public static float calculateSlide(float fraction) {
        return sSlideInterpolator.getInterpolation(fraction);
    }

    public static float calculateScale(float fraction) {
        if (fraction <= 0.5f) {
            return WMElement.CAMERASIZEVALUE1B1 - (sZoomOutInterpolator.getInterpolation(fraction * 2.0f) * 0.2f);
        }
        return 0.8f + (sZoomInInterpolator.getInterpolation((fraction - 0.5f) * 2.0f) * 0.2f);
    }
}
