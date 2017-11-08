package com.amap.api.mapcore.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Marker;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.IPoint;

/* compiled from: MapOverlayViewGroup */
public class em extends ViewGroup implements cq {
    int a = 0;
    int b = 0;
    private l c;
    private Context d;
    private eo e;
    private el f;
    private ej g;
    private en h;
    private ei i;
    private ek j;
    private ep k;
    private View l;
    private View m;
    private TextView n;
    private TextView o;
    private cr p;
    private Drawable q = null;
    private InfoWindowAdapter r;
    private boolean s = true;
    private InfoWindowAdapter t = new InfoWindowAdapter(this) {
        final /* synthetic */ em a;

        {
            this.a = r1;
        }

        public View getInfoWindow(Marker marker) {
            try {
                if (this.a.q == null) {
                    this.a.q = ec.a(this.a.d, "infowindow_bg.9.png");
                }
                if (this.a.m == null) {
                    this.a.m = new LinearLayout(this.a.d);
                    this.a.m.setBackground(this.a.q);
                    this.a.n = new TextView(this.a.d);
                    this.a.n.setText(marker.getTitle());
                    this.a.n.setTextColor(-16777216);
                    this.a.o = new TextView(this.a.d);
                    this.a.o.setTextColor(-16777216);
                    this.a.o.setText(marker.getSnippet());
                    ((LinearLayout) this.a.m).setOrientation(1);
                    ((LinearLayout) this.a.m).addView(this.a.n);
                    ((LinearLayout) this.a.m).addView(this.a.o);
                }
            } catch (Throwable th) {
                fo.b(th, "MapOverlayViewGroup", "showInfoWindow decodeDrawableFromAsset");
                th.printStackTrace();
            }
            return this.a.m;
        }

        public View getInfoContents(Marker marker) {
            return null;
        }
    };

    /* compiled from: MapOverlayViewGroup */
    public static class a extends LayoutParams {
        public FPoint a = null;
        public int b = 0;
        public int c = 0;
        public int d = 51;

        public a(int i, int i2, FPoint fPoint, int i3, int i4, int i5) {
            super(i, i2);
            this.a = fPoint;
            this.b = i3;
            this.c = i4;
            this.d = i5;
        }
    }

    public em(Context context, l lVar) {
        super(context);
        this.c = lVar;
        this.d = context;
        setBackgroundColor(-1);
        a(context);
    }

    private void a(Context context) {
        this.e = new eo(context, this.c);
        this.h = new en(context, this.c);
        this.i = new ei(context);
        this.j = new ek(context);
        this.k = new ep(context, this.c);
        this.f = new el(context, this.c);
        this.g = new ej(context, this.c);
        LayoutParams layoutParams = new LayoutParams(-1, -1);
        addView(this.c.z(), 0, layoutParams);
        addView(this.i, 1, layoutParams);
        addView(this.e, layoutParams);
        addView(this.h, layoutParams);
        addView(this.j, new LayoutParams(-2, -2));
        addView(this.k, new a(-2, -2, new FPoint(0.0f, 0.0f), 0, 0, 83));
        addView(this.f, new a(-2, -2, new FPoint(0.0f, 0.0f), 0, 0, 83));
        addView(this.g, new a(-2, -2, new FPoint(0.0f, 0.0f), 0, 0, 51));
        this.g.setVisibility(8);
        this.r = this.t;
        try {
            if (!this.c.m().isMyLocationButtonEnabled()) {
                this.f.setVisibility(8);
            }
        } catch (Throwable th) {
            fo.b(th, "AMapDelegateImpGLSurfaceView", "locationView gone");
            th.printStackTrace();
        }
    }

    public void a(boolean z) {
        if (this.j != null && z && this.c.f()) {
            this.j.a(true);
        }
    }

    public void b(boolean z) {
        if (this.k != null) {
            this.k.a(z);
        }
    }

    public void c(boolean z) {
        if (this.f != null) {
            if (z) {
                this.f.setVisibility(0);
            } else {
                this.f.setVisibility(8);
            }
        }
    }

    public void d(boolean z) {
        if (this.g != null) {
            this.g.a(z);
        }
    }

    public void e(boolean z) {
        if (this.h != null) {
            this.h.a(z);
        }
    }

    public void a(float f) {
        if (this.k != null) {
            this.k.a(f);
        }
    }

    public void a(int i) {
        if (this.k != null) {
            this.k.a(i);
        }
    }

    public void b(int i) {
        if (this.e != null) {
            this.e.a(i);
            this.e.invalidate();
            l();
        }
    }

    private void l() {
        if (this.h != null && this.h.getVisibility() == 0) {
            this.h.invalidate();
        }
    }

    public void c(int i) {
        if (this.e != null) {
            this.e.b(i);
            l();
        }
    }

    public void d(int i) {
        if (this.e != null) {
            this.e.c(i);
            l();
        }
    }

    public float e(int i) {
        if (this.e == null) {
            return 0.0f;
        }
        l();
        return this.e.d(i);
    }

    public void a(int i, float f) {
        if (this.e != null) {
            this.e.a(i, f);
            l();
        }
    }

    public void a(InfoWindowAdapter infoWindowAdapter) throws RemoteException {
        if (infoWindowAdapter != null) {
            this.r = infoWindowAdapter;
        } else {
            this.r = this.t;
        }
    }

    public Point a() {
        if (this.e != null) {
            return this.e.b();
        }
        return null;
    }

    public void f(boolean z) {
        if (this.e != null && z) {
            this.e.a(false);
        } else {
            this.e.a(true);
        }
    }

    public en b() {
        return this.h;
    }

    public ei c() {
        return this.i;
    }

    public ek f() {
        return this.j;
    }

    public el g() {
        return this.f;
    }

    public ej h() {
        return this.g;
    }

    public eo i() {
        return this.e;
    }

    public void a(CameraPosition cameraPosition) {
        if (g.c != 1) {
            if (cameraPosition.zoom >= 10.0f && !ee.a(cameraPosition.target.latitude, cameraPosition.target.longitude)) {
                this.e.setVisibility(8);
            } else if (this.c.A() == -1) {
                this.e.setVisibility(0);
            }
        }
    }

    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int childCount = getChildCount();
        for (int i5 = 0; i5 < childCount; i5++) {
            View childAt = getChildAt(i5);
            if (childAt != null) {
                if (childAt.getLayoutParams() instanceof a) {
                    a(childAt, (a) childAt.getLayoutParams());
                } else {
                    a(childAt, childAt.getLayoutParams());
                }
            }
        }
        this.e.c();
    }

    private void a(View view, LayoutParams layoutParams) {
        int[] iArr = new int[2];
        a(view, layoutParams.width, layoutParams.height, iArr);
        if (view instanceof ek) {
            a(view, iArr[0], iArr[1], 20, (this.c.q().y - 80) - iArr[1], 51);
            return;
        }
        a(view, iArr[0], iArr[1], 0, 0, 51);
    }

    private void a(View view, a aVar) {
        int[] iArr = new int[2];
        a(view, aVar.width, aVar.height, iArr);
        if (view instanceof ep) {
            a(view, iArr[0], iArr[1], getWidth() - iArr[0], getHeight(), aVar.d);
        } else if (view instanceof el) {
            a(view, iArr[0], iArr[1], getWidth() - iArr[0], iArr[1], aVar.d);
        } else if (view instanceof ej) {
            a(view, iArr[0], iArr[1], 0, 0, aVar.d);
        } else if (aVar.a != null) {
            IPoint iPoint = new IPoint();
            this.c.c().map2Win(aVar.a.x, aVar.a.y, iPoint);
            iPoint.x += aVar.b;
            iPoint.y += aVar.c;
            a(view, iArr[0], iArr[1], iPoint.x, iPoint.y, aVar.d);
        }
    }

    public void a(cr crVar) {
        if (crVar != null) {
            try {
                if (crVar.getTitle() != null || crVar.getSnippet() != null) {
                    if (!(this.p == null || this.p.getId().equals(crVar.getId()))) {
                        d();
                    }
                    if (this.r != null) {
                        this.p = crVar;
                        crVar.a(true);
                    }
                }
            } catch (Throwable th) {
            }
        }
    }

    private View b(cr crVar) throws RemoteException {
        View view = null;
        Marker marker = new Marker(crVar);
        try {
            if (this.q == null) {
                this.q = ec.a(this.d, "infowindow_bg.9.png");
            }
        } catch (Throwable th) {
            fo.b(th, "MapOverlayViewGroup", "showInfoWindow decodeDrawableFromAsset");
            th.printStackTrace();
        }
        try {
            view = this.r.getInfoWindow(marker);
            if (view == null) {
                view = this.r.getInfoContents(marker);
            }
            if (view == null) {
                view = this.t.getInfoWindow(marker);
            }
            if (view.getBackground() == null) {
                view.setBackground(this.q);
            }
        } catch (Throwable th2) {
            fo.b(th2, "MapOverlayViewGroup", "getInfoWindow or getInfoContents");
            th2.printStackTrace();
        }
        return view;
    }

    public void e() {
        try {
            if (this.p == null || !this.p.k()) {
                if (this.l != null && this.l.getVisibility() == 0) {
                    this.l.setVisibility(8);
                }
            } else if (this.s) {
                int e = this.p.e() + this.p.c();
                int f = (this.p.f() + this.p.d()) + 2;
                if (this.p.g()) {
                    if (e == this.a && f == this.b) {
                        return;
                    }
                }
                a(b(this.p), e, f);
                this.p.h();
                a aVar = (a) this.l.getLayoutParams();
                if (aVar != null) {
                    aVar.a = this.p.a();
                    aVar.b = e;
                    aVar.c = f;
                }
                onLayout(false, 0, 0, 0, 0);
                this.a = e;
                this.b = f;
                if (this.r == this.t) {
                    if (this.n != null) {
                        this.n.setText(this.p.getTitle());
                    }
                    if (this.o != null) {
                        this.o.setText(this.p.getSnippet());
                    }
                }
                if (this.l.getVisibility() == 8) {
                    this.l.setVisibility(0);
                }
            }
        } catch (Throwable th) {
            fo.b(th, "MapOverlayViewGroup", "redrawInfoWindow");
            th.printStackTrace();
        }
    }

    private void a(View view, int i, int i2) throws RemoteException {
        int i3 = -2;
        if (view != null) {
            int i4;
            if (this.l != null) {
                if (view != this.l) {
                    this.l.clearFocus();
                    removeView(this.l);
                } else {
                    return;
                }
            }
            this.l = view;
            LayoutParams layoutParams = this.l.getLayoutParams();
            this.l.setDrawingCacheEnabled(true);
            this.l.setDrawingCacheQuality(0);
            this.p.h();
            if (layoutParams == null) {
                i4 = -2;
            } else {
                i4 = layoutParams.width;
                i3 = layoutParams.height;
            }
            addView(this.l, new a(i4, i3, this.p.a(), i, i2, 81));
        }
    }

    public void d() {
        if (this.l != null) {
            this.l.clearFocus();
            removeView(this.l);
            eh.a(this.l.getBackground());
            eh.a(this.q);
            this.l = null;
        }
        if (this.p != null) {
            this.p.a(false);
        }
        this.p = null;
        this.m = null;
        this.n = null;
        this.o = null;
    }

    private void a(View view, int i, int i2, int[] iArr) {
        if (view instanceof ListView) {
            View view2 = (View) view.getParent();
            if (view2 != null) {
                iArr[0] = view2.getWidth();
                iArr[1] = view2.getHeight();
            }
        }
        if (i <= 0 || i2 <= 0) {
            view.measure(0, 0);
        }
        if (i == -2) {
            iArr[0] = view.getMeasuredWidth();
        } else if (i != -1) {
            iArr[0] = i;
        } else {
            iArr[0] = getMeasuredWidth();
        }
        if (i2 == -2) {
            iArr[1] = view.getMeasuredHeight();
        } else if (i2 != -1) {
            iArr[1] = i2;
        } else {
            iArr[1] = getMeasuredHeight();
        }
    }

    private void a(View view, int i, int i2, int i3, int i4, int i5) {
        int i6 = i5 & 7;
        int i7 = i5 & 112;
        if (i6 == 5) {
            i3 -= i;
        } else if (i6 == 1) {
            i3 -= i / 2;
        }
        if (i7 == 80) {
            i4 -= i2;
        } else if (i7 == 17) {
            i4 -= i2 / 2;
        } else if (i7 == 16) {
            i4 = (i4 / 2) - (i2 / 2);
        }
        view.layout(i3, i4, i3 + i, i4 + i2);
    }

    public void j() {
        d();
        eh.a(this.q);
        removeAllViews();
        this.n = null;
        this.o = null;
        this.m = null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean a(MotionEvent motionEvent) {
        if (this.l == null || this.p == null || !eh.a(new Rect(this.l.getLeft(), this.l.getTop(), this.l.getRight(), this.l.getBottom()), (int) motionEvent.getX(), (int) motionEvent.getY())) {
            return false;
        }
        return true;
    }

    public void a(Canvas canvas) {
        if (this.l != null && this.p != null) {
            Bitmap drawingCache = this.l.getDrawingCache(true);
            if (drawingCache != null) {
                canvas.drawBitmap(drawingCache, (float) this.l.getLeft(), (float) this.l.getTop(), new Paint());
            }
        }
    }

    public void k() {
        this.a = 0;
        this.b = 0;
    }
}
