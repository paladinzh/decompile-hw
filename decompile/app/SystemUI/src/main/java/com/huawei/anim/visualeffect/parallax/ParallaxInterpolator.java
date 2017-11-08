package com.huawei.anim.visualeffect.parallax;

import android.view.animation.Interpolator;

public class ParallaxInterpolator implements Interpolator {
    private final double A;
    private final double A1;
    private final double A2;
    private final double B;
    private final double B1;
    private final double B2;
    private final double mPOIA;
    private final double mPOIB;
    private final double mPercent;

    public ParallaxInterpolator(double poi, double prec) {
        if (poi > 1.5707963267948966d) {
            throw new IllegalArgumentException("point of inflexion cannot larger than half PI");
        }
        this.mPOIA = poi;
        this.mPOIB = 3.141592653589793d - poi;
        this.mPercent = prec;
        this.B = 0.5d;
        this.A = ((1.0d - this.mPercent) - this.B) / Math.sin(this.mPOIA);
        double sin = this.mPercent / (Math.sin(1.5707963267948966d) - Math.sin(this.mPOIA));
        this.A2 = sin;
        this.A1 = sin;
        this.B1 = this.A1;
        this.B2 = 1.0d - this.B1;
    }

    public float getInterpolation(float input) {
        double r = clamp((double) input, -3.141592653589793d, 3.141592653589793d);
        if ((-this.mPOIB) < r && r < (-this.mPOIA)) {
            return (float) ((this.A1 * Math.sin(r)) + this.B1);
        }
        if (this.mPOIA >= r || r >= this.mPOIB) {
            return (float) ((this.A * Math.sin(r)) + this.B);
        }
        return (float) ((this.A2 * Math.sin(r)) + this.B2);
    }

    private static final double clamp(double x, double min, double max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }
}
