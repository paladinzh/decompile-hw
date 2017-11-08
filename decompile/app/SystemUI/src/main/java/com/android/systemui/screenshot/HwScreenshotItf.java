package com.android.systemui.screenshot;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.view.animation.Interpolator;

interface HwScreenshotItf {
    Bitmap getScreenshotBitmap(int i, int i2);

    void onDropInAnimationStart();

    void onDropInAnimationUpdate(float f, float f2);

    void onDropOutAnimationEnd();

    void onDropOutAnimationUpdateWithAllBarVisible(float f, float f2, Interpolator interpolator, PointF pointF);

    void onDropOutAnimationUpdateWithOneBarInvisible(float f, float f2);

    void playSoundAndSetViewLayer();

    void preAnimationStart();

    void resetMembersIfNeeded();
}
