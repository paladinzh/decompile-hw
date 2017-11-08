package com.amap.api.mapcore.util;

import android.content.Context;
import android.graphics.PointF;
import android.os.Build.VERSION;
import android.view.MotionEvent;

/* compiled from: BaseGestureDetector */
public abstract class aj {
    protected final Context a;
    protected boolean b;
    protected MotionEvent c;
    protected MotionEvent d;
    protected float e;
    protected float f;
    protected long g;

    protected abstract void a(int i, MotionEvent motionEvent);

    protected abstract void b(int i, MotionEvent motionEvent);

    public aj(Context context) {
        this.a = context;
    }

    public boolean a(MotionEvent motionEvent) {
        int action = motionEvent.getAction() & 255;
        if (this.b) {
            b(action, motionEvent);
        } else {
            a(action, motionEvent);
        }
        return true;
    }

    protected void b(MotionEvent motionEvent) {
        MotionEvent motionEvent2 = this.c;
        if (this.d != null) {
            this.d.recycle();
            this.d = null;
        }
        this.d = MotionEvent.obtain(motionEvent);
        this.g = motionEvent.getEventTime() - motionEvent2.getEventTime();
        if (VERSION.SDK_INT < 8) {
            this.e = motionEvent.getPressure(0);
            this.f = motionEvent2.getPressure(0);
            return;
        }
        this.e = motionEvent.getPressure(motionEvent.getActionIndex());
        this.f = motionEvent2.getPressure(motionEvent2.getActionIndex());
    }

    protected void a() {
        if (this.c != null) {
            this.c.recycle();
            this.c = null;
        }
        if (this.d != null) {
            this.d.recycle();
            this.d = null;
        }
        this.b = false;
    }

    public long b() {
        return this.g;
    }

    public static PointF c(MotionEvent motionEvent) {
        float f = 0.0f;
        int pointerCount = motionEvent.getPointerCount();
        float f2 = 0.0f;
        for (int i = 0; i < pointerCount; i++) {
            f2 += motionEvent.getX(i);
            f += motionEvent.getY(i);
        }
        return new PointF(f2 / ((float) pointerCount), f / ((float) pointerCount));
    }
}
