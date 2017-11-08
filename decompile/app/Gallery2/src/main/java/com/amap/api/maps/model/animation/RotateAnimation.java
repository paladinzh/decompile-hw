package com.amap.api.maps.model.animation;

import android.view.animation.Interpolator;
import com.amap.api.mapcore.util.dl;

public class RotateAnimation extends Animation {
    public RotateAnimation(float f, float f2, float f3, float f4, float f5) {
        this.glAnimation = new dl(f, f2, f3, f4, f5);
    }

    public RotateAnimation(float f, float f2) {
        this.glAnimation = new dl(f, f2, 0.0f, 0.0f, 0.0f);
    }

    public void setDuration(long j) {
        this.glAnimation.a(j);
    }

    public void setInterpolator(Interpolator interpolator) {
        this.glAnimation.a(interpolator);
    }
}
