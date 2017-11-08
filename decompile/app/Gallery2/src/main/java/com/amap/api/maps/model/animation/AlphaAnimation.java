package com.amap.api.maps.model.animation;

import android.view.animation.Interpolator;
import com.amap.api.mapcore.util.dh;

public class AlphaAnimation extends Animation {
    public AlphaAnimation(float f, float f2) {
        this.glAnimation = new dh(f, f2);
    }

    public void setDuration(long j) {
        this.glAnimation.a(j);
    }

    public void setInterpolator(Interpolator interpolator) {
        this.glAnimation.a(interpolator);
    }
}
