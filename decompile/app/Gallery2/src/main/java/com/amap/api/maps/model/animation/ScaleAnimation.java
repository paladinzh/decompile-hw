package com.amap.api.maps.model.animation;

import android.view.animation.Interpolator;
import com.amap.api.mapcore.util.dm;

public class ScaleAnimation extends Animation {
    public ScaleAnimation(float f, float f2, float f3, float f4) {
        this.glAnimation = new dm(f, f2, f3, f4);
    }

    public void setDuration(long j) {
        this.glAnimation.a(j);
    }

    public void setInterpolator(Interpolator interpolator) {
        this.glAnimation.a(interpolator);
    }
}
