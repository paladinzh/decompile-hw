package com.android.systemui.statusbar.phone;

import android.view.animation.Interpolator;

public class BounceInterpolator implements Interpolator {
    public float getInterpolation(float t) {
        t *= 1.1f;
        if (t < 0.36363637f) {
            return (7.5625f * t) * t;
        }
        float t2;
        if (t < 0.72727275f) {
            t2 = t - 0.54545456f;
            return ((7.5625f * t2) * t2) + 0.75f;
        } else if (t >= 0.90909094f) {
            return 1.0f;
        } else {
            t2 = t - 0.8181818f;
            return ((7.5625f * t2) * t2) + 0.9375f;
        }
    }
}
