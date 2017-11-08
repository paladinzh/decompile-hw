package com.autonavi.amap.mapcore.interfaces;

import com.amap.api.mapcore.util.di;
import com.amap.api.maps.model.animation.Animation.AnimationListener;

public interface IAnimation {
    void setAnimation(di diVar);

    void setAnimationListener(AnimationListener animationListener);

    boolean startAnimation();
}
