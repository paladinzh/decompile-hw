package com.amap.api.mapcore.util;

import android.content.Context;
import android.view.MotionEvent;

/* compiled from: BaseGestureDetector */
public abstract class b {
    protected final Context a;
    protected boolean b;
    protected MotionEvent c;
    protected MotionEvent d;
    protected float e;
    protected float f;
    protected long g;

    protected abstract void a(int i, MotionEvent motionEvent);

    protected abstract void b(int i, MotionEvent motionEvent);

    public b(Context context) {
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
        if (this.c != null) {
            MotionEvent motionEvent2 = this.c;
            if (this.d != null) {
                this.d.recycle();
                this.d = null;
            }
            this.d = MotionEvent.obtain(motionEvent);
            this.g = motionEvent.getEventTime() - motionEvent2.getEventTime();
            this.e = motionEvent.getPressure(c(motionEvent));
            this.f = motionEvent2.getPressure(c(motionEvent2));
        }
    }

    public final int c(MotionEvent motionEvent) {
        return (motionEvent.getAction() & 65280) >> 8;
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
}
