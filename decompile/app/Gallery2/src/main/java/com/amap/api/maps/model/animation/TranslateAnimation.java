package com.amap.api.maps.model.animation;

import android.view.animation.Interpolator;
import com.amap.api.mapcore.util.do;
import com.amap.api.maps.model.LatLng;

public class TranslateAnimation extends Animation {
    public TranslateAnimation(LatLng latLng) {
        this.glAnimation = new do(latLng);
    }

    public void setDuration(long j) {
        this.glAnimation.a(j);
    }

    public void setInterpolator(Interpolator interpolator) {
        this.glAnimation.a(interpolator);
    }
}
