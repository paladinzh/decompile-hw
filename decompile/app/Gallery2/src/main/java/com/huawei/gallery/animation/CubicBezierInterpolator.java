package com.huawei.gallery.animation;

import android.view.animation.Interpolator;
import com.autonavi.amap.mapcore.MapConfig;
import com.huawei.watermark.manager.parse.WMElement;

public class CubicBezierInterpolator implements Interpolator {
    float mControlPoint1x;
    float mControlPoint1y;
    float mControlPoint2x;
    float mControlPoint2y;

    public CubicBezierInterpolator() {
        this(0.2f, -0.05f, 0.0f, 1.05f);
    }

    public CubicBezierInterpolator(float cx1, float cy1, float cx2, float cy2) {
        this.mControlPoint1x = 0.0f;
        this.mControlPoint1y = 0.0f;
        this.mControlPoint2x = 0.0f;
        this.mControlPoint2y = 0.0f;
        this.mControlPoint1x = cx1;
        this.mControlPoint1y = cy1;
        this.mControlPoint2x = cx2;
        this.mControlPoint2y = cy2;
    }

    public float getInterpolation(float input) {
        return getCubicBezierY(((float) binarySearch(input)) * 2.5E-4f);
    }

    private float getCubicBezierX(float t) {
        return ((((((WMElement.CAMERASIZEVALUE1B1 - t) * MapConfig.MIN_ZOOM) * (WMElement.CAMERASIZEVALUE1B1 - t)) * t) * this.mControlPoint1x) + (((((WMElement.CAMERASIZEVALUE1B1 - t) * MapConfig.MIN_ZOOM) * t) * t) * this.mControlPoint2x)) + ((t * t) * t);
    }

    protected float getCubicBezierY(float t) {
        return ((((((WMElement.CAMERASIZEVALUE1B1 - t) * MapConfig.MIN_ZOOM) * (WMElement.CAMERASIZEVALUE1B1 - t)) * t) * this.mControlPoint1y) + (((((WMElement.CAMERASIZEVALUE1B1 - t) * MapConfig.MIN_ZOOM) * t) * t) * this.mControlPoint2y)) + ((t * t) * t);
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
