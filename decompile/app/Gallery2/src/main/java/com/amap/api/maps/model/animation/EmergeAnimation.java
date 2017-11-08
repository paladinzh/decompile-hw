package com.amap.api.maps.model.animation;

import android.view.animation.Interpolator;
import com.amap.api.mapcore.util.dk;
import com.amap.api.maps.model.LatLng;

public class EmergeAnimation extends Animation {
    public EmergeAnimation(LatLng latLng) {
        this.glAnimation = new dk(latLng);
    }

    public void setDuration(long j) {
        this.glAnimation.a(j);
    }

    public void setInterpolator(Interpolator interpolator) {
        this.glAnimation.a(interpolator);
    }
}
