package com.android.mms.attachment.utils;

import android.view.animation.Interpolator;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;

public class CubicBezierInterpolator implements Interpolator {
    private final float mX1;
    private final float mX2;
    private final float mY1;
    private final float mY2;

    public CubicBezierInterpolator(float x1, float y1, float x2, float y2) {
        this.mX1 = x1;
        this.mY1 = y1;
        this.mX2 = x2;
        this.mY2 = y2;
    }

    public float getInterpolation(float v) {
        return getY(getTForXValue(v));
    }

    private float getX(float t) {
        return getCoordinate(t, this.mX1, this.mX2);
    }

    private float getY(float t) {
        return getCoordinate(t, this.mY1, this.mY2);
    }

    private float getCoordinate(float t, float p1, float p2) {
        if (t == 0.0f || t == ContentUtil.FONT_SIZE_NORMAL) {
            return t;
        }
        float ip0 = linearInterpolate(0.0f, p1, t);
        float ip1 = linearInterpolate(p1, p2, t);
        return linearInterpolate(linearInterpolate(ip0, ip1, t), linearInterpolate(ip1, linearInterpolate(p2, ContentUtil.FONT_SIZE_NORMAL, t), t), t);
    }

    private float linearInterpolate(float a, float b, float progress) {
        return ((b - a) * progress) + a;
    }

    private float getTForXValue(float x) {
        if (x <= 0.0f) {
            return 0.0f;
        }
        if (x >= ContentUtil.FONT_SIZE_NORMAL) {
            return ContentUtil.FONT_SIZE_NORMAL;
        }
        int i;
        float t = x;
        float minT = 0.0f;
        float maxT = ContentUtil.FONT_SIZE_NORMAL;
        float value = 0.0f;
        for (i = 0; i < 8; i++) {
            value = getX(t);
            double derivative = (double) ((getX(1.0E-6f + t) - value) / 1.0E-6f);
            if (Math.abs(value - x) < 1.0E-6f) {
                return t;
            }
            if (Math.abs(derivative) < 9.999999974752427E-7d) {
                break;
            }
            if (value < x) {
                minT = t;
            } else {
                maxT = t;
            }
            t = (float) (((double) t) - (((double) (value - x)) / derivative));
        }
        i = 0;
        while (Math.abs(value - x) > 1.0E-6f && i < 8) {
            if (value < x) {
                minT = t;
                t = (t + maxT) / 2.0f;
            } else {
                maxT = t;
                t = (t + minT) / 2.0f;
            }
            value = getX(t);
            i++;
        }
        return t;
    }
}
