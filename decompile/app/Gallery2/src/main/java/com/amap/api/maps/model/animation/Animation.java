package com.amap.api.maps.model.animation;

import android.view.animation.Interpolator;
import com.amap.api.mapcore.util.di;

public abstract class Animation {
    public di glAnimation;

    public interface AnimationListener {
        void onAnimationEnd();

        void onAnimationStart();
    }

    public abstract void setDuration(long j);

    public abstract void setInterpolator(Interpolator interpolator);

    public Animation() {
        this.glAnimation = null;
        this.glAnimation = new di();
    }

    public void setAnimationListener(AnimationListener animationListener) {
        this.glAnimation.a(animationListener);
    }
}
