package com.huawei.hwtransition.interpolator;

import android.view.animation.Interpolator;

public class TriangleInterpolator implements Interpolator {
    public float getInterpolation(float input) {
        if (input <= 0.5f) {
            return 2.0f * input;
        }
        return (1.0f - input) * 2.0f;
    }
}
