package com.android.hwtransition.interpolator;

import android.view.animation.Interpolator;

public class CubicBezierInterpolator implements Interpolator {
    float mControlPoint1x;
    float mControlPoint1y;
    float mControlPoint2x;
    float mControlPoint2y;

    public float getInterpolation(float input) {
        return getCubicBezierY(((float) BinarySearch(input)) * 2.5E-4f);
    }

    private float getCubicBezierX(float t) {
        return ((((((1.0f - t) * 3.0f) * (1.0f - t)) * t) * this.mControlPoint1x) + (((((1.0f - t) * 3.0f) * t) * t) * this.mControlPoint2x)) + ((t * t) * t);
    }

    protected float getCubicBezierY(float t) {
        return ((((((1.0f - t) * 3.0f) * (1.0f - t)) * t) * this.mControlPoint1y) + (((((1.0f - t) * 3.0f) * t) * t) * this.mControlPoint2y)) + ((t * t) * t);
    }

    long BinarySearch(float key) {
        long low = 0;
        long high = 4000;
        while (low <= high) {
            long middle = (low + high) >>> 1;
            float approximation = getCubicBezierX(((float) middle) * 2.5E-4f);
            if (approximation < key) {
                low = middle + 1;
            } else if (approximation <= key) {
                return middle;
            } else {
                high = middle - 1;
            }
        }
        return low;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("CubicBezierInterpolator");
        sb.append("  mControlPoint1x = " + this.mControlPoint1x);
        sb.append(", mControlPoint1y = " + this.mControlPoint1y);
        sb.append(", mControlPoint2x = " + this.mControlPoint2x);
        sb.append(", mControlPoint2y = " + this.mControlPoint2y);
        return sb.toString();
    }
}
