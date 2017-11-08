package com.amap.api.mapcore.util;

import android.content.Context;
import android.view.MotionEvent;
import java.lang.reflect.Method;

/* compiled from: MultiTouchSupport */
public class c {
    protected Method a;
    protected Method b;
    protected Method c;
    private boolean d = false;
    private final a e;
    private long f = 0;
    private boolean g = false;

    /* compiled from: MultiTouchSupport */
    public interface a {
        void a();

        void a(float f, float f2, float f3, float f4, float f5);

        boolean a(MotionEvent motionEvent, float f, float f2, float f3, float f4);
    }

    public c(Context context, a aVar) {
        this.e = aVar;
        c();
    }

    public boolean a() {
        return this.g;
    }

    public long b() {
        return this.f;
    }

    private void c() {
        try {
            this.a = MotionEvent.class.getMethod("getPointerCount", new Class[0]);
            this.b = MotionEvent.class.getMethod("getX", new Class[]{Integer.TYPE});
            this.c = MotionEvent.class.getMethod("getY", new Class[]{Integer.TYPE});
            this.d = true;
        } catch (Throwable th) {
            this.d = false;
            ce.a(th, "MultiTouchSupport", "initMethods");
            th.printStackTrace();
        }
    }

    public boolean a(MotionEvent motionEvent) {
        if (!this.d) {
            return false;
        }
        int action = motionEvent.getAction() & 255;
        try {
            if (((Integer) this.a.invoke(motionEvent, new Object[0])).intValue() >= 2) {
                Float f = (Float) this.b.invoke(motionEvent, new Object[]{Integer.valueOf(0)});
                Float f2 = (Float) this.b.invoke(motionEvent, new Object[]{Integer.valueOf(1)});
                Float f3 = (Float) this.c.invoke(motionEvent, new Object[]{Integer.valueOf(0)});
                Float f4 = (Float) this.c.invoke(motionEvent, new Object[]{Integer.valueOf(1)});
                float sqrt = (float) Math.sqrt((double) (((f2.floatValue() - f.floatValue()) * (f2.floatValue() - f.floatValue())) + ((f4.floatValue() - f3.floatValue()) * (f4.floatValue() - f3.floatValue()))));
                if (action == 5) {
                    this.e.a(sqrt, f.floatValue(), f3.floatValue(), f2.floatValue(), f4.floatValue());
                    this.g = true;
                    return true;
                } else if (action != 6) {
                    if (this.g && action == 2) {
                        return this.e.a(motionEvent, f.floatValue(), f3.floatValue(), f2.floatValue(), f4.floatValue());
                    }
                    return false;
                } else {
                    this.f = motionEvent.getEventTime();
                    if (motionEvent.getPointerCount() == 2) {
                        if (!(this.f - motionEvent.getDownTime() >= 100)) {
                            this.e.a();
                        }
                    }
                    if (this.g) {
                        this.g = false;
                    }
                    return false;
                }
            }
            this.f = 0;
            this.g = false;
            return false;
        } catch (Throwable th) {
            ce.a(th, "MultiTouchSupport", "onTouchEvent");
            th.printStackTrace();
        }
    }
}
