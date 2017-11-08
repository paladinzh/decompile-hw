package com.android.deskclock.stopwatch;

import android.view.animation.Interpolator;

public class SimpleBoundInterpolator implements Interpolator {
    private float mCirles;

    public SimpleBoundInterpolator(float mCirles) {
        this.mCirles = mCirles;
    }

    public float getInterpolation(float input) {
        return (float) Math.sin(((((double) (this.mCirles * 2.0f)) * 3.141592653589793d) * ((double) input)) + 1.5707963267948966d);
    }
}
