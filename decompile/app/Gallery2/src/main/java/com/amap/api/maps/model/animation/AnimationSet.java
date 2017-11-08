package com.amap.api.maps.model.animation;

import android.view.animation.Interpolator;
import com.amap.api.mapcore.util.dj;

public class AnimationSet extends Animation {
    public AnimationSet(boolean z) {
        this.glAnimation = new dj(z);
    }

    public void setDuration(long j) {
        this.glAnimation.a(j);
    }

    public void setInterpolator(Interpolator interpolator) {
        this.glAnimation.a(interpolator);
    }

    public void addAnimation(Animation animation) {
        ((dj) this.glAnimation).a(animation);
    }

    public void cleanAnimation() {
        ((dj) this.glAnimation).o();
    }
}
