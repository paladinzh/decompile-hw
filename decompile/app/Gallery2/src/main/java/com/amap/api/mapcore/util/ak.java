package com.amap.api.mapcore.util;

import android.content.Context;
import android.graphics.PointF;
import android.view.MotionEvent;

/* compiled from: HoverGestureDetector */
public class ak extends ao {
    private static final PointF l = new PointF();
    private final a m;
    private boolean n;
    private PointF o;
    private PointF p;
    private PointF q = new PointF();
    private PointF r = new PointF();

    /* compiled from: HoverGestureDetector */
    public interface a {
        boolean a(ak akVar);

        boolean b(ak akVar);

        void c(ak akVar);
    }

    public ak(Context context, a aVar) {
        super(context);
        this.m = aVar;
    }

    protected void a(int i, MotionEvent motionEvent) {
        switch (i) {
            case 2:
                if (this.n) {
                    this.n = d(motionEvent);
                    if (!this.n) {
                        this.b = this.m.b(this);
                        return;
                    }
                    return;
                }
                return;
            case 5:
                a();
                this.c = MotionEvent.obtain(motionEvent);
                this.g = 0;
                b(motionEvent);
                this.n = d(motionEvent);
                if (!this.n) {
                    this.b = this.m.b(this);
                    return;
                }
                return;
            case 6:
                if (!this.n) {
                    return;
                }
                return;
            default:
                return;
        }
    }

    protected void b(int i, MotionEvent motionEvent) {
        switch (i) {
            case 2:
                b(motionEvent);
                if (this.e / this.f > 0.67f && this.m.a(this)) {
                    this.c.recycle();
                    this.c = MotionEvent.obtain(motionEvent);
                    return;
                }
                return;
            case 3:
                if (!this.n) {
                    this.m.c(this);
                }
                a();
                return;
            case 6:
                b(motionEvent);
                if (!this.n) {
                    this.m.c(this);
                }
                a();
                return;
            default:
                return;
        }
    }

    protected void a() {
        super.a();
        this.n = false;
    }

    protected void b(MotionEvent motionEvent) {
        Object obj = null;
        super.b(motionEvent);
        MotionEvent motionEvent2 = this.c;
        this.o = aj.c(motionEvent);
        this.p = aj.c(motionEvent2);
        if (this.c.getPointerCount() != motionEvent.getPointerCount()) {
            obj = 1;
        }
        this.r = obj == null ? new PointF(this.o.x - this.p.x, this.o.y - this.p.y) : l;
        PointF pointF = this.q;
        pointF.x += this.r.x;
        pointF = this.q;
        pointF.y += this.r.y;
    }

    public PointF c() {
        return this.r;
    }
}
