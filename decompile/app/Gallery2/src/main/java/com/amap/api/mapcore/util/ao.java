package com.amap.api.mapcore.util;

import android.content.Context;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import com.amap.api.maps.model.GroundOverlayOptions;

/* compiled from: TwoFingerGestureDetector */
public abstract class ao extends aj {
    protected float h;
    protected float i;
    protected float j;
    protected float k;
    private final float l;
    private float m;
    private float n;
    private float o;
    private float p;
    private float q = 0.0f;
    private float r = 0.0f;
    private float s = 0.0f;
    private float t = 0.0f;

    public ao(Context context) {
        super(context);
        this.l = (float) ViewConfiguration.get(context).getScaledEdgeSlop();
    }

    protected void b(MotionEvent motionEvent) {
        super.b(motionEvent);
        MotionEvent motionEvent2 = this.c;
        int pointerCount = this.c.getPointerCount();
        int pointerCount2 = motionEvent.getPointerCount();
        if (pointerCount2 == 2 && pointerCount2 == pointerCount) {
            this.o = GroundOverlayOptions.NO_DIMENSION;
            this.p = GroundOverlayOptions.NO_DIMENSION;
            float x = motionEvent2.getX(0);
            float y = motionEvent2.getY(0);
            float x2 = motionEvent2.getX(1);
            float y2 = motionEvent2.getY(1);
            float f = y2 - y;
            this.h = x2 - x;
            this.i = f;
            float x3 = motionEvent.getX(0);
            f = motionEvent.getY(0);
            float x4 = motionEvent.getX(1);
            float y3 = motionEvent.getY(1);
            float f2 = y3 - f;
            this.j = x4 - x3;
            this.k = f2;
            this.q = x3 - x;
            this.r = f - y;
            this.s = x4 - x2;
            this.t = y3 - y2;
        }
    }

    public PointF a(int i) {
        if (i != 0) {
            return new PointF(this.s, this.t);
        }
        return new PointF(this.q, this.r);
    }

    protected static float a(MotionEvent motionEvent, int i) {
        float x = motionEvent.getX() - motionEvent.getRawX();
        if (i >= motionEvent.getPointerCount()) {
            return 0.0f;
        }
        return x + motionEvent.getX(i);
    }

    protected static float b(MotionEvent motionEvent, int i) {
        float y = motionEvent.getY() - motionEvent.getRawY();
        if (i >= motionEvent.getPointerCount()) {
            return 0.0f;
        }
        return y + motionEvent.getY(i);
    }

    protected boolean d(MotionEvent motionEvent) {
        boolean z;
        boolean z2;
        DisplayMetrics displayMetrics = this.a.getResources().getDisplayMetrics();
        this.m = ((float) displayMetrics.widthPixels) - this.l;
        this.n = ((float) displayMetrics.heightPixels) - this.l;
        float f = this.l;
        float f2 = this.m;
        float f3 = this.n;
        float rawX = motionEvent.getRawX();
        float rawY = motionEvent.getRawY();
        float a = a(motionEvent, 1);
        float b = b(motionEvent, 1);
        if (!(rawX < f)) {
            if (!(rawY < f)) {
                if (!(rawX > f2) && rawY <= f3) {
                    z = false;
                    if (a >= f) {
                        z2 = true;
                    } else {
                        z2 = false;
                    }
                    if (!z2) {
                        if (!(b >= f)) {
                            if (!(a <= f2) && b <= f3) {
                                z2 = false;
                                return (z && z2) || z || z2;
                            }
                        }
                    }
                    z2 = true;
                    if (z) {
                        return true;
                    }
                    return true;
                }
            }
        }
        z = true;
        if (a >= f) {
            z2 = false;
        } else {
            z2 = true;
        }
        if (z2) {
            if (b >= f) {
            }
            if (b >= f) {
                if (a <= f2) {
                }
                z2 = false;
                if (z) {
                    return true;
                }
                return true;
            }
        }
        z2 = true;
        if (z) {
            return true;
        }
        return true;
    }
}
