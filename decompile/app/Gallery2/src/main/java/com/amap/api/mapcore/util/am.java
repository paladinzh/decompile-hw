package com.amap.api.mapcore.util;

import android.content.Context;
import android.os.Build.VERSION;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import com.amap.api.maps.model.GroundOverlayOptions;

/* compiled from: ScaleGestureDetector */
public class am {
    private final Context a;
    private final a b;
    private boolean c;
    private MotionEvent d;
    private MotionEvent e;
    private float f;
    private float g;
    private float h;
    private float i;
    private float j;
    private float k;
    private float l;
    private float m;
    private float n;
    private float o;
    private float p;
    private long q;
    private final float r;
    private float s;
    private float t;
    private boolean u;
    private boolean v;
    private int w;
    private int x;
    private boolean y;

    /* compiled from: ScaleGestureDetector */
    public interface a {
        boolean a(am amVar);

        boolean b(am amVar);

        void c(am amVar);
    }

    public am(Context context, a aVar) {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        this.a = context;
        this.b = aVar;
        this.r = (float) viewConfiguration.getScaledEdgeSlop();
    }

    public boolean a(MotionEvent motionEvent) {
        int i = -1;
        boolean z = true;
        boolean z2 = false;
        int action = motionEvent.getAction() & 255;
        if (action == 0) {
            j();
        }
        if (!this.v) {
            int findPointerIndex;
            if (!this.c) {
                float f;
                float f2;
                float f3;
                int findPointerIndex2;
                float b;
                float a;
                float b2;
                boolean z3;
                switch (action) {
                    case 0:
                        this.w = motionEvent.getPointerId(0);
                        this.y = true;
                        break;
                    case 1:
                        j();
                        break;
                    case 2:
                        if (this.u) {
                            boolean z4;
                            boolean z5;
                            int i2;
                            f = this.r;
                            f2 = this.s;
                            f3 = this.t;
                            findPointerIndex2 = motionEvent.findPointerIndex(this.w);
                            findPointerIndex = motionEvent.findPointerIndex(this.x);
                            float a2 = a(motionEvent, findPointerIndex2);
                            b = b(motionEvent, findPointerIndex2);
                            a = a(motionEvent, findPointerIndex);
                            b2 = b(motionEvent, findPointerIndex);
                            if (!(a2 < f)) {
                                if (!(b < f)) {
                                    if (!(a2 > f2) && b <= f3) {
                                        z4 = false;
                                        if (a >= f) {
                                            z5 = true;
                                        } else {
                                            z5 = false;
                                        }
                                        if (!z5) {
                                            if (!(b2 >= f)) {
                                                if (!(a <= f2) && b2 <= f3) {
                                                    z5 = false;
                                                    if (z4) {
                                                        i2 = findPointerIndex2;
                                                        z3 = z4;
                                                    } else {
                                                        i2 = a(motionEvent, this.x, findPointerIndex2);
                                                        if (i2 >= 0) {
                                                            i2 = findPointerIndex2;
                                                            z3 = z4;
                                                        } else {
                                                            this.w = motionEvent.getPointerId(i2);
                                                            a(motionEvent, i2);
                                                            b(motionEvent, i2);
                                                            z3 = false;
                                                        }
                                                    }
                                                    if (z5) {
                                                        i = findPointerIndex;
                                                    } else {
                                                        i = a(motionEvent, this.w, findPointerIndex);
                                                        if (i >= 0) {
                                                            i = findPointerIndex;
                                                        } else {
                                                            this.x = motionEvent.getPointerId(i);
                                                            a(motionEvent, i);
                                                            b(motionEvent, i);
                                                            z5 = false;
                                                        }
                                                    }
                                                    if (!z3 || !z5) {
                                                        if (!z3) {
                                                            if (!z5) {
                                                                this.f = motionEvent.getX(i2);
                                                                this.g = motionEvent.getY(i2);
                                                                break;
                                                            }
                                                            this.u = false;
                                                            this.c = this.b.b(this);
                                                            break;
                                                        }
                                                        this.f = motionEvent.getX(i);
                                                        this.g = motionEvent.getY(i);
                                                        break;
                                                    }
                                                    this.f = GroundOverlayOptions.NO_DIMENSION;
                                                    this.g = GroundOverlayOptions.NO_DIMENSION;
                                                    break;
                                                }
                                            }
                                        }
                                        z5 = true;
                                        if (z4) {
                                            i2 = a(motionEvent, this.x, findPointerIndex2);
                                            if (i2 >= 0) {
                                                this.w = motionEvent.getPointerId(i2);
                                                a(motionEvent, i2);
                                                b(motionEvent, i2);
                                                z3 = false;
                                            } else {
                                                i2 = findPointerIndex2;
                                                z3 = z4;
                                            }
                                        } else {
                                            i2 = findPointerIndex2;
                                            z3 = z4;
                                        }
                                        if (z5) {
                                            i = a(motionEvent, this.w, findPointerIndex);
                                            if (i >= 0) {
                                                this.x = motionEvent.getPointerId(i);
                                                a(motionEvent, i);
                                                b(motionEvent, i);
                                                z5 = false;
                                            } else {
                                                i = findPointerIndex;
                                            }
                                        } else {
                                            i = findPointerIndex;
                                        }
                                        if (!z3) {
                                            this.f = GroundOverlayOptions.NO_DIMENSION;
                                            this.g = GroundOverlayOptions.NO_DIMENSION;
                                        }
                                        if (!z3) {
                                            this.f = motionEvent.getX(i);
                                            this.g = motionEvent.getY(i);
                                        } else if (!z5) {
                                            this.f = motionEvent.getX(i2);
                                            this.g = motionEvent.getY(i2);
                                        } else {
                                            this.u = false;
                                            this.c = this.b.b(this);
                                        }
                                    }
                                }
                            }
                            z4 = true;
                            if (a >= f) {
                                z5 = false;
                            } else {
                                z5 = true;
                            }
                            if (z5) {
                                if (b2 >= f) {
                                }
                                if (b2 >= f) {
                                    if (a <= f2) {
                                    }
                                    z5 = false;
                                    if (z4) {
                                        i2 = findPointerIndex2;
                                        z3 = z4;
                                    } else {
                                        i2 = a(motionEvent, this.x, findPointerIndex2);
                                        if (i2 >= 0) {
                                            i2 = findPointerIndex2;
                                            z3 = z4;
                                        } else {
                                            this.w = motionEvent.getPointerId(i2);
                                            a(motionEvent, i2);
                                            b(motionEvent, i2);
                                            z3 = false;
                                        }
                                    }
                                    if (z5) {
                                        i = findPointerIndex;
                                    } else {
                                        i = a(motionEvent, this.w, findPointerIndex);
                                        if (i >= 0) {
                                            i = findPointerIndex;
                                        } else {
                                            this.x = motionEvent.getPointerId(i);
                                            a(motionEvent, i);
                                            b(motionEvent, i);
                                            z5 = false;
                                        }
                                    }
                                    if (!z3) {
                                        this.f = GroundOverlayOptions.NO_DIMENSION;
                                        this.g = GroundOverlayOptions.NO_DIMENSION;
                                    }
                                    if (!z3) {
                                        this.f = motionEvent.getX(i);
                                        this.g = motionEvent.getY(i);
                                    } else if (!z5) {
                                        this.u = false;
                                        this.c = this.b.b(this);
                                    } else {
                                        this.f = motionEvent.getX(i2);
                                        this.g = motionEvent.getY(i2);
                                    }
                                    break;
                                }
                            }
                            z5 = true;
                            if (z4) {
                                i2 = a(motionEvent, this.x, findPointerIndex2);
                                if (i2 >= 0) {
                                    this.w = motionEvent.getPointerId(i2);
                                    a(motionEvent, i2);
                                    b(motionEvent, i2);
                                    z3 = false;
                                } else {
                                    i2 = findPointerIndex2;
                                    z3 = z4;
                                }
                            } else {
                                i2 = findPointerIndex2;
                                z3 = z4;
                            }
                            if (z5) {
                                i = a(motionEvent, this.w, findPointerIndex);
                                if (i >= 0) {
                                    this.x = motionEvent.getPointerId(i);
                                    a(motionEvent, i);
                                    b(motionEvent, i);
                                    z5 = false;
                                } else {
                                    i = findPointerIndex;
                                }
                            } else {
                                i = findPointerIndex;
                            }
                            if (!z3) {
                                this.f = GroundOverlayOptions.NO_DIMENSION;
                                this.g = GroundOverlayOptions.NO_DIMENSION;
                            }
                            if (!z3) {
                                this.f = motionEvent.getX(i);
                                this.g = motionEvent.getY(i);
                            } else if (!z5) {
                                this.f = motionEvent.getX(i2);
                                this.g = motionEvent.getY(i2);
                            } else {
                                this.u = false;
                                this.c = this.b.b(this);
                            }
                        }
                        break;
                    case 3:
                    case 4:
                        break;
                    case 5:
                        boolean z6;
                        DisplayMetrics displayMetrics = this.a.getResources().getDisplayMetrics();
                        this.s = ((float) displayMetrics.widthPixels) - this.r;
                        this.t = ((float) displayMetrics.heightPixels) - this.r;
                        if (this.d != null) {
                            this.d.recycle();
                        }
                        this.d = MotionEvent.obtain(motionEvent);
                        this.q = 0;
                        if (VERSION.SDK_INT >= 8) {
                            action = motionEvent.getActionIndex();
                            findPointerIndex = motionEvent.findPointerIndex(this.w);
                            this.x = motionEvent.getPointerId(action);
                            if (findPointerIndex >= 0 && findPointerIndex != action) {
                                i = action;
                                action = findPointerIndex;
                            } else {
                                if (findPointerIndex != action) {
                                    i = this.x;
                                }
                                i = a(motionEvent, i, findPointerIndex);
                                this.w = motionEvent.getPointerId(i);
                                int i3 = action;
                                action = i;
                                i = i3;
                            }
                        } else if (motionEvent.getPointerCount() <= 0) {
                            i = 0;
                            action = 0;
                        } else {
                            i = motionEvent.findPointerIndex(1);
                            action = motionEvent.findPointerIndex(this.w);
                            this.x = motionEvent.getPointerId(i);
                        }
                        this.y = false;
                        b(motionEvent);
                        f = this.r;
                        f2 = this.s;
                        f3 = this.t;
                        float a3 = a(motionEvent, action);
                        b = b(motionEvent, action);
                        a = a(motionEvent, i);
                        b2 = b(motionEvent, i);
                        if (!(a3 < f)) {
                            if (!(b < f)) {
                                if (!(a3 > f2) && b <= f3) {
                                    z6 = false;
                                    if (a >= f) {
                                        z3 = true;
                                    } else {
                                        z3 = false;
                                    }
                                    if (!z3) {
                                        if (!(b2 >= f)) {
                                            if (!(a <= f2) && b2 <= f3) {
                                                z3 = false;
                                                if (!z6 || !z3) {
                                                    if (!z6) {
                                                        if (!z3) {
                                                            this.f = motionEvent.getX(action);
                                                            this.g = motionEvent.getY(action);
                                                            this.u = true;
                                                            break;
                                                        }
                                                        this.u = false;
                                                        this.c = this.b.b(this);
                                                        break;
                                                    }
                                                    this.f = motionEvent.getX(i);
                                                    this.g = motionEvent.getY(i);
                                                    this.u = true;
                                                    break;
                                                }
                                                this.f = GroundOverlayOptions.NO_DIMENSION;
                                                this.g = GroundOverlayOptions.NO_DIMENSION;
                                                this.u = true;
                                                break;
                                            }
                                        }
                                    }
                                    z3 = true;
                                    if (!z6) {
                                        this.f = GroundOverlayOptions.NO_DIMENSION;
                                        this.g = GroundOverlayOptions.NO_DIMENSION;
                                        this.u = true;
                                    }
                                    if (!z6) {
                                        this.f = motionEvent.getX(i);
                                        this.g = motionEvent.getY(i);
                                        this.u = true;
                                    } else if (!z3) {
                                        this.f = motionEvent.getX(action);
                                        this.g = motionEvent.getY(action);
                                        this.u = true;
                                    } else {
                                        this.u = false;
                                        this.c = this.b.b(this);
                                    }
                                }
                            }
                        }
                        z6 = true;
                        if (a >= f) {
                            z3 = false;
                        } else {
                            z3 = true;
                        }
                        if (z3) {
                            if (b2 >= f) {
                            }
                            if (b2 >= f) {
                                if (a <= f2) {
                                }
                                z3 = false;
                                if (!z6) {
                                    this.f = GroundOverlayOptions.NO_DIMENSION;
                                    this.g = GroundOverlayOptions.NO_DIMENSION;
                                    this.u = true;
                                }
                                if (!z6) {
                                    this.f = motionEvent.getX(i);
                                    this.g = motionEvent.getY(i);
                                    this.u = true;
                                } else if (!z3) {
                                    this.u = false;
                                    this.c = this.b.b(this);
                                } else {
                                    this.f = motionEvent.getX(action);
                                    this.g = motionEvent.getY(action);
                                    this.u = true;
                                }
                                break;
                            }
                        }
                        z3 = true;
                        if (!z6) {
                            this.f = GroundOverlayOptions.NO_DIMENSION;
                            this.g = GroundOverlayOptions.NO_DIMENSION;
                            this.u = true;
                        }
                        if (!z6) {
                            this.f = motionEvent.getX(i);
                            this.g = motionEvent.getY(i);
                            this.u = true;
                        } else if (!z3) {
                            this.f = motionEvent.getX(action);
                            this.g = motionEvent.getY(action);
                            this.u = true;
                        } else {
                            this.u = false;
                            this.c = this.b.b(this);
                        }
                        break;
                    case 6:
                        if (this.u) {
                            findPointerIndex = motionEvent.getPointerCount();
                            if (VERSION.SDK_INT < 8) {
                                action = 0;
                            } else {
                                action = motionEvent.getActionIndex();
                            }
                            findPointerIndex2 = motionEvent.getPointerId(action);
                            if (findPointerIndex > 2) {
                                if (findPointerIndex2 != this.w) {
                                    if (findPointerIndex2 == this.x) {
                                        i = a(motionEvent, this.w, action);
                                        if (i >= 0) {
                                            this.x = motionEvent.getPointerId(i);
                                            break;
                                        }
                                    }
                                }
                                i = a(motionEvent, this.x, action);
                                if (i >= 0) {
                                    this.w = motionEvent.getPointerId(i);
                                    break;
                                }
                            }
                            action = motionEvent.findPointerIndex(findPointerIndex2 != this.w ? this.w : this.x);
                            if (action >= 0) {
                                this.w = motionEvent.getPointerId(action);
                                this.y = true;
                                this.x = -1;
                                this.f = motionEvent.getX(action);
                                this.g = motionEvent.getY(action);
                                break;
                            }
                            this.v = true;
                            if (this.c) {
                                this.b.c(this);
                            }
                            return false;
                        }
                        break;
                    default:
                        break;
                }
            }
            int findPointerIndex3;
            switch (action) {
                case 1:
                    j();
                    break;
                case 2:
                    b(motionEvent);
                    if (this.o / this.p > 0.67f && this.b.a(this)) {
                        this.d.recycle();
                        this.d = MotionEvent.obtain(motionEvent);
                        break;
                    }
                case 3:
                    this.b.c(this);
                    j();
                    break;
                case 5:
                    this.b.c(this);
                    findPointerIndex = this.w;
                    action = this.x;
                    j();
                    this.d = MotionEvent.obtain(motionEvent);
                    if (this.y) {
                        action = findPointerIndex;
                    }
                    this.w = action;
                    if (VERSION.SDK_INT < 8) {
                        this.x = motionEvent.getPointerId(1);
                    } else {
                        this.x = motionEvent.getPointerId(motionEvent.getActionIndex());
                    }
                    this.y = false;
                    findPointerIndex3 = motionEvent.findPointerIndex(this.w);
                    if (findPointerIndex3 < 0 || this.w == this.x) {
                        if (this.w != this.x) {
                            i = this.x;
                        }
                        this.w = motionEvent.getPointerId(a(motionEvent, i, findPointerIndex3));
                    }
                    b(motionEvent);
                    this.c = this.b.b(this);
                    break;
                case 6:
                    action = motionEvent.getPointerCount();
                    if (VERSION.SDK_INT < 8) {
                        i = 0;
                    } else {
                        i = motionEvent.getActionIndex();
                    }
                    findPointerIndex = motionEvent.getPointerId(i);
                    if (action <= 2) {
                        z2 = true;
                    } else {
                        if (findPointerIndex == this.w) {
                            i = a(motionEvent, this.x, i);
                            if (i < 0) {
                                z2 = true;
                            } else {
                                this.b.c(this);
                                this.w = motionEvent.getPointerId(i);
                                this.y = true;
                                this.d = MotionEvent.obtain(motionEvent);
                                b(motionEvent);
                                this.c = this.b.b(this);
                            }
                        } else if (findPointerIndex == this.x) {
                            i = a(motionEvent, this.w, i);
                            if (i < 0) {
                                z2 = true;
                            } else {
                                this.b.c(this);
                                this.x = motionEvent.getPointerId(i);
                                this.y = false;
                                this.d = MotionEvent.obtain(motionEvent);
                                b(motionEvent);
                                this.c = this.b.b(this);
                            }
                        }
                        this.d.recycle();
                        this.d = MotionEvent.obtain(motionEvent);
                        b(motionEvent);
                    }
                    if (z2) {
                        b(motionEvent);
                        i = findPointerIndex != this.w ? this.w : this.x;
                        findPointerIndex3 = motionEvent.findPointerIndex(i);
                        this.f = motionEvent.getX(findPointerIndex3);
                        this.g = motionEvent.getY(findPointerIndex3);
                        this.b.c(this);
                        j();
                        this.w = i;
                        this.y = true;
                        break;
                    }
                    break;
            }
        }
        z = false;
        return z;
    }

    private int a(MotionEvent motionEvent, int i, int i2) {
        int pointerCount = motionEvent.getPointerCount();
        int findPointerIndex = motionEvent.findPointerIndex(i);
        int i3 = 0;
        while (i3 < pointerCount) {
            if (!(i3 == i2 || i3 == findPointerIndex)) {
                float f = this.r;
                float f2 = this.s;
                float f3 = this.t;
                float a = a(motionEvent, i3);
                float b = b(motionEvent, i3);
                if (a >= f && b >= f && a <= f2 && b <= f3) {
                    return i3;
                }
            }
            i3++;
        }
        return -1;
    }

    private static float a(MotionEvent motionEvent, int i) {
        if (i < 0) {
            return Float.MIN_VALUE;
        }
        if (i != 0) {
            return (motionEvent.getRawX() - motionEvent.getX()) + motionEvent.getX(i);
        }
        return motionEvent.getRawX();
    }

    private static float b(MotionEvent motionEvent, int i) {
        if (i < 0) {
            return Float.MIN_VALUE;
        }
        if (i != 0) {
            return (motionEvent.getRawY() - motionEvent.getY()) + motionEvent.getY(i);
        }
        return motionEvent.getRawY();
    }

    private void b(MotionEvent motionEvent) {
        if (this.e != null) {
            this.e.recycle();
        }
        this.e = MotionEvent.obtain(motionEvent);
        this.l = GroundOverlayOptions.NO_DIMENSION;
        this.m = GroundOverlayOptions.NO_DIMENSION;
        this.n = GroundOverlayOptions.NO_DIMENSION;
        MotionEvent motionEvent2 = this.d;
        int findPointerIndex = motionEvent2.findPointerIndex(this.w);
        int findPointerIndex2 = motionEvent2.findPointerIndex(this.x);
        int findPointerIndex3 = motionEvent.findPointerIndex(this.w);
        int findPointerIndex4 = motionEvent.findPointerIndex(this.x);
        if (findPointerIndex >= 0 && findPointerIndex2 >= 0 && findPointerIndex3 >= 0 && findPointerIndex4 >= 0) {
            float x = motionEvent2.getX(findPointerIndex);
            float y = motionEvent2.getY(findPointerIndex);
            float x2 = motionEvent2.getX(findPointerIndex2);
            float y2 = motionEvent2.getY(findPointerIndex2);
            float x3 = motionEvent.getX(findPointerIndex3);
            float y3 = motionEvent.getY(findPointerIndex3);
            x = x2 - x;
            y = y2 - y;
            x2 = motionEvent.getX(findPointerIndex4) - x3;
            y2 = motionEvent.getY(findPointerIndex4) - y3;
            this.h = x;
            this.i = y;
            this.j = x2;
            this.k = y2;
            this.f = (x2 * 0.5f) + x3;
            this.g = (y2 * 0.5f) + y3;
            this.q = motionEvent.getEventTime() - motionEvent2.getEventTime();
            this.o = motionEvent.getPressure(findPointerIndex3) + motionEvent.getPressure(findPointerIndex4);
            this.p = motionEvent2.getPressure(findPointerIndex2) + motionEvent2.getPressure(findPointerIndex);
            return;
        }
        this.v = true;
        if (this.c) {
            this.b.c(this);
        }
    }

    private void j() {
        if (this.d != null) {
            this.d.recycle();
            this.d = null;
        }
        if (this.e != null) {
            this.e.recycle();
            this.e = null;
        }
        this.u = false;
        this.c = false;
        this.w = -1;
        this.x = -1;
        this.v = false;
    }

    public float a() {
        return this.f;
    }

    public float b() {
        return this.g;
    }

    public float c() {
        if (this.l == GroundOverlayOptions.NO_DIMENSION) {
            float f = this.j;
            float f2 = this.k;
            this.l = (float) Math.sqrt((double) ((f * f) + (f2 * f2)));
        }
        return this.l;
    }

    public float d() {
        return this.j;
    }

    public float e() {
        return this.k;
    }

    public float f() {
        if (this.m == GroundOverlayOptions.NO_DIMENSION) {
            float f = this.h;
            float f2 = this.i;
            this.m = (float) Math.sqrt((double) ((f * f) + (f2 * f2)));
        }
        return this.m;
    }

    public float g() {
        return this.h;
    }

    public float h() {
        return this.i;
    }

    public float i() {
        if (this.n == GroundOverlayOptions.NO_DIMENSION) {
            this.n = c() / f();
        }
        return this.n;
    }
}
