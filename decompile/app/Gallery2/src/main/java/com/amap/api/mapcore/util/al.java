package com.amap.api.mapcore.util;

import android.content.Context;
import android.graphics.PointF;
import android.view.MotionEvent;

/* compiled from: MoveGestureDetector */
public class al extends aj {
    private static final PointF h = new PointF();
    private final a i;
    private PointF j;
    private PointF k;
    private PointF l = new PointF();
    private PointF m = new PointF();

    /* compiled from: MoveGestureDetector */
    public interface a {
        boolean a(al alVar);

        boolean b(al alVar);

        void c(al alVar);
    }

    public al(Context context, a aVar) {
        super(context);
        this.i = aVar;
    }

    protected void a(int i, MotionEvent motionEvent) {
        switch (i) {
            case 0:
                a();
                this.c = MotionEvent.obtain(motionEvent);
                this.g = 0;
                b(motionEvent);
                return;
            case 2:
                this.b = this.i.b(this);
                return;
            case 5:
                if (this.c != null) {
                    this.c.recycle();
                }
                this.c = MotionEvent.obtain(motionEvent);
                b(motionEvent);
                return;
            default:
                return;
        }
    }

    protected void b(int i, MotionEvent motionEvent) {
        switch (i) {
            case 1:
            case 3:
                this.i.c(this);
                a();
                return;
            case 2:
                b(motionEvent);
                if (this.e / this.f > 0.67f && this.i.a(this)) {
                    this.c.recycle();
                    this.c = MotionEvent.obtain(motionEvent);
                    return;
                }
                return;
            default:
                return;
        }
    }

    protected void b(MotionEvent motionEvent) {
        Object obj = null;
        super.b(motionEvent);
        MotionEvent motionEvent2 = this.c;
        this.j = aj.c(motionEvent);
        this.k = aj.c(motionEvent2);
        if (this.c.getPointerCount() != motionEvent.getPointerCount()) {
            obj = 1;
        }
        this.m = obj == null ? new PointF(this.j.x - this.k.x, this.j.y - this.k.y) : h;
        if (obj != null) {
            this.c.recycle();
            this.c = MotionEvent.obtain(motionEvent);
        }
        PointF pointF = this.l;
        pointF.x += this.m.x;
        pointF = this.l;
        pointF.y += this.m.y;
    }

    public PointF c() {
        return this.m;
    }
}
