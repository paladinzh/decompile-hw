package com.amap.api.mapcore.util;

import android.content.Context;
import android.graphics.PointF;
import android.os.RemoteException;
import android.os.SystemClock;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import com.amap.api.maps.model.AMapGestureListener;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapCore;
import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: GLMapGestureDetector */
public class h {
    l a;
    MapCore b;
    Context c;
    GestureDetector d;
    AMapGestureListener e;
    private an f;
    private al g;
    private ak h;
    private ap i;
    private boolean j = false;
    private int k = 0;
    private int l = 0;
    private int m = 0;
    private int n = 0;
    private int o = 0;
    private int p = 0;
    private boolean q = false;

    /* compiled from: GLMapGestureDetector */
    private class a implements OnDoubleTapListener, OnGestureListener {
        float a;
        long b;
        final /* synthetic */ h c;
        private int d;

        private a(h hVar) {
            this.c = hVar;
            this.d = 0;
            this.a = 0.0f;
            this.b = 0;
        }

        public boolean onDown(MotionEvent motionEvent) {
            this.c.q = false;
            return true;
        }

        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
            if (this.c.e != null) {
                this.c.e.onFling(f, f2);
            }
            try {
                if (this.c.a.m().isScrollGesturesEnabled() && this.c.n <= 0 && this.c.l <= 0 && this.c.m == 0) {
                    this.c.b.startMapSlidAnim(new IPoint((int) motionEvent2.getX(), (int) motionEvent2.getY()), f, f2);
                }
                return true;
            } catch (Throwable th) {
                fo.b(th, "GLMapGestrureDetector", "onFling");
                th.printStackTrace();
                return true;
            }
        }

        public void onLongPress(MotionEvent motionEvent) {
            if (this.c.p == 1) {
                this.c.a.b(motionEvent);
                if (this.c.e != null) {
                    this.c.e.onLongPress(motionEvent.getX(), motionEvent.getY());
                }
            }
        }

        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
            if (this.c.e != null) {
                this.c.e.onScroll(f, f2);
            }
            return false;
        }

        public void onShowPress(MotionEvent motionEvent) {
        }

        public boolean onSingleTapUp(MotionEvent motionEvent) {
            return false;
        }

        public boolean onDoubleTap(MotionEvent motionEvent) {
            this.c.d.setIsLongpressEnabled(false);
            this.d = motionEvent.getPointerCount();
            if (this.c.e != null) {
                this.c.e.onDoubleTap(motionEvent.getX(), motionEvent.getY());
            }
            return false;
        }

        public boolean onDoubleTapEvent(MotionEvent motionEvent) {
            boolean z;
            if (this.d < motionEvent.getPointerCount()) {
                this.d = motionEvent.getPointerCount();
            }
            int action = motionEvent.getAction() & 255;
            if (this.d != 1) {
                z = false;
            } else {
                try {
                    if (!this.c.a.m().isZoomGesturesEnabled()) {
                        return false;
                    }
                } catch (Throwable th) {
                    fo.b(th, "GLMapGestrureDetector", "onDoubleTapEvent");
                    th.printStackTrace();
                }
                if (action == 0) {
                    this.a = motionEvent.getY();
                    this.c.b.addGestureMessage(new av(100, WMElement.CAMERASIZEVALUE1B1, 0, 0));
                    this.b = SystemClock.uptimeMillis();
                    z = true;
                } else if (action != 2) {
                    this.c.d.setIsLongpressEnabled(true);
                    this.c.b.addGestureMessage(new av(102, WMElement.CAMERASIZEVALUE1B1, 0, 0));
                    if (action != 1) {
                        this.c.q = false;
                        z = true;
                    } else {
                        long uptimeMillis = SystemClock.uptimeMillis() - this.b;
                        if (this.c.q) {
                            if (uptimeMillis >= 200) {
                                z = true;
                            } else {
                                z = false;
                            }
                            if (z) {
                                this.c.q = false;
                                z = true;
                            }
                        }
                        return this.c.a.d(motionEvent);
                    }
                } else {
                    this.c.q = true;
                    float y = this.a - motionEvent.getY();
                    if (Math.abs(y) >= 2.0f) {
                        float mapHeight = (4.0f * y) / ((float) this.c.a.getMapHeight());
                        if (y > 0.0f) {
                            this.c.b.addGestureMessage(new av(101, mapHeight, 0, 0));
                        } else {
                            this.c.b.addGestureMessage(new av(101, mapHeight, 0, 0));
                        }
                        this.a = motionEvent.getY();
                    }
                    z = true;
                }
            }
            return z;
        }

        public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
            if (this.c.p != 1) {
                return false;
            }
            if (this.c.e != null) {
                this.c.e.onSingleTap(motionEvent.getX(), motionEvent.getY());
            }
            return this.c.a.c(motionEvent);
        }
    }

    /* compiled from: GLMapGestureDetector */
    private class b implements com.amap.api.mapcore.util.ak.a {
        final /* synthetic */ h a;

        private b(h hVar) {
            this.a = hVar;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean a(ak akVar) {
            boolean z = false;
            try {
                if (!this.a.a.m().isTiltGesturesEnabled()) {
                    return true;
                }
                if (this.a.m > 3) {
                    return false;
                }
                float f = akVar.c().x;
                float f2 = akVar.c().y;
                if (!this.a.j) {
                    PointF a = akVar.a(0);
                    PointF a2 = akVar.a(1);
                    if (a.y > 10.0f) {
                    }
                    if (a.y < -10.0f) {
                    }
                    if (z && Math.abs(f2) > 10.0f && Math.abs(f) < 10.0f) {
                        this.a.j = true;
                    }
                }
                if (this.a.j) {
                    this.a.j = true;
                    float f3 = f2 / 6.0f;
                    if (Math.abs(f3) > WMElement.CAMERASIZEVALUE1B1) {
                        this.a.b.addGestureMessage(new as(101, f3));
                        this.a.n = this.a.n + 1;
                        return true;
                    }
                }
                return true;
            } catch (Throwable th) {
                fo.b(th, "GLMapGestrureDetector", "onHove");
                th.printStackTrace();
                return true;
            }
        }

        public boolean b(ak akVar) {
            try {
                if (!this.a.a.m().isTiltGesturesEnabled()) {
                    return true;
                }
                this.a.b.addGestureMessage(new as(100, this.a.a.getCameraAngle()));
                return true;
            } catch (Throwable th) {
                fo.b(th, "GLMapGestrureDetector", "onHoveBegin");
                th.printStackTrace();
                return true;
            }
        }

        public void c(ak akVar) {
            try {
                if (this.a.a.m().isTiltGesturesEnabled()) {
                    this.a.j = false;
                    this.a.b.addGestureMessage(new as(102, this.a.a.getCameraAngle()));
                }
            } catch (Throwable th) {
                fo.b(th, "GLMapGestrureDetector", "onHoveEnd");
                th.printStackTrace();
            }
        }
    }

    /* compiled from: GLMapGestureDetector */
    private class c implements com.amap.api.mapcore.util.al.a {
        final /* synthetic */ h a;
        private final float b;

        private c(h hVar) {
            this.a = hVar;
            this.b = WMElement.CAMERASIZEVALUE1B1;
        }

        public boolean a(al alVar) {
            try {
                if (!this.a.a.m().isScrollGesturesEnabled() || this.a.j) {
                    return true;
                }
                boolean z;
                PointF c = alVar.c();
                if (Math.abs(c.x) > WMElement.CAMERASIZEVALUE1B1) {
                    z = true;
                } else {
                    z = false;
                }
                if (!z && Math.abs(c.y) <= WMElement.CAMERASIZEVALUE1B1) {
                    return false;
                }
                this.a.b.addGestureMessage(new at(101, c.x, c.y));
                this.a.k = this.a.k + 1;
                return true;
            } catch (Throwable th) {
                fo.b(th, "GLMapGestrureDetector", "onMove");
                th.printStackTrace();
                return true;
            }
        }

        public boolean b(al alVar) {
            try {
                if (!this.a.a.m().isScrollGesturesEnabled()) {
                    return true;
                }
                this.a.b.addGestureMessage(new at(100, 0.0f, 0.0f));
                return true;
            } catch (Throwable th) {
                fo.b(th, "GLMapGestrureDetector", "onMoveBegin");
                th.printStackTrace();
                return true;
            }
        }

        public void c(al alVar) {
            try {
                if (this.a.a.m().isScrollGesturesEnabled()) {
                    this.a.b.addGestureMessage(new at(102, 0.0f, 0.0f));
                }
            } catch (Throwable th) {
                fo.b(th, "GLMapGestrureDetector", "onMoveEnd");
                th.printStackTrace();
            }
        }
    }

    /* compiled from: GLMapGestureDetector */
    private class d extends com.amap.api.mapcore.util.an.a {
        final /* synthetic */ h a;
        private final float b;
        private final float c;
        private final float d;
        private final float e;
        private boolean f;
        private boolean g;
        private boolean h;
        private PointF i;

        private d(h hVar) {
            this.a = hVar;
            this.b = 0.06f;
            this.c = 0.01f;
            this.d = 4.0f;
            this.e = WMElement.CAMERASIZEVALUE1B1;
            this.f = false;
            this.g = false;
            this.h = false;
            this.i = null;
        }

        public boolean a(an anVar) {
            int i;
            boolean z;
            Throwable th;
            boolean z2;
            int i2 = -1;
            boolean z3 = false;
            float i3 = anVar.i();
            if (this.a.o != 0) {
                i = -1;
            } else {
                i = (int) anVar.a();
                i2 = (int) anVar.b();
            }
            float abs = Math.abs(((float) i) - this.i.x);
            float abs2 = Math.abs(((float) i2) - this.i.y);
            this.i.x = (float) i;
            this.i.y = (float) i2;
            float log = (float) Math.log((double) i3);
            if (this.a.l <= 0 && ((double) Math.abs(log)) > 0.2d) {
                this.h = true;
            }
            try {
                if (this.a.a.m().isZoomGesturesEnabled()) {
                    if (!this.f && 0.06f < Math.abs(log)) {
                        this.f = true;
                    }
                    if (!this.f) {
                        z = false;
                    } else if (0.01f < Math.abs(log)) {
                        if (abs > 2.0f) {
                            z = true;
                        } else {
                            z = false;
                        }
                        if (z || abs2 > 2.0f) {
                            try {
                                if (Math.abs(log) < 0.02f) {
                                    z = true;
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                z2 = true;
                                fo.b(th, "GLMapGestrureDetector", "onScaleRotate");
                                th.printStackTrace();
                                z = z2;
                                if (this.a.a.m().isRotateGesturesEnabled()) {
                                    if (!this.h) {
                                        log = anVar.j();
                                        this.g = true;
                                        if (this.g) {
                                            return z;
                                        }
                                        if (WMElement.CAMERASIZEVALUE1B1 < Math.abs(log)) {
                                            return z;
                                        }
                                        if (abs > 4.0f) {
                                            z3 = true;
                                        }
                                        if (!z3) {
                                        }
                                        return z;
                                    }
                                }
                                return z;
                            }
                        }
                        this.a.l = this.a.l + 1;
                        this.a.b.addGestureMessage(new av(101, log, i, i2));
                        z = true;
                    } else {
                        z = false;
                    }
                } else {
                    z = false;
                }
            } catch (Throwable th3) {
                th = th3;
                z2 = false;
                fo.b(th, "GLMapGestrureDetector", "onScaleRotate");
                th.printStackTrace();
                z = z2;
                if (this.a.a.m().isRotateGesturesEnabled()) {
                    if (this.h) {
                        log = anVar.j();
                        this.g = true;
                        if (this.g) {
                            return z;
                        }
                        if (WMElement.CAMERASIZEVALUE1B1 < Math.abs(log)) {
                            return z;
                        }
                        if (abs > 4.0f) {
                            z3 = true;
                        }
                        if (z3) {
                        }
                        return z;
                    }
                }
                return z;
            }
            try {
                if (this.a.a.m().isRotateGesturesEnabled()) {
                    if (this.h) {
                        log = anVar.j();
                        if (!this.g && Math.abs(log) >= 4.0f) {
                            this.g = true;
                        }
                        if (this.g) {
                            return z;
                        }
                        if (WMElement.CAMERASIZEVALUE1B1 < Math.abs(log)) {
                            return z;
                        }
                        if (abs > 4.0f) {
                            z3 = true;
                        }
                        if ((z3 || abs2 > 4.0f) && Math.abs(log) < 2.0f) {
                            return z;
                        }
                        this.a.b.addGestureMessage(new au(101, log, i, i2));
                        this.a.m = this.a.m + 1;
                        return true;
                    }
                }
                return z;
            } catch (Throwable th4) {
                fo.b(th4, "GLMapGestrureDetector", "onScaleRotate");
                th4.printStackTrace();
                return z;
            }
        }

        public boolean b(an anVar) {
            int a = (int) anVar.a();
            int b = (int) anVar.b();
            this.h = false;
            this.i = new PointF((float) a, (float) b);
            this.f = false;
            this.g = false;
            this.a.b.addGestureMessage(new av(100, WMElement.CAMERASIZEVALUE1B1, a, b));
            try {
                if (this.a.a.m().isRotateGesturesEnabled()) {
                    this.a.b.addGestureMessage(new au(100, this.a.a.y(), a, b));
                }
            } catch (Throwable th) {
                fo.b(th, "GLMapGestrureDetector", "onScaleRotateBegin");
                th.printStackTrace();
            }
            return true;
        }

        public void c(an anVar) {
            this.h = false;
            this.a.b.addGestureMessage(new av(102, WMElement.CAMERASIZEVALUE1B1, 0, 0));
            try {
                if (this.a.a.m().isRotateGesturesEnabled()) {
                    this.a.b.addGestureMessage(new au(102, this.a.a.y(), 0, 0));
                }
            } catch (Throwable th) {
                fo.b(th, "GLMapGestrureDetector", "onScaleRotateEnd");
                th.printStackTrace();
            }
        }
    }

    /* compiled from: GLMapGestureDetector */
    private class e extends com.amap.api.mapcore.util.ap.b {
        final /* synthetic */ h a;

        private e(h hVar) {
            this.a = hVar;
        }

        public void a(ap apVar) {
            Object obj = null;
            try {
                if (this.a.a.m().isZoomGesturesEnabled()) {
                    if (apVar.b() >= 100) {
                        obj = 1;
                    }
                    if (obj == null) {
                        try {
                            this.a.a.b(ag.b());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Throwable th) {
                fo.b(th, "GLMapGestrureDetector", "onZoomOut");
                th.printStackTrace();
            }
        }
    }

    public void a(AMapGestureListener aMapGestureListener) {
        this.e = aMapGestureListener;
    }

    public void a() {
        this.k = 0;
        this.m = 0;
        this.l = 0;
        this.n = 0;
        this.p = 0;
    }

    public h(Context context, l lVar) {
        this.c = context;
        this.a = lVar;
        this.b = lVar.a();
        Object aVar = new a();
        this.d = new GestureDetector(this.c, aVar);
        this.d.setOnDoubleTapListener(aVar);
        this.f = new an(this.c, new d());
        this.g = new al(this.c, new c());
        this.h = new ak(this.c, new b());
        this.i = new ap(this.c, new e());
    }

    public boolean a(MotionEvent motionEvent) {
        if (this.p < motionEvent.getPointerCount()) {
            this.p = motionEvent.getPointerCount();
        }
        if (this.q && this.p >= 2) {
            this.q = false;
        }
        try {
            if (this.e != null) {
                if (motionEvent.getAction() == 0) {
                    this.e.onDown(motionEvent.getX(), motionEvent.getY());
                } else if (motionEvent.getAction() == 1) {
                    this.e.onUp(motionEvent.getX(), motionEvent.getY());
                }
            }
            this.d.onTouchEvent(motionEvent);
            boolean a = this.h.a(motionEvent);
            if (!this.j || this.n <= 0) {
                this.i.a(motionEvent);
                if (!this.q) {
                    this.f.a(motionEvent);
                    a = this.g.a(motionEvent);
                }
            }
            return a;
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return false;
        }
    }
}
