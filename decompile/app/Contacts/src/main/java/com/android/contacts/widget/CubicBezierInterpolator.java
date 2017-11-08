package com.android.contacts.widget;

import android.view.animation.Interpolator;

public class CubicBezierInterpolator implements Interpolator {
    public static final Interpolator TYPE_A = new CubicBezierInterpolator(0.51f, 0.35f, 0.15f, 1.0f);
    public static final Interpolator TYPE_B = new CubicBezierInterpolator(0.53f, 0.51f, 0.69f, 0.99f);
    public static final Interpolator TYPE_C = new CubicBezierInterpolator(0.5f, 0.2f, 0.55f, 0.9f);
    float mControlPoint1x = 0.0f;
    float mControlPoint1y = 0.0f;
    float mControlPoint2x = 0.0f;
    float mControlPoint2y = 0.0f;

    public CubicBezierInterpolator(float cx1, float cy1, float cx2, float cy2) {
        this.mControlPoint1x = cx1;
        this.mControlPoint1y = cy1;
        this.mControlPoint2x = cx2;
        this.mControlPoint2y = cy2;
    }

    public float getInterpolation(float input) {
        return getCubicBezierY(((float) binarySearch(input)) * 2.5E-4f);
    }

    private float getCubicBezierX(float t) {
        return ((((((1.0f - t) * 3.0f) * (1.0f - t)) * t) * this.mControlPoint1x) + (((((1.0f - t) * 3.0f) * t) * t) * this.mControlPoint2x)) + ((t * t) * t);
    }

    protected float getCubicBezierY(float t) {
        return ((((((1.0f - t) * 3.0f) * (1.0f - t)) * t) * this.mControlPoint1y) + (((((1.0f - t) * 3.0f) * t) * t) * this.mControlPoint2y)) + ((t * t) * t);
    }

    long binarySearch(float key) {
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
