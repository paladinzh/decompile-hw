package com.amap.api.mapcore.util;

import android.content.Context;
import android.graphics.PointF;
import android.view.MotionEvent;

/* compiled from: ZoomOutGestureDetector */
public class ap extends ao {
    private static final PointF n = new PointF();
    private final a l;
    private boolean m;
    private PointF o;
    private PointF p;
    private PointF q = new PointF();
    private PointF r = new PointF();

    /* compiled from: ZoomOutGestureDetector */
    public interface a {
        void a(ap apVar);

        boolean b(ap apVar);
    }

    /* compiled from: ZoomOutGestureDetector */
    public static class b implements a {
        public boolean b(ap apVar) {
            return true;
        }

        public void a(ap apVar) {
        }
    }

    public ap(Context context, a aVar) {
        super(context);
        this.l = aVar;
    }

    protected void a(int i, MotionEvent motionEvent) {
        switch (i) {
            case 5:
                a();
                this.c = MotionEvent.obtain(motionEvent);
                this.g = 0;
                b(motionEvent);
                this.m = d(motionEvent);
                if (!this.m) {
                    this.b = this.l.b(this);
                    return;
                }
                return;
            default:
                return;
        }
    }

    protected void b(int i, MotionEvent motionEvent) {
        switch (i) {
            case 3:
                a();
                return;
            case 6:
                b(motionEvent);
                if (!this.m) {
                    this.l.a(this);
                }
                a();
                return;
            default:
                return;
        }
    }

    protected void a() {
        super.a();
        this.m = false;
        this.q.x = 0.0f;
        this.r.x = 0.0f;
        this.q.y = 0.0f;
        this.r.y = 0.0f;
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
        this.r = obj == null ? new PointF(this.o.x - this.p.x, this.o.y - this.p.y) : n;
        PointF pointF = this.q;
        pointF.x += this.r.x;
        pointF = this.q;
        pointF.y += this.r.y;
    }
}
