package com.amap.api.mapcore;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.amap.api.mapcore.util.au;
import com.amap.api.mapcore.util.bj;
import com.amap.api.mapcore.util.bl;
import com.amap.api.mapcore.util.ce;
import com.amap.api.maps.AMap.CancelableCallback;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnCacheRemoveListener;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnIndoorBuildingActiveListener;
import com.amap.api.maps.AMap.OnInfoWindowClickListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.AMap.OnMapLongClickListener;
import com.amap.api.maps.AMap.OnMapScreenShotListener;
import com.amap.api.maps.AMap.OnMapTouchListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.AMap.OnMarkerDragListener;
import com.amap.api.maps.AMap.OnMyLocationChangeListener;
import com.amap.api.maps.AMap.OnPOIClickListener;
import com.amap.api.maps.AMap.OnPolylineClickListener;
import com.amap.api.maps.AMap.onMapPrintScreenListener;
import com.amap.api.maps.CustomRenderer;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.model.ArcOptions;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.LatLngBounds.Builder;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.MyTrafficStyle;
import com.amap.api.maps.model.NavigateArrowOptions;
import com.amap.api.maps.model.Poi;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.Text;
import com.amap.api.maps.model.TextOptions;
import com.amap.api.maps.model.TileOverlay;
import com.amap.api.maps.model.TileOverlayOptions;
import com.amap.api.services.core.AMapException;
import com.autonavi.amap.mapcore.DPoint;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.GLMapResManager;
import com.autonavi.amap.mapcore.GLMapResManager.MapViewMode;
import com.autonavi.amap.mapcore.GLMapResManager.MapViewModeState;
import com.autonavi.amap.mapcore.GLMapResManager.MapViewTime;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapCore;
import com.autonavi.amap.mapcore.MapProjection;
import com.autonavi.amap.mapcore.SelectedMapPoi;
import com.autonavi.amap.mapcore.VMapDataCache;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.google.android.gms.location.places.Place;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class AMapDelegateImp implements Renderer, ab {
    private static final double aH = Math.log(2.0d);
    private CopyOnWriteArrayList<Integer> A = new CopyOnWriteArrayList();
    private CopyOnWriteArrayList<Integer> B = new CopyOnWriteArrayList();
    private MapViewTime C = MapViewTime.DAY;
    private MapViewMode D = MapViewMode.NORAML;
    private MapViewModeState E = MapViewModeState.NORMAL;
    private int F = 1;
    private MapCore G;
    private Context H;
    private a I = null;
    private MapProjection J;
    private GestureDetector K;
    private ScaleGestureDetector L;
    private com.amap.api.mapcore.util.d M;
    private SurfaceHolder N = null;
    private az O;
    private br P;
    private as Q;
    private r R;
    private bj S;
    private n T;
    private ar U;
    private OnMyLocationChangeListener V;
    private OnMarkerClickListener W;
    private OnPolylineClickListener X;
    private OnMarkerDragListener Y;
    private OnMapLoadedListener Z;
    float a = 14.0f;
    private onMapPrintScreenListener aA = null;
    private OnMapScreenShotListener aB = null;
    private Handler aC = new Handler();
    private com.amap.api.mapcore.util.f aD = null;
    private p aE = null;
    private Timer aF;
    private TimeChangedReceiver aG = null;
    private boolean aI = true;
    private boolean aJ = false;
    private boolean aK = false;
    private boolean aL = false;
    private boolean aM = false;
    private boolean aN = true;
    private boolean aO = false;
    private boolean aP = false;
    private boolean aQ = false;
    private Boolean aR = Boolean.valueOf(false);
    private boolean aS = false;
    private boolean aT = true;
    private boolean aU = false;
    private Handler aV = new Handler();
    private int aW = 0;
    private t aX = new t();
    private boolean aY;
    private boolean aZ;
    private OnCameraChangeListener aa;
    private OnMapClickListener ab;
    private OnMapTouchListener ac;
    private OnPOIClickListener ad;
    private OnMapLongClickListener ae;
    private OnInfoWindowClickListener af;
    private OnIndoorBuildingActiveListener ag;
    private InfoWindowAdapter ah;
    private InfoWindowAdapter ai;
    private View aj;
    private ah ak;
    private bh al;
    private am am;
    private aq an;
    private LocationSource ao;
    private Rect ap = new Rect();
    private l aq;
    private com.amap.api.mapcore.util.c ar;
    private bb as;
    private o at;
    private int au = 0;
    private int av = 0;
    private CancelableCallback aw = null;
    private int ax = 0;
    private Drawable ay = null;
    private Location az;
    float b = 0.0f;
    private volatile boolean ba = false;
    private volatile boolean bb = false;
    private Handler bc = new Handler();
    private Runnable bd = new i(this);
    private volatile boolean be = false;
    private boolean bf = false;
    private boolean bg = false;
    private boolean bh = false;
    private Marker bi = null;
    private ah bj = null;
    private boolean bk = false;
    private boolean bl = false;
    private boolean bm = false;
    private int bn = 0;
    private boolean bo = false;
    private Thread bp = new c(this);
    private LatLngBounds bq = null;
    private boolean br = false;
    private boolean bs = false;
    private int bt;
    private int bu;
    private Handler bv = new e(this);
    private Runnable bw = new f(this);
    private Runnable bx = new g(this);
    private a by = new h(this);
    float c = 0.0f;
    public aw d;
    av e = new av(this);
    bs f;
    bo g;
    w h = null;
    GLMapResManager i = null;
    ae j = null;
    Runnable k;
    final Handler l = new d(this);
    CustomRenderer m;
    private int n = -1;
    private int o = -1;
    private int p = 40;
    private Bitmap q = null;
    private Bitmap r = null;
    private int s = 221072480;
    private int t = 101633521;
    private boolean u = false;
    private boolean v = true;
    private boolean w = true;
    private boolean x = false;
    private MyTrafficStyle y = null;
    private float z = 1.0f;

    public class TimeChangedReceiver extends BroadcastReceiver {
        final /* synthetic */ AMapDelegateImp a;

        public TimeChangedReceiver(AMapDelegateImp aMapDelegateImp) {
            this.a = aMapDelegateImp;
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.DATE_CHANGED".equals(action)) {
            }
            if ("android.intent.action.TIME_SET".equals(action)) {
                this.a.l.sendEmptyMessage(22);
            }
        }
    }

    private static abstract class a implements Runnable {
        boolean b;
        boolean c;
        MapViewMode d;
        MapViewTime e;
        MapViewModeState f;

        private a() {
            this.b = false;
            this.c = false;
        }

        public void run() {
            this.b = false;
        }
    }

    private class b implements com.amap.api.mapcore.util.c.a {
        Float a;
        Float b;
        IPoint c;
        float d;
        p e;
        final /* synthetic */ AMapDelegateImp f;
        private float g;
        private float h;
        private float i;
        private float j;
        private float k;

        private b(AMapDelegateImp aMapDelegateImp) {
            this.f = aMapDelegateImp;
            this.a = null;
            this.b = null;
            this.c = new IPoint();
            this.d = 0.0f;
            this.e = p.a();
        }

        public void a(float f, float f2, float f3, float f4, float f5) {
            this.g = f2;
            this.i = f3;
            this.h = f4;
            this.j = f5;
            this.k = (this.j - this.i) / (this.h - this.g);
            this.a = null;
            this.b = null;
            if (this.f.bs) {
                this.e.a = a.changeGeoCenterZoomTiltBearing;
                this.f.a(this.f.bt, this.f.bu, this.c);
                this.e.o = this.c;
                this.e.n = this.f.bs;
            } else {
                this.e.a = a.changeTilt;
            }
            this.e.d = this.f.J.getMapZoomer();
            this.e.g = this.f.J.getMapAngle();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean a(MotionEvent motionEvent, float f, float f2, float f3, float f4) {
            try {
                if (!this.f.an.h()) {
                    return true;
                }
                if (this.f.bg || this.f.bl) {
                    return true;
                }
                if (this.b == null) {
                    this.b = Float.valueOf(f4);
                }
                if (this.a == null) {
                    this.a = Float.valueOf(f2);
                }
                float f5 = this.i - f2;
                float f6 = this.j - f4;
                float f7 = this.g - f;
                float f8 = this.h - f3;
                if (((double) Math.abs(this.k - ((f4 - f2) / (f3 - f)))) < 0.2d) {
                    if (f5 > 0.0f) {
                    }
                    if (f5 < 0.0f) {
                    }
                }
                return false;
            } catch (RemoteException e) {
                e.printStackTrace();
                return true;
            }
        }

        public void a() {
            if (!this.f.bg) {
                try {
                    if (!this.f.an.g()) {
                        return;
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                try {
                    this.f.b(p.c());
                } catch (Throwable e2) {
                    ce.a(e2, "AMapDelegateImpGLSurfaceView", "onMultiTouchSingleTap");
                    e2.printStackTrace();
                }
            }
        }
    }

    private class c implements OnDoubleTapListener {
        final /* synthetic */ AMapDelegateImp a;

        private c(AMapDelegateImp aMapDelegateImp) {
            this.a = aMapDelegateImp;
        }

        public boolean onDoubleTap(MotionEvent motionEvent) {
            try {
                if (!this.a.an.g()) {
                    return true;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if (this.a.bn > 1) {
                return true;
            }
            this.a.bm = true;
            if (this.a.J.getMapZoomer() == this.a.s()) {
                return true;
            }
            try {
                this.a.b(p.a(1.0f, new Point((int) motionEvent.getX(), (int) motionEvent.getY())));
            } catch (Throwable e2) {
                ce.a(e2, "AMapDelegateImpGLSurfaceView", "onDoubleTap");
                e2.printStackTrace();
            }
            return true;
        }

        public boolean onDoubleTapEvent(MotionEvent motionEvent) {
            return false;
        }

        public boolean onSingleTapConfirmed(final MotionEvent motionEvent) {
            this.a.bk = false;
            if (this.a.bo) {
                this.a.bo = false;
                return true;
            }
            try {
                if (this.a.aj != null) {
                    if (this.a.d.a(new Rect(this.a.aj.getLeft(), this.a.aj.getTop(), this.a.aj.getRight(), this.a.aj.getBottom()), (int) motionEvent.getX(), (int) motionEvent.getY())) {
                        if (this.a.af != null) {
                            ah d = this.a.d.d();
                            if (!d.o()) {
                                return true;
                            }
                            this.a.af.onInfoWindowClick(new Marker(d));
                        }
                        return true;
                    }
                }
                if (this.a.d.b(motionEvent)) {
                    final ah d2 = this.a.d.d();
                    if (d2 == null || !d2.o()) {
                        return true;
                    }
                    Marker marker = new Marker(d2);
                    if (this.a.W != null) {
                        if (!this.a.W.onMarkerClick(marker) && this.a.d.a() > 0) {
                            this.a.aC.postDelayed(new Runnable(this) {
                                final /* synthetic */ c b;

                                public void run() {
                                    try {
                                        this.b.a.a(d2);
                                    } catch (Throwable th) {
                                        ce.a(th, "AMapDelegateImpGLSurfaceView", "onSingleTapUp showInfoWindow");
                                        th.printStackTrace();
                                    }
                                }
                            }, 20);
                            if (!d2.F()) {
                                LatLng g = d2.g();
                                if (g != null) {
                                    IPoint iPoint = new IPoint();
                                    this.a.a(g.latitude, g.longitude, iPoint);
                                    this.a.a(p.a(iPoint));
                                }
                            }
                        } else {
                            this.a.d.c(d2);
                            return true;
                        }
                    }
                    this.a.d.c(d2);
                    return true;
                }
                DPoint dPoint;
                if (this.a.ab != null) {
                    dPoint = new DPoint();
                    this.a.a((int) motionEvent.getX(), (int) motionEvent.getY(), dPoint);
                    this.a.ab.onMapClick(new LatLng(dPoint.y, dPoint.x));
                }
                if (this.a.X != null) {
                    dPoint = new DPoint();
                    this.a.a((int) motionEvent.getX(), (int) motionEvent.getY(), dPoint);
                    LatLng latLng = new LatLng(dPoint.y, dPoint.x);
                    if (latLng != null) {
                        aj a = this.a.h.a(latLng);
                        if (a != null) {
                            this.a.X.onPolylineClick(new Polyline((al) a));
                            return true;
                        }
                    }
                }
                this.a.a(new Runnable(this) {
                    final /* synthetic */ c b;

                    public void run() {
                        final Poi a = this.b.a.a((int) motionEvent.getX(), (int) motionEvent.getY(), 25);
                        if (this.b.a.ad != null && a != null) {
                            this.b.a.l.post(new Runnable(this) {
                                final /* synthetic */ AnonymousClass2 b;

                                public void run() {
                                    this.b.b.a.ad.onPOIClick(a);
                                }
                            });
                        }
                    }
                });
                return true;
            } catch (Throwable e) {
                ce.a(e, "AMapDelegateImpGLSurfaceView", "onSingleTapUp moveCamera");
                e.printStackTrace();
            } catch (Throwable e2) {
                ce.a(e2, "AMapDelegateImpGLSurfaceView", "onSingleTapUp");
                e2.printStackTrace();
                return true;
            }
        }
    }

    private class d implements OnGestureListener {
        FPoint a;
        IPoint b;
        IPoint c;
        p d;
        final /* synthetic */ AMapDelegateImp e;

        private d(AMapDelegateImp aMapDelegateImp) {
            this.e = aMapDelegateImp;
            this.a = new FPoint();
            this.b = new IPoint();
            this.c = new IPoint();
            this.d = p.a(this.c);
        }

        public boolean onDown(MotionEvent motionEvent) {
            this.e.bk = false;
            if (!this.e.bm) {
                try {
                    this.e.u();
                } catch (Throwable e) {
                    ce.a(e, "AMapDelegateImpGLSurfaceView", "onDown");
                    e.printStackTrace();
                }
            }
            this.e.bm = false;
            this.e.bn = 0;
            this.a.x = motionEvent.getX();
            this.a.y = motionEvent.getY();
            this.e.a((int) this.a.x, (int) this.a.y, this.b);
            return true;
        }

        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
            this.e.bk = false;
            try {
                if (!this.e.an.f()) {
                    return true;
                }
            } catch (Throwable e) {
                ce.a(e, "AMapDelegateImpGLSurfaceView", "onFling");
                e.printStackTrace();
            }
            if (!this.e.ar.a()) {
                if (motionEvent.getEventTime() - this.e.ar.b() >= 30) {
                    int l = this.e.l();
                    int m = this.e.m();
                    int i = l * 2;
                    int i2 = m * 2;
                    this.e.au = l / 2;
                    this.e.av = m / 2;
                    this.e.aw = null;
                    if (!(this.e.aj == null || this.e.ak == null || this.e.ak.F())) {
                        this.e.aT = false;
                        if (this.e.al != null) {
                            this.e.al.c(true);
                        }
                    }
                    this.e.at.a(this.e.au, this.e.av, (((int) (-f)) * 3) / 5, (((int) (-f2)) * 3) / 5, -i, i, -i2, i2);
                    if (this.e.g != null) {
                        this.e.g.b(true);
                    }
                    return true;
                }
            }
            return true;
        }

        public void onLongPress(MotionEvent motionEvent) {
            this.e.bk = false;
            this.e.bj = this.e.d.a(motionEvent);
            if (this.e.Y != null && this.e.bj != null && this.e.bj.k()) {
                this.e.bi = new Marker(this.e.bj);
                LatLng position = this.e.bi.getPosition();
                LatLng g = this.e.bj.g();
                IPoint iPoint = new IPoint();
                this.e.b(g.latitude, g.longitude, iPoint);
                iPoint.y -= 60;
                DPoint dPoint = new DPoint();
                this.e.a(iPoint.x, iPoint.y, dPoint);
                this.e.bi.setPosition(new LatLng((position.latitude + dPoint.y) - g.latitude, (dPoint.x + position.longitude) - g.longitude));
                this.e.d.c(this.e.bj);
                this.e.Y.onMarkerDragStart(this.e.bi);
                this.e.bh = true;
            } else if (this.e.ae != null) {
                DPoint dPoint2 = new DPoint();
                this.e.a((int) motionEvent.getX(), (int) motionEvent.getY(), dPoint2);
                this.e.ae.onMapLongClick(new LatLng(dPoint2.y, dPoint2.x));
                this.e.bo = true;
            }
        }

        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
            this.e.bk = true;
            if (this.e.at.a() || this.e.at.j() != 1) {
                if (!this.e.ar.a()) {
                    if (motionEvent2.getEventTime() - this.e.ar.b() >= 30) {
                        if (motionEvent2.getPointerCount() < 2) {
                            try {
                                if (!this.e.an.f()) {
                                    this.e.bk = false;
                                    return true;
                                } else if (this.e.bn <= 1) {
                                    if (!(this.e.aj == null || this.e.ak == null || this.e.ak.F() || this.e.al == null)) {
                                        this.e.al.c(true);
                                    }
                                    IPoint iPoint = new IPoint();
                                    this.e.a((int) motionEvent2.getX(), (int) motionEvent2.getY(), iPoint);
                                    int i = this.b.x - iPoint.x;
                                    int i2 = this.b.y - iPoint.y;
                                    IPoint iPoint2 = new IPoint();
                                    this.e.J.getGeoCenter(iPoint2);
                                    this.c.x = i + iPoint2.x;
                                    this.c.y = i2 + iPoint2.y;
                                    this.d.o = this.c;
                                    this.e.e.a(this.d);
                                } else {
                                    this.e.bk = false;
                                    return true;
                                }
                            } catch (Throwable th) {
                                ce.a(th, "AMapDelegateImpGLSurfaceView", "onScroll");
                                th.printStackTrace();
                            }
                        } else {
                            this.e.bk = false;
                        }
                        return true;
                    }
                }
            }
            this.e.bk = false;
            return true;
        }

        public void onShowPress(MotionEvent motionEvent) {
        }

        public boolean onSingleTapUp(MotionEvent motionEvent) {
            return false;
        }
    }

    private class e implements com.amap.api.mapcore.ar.a {
        final /* synthetic */ AMapDelegateImp a;

        private e(AMapDelegateImp aMapDelegateImp) {
            this.a = aMapDelegateImp;
        }

        public void a(int i) {
            if (this.a.aD != null) {
                this.a.aD.activeFloorIndex = this.a.aD.floor_indexs[i];
                this.a.aD.activeFloorName = this.a.aD.floor_names[i];
                try {
                    this.a.b(this.a.aD);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class f implements com.amap.api.mapcore.util.d.a {
        float a;
        float b;
        IPoint c;
        p d;
        final /* synthetic */ AMapDelegateImp e;

        private f(AMapDelegateImp aMapDelegateImp) {
            this.e = aMapDelegateImp;
            this.a = 0.0f;
            this.b = 0.0f;
            this.c = new IPoint();
            this.d = p.a();
        }

        public boolean a(com.amap.api.mapcore.util.d dVar) {
            boolean z = false;
            if (this.e.bf) {
                return false;
            }
            float b = dVar.b();
            this.a += b;
            if (!this.e.bl) {
                if (Math.abs(this.a) > 30.0f) {
                    z = true;
                }
                if (!z) {
                    if (Math.abs(this.a) > 350.0f) {
                    }
                    return true;
                }
            }
            this.e.bl = true;
            this.b = this.e.J.getMapAngle() + b;
            this.d.g = this.b;
            this.e.e.a(this.d);
            this.a = 0.0f;
            return true;
        }

        public boolean b(com.amap.api.mapcore.util.d dVar) {
            try {
                if (!this.e.an.i()) {
                    return false;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if (this.e.bs) {
                this.d.n = this.e.bs;
                this.d.a = a.changeBearingGeoCenter;
                this.e.a(this.e.bt, this.e.bu, this.c);
                this.d.o = this.c;
            } else {
                this.d.a = a.changeBearing;
            }
            this.e.bl = false;
            this.a = 0.0f;
            this.e.bn = 2;
            if (this.e.bf || ((float) this.e.n()) / 8.0f >= dVar.c()) {
                return false;
            }
            return true;
        }

        public void c(com.amap.api.mapcore.util.d dVar) {
            this.a = 0.0f;
            if (this.e.bl) {
                this.e.bl = false;
                p a = p.a();
                a.p = true;
                this.e.e.a(a);
            }
            this.e.al();
        }
    }

    private class g implements OnScaleGestureListener {
        p a;
        final /* synthetic */ AMapDelegateImp b;
        private float c;
        private IPoint d;

        private g(AMapDelegateImp aMapDelegateImp) {
            this.b = aMapDelegateImp;
            this.c = 0.0f;
            this.d = new IPoint();
            this.a = p.a();
        }

        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            if (this.b.bf) {
                return false;
            }
            float scaleFactor = scaleGestureDetector.getScaleFactor();
            if (!this.b.bg) {
                if (!(((double) scaleFactor) > 1.08d)) {
                    if (((double) scaleFactor) < 0.92d) {
                    }
                    return false;
                }
            }
            this.b.bg = true;
            this.a.d = bj.a(((float) (Math.log((double) scaleFactor) / AMapDelegateImp.aH)) + this.c);
            this.b.e.a(this.a);
            return false;
        }

        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            try {
                if (!this.b.an.g() || this.b.bn < 2) {
                    return false;
                }
            } catch (Throwable e) {
                ce.a(e, "AMapDelegateImpGLSurfaceView", "onScaleBegin");
                e.printStackTrace();
            }
            this.b.bn = 2;
            if (this.b.bf) {
                return false;
            }
            if (this.b.bs) {
                this.a.n = this.b.bs;
                this.a.a = a.changeGeoCenterZoom;
                this.b.a(this.b.bt, this.b.bu, this.d);
                this.a.o = this.d;
            } else {
                this.a.a = a.zoomTo;
            }
            this.b.bg = false;
            this.c = this.b.J.getMapZoomer();
            return true;
        }

        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
            this.c = 0.0f;
            if (this.b.bg) {
                this.b.bg = false;
                p a = p.a();
                a.p = true;
                this.b.e.a(a);
            }
            this.b.al();
        }
    }

    private class h extends TimerTask {
        AMapDelegateImp a;
        final /* synthetic */ AMapDelegateImp b;

        public h(AMapDelegateImp aMapDelegateImp, AMapDelegateImp aMapDelegateImp2) {
            this.b = aMapDelegateImp;
            this.a = aMapDelegateImp2;
        }

        public void run() {
            if (!this.b.ba || this.b.bb || !this.b.h.d()) {
                this.b.j.requestRender();
            } else if (!this.b.d.c()) {
                this.b.j.requestRender();
            }
        }
    }

    private class i implements Runnable {
        final /* synthetic */ AMapDelegateImp a;
        private Context b;
        private OnCacheRemoveListener c;

        public i(AMapDelegateImp aMapDelegateImp, Context context, OnCacheRemoveListener onCacheRemoveListener) {
            this.a = aMapDelegateImp;
            this.b = context;
            this.c = onCacheRemoveListener;
        }

        public void run() {
            Throwable th;
            Throwable th2;
            boolean z;
            boolean z2 = true;
            try {
                boolean z3;
                Context applicationContext = this.b.getApplicationContext();
                String b = bj.b(applicationContext);
                String a = bj.a(applicationContext);
                boolean z4 = this.a.a(new File(b));
                if (z4) {
                    try {
                        if (this.a.a(new File(a))) {
                            z3 = true;
                            this.a.G.setParameter(2601, 1, 0, 0, 0);
                            if (this.c == null) {
                                this.c.onRemoveCacheFinish(z3);
                            }
                        }
                    } catch (Throwable th3) {
                        th2 = th3;
                        z = z4;
                        this.a.G.setParameter(2601, 1, 0, 0, 0);
                        if (this.c != null) {
                            this.c.onRemoveCacheFinish(z);
                        }
                        throw th2;
                    }
                }
                z3 = false;
                try {
                    this.a.G.setParameter(2601, 1, 0, 0, 0);
                    if (this.c == null) {
                        this.c.onRemoveCacheFinish(z3);
                    }
                } catch (Throwable th4) {
                    th4.printStackTrace();
                }
            } catch (Throwable th5) {
                th4 = th5;
                ce.a(th4, "AMapDelegateImpGLSurfaceView", "RemoveCacheRunnable");
                this.a.G.setParameter(2601, 1, 0, 0, 0);
                if (this.c != null) {
                    this.c.onRemoveCacheFinish(false);
                }
            }
        }

        public boolean equals(Object obj) {
            return obj instanceof i;
        }
    }

    public MapCore a() {
        return this.G;
    }

    public int b() {
        return this.n;
    }

    public MapProjection c() {
        if (this.J == null) {
            this.J = this.G.getMapstate();
        }
        return this.J;
    }

    public void a(GL10 gl10) {
        int i = 0;
        if (!this.aU) {
            int[] iArr = new int[VTMCDataCache.MAXSIZE];
            this.A.clear();
            gl10.glGenTextures(VTMCDataCache.MAXSIZE, iArr, 0);
            while (i < iArr.length) {
                this.A.add(Integer.valueOf(iArr[i]));
                i++;
            }
            this.aU = true;
        }
    }

    public AMapDelegateImp(ae aeVar, Context context, AttributeSet attributeSet) {
        s.c = bl.c(context);
        this.j = aeVar;
        this.H = context;
        this.an = new bp(this);
        this.G = new MapCore(this.H);
        this.I = new a(this);
        this.G.setMapCallback(this.I);
        aeVar.setRenderer(this);
        ad();
        this.i = new GLMapResManager(this, context);
        this.am = new bi(this);
        this.aq = new l(this);
        this.K = new GestureDetector(context, new d());
        this.K.setOnDoubleTapListener(new c());
        this.K.setIsLongpressEnabled(true);
        this.L = new ScaleGestureDetector(context, new g());
        this.M = new com.amap.api.mapcore.util.d(context, new f());
        this.ar = new com.amap.api.mapcore.util.c(context, new b());
        this.O = new az(this, context, this) {
            final /* synthetic */ AMapDelegateImp a;

            protected void a() {
                super.a();
                this.a.aC.removeCallbacks(this.a.bx);
                this.a.aC.post(this.a.bw);
            }
        };
        this.h = new w(this);
        this.P = new br(this.H, this);
        this.S = new bj(this.H, this);
        this.T = new n(this.H);
        this.U = new ar(this.H);
        this.g = new bo(this.H, this);
        this.f = new bs(this.H, this);
        this.Q = new as(this.H, this.e, this);
        this.R = new r(this.H, this.e, this);
        this.d = new aw(this.H, attributeSet, this);
        LayoutParams layoutParams = new LayoutParams(-1, -1);
        this.O.addView((View) this.j, 0, layoutParams);
        this.O.addView(this.T, 1, layoutParams);
        this.O.addView(this.d, new com.amap.api.mapcore.az.a(layoutParams));
        this.O.addView(this.P, layoutParams);
        this.O.addView(this.S, layoutParams);
        this.O.addView(this.g, layoutParams);
        this.O.addView(this.U, new LayoutParams(-2, -2));
        this.U.a(new e());
        this.O.addView(this.f, new com.amap.api.mapcore.az.a(-2, -2, new FPoint(0.0f, 0.0f), 0, 0, 83));
        this.O.addView(this.Q, new com.amap.api.mapcore.az.a(-2, -2, new FPoint(0.0f, 0.0f), 0, 0, 83));
        try {
            if (!this.an.e()) {
                this.Q.setVisibility(8);
            }
        } catch (Throwable e) {
            ce.a(e, "AMapDelegateImpGLSurfaceView", "locationView gone");
            e.printStackTrace();
        }
        this.O.addView(this.R, new com.amap.api.mapcore.az.a(-2, -2, new FPoint(0.0f, 0.0f), 0, 0, 51));
        this.R.setVisibility(8);
        this.at = new o(context);
        this.as = new bb(this, context);
        this.ai = new InfoWindowAdapter(this) {
            final /* synthetic */ AMapDelegateImp a;

            {
                this.a = r1;
            }

            public View getInfoWindow(Marker marker) {
                return null;
            }

            public View getInfoContents(Marker marker) {
                return null;
            }
        };
        this.ah = this.ai;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.TIME_SET");
        intentFilter.addAction("android.intent.action.DATE_CHANGED");
        this.aG = new TimeChangedReceiver(this);
        this.H.registerReceiver(this.aG, intentFilter);
        this.f.setId(AutoTestConfig.ZoomControllerViewId);
        this.S.setId(AutoTestConfig.ScaleControlsViewId);
        this.Q.setId(AutoTestConfig.MyLocationViewId);
        this.R.setId(AutoTestConfig.CompassViewId);
    }

    public void a(v vVar) {
        this.aX.a(vVar);
    }

    public void a(OnMyLocationChangeListener onMyLocationChangeListener) {
        this.V = onMyLocationChangeListener;
    }

    public void d() {
        this.aZ = false;
    }

    public void e() {
        this.aZ = true;
        if (this.e != null) {
            this.e.e();
        }
    }

    public void f() {
        if (this.aW != 1) {
            this.aW = 1;
            if (!this.aJ) {
                a(new Runnable(this) {
                    final /* synthetic */ AMapDelegateImp a;

                    {
                        this.a = r1;
                    }

                    public void run() {
                        this.a.ad();
                        this.a.ah();
                        if (this.a.I != null) {
                            this.a.I.onResume(this.a.G);
                            this.a.f(false);
                        }
                        if (this.a.g != null) {
                            this.a.g.d();
                        }
                        if (this.a.as != null) {
                            this.a.as.a();
                        }
                        this.a.aY = false;
                    }
                });
            }
            if (this.j instanceof j) {
                ((j) this.j).onResume();
            } else {
                ((k) this.j).c();
            }
        }
    }

    public void g() {
        if (this.aW == 1) {
            this.aW = -1;
            this.aY = true;
            this.aM = false;
            if (this.T != null) {
                this.T.a(true);
            }
            if (this.I != null) {
                this.I.destoryMap(this.G);
            }
            ai();
            IPoint iPoint = new IPoint();
            this.J.getGeoCenter(iPoint);
            this.s = iPoint.x;
            this.t = iPoint.y;
            this.a = this.J.getMapZoomer();
            this.c = this.J.getMapAngle();
            this.b = this.J.getCameraHeaderAngle();
            if (this.j instanceof j) {
                ((j) this.j).onPause();
            } else {
                ((k) this.j).b();
            }
            ae();
        }
    }

    private void ad() {
        if (!this.aJ) {
            this.G.newMap();
            this.I.onResume(this.G);
            this.J = this.G.getMapstate();
            this.J.setGeoCenter(this.s, this.t);
            this.J.setMapAngle(this.c);
            this.J.setMapZoomer(this.a);
            this.J.setCameraHeaderAngle(this.b);
            this.G.setMapstate(this.J);
            this.aJ = true;
            af();
            this.j.setRenderMode(0);
        }
    }

    private void ae() {
        a(new Runnable(this) {
            final /* synthetic */ AMapDelegateImp a;

            {
                this.a = r1;
            }

            public void run() {
                if (this.a.aJ) {
                    this.a.C = MapViewTime.DAY;
                    this.a.D = MapViewMode.NORAML;
                    this.a.E = MapViewModeState.NORMAL;
                    try {
                        this.a.G.destroy();
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                    VMapDataCache.getInstance().reset();
                    this.a.aJ = false;
                }
            }
        });
    }

    private void af() {
        try {
            k(this.u);
            l(this.v);
            j(this.w);
            i(this.x);
            a(this.y);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void a(MyLocationStyle myLocationStyle) {
        if (this.as != null) {
            this.as.a(myLocationStyle);
        }
    }

    public void a(int i) {
        if (this.as != null) {
            this.as.a(i);
        }
    }

    public void a(float f) throws RemoteException {
        if (this.as != null) {
            this.as.a(f);
        }
    }

    public void a(Location location) throws RemoteException {
        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            try {
                if (this.aI && this.ao != null) {
                    if (this.as != null) {
                        if (this.az != null) {
                            this.as.a(location);
                            if (this.V != null) {
                                if (this.az != null && this.az.getBearing() == location.getBearing() && this.az.getAccuracy() == location.getAccuracy() && this.az.getLatitude() == location.getLatitude()) {
                                    if (this.az.getLongitude() != location.getLongitude()) {
                                    }
                                }
                                this.V.onMyLocationChange(location);
                            }
                            this.az = new Location(location);
                            f(false);
                            return;
                        }
                    }
                    if (this.as == null) {
                        this.as = new bb(this, this.H);
                    }
                    a(p.a(latLng, this.J.getMapZoomer()));
                    this.as.a(location);
                    if (this.V != null) {
                        if (this.az.getLongitude() != location.getLongitude()) {
                            this.V.onMyLocationChange(location);
                        }
                    }
                    this.az = new Location(location);
                    f(false);
                    return;
                }
                this.as.b();
                this.as = null;
            } catch (Throwable e) {
                ce.a(e, "AMapDelegateImpGLSurfaceView", "showMyLocationOverlay");
                e.printStackTrace();
            }
        }
    }

    public void a(boolean z) {
        if (this.f != null) {
            this.f.a(z);
        }
    }

    public void b(boolean z) {
        if (this.U != null && z && ag()) {
            this.U.a(true);
        }
    }

    private boolean ag() {
        if (!(this.J.getMapZoomer() < 17.0f || this.aD == null || this.aD.g == null)) {
            IPoint iPoint = new IPoint();
            b(this.aD.g.x, this.aD.g.y, iPoint);
            if (this.ap.contains(iPoint.x, iPoint.y)) {
                return true;
            }
        }
        return false;
    }

    public void h() {
        this.aR = Boolean.valueOf(true);
        try {
            ai();
            if (this.r != null) {
                this.r.recycle();
                this.r = null;
            }
            if (this.q != null) {
                this.q.recycle();
                this.q = null;
            }
            if (!(this.l == null || this.k == null)) {
                this.l.removeCallbacks(this.k);
                this.k = null;
            }
            if (this.bc != null) {
                this.bc.removeCallbacks(this.bd);
            }
            if (this.aG != null) {
                this.H.unregisterReceiver(this.aG);
            }
            if (this.f != null) {
                this.f.a();
            }
            if (this.S != null) {
                this.S.a();
            }
            if (this.P != null) {
                this.P.a();
            }
            if (this.Q != null) {
                this.Q.a();
            }
            if (this.R != null) {
                this.R.a();
            }
            if (this.g != null) {
                this.g.b();
                this.g.e();
            }
            if (this.h != null) {
                this.h.a();
            }
            if (this.d != null) {
                this.d.e();
            }
            if (this.U != null) {
                this.U.b();
            }
            if (this.bp != null) {
                this.bp.interrupt();
                this.bp = null;
            }
            if (this.I != null) {
                this.I.OnMapDestory(this.G);
                this.G.setMapCallback(null);
                this.I = null;
            }
            E();
            bj.a(this.ay);
            if (this.A != null) {
                this.A.clear();
            }
            if (this.B != null) {
                this.B.clear();
            }
            if (this.G != null) {
                a(new Runnable(this) {
                    final /* synthetic */ AMapDelegateImp a;

                    {
                        this.a = r1;
                    }

                    public void run() {
                        try {
                            this.a.G.destroy();
                            this.a.G = null;
                        } catch (Throwable th) {
                            th.printStackTrace();
                        }
                    }
                });
                Thread.sleep(200);
            }
            if (this.O != null) {
                this.O.removeAllViews();
                this.O = null;
            }
            this.y = null;
            ce.b();
        } catch (Throwable th) {
            ce.a(th, "AMapDelegateImpGLSurfaceView", "destroy");
            th.printStackTrace();
        }
    }

    public void c(boolean z) {
        if (this.Q != null) {
            if (z) {
                this.Q.setVisibility(0);
            } else {
                this.Q.setVisibility(8);
            }
        }
    }

    public void d(boolean z) {
        if (this.R != null) {
            this.R.a(z);
        }
    }

    void i() {
        this.l.obtainMessage(14).sendToTarget();
    }

    public void e(boolean z) {
        if (this.S != null) {
            this.S.a(z);
        }
    }

    void j() {
        this.l.post(new Runnable(this) {
            final /* synthetic */ AMapDelegateImp a;

            {
                this.a = r1;
            }

            public void run() {
                this.a.S.b();
            }
        });
    }

    public boolean a(String str) throws RemoteException {
        f(false);
        return this.h.c(str);
    }

    public synchronized void f(boolean z) {
        if (!z) {
            this.bb = false;
            this.bc.removeCallbacks(this.bd);
            this.ba = false;
        } else if (!(this.ba || this.bb)) {
            this.bb = true;
            this.bc.postDelayed(this.bd, 6000);
        }
    }

    public void onDrawFrame(GL10 gl10) {
        int i = 0;
        try {
            if (this.aJ) {
                gl10.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
                gl10.glClear(16640);
                this.G.setGL(gl10);
                this.G.drawFrame(gl10);
                a(gl10);
                this.g.a(gl10);
                this.h.a(gl10, false, this.ax);
                this.d.a(gl10);
                this.aX.a(gl10);
                if (this.al != null) {
                    this.al.a(gl10);
                }
                if (this.aS) {
                    if (this.G.canStopRenderMap()) {
                        i = 1;
                    }
                    Message obtainMessage = this.l.obtainMessage(16, a(0, 0, n(), o(), gl10));
                    obtainMessage.arg1 = i;
                    obtainMessage.sendToTarget();
                    this.aS = false;
                }
                if (!this.at.a()) {
                    this.l.sendEmptyMessage(13);
                }
                if (this.T != null) {
                    i = this.T.getVisibility();
                    n nVar = this.T;
                    if (i != 8) {
                        if (!this.aK) {
                            this.l.sendEmptyMessage(11);
                            this.aK = true;
                        }
                        this.aM = true;
                        this.l.post(new Runnable(this) {
                            final /* synthetic */ AMapDelegateImp a;

                            {
                                this.a = r1;
                            }

                            public void run() {
                                if (!this.a.aY) {
                                    try {
                                        this.a.b(this.a.F);
                                        if (this.a.aD != null) {
                                            this.a.b(this.a.aD);
                                        }
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                    this.a.T.a(false);
                                }
                            }
                        });
                        return;
                    }
                    return;
                }
                return;
            }
            gl10.glClearColor(0.9453125f, 0.93359f, 0.9101f, 1.0f);
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public Rect k() {
        return this.ap;
    }

    public int l() {
        return this.ap.width();
    }

    public int m() {
        return this.ap.height();
    }

    public int n() {
        return this.j.getWidth();
    }

    public int o() {
        return this.j.getHeight();
    }

    public void a(MyTrafficStyle myTrafficStyle) {
        if (this.aJ && myTrafficStyle != null) {
            this.y = myTrafficStyle;
            this.G.setParameter(AMapException.CODE_AMAP_CLIENT_USERID_ILLEGAL, 1, 1, 1, 1);
            this.G.setParameter(AMapException.CODE_AMAP_CLIENT_NEARBY_NULL_RESULT, myTrafficStyle.getSmoothColor(), myTrafficStyle.getSlowColor(), myTrafficStyle.getCongestedColor(), myTrafficStyle.getSeriousCongestedColor());
        }
    }

    private synchronized void ah() {
        if (this.aF != null) {
            ai();
        }
        if (this.aF == null) {
            this.aF = new Timer();
        }
        this.aF.schedule(new h(this, this), 0, (long) (1000 / this.p));
    }

    private synchronized void ai() {
        if (this.aF != null) {
            this.aF.cancel();
            this.aF = null;
        }
    }

    private synchronized void aj() {
        try {
            if (!this.be) {
                this.i.setStyleData();
                this.i.setIconsData(true);
                this.i.setTrafficTexture(true);
                this.i.setOtherMapTexture(true);
                this.i.setRoadGuideTexture(true);
                this.i.setBkTexture(true);
                this.be = true;
            }
        } catch (Throwable th) {
            ce.a(th, "AMapDelegateImpGLSurfaceView", "setInternaltexture");
            th.printStackTrace();
        }
    }

    public int p() {
        return this.o;
    }

    public void q() {
        try {
            if (this.aT && this.aj != null && this.ak != null) {
                com.amap.api.mapcore.az.a aVar = (com.amap.api.mapcore.az.a) this.aj.getLayoutParams();
                if (aVar != null) {
                    this.ak.d();
                    int D = this.ak.D() + this.ak.B();
                    int E = (this.ak.E() + this.ak.C()) + 2;
                    aVar.a = this.ak.f();
                    aVar.b = D;
                    aVar.c = E;
                    if (this.al != null) {
                        this.al.a(this.ak.f());
                        this.al.b(D, E);
                    }
                }
                this.O.onLayout(false, 0, 0, 0, 0);
                f(false);
            }
        } catch (Throwable th) {
            ce.a(th, "AMapDelegateImpGLSurfaceView", "redrawInfoWindow");
            th.printStackTrace();
        }
    }

    public void g(boolean z) {
        this.j.setZOrderOnTop(z);
    }

    public CameraPosition r() throws RemoteException {
        return n(this.bs);
    }

    public float s() {
        return s.f;
    }

    public float t() {
        return 3.0f;
    }

    public void a(p pVar) throws RemoteException {
        boolean z = false;
        if (this.aY || this.aZ) {
            this.e.e();
        }
        if (pVar.a == a.newLatLngBounds) {
            if (n() > 0 && o() > 0) {
                z = true;
            }
            au.a(z, (Object) "the map must have a size");
        }
        u();
        pVar.p = true;
        pVar.n = this.bs;
        this.e.a(pVar);
    }

    public void b(p pVar) throws RemoteException {
        a(pVar, null);
    }

    public void a(p pVar, CancelableCallback cancelableCallback) throws RemoteException {
        a(pVar, 250, cancelableCallback);
    }

    public void a(p pVar, long j, CancelableCallback cancelableCallback) throws RemoteException {
        if (pVar.a == a.newLatLngBounds) {
            boolean z;
            if (n() > 0 && o() > 0) {
                z = true;
            } else {
                z = false;
            }
            au.a(z, (Object) "the map must have a size");
        }
        if (!this.at.a()) {
            this.at.a(true);
            if (this.aw != null) {
                this.aw.onCancel();
            }
        }
        this.at.b(this.bs);
        this.aw = cancelableCallback;
        if (this.aP) {
            this.aQ = true;
        }
        this.aO = false;
        IPoint iPoint;
        if (pVar.a != a.scrollBy) {
            float mapZoomer;
            float a;
            if (pVar.a == a.zoomIn) {
                mapZoomer = this.J.getMapZoomer();
                a = bj.a(1.0f + mapZoomer) - mapZoomer;
                if (a == 0.0f) {
                    this.l.obtainMessage(17).sendToTarget();
                    return;
                }
                iPoint = new IPoint();
                if (this.bs) {
                    a(this.bt, this.bu, iPoint);
                } else {
                    this.J.getGeoCenter(iPoint);
                }
                this.at.a(new AccelerateInterpolator());
                this.at.a(iPoint.x, iPoint.y, mapZoomer, this.J.getMapAngle(), this.J.getCameraHeaderAngle(), 0, 0, a, 0.0f, 0.0f, j);
            } else if (pVar.a == a.zoomOut) {
                mapZoomer = this.J.getMapZoomer();
                a = bj.a(mapZoomer - 1.0f) - mapZoomer;
                if (a == 0.0f) {
                    this.l.obtainMessage(17).sendToTarget();
                    return;
                }
                iPoint = new IPoint();
                if (this.bs) {
                    a(this.bt, this.bu, iPoint);
                } else {
                    this.J.getGeoCenter(iPoint);
                }
                this.at.a(new AccelerateInterpolator());
                this.at.a(iPoint.x, iPoint.y, mapZoomer, this.J.getMapAngle(), this.J.getCameraHeaderAngle(), 0, 0, a, 0.0f, 0.0f, j);
            } else if (pVar.a == a.zoomTo) {
                mapZoomer = this.J.getMapZoomer();
                a = bj.a(pVar.d) - mapZoomer;
                if (a == 0.0f) {
                    this.l.obtainMessage(17).sendToTarget();
                    return;
                }
                iPoint = new IPoint();
                if (this.bs) {
                    a(this.bt, this.bu, iPoint);
                } else {
                    this.J.getGeoCenter(iPoint);
                }
                this.at.a(new AccelerateInterpolator());
                this.at.a(iPoint.x, iPoint.y, mapZoomer, this.J.getMapAngle(), this.J.getCameraHeaderAngle(), 0, 0, a, 0.0f, 0.0f, j);
            } else if (pVar.a == a.zoomBy) {
                this.at.b(false);
                r2 = pVar.e;
                mapZoomer = this.J.getMapZoomer();
                a = bj.a(mapZoomer + r2) - mapZoomer;
                if (a == 0.0f) {
                    this.l.obtainMessage(17).sendToTarget();
                    return;
                }
                Point point = pVar.m;
                IPoint iPoint2 = new IPoint();
                this.J.getGeoCenter(iPoint2);
                r9 = 0;
                r10 = 0;
                IPoint iPoint3 = new IPoint();
                int i;
                if (point != null) {
                    a(point.x, point.y, iPoint3);
                    r3 = iPoint2.x - iPoint3.x;
                    i = iPoint2.y - iPoint3.y;
                    r9 = (int) ((((double) r3) / Math.pow(2.0d, (double) r2)) - ((double) r3));
                    r10 = (int) ((((double) i) / Math.pow(2.0d, (double) r2)) - ((double) i));
                } else if (this.bs) {
                    a(this.bt, this.bu, iPoint3);
                    r3 = iPoint2.x - iPoint3.x;
                    i = iPoint2.y - iPoint3.y;
                    r9 = (int) ((((double) r3) / Math.pow(2.0d, (double) r2)) - ((double) r3));
                    r10 = (int) ((((double) i) / Math.pow(2.0d, (double) r2)) - ((double) i));
                }
                this.at.a(new AccelerateInterpolator());
                this.at.a(iPoint2.x, iPoint2.y, mapZoomer, this.J.getMapAngle(), this.J.getCameraHeaderAngle(), r9, r10, a, 0.0f, 0.0f, j);
            } else if (pVar.a == a.newCameraPosition) {
                iPoint = new IPoint();
                if (this.bs) {
                    a(this.bt, this.bu, iPoint);
                } else {
                    this.J.getGeoCenter(iPoint);
                }
                r3 = new IPoint();
                CameraPosition cameraPosition = pVar.h;
                MapProjection.lonlat2Geo(cameraPosition.target.longitude, cameraPosition.target.latitude, r3);
                mapZoomer = this.J.getMapZoomer();
                r9 = r3.x - iPoint.x;
                r10 = r3.y - iPoint.y;
                a = bj.a(cameraPosition.zoom) - mapZoomer;
                r7 = this.J.getMapAngle();
                r12 = (cameraPosition.bearing % 360.0f) - (r7 % 360.0f);
                if (Math.abs(r12) >= 180.0f) {
                    r12 -= Math.signum(r12) * 360.0f;
                }
                r8 = this.J.getCameraHeaderAngle();
                r13 = bj.a(cameraPosition.tilt, cameraPosition.zoom) - r8;
                if (r9 == 0 && r10 == 0 && a == 0.0f && r12 == 0.0f && r13 == 0.0f) {
                    this.l.obtainMessage(17).sendToTarget();
                    return;
                } else {
                    this.at.a(new AccelerateInterpolator());
                    this.at.a(iPoint.x, iPoint.y, mapZoomer, r7, r8, r9, r10, a, r12, r13, j);
                }
            } else if (pVar.a == a.changeBearing) {
                r7 = this.J.getMapAngle();
                r12 = (pVar.g % 360.0f) - (r7 % 360.0f);
                if (Math.abs(r12) >= 180.0f) {
                    r12 -= Math.signum(r12) * 360.0f;
                }
                if (r12 == 0.0f) {
                    this.l.obtainMessage(17).sendToTarget();
                    return;
                }
                iPoint = new IPoint();
                if (this.bs) {
                    a(this.bt, this.bu, iPoint);
                } else {
                    this.J.getGeoCenter(iPoint);
                }
                this.at.a(new AccelerateInterpolator());
                this.at.a(iPoint.x, iPoint.y, this.J.getMapZoomer(), r7, this.J.getCameraHeaderAngle(), 0, 0, 0.0f, r12, 0.0f, j);
            } else if (pVar.a == a.changeTilt) {
                r8 = this.J.getCameraHeaderAngle();
                r13 = pVar.f - r8;
                if (r13 == 0.0f) {
                    this.l.obtainMessage(17).sendToTarget();
                    return;
                }
                iPoint = new IPoint();
                if (this.bs) {
                    a(this.bt, this.bu, iPoint);
                } else {
                    this.J.getGeoCenter(iPoint);
                }
                this.at.a(new AccelerateInterpolator());
                this.at.a(iPoint.x, iPoint.y, this.J.getMapZoomer(), this.J.getMapAngle(), r8, 0, 0, 0.0f, 0.0f, r13, j);
            } else if (pVar.a == a.changeCenter) {
                iPoint = new IPoint();
                if (this.bs) {
                    a(this.bt, this.bu, iPoint);
                } else {
                    this.J.getGeoCenter(iPoint);
                }
                r9 = pVar.o.x - iPoint.x;
                r10 = pVar.o.y - iPoint.y;
                if (r9 == 0 && r10 == 0) {
                    this.l.obtainMessage(17).sendToTarget();
                    return;
                } else {
                    this.at.a(new AccelerateDecelerateInterpolator());
                    this.at.a(iPoint.x, iPoint.y, this.J.getMapZoomer(), this.J.getMapAngle(), this.J.getCameraHeaderAngle(), r9, r10, 0.0f, 0.0f, 0.0f, j);
                }
            } else if (pVar.a == a.newLatLngBounds || pVar.a == a.newLatLngBoundsWithSize) {
                int i2;
                this.at.b(false);
                if (pVar.a != a.newLatLngBounds) {
                    r9 = pVar.k;
                    i2 = pVar.l;
                } else {
                    r9 = n();
                    i2 = o();
                }
                float mapAngle = this.J.getMapAngle() % 360.0f;
                float cameraHeaderAngle = this.J.getCameraHeaderAngle();
                r12 = -mapAngle;
                if (Math.abs(r12) >= 180.0f) {
                    r12 -= Math.signum(r12) * 360.0f;
                }
                r13 = -cameraHeaderAngle;
                LatLngBounds latLngBounds = pVar.i;
                int i3 = pVar.j;
                IPoint iPoint4 = new IPoint();
                this.J.getGeoCenter(iPoint4);
                float mapZoomer2 = this.J.getMapZoomer();
                this.at.a(new AccelerateInterpolator());
                iPoint = new IPoint();
                r3 = new IPoint();
                MapProjection.lonlat2Geo(latLngBounds.northeast.longitude, latLngBounds.northeast.latitude, iPoint);
                MapProjection.lonlat2Geo(latLngBounds.southwest.longitude, latLngBounds.southwest.latitude, r3);
                r10 = iPoint.x - r3.x;
                int i4 = r3.y - iPoint.y;
                if (r10 <= 0 && i4 <= 0) {
                    this.l.obtainMessage(17).sendToTarget();
                    return;
                }
                Object obj;
                int i5 = (iPoint.x + r3.x) / 2;
                int i6 = (iPoint.y + r3.y) / 2;
                IPoint iPoint5 = new IPoint();
                b((latLngBounds.northeast.latitude + latLngBounds.southwest.latitude) / 2.0d, (latLngBounds.northeast.longitude + latLngBounds.southwest.longitude) / 2.0d, iPoint5);
                if (this.ap.contains(iPoint5.x, iPoint5.y)) {
                    obj = null;
                } else {
                    obj = 1;
                }
                int a2;
                if (obj != null) {
                    final CancelableCallback cancelableCallback2 = this.aw;
                    final LatLngBounds latLngBounds2 = latLngBounds;
                    final int i7 = r9;
                    final int i8 = i2;
                    final int i9 = i3;
                    final long j2 = j;
                    this.aw = new CancelableCallback(this) {
                        final /* synthetic */ AMapDelegateImp g;

                        public void onFinish() {
                            try {
                                this.g.a(p.a(latLngBounds2, i7, i8, i9), j2, cancelableCallback2);
                            } catch (Throwable th) {
                                th.printStackTrace();
                            }
                        }

                        public void onCancel() {
                            if (cancelableCallback2 != null) {
                                cancelableCallback2.onCancel();
                            }
                        }
                    };
                    i4 = ((iPoint4.x + i5) / 2) - iPoint4.x;
                    r10 = ((iPoint4.y + i6) / 2) - iPoint4.y;
                    a2 = (int) bj.a((double) (((float) l()) / 2.0f), (double) (((float) m()) / 2.0f), (double) Math.abs(i5 - iPoint4.x), (double) Math.abs(i6 - iPoint4.y));
                    r2 = a2 != 0 ? ((float) a2) - mapZoomer2 : 0.0f;
                    if (r2 >= 0.0f) {
                        a = 0.0f;
                    } else {
                        a = r2;
                    }
                    this.aO = true;
                    this.at.a(iPoint4.x, iPoint4.y, mapZoomer2, mapAngle, cameraHeaderAngle, i4, r10, a, r12 / 2.0f, r13 / 2.0f, j / 2);
                } else {
                    a2 = r9 - (i3 * 2);
                    r3 = i2 - (i3 * 2);
                    if (a2 <= 0) {
                        a2 = 1;
                    }
                    if (r3 <= 0) {
                        r3 = 1;
                    }
                    a = bj.a((float) ((int) (Math.min(Math.log(((double) this.J.getMapLenWithWin(a2)) / ((double) this.J.getMapLenWithGeo(r10))) / Math.log(2.0d), Math.log(((double) this.J.getMapLenWithWin(r3)) / ((double) this.J.getMapLenWithGeo(i4))) / Math.log(2.0d)) + ((double) mapZoomer2)))) - mapZoomer2;
                    r9 = i5 - iPoint4.x;
                    r10 = i6 - iPoint4.y;
                    if (r9 == 0 && r10 == 0 && a == 0.0f) {
                        this.l.obtainMessage(17).sendToTarget();
                        return;
                    } else {
                        this.at.a(new DecelerateInterpolator());
                        this.at.a(iPoint4.x, iPoint4.y, mapZoomer2, mapAngle, cameraHeaderAngle, r9, r10, a, r12, r13, j);
                    }
                }
            } else {
                pVar.p = true;
                this.e.a(pVar);
            }
        } else if (pVar.b == 0.0f && pVar.c == 0.0f) {
            this.l.obtainMessage(17).sendToTarget();
            return;
        } else {
            this.at.b(false);
            iPoint = new IPoint();
            this.J.getGeoCenter(iPoint);
            IPoint iPoint6 = new IPoint();
            a((n() / 2) + ((int) pVar.b), (o() / 2) + ((int) pVar.c), iPoint6);
            this.at.a(new AccelerateDecelerateInterpolator());
            this.at.a(iPoint.x, iPoint.y, this.J.getMapZoomer(), this.J.getMapAngle(), this.J.getCameraHeaderAngle(), iPoint6.x - iPoint.x, iPoint6.y - iPoint.y, 0.0f, 0.0f, 0.0f, j);
        }
        f(false);
    }

    public void u() throws RemoteException {
        if (!this.at.a()) {
            this.at.a(true);
            a(true, null);
            if (this.aw != null) {
                this.aw.onCancel();
            }
            if (!(this.aj == null || this.al == null)) {
                this.aj.setVisibility(0);
            }
            this.aw = null;
        }
        f(false);
    }

    public al a(PolylineOptions polylineOptions) throws RemoteException {
        if (polylineOptions == null) {
            return null;
        }
        aj bgVar = new bg(this.h);
        bgVar.a(polylineOptions.getColor());
        bgVar.b(polylineOptions.isGeodesic());
        bgVar.c(polylineOptions.isDottedLine());
        bgVar.a(polylineOptions.getPoints());
        bgVar.a(polylineOptions.isVisible());
        bgVar.b(polylineOptions.getWidth());
        bgVar.a(polylineOptions.getZIndex());
        bgVar.d(polylineOptions.isUseTexture());
        if (polylineOptions.getColorValues() != null) {
            bgVar.e(polylineOptions.getColorValues());
            bgVar.e(polylineOptions.isUseGradient());
        }
        if (polylineOptions.getCustomTexture() != null) {
            bgVar.a(polylineOptions.getCustomTexture());
        }
        if (polylineOptions.getCustomTextureList() != null) {
            bgVar.c(polylineOptions.getCustomTextureList());
            bgVar.d(polylineOptions.getCustomTextureIndex());
        }
        this.h.a(bgVar);
        f(false);
        return bgVar;
    }

    public ai a(NavigateArrowOptions navigateArrowOptions) throws RemoteException {
        if (navigateArrowOptions == null) {
            return null;
        }
        aj bcVar = new bc(this);
        bcVar.a(navigateArrowOptions.getTopColor());
        bcVar.a(navigateArrowOptions.getPoints());
        bcVar.a(navigateArrowOptions.isVisible());
        bcVar.b(navigateArrowOptions.getWidth());
        bcVar.a(navigateArrowOptions.getZIndex());
        this.h.a(bcVar);
        f(false);
        return bcVar;
    }

    public ak a(PolygonOptions polygonOptions) throws RemoteException {
        if (polygonOptions == null) {
            return null;
        }
        aj bfVar = new bf(this);
        bfVar.a(polygonOptions.getFillColor());
        bfVar.a(polygonOptions.getPoints());
        bfVar.a(polygonOptions.isVisible());
        bfVar.b(polygonOptions.getStrokeWidth());
        bfVar.a(polygonOptions.getZIndex());
        bfVar.b(polygonOptions.getStrokeColor());
        this.h.a(bfVar);
        f(false);
        return bfVar;
    }

    public ad a(CircleOptions circleOptions) throws RemoteException {
        if (circleOptions == null) {
            return null;
        }
        aj qVar = new q(this);
        qVar.b(circleOptions.getFillColor());
        qVar.a(circleOptions.getCenter());
        qVar.a(circleOptions.isVisible());
        qVar.b(circleOptions.getStrokeWidth());
        qVar.a(circleOptions.getZIndex());
        qVar.a(circleOptions.getStrokeColor());
        qVar.a(circleOptions.getRadius());
        this.h.a(qVar);
        f(false);
        return qVar;
    }

    public ac a(ArcOptions arcOptions) throws RemoteException {
        if (arcOptions == null) {
            return null;
        }
        aj mVar = new m(this);
        mVar.a(arcOptions.getStrokeColor());
        mVar.a(arcOptions.getStart());
        mVar.b(arcOptions.getPassed());
        mVar.c(arcOptions.getEnd());
        mVar.a(arcOptions.isVisible());
        mVar.b(arcOptions.getStrokeWidth());
        mVar.a(arcOptions.getZIndex());
        this.h.a(mVar);
        f(false);
        return mVar;
    }

    public af a(GroundOverlayOptions groundOverlayOptions) throws RemoteException {
        if (groundOverlayOptions == null) {
            return null;
        }
        aj aaVar = new aa(this);
        aaVar.b(groundOverlayOptions.getAnchorU(), groundOverlayOptions.getAnchorV());
        aaVar.a(groundOverlayOptions.getWidth(), groundOverlayOptions.getHeight());
        aaVar.a(groundOverlayOptions.getImage());
        aaVar.a(groundOverlayOptions.getLocation());
        aaVar.a(groundOverlayOptions.getBounds());
        aaVar.c(groundOverlayOptions.getBearing());
        aaVar.d(groundOverlayOptions.getTransparency());
        aaVar.a(groundOverlayOptions.isVisible());
        aaVar.a(groundOverlayOptions.getZIndex());
        this.h.a(aaVar);
        f(false);
        return aaVar;
    }

    public Marker a(MarkerOptions markerOptions) throws RemoteException {
        if (markerOptions == null) {
            return null;
        }
        ah baVar = new ba(markerOptions, this.d);
        this.d.a(baVar);
        f(false);
        return new Marker(baVar);
    }

    public Text a(TextOptions textOptions) throws RemoteException {
        if (textOptions == null) {
            return null;
        }
        ah blVar = new bl(textOptions, this.d);
        this.d.a(blVar);
        f(false);
        return new Text(blVar);
    }

    public ArrayList<Marker> a(ArrayList<MarkerOptions> arrayList, boolean z) throws RemoteException {
        int i = 0;
        if (arrayList == null || arrayList.size() == 0) {
            return null;
        }
        ArrayList<Marker> arrayList2 = new ArrayList();
        try {
            MarkerOptions markerOptions;
            if (arrayList.size() == 1) {
                markerOptions = (MarkerOptions) arrayList.get(0);
                if (markerOptions != null) {
                    arrayList2.add(a(markerOptions));
                    if (z && markerOptions.getPosition() != null) {
                        a(p.a(markerOptions.getPosition(), 18.0f));
                    }
                    return arrayList2;
                }
            }
            final Builder builder = LatLngBounds.builder();
            int i2 = 0;
            while (i2 < arrayList.size()) {
                int i3;
                markerOptions = (MarkerOptions) arrayList.get(i2);
                if (arrayList.get(i2) == null) {
                    i3 = i;
                } else {
                    arrayList2.add(a(markerOptions));
                    if (markerOptions.getPosition() == null) {
                        i3 = i;
                    } else {
                        builder.include(markerOptions.getPosition());
                        i3 = i + 1;
                    }
                }
                i2++;
                i = i3;
            }
            if (z && i > 0) {
                if (this.aK) {
                    this.l.postDelayed(new Runnable(this) {
                        final /* synthetic */ AMapDelegateImp b;

                        public void run() {
                            try {
                                this.b.a(p.a(builder.build(), 50));
                            } catch (Throwable th) {
                            }
                        }
                    }, 50);
                } else {
                    this.aE = p.a(builder.build(), 50);
                }
            }
            return arrayList2;
        } catch (Throwable th) {
            ce.a(th, "AMapDelegateImpGLSurfaceView", "addMarkers");
            th.printStackTrace();
            return arrayList2;
        }
    }

    public TileOverlay a(TileOverlayOptions tileOverlayOptions) throws RemoteException {
        if (tileOverlayOptions == null || tileOverlayOptions.getTileProvider() == null) {
            return null;
        }
        ap bnVar = new bn(tileOverlayOptions, this.g);
        this.g.a(bnVar);
        f(false);
        return new TileOverlay(bnVar);
    }

    public void v() throws RemoteException {
        try {
            h(false);
        } catch (Throwable th) {
            ce.a(th, "AMapDelegateImpGLSurfaceView", "clear");
            Log.d("amapApi", "AMapDelegateImpGLSurfaceView clear erro" + th.getMessage());
            th.printStackTrace();
        }
    }

    public void h(boolean z) throws RemoteException {
        String str = null;
        try {
            String str2;
            E();
            if (this.as == null) {
                str2 = null;
            } else if (z) {
                str2 = this.as.c();
                str = this.as.d();
            } else {
                this.as.e();
                str2 = null;
            }
            this.h.b(str);
            this.g.b();
            this.d.a(str2);
            f(false);
        } catch (Throwable th) {
            ce.a(th, "AMapDelegateImpGLSurfaceView", "clear");
            Log.d("amapApi", "AMapDelegateImpGLSurfaceView clear erro" + th.getMessage());
            th.printStackTrace();
        }
    }

    public int w() throws RemoteException {
        return this.F;
    }

    public void b(int i) throws RemoteException {
        this.F = i;
        if (this.aK) {
            if (i == 1) {
                a(MapViewMode.NORAML, MapViewTime.DAY);
            } else if (i == 2) {
                a(MapViewMode.SATELLITE, MapViewTime.DAY);
            } else if (i == 3) {
                a(MapViewMode.NORAML, MapViewTime.NIGHT, MapViewModeState.NAVI_CAR);
            } else if (i != 4) {
                try {
                    this.F = 1;
                } catch (Throwable th) {
                    ce.a(th, "AMapDelegateImpGLSurfaceView", "setMaptype");
                    th.printStackTrace();
                }
            } else {
                a(MapViewMode.NORAML, MapViewTime.DAY, MapViewModeState.NAVI_CAR);
            }
            f(false);
        }
    }

    public void a(MapViewMode mapViewMode, MapViewTime mapViewTime) {
        a(mapViewMode, mapViewTime, MapViewModeState.NORMAL);
    }

    public void a(MapViewMode mapViewMode, MapViewTime mapViewTime, MapViewModeState mapViewModeState) {
        if (this.C != mapViewTime || this.D != mapViewMode || this.E != mapViewModeState) {
            if (this.aL) {
                final MapViewTime mapViewTime2 = this.C;
                final MapViewMode mapViewMode2 = this.D;
                MapViewModeState mapViewModeState2 = this.E;
                if (this.be && this.aJ) {
                    final MapViewTime mapViewTime3 = mapViewTime;
                    final MapViewMode mapViewMode3 = mapViewMode;
                    final MapViewModeState mapViewModeState3 = mapViewModeState;
                    a(new Runnable(this) {
                        final /* synthetic */ AMapDelegateImp f;

                        public void run() {
                            int i;
                            String styleName = this.f.i.getStyleName();
                            String iconName = this.f.i.getIconName();
                            this.f.C = mapViewTime3;
                            this.f.D = mapViewMode3;
                            this.f.E = mapViewModeState3;
                            String styleName2 = this.f.i.getStyleName();
                            String iconName2 = this.f.i.getIconName();
                            if (this.f.D == MapViewMode.SATELLITE || this.f.C == MapViewTime.NIGHT || mapViewTime2 == MapViewTime.NIGHT || mapViewMode2 == MapViewMode.SATELLITE) {
                                this.f.l.post(new Runnable(this) {
                                    final /* synthetic */ AnonymousClass3 a;

                                    {
                                        this.a = r1;
                                    }

                                    public void run() {
                                        this.a.f.am();
                                    }
                                });
                            }
                            this.f.G.setParameter(2501, 0, 0, 0, 0);
                            if (!styleName.equals(styleName2)) {
                                this.f.i.setStyleData();
                            }
                            if (this.f.D == MapViewMode.SATELLITE || mapViewMode2 == MapViewMode.SATELLITE) {
                                MapCore g = this.f.G;
                                if (this.f.D != MapViewMode.SATELLITE) {
                                    i = 0;
                                } else {
                                    i = 1;
                                }
                                g.setParameter(2011, i, 0, 0, 0);
                            }
                            if (this.f.C == MapViewTime.NIGHT || mapViewTime2 == MapViewTime.NIGHT) {
                                g = this.f.G;
                                if (this.f.C != MapViewTime.NIGHT) {
                                    i = 0;
                                } else {
                                    i = 1;
                                }
                                g.setParameter(2401, i, 0, 0, 0);
                                this.f.i.setRoadGuideTexture(true);
                                this.f.i.setBkTexture(true);
                            }
                            if (!iconName.equals(iconName2)) {
                                this.f.i.setIconsData(true);
                            }
                            this.f.i.setTrafficTexture(true);
                            if (this.f.E != null) {
                                this.f.G.setParameter(2013, this.f.D.ordinal(), this.f.C.ordinal(), this.f.E.ordinal(), 0);
                            }
                            this.f.G.setParameter(2501, 1, 1, 0, 0);
                        }
                    });
                } else {
                    this.by.d = mapViewMode;
                    this.by.e = mapViewTime;
                    this.by.b = true;
                }
            } else {
                this.C = mapViewTime;
                this.D = mapViewMode;
                this.E = mapViewModeState;
            }
        }
    }

    public boolean x() throws RemoteException {
        return this.x;
    }

    public void i(boolean z) throws RemoteException {
        this.x = z;
        this.e.a(new au(2).a(z));
    }

    public void j(final boolean z) throws RemoteException {
        this.w = z;
        a(new Runnable(this) {
            final /* synthetic */ AMapDelegateImp b;

            public void run() {
                if (this.b.G != null) {
                    this.b.G.setParameter(Place.TYPE_SUBLOCALITY_LEVEL_2, !z ? 0 : 1, 0, 0, 0);
                }
            }
        });
    }

    public void k(final boolean z) throws RemoteException {
        this.u = z;
        if (z) {
            this.G.setParameter(Place.TYPE_SUBLOCALITY_LEVEL_4, 1, 0, 0, 0);
        } else {
            this.G.setParameter(Place.TYPE_SUBLOCALITY_LEVEL_4, 0, 0, 0, 0);
            s.f = 19.0f;
            if (this.an.c()) {
                this.l.sendEmptyMessage(21);
            }
        }
        if (this.an.a()) {
            this.l.post(new Runnable(this) {
                final /* synthetic */ AMapDelegateImp b;

                public void run() {
                    if (z) {
                        this.b.b(true);
                    } else {
                        this.b.U.a(false);
                    }
                }
            });
        }
    }

    public void l(final boolean z) throws RemoteException {
        this.v = z;
        a(new Runnable(this) {
            final /* synthetic */ AMapDelegateImp b;

            public void run() {
                if (this.b.G != null) {
                    this.b.G.setParameter(Place.TYPE_STREET_ADDRESS, !z ? 0 : 1, 0, 0, 0);
                }
            }
        });
    }

    public boolean y() throws RemoteException {
        return this.aI;
    }

    public void m(boolean z) throws RemoteException {
        try {
            if (this.ao == null) {
                this.Q.a(false);
            } else if (z) {
                this.ao.activate(this.aq);
                this.Q.a(true);
                if (this.as == null) {
                    this.as = new bb(this, this.H);
                }
            } else {
                if (this.as != null) {
                    this.as.b();
                    this.as = null;
                }
                this.az = null;
                this.ao.deactivate();
            }
            if (!z) {
                this.an.e(z);
            }
            this.aI = z;
            f(false);
        } catch (Throwable th) {
            ce.a(th, "AMapDelegateImpGLSurfaceView", "setMyLocationEnabled");
            th.printStackTrace();
        }
    }

    public Location z() throws RemoteException {
        if (this.ao == null) {
            return null;
        }
        return this.aq.a;
    }

    public void a(LocationSource locationSource) throws RemoteException {
        try {
            this.ao = locationSource;
            if (locationSource == null) {
                this.Q.a(false);
            } else {
                this.Q.a(true);
            }
        } catch (Throwable th) {
            ce.a(th, "AMapDelegateImpGLSurfaceView", "setLocationSource");
            th.printStackTrace();
        }
    }

    public aq A() throws RemoteException {
        return this.an;
    }

    public am B() throws RemoteException {
        return this.am;
    }

    public void a(OnCameraChangeListener onCameraChangeListener) throws RemoteException {
        this.aa = onCameraChangeListener;
    }

    void a(CameraPosition cameraPosition) {
        Message message = new Message();
        message.what = 10;
        message.obj = cameraPosition;
        this.l.sendMessage(message);
    }

    public OnCameraChangeListener C() throws RemoteException {
        return this.aa;
    }

    public void a(OnMapClickListener onMapClickListener) throws RemoteException {
        this.ab = onMapClickListener;
    }

    public void a(OnMapTouchListener onMapTouchListener) throws RemoteException {
        this.ac = onMapTouchListener;
    }

    public void a(OnPOIClickListener onPOIClickListener) throws RemoteException {
        this.ad = onPOIClickListener;
    }

    public void a(OnMapLongClickListener onMapLongClickListener) throws RemoteException {
        this.ae = onMapLongClickListener;
    }

    public void a(OnMarkerClickListener onMarkerClickListener) throws RemoteException {
        this.W = onMarkerClickListener;
    }

    public void a(OnPolylineClickListener onPolylineClickListener) throws RemoteException {
        this.X = onPolylineClickListener;
    }

    public void a(OnMarkerDragListener onMarkerDragListener) throws RemoteException {
        this.Y = onMarkerDragListener;
    }

    public void a(OnMapLoadedListener onMapLoadedListener) throws RemoteException {
        this.Z = onMapLoadedListener;
    }

    public void a(OnInfoWindowClickListener onInfoWindowClickListener) throws RemoteException {
        this.af = onInfoWindowClickListener;
    }

    public void a(OnIndoorBuildingActiveListener onIndoorBuildingActiveListener) throws RemoteException {
        this.ag = onIndoorBuildingActiveListener;
    }

    public void a(InfoWindowAdapter infoWindowAdapter) throws RemoteException {
        if (infoWindowAdapter != null) {
            this.ah = infoWindowAdapter;
        } else {
            this.ah = this.ai;
        }
    }

    public View D() throws RemoteException {
        return this.O;
    }

    public float b(float f) throws RemoteException {
        return bj.a(f);
    }

    public float c(int i) {
        if (this.aJ) {
            return this.J.getMapLenWithWin(i);
        }
        return 0.0f;
    }

    public void a(int i, int i2, DPoint dPoint) {
        a(this.J, i, i2, dPoint);
    }

    private void a(MapProjection mapProjection, int i, int i2, DPoint dPoint) {
        if (this.aJ) {
            FPoint fPoint = new FPoint();
            mapProjection.win2Map(i, i2, fPoint);
            IPoint iPoint = new IPoint();
            mapProjection.map2Geo(fPoint.x, fPoint.y, iPoint);
            MapProjection.geo2LonLat(iPoint.x, iPoint.y, dPoint);
        }
    }

    public void a(int i, int i2, IPoint iPoint) {
        if (this.aJ) {
            FPoint fPoint = new FPoint();
            this.J.win2Map(i, i2, fPoint);
            this.J.map2Geo(fPoint.x, fPoint.y, iPoint);
        }
    }

    public void b(int i, int i2, IPoint iPoint) {
        if (this.aJ) {
            FPoint fPoint = new FPoint();
            this.J.geo2Map(i, i2, fPoint);
            this.J.map2Win(fPoint.x, fPoint.y, iPoint);
        }
    }

    public void a(double d, double d2, FPoint fPoint) {
        if (this.aJ) {
            IPoint iPoint = new IPoint();
            MapProjection.lonlat2Geo(d2, d, iPoint);
            this.J.geo2Map(iPoint.x, iPoint.y, fPoint);
        }
    }

    public void a(double d, double d2, IPoint iPoint) {
        MapProjection.lonlat2Geo(d2, d, iPoint);
    }

    public void a(int i, int i2, FPoint fPoint) {
        if (this.aJ) {
            this.J.win2Map(i, i2, fPoint);
        }
    }

    public void b(int i, int i2, FPoint fPoint) {
        if (this.aJ) {
            this.J.geo2Map(i2, i, fPoint);
        }
    }

    public void a(float f, float f2, IPoint iPoint) {
        if (this.aJ) {
            this.J.map2Geo(f, f2, iPoint);
        }
    }

    public void b(int i, int i2, DPoint dPoint) {
        MapProjection.geo2LonLat(i, i2, dPoint);
    }

    public void b(double d, double d2, IPoint iPoint) {
        if (this.aJ) {
            MapProjection mapProjection = new MapProjection(this.G);
            mapProjection.recalculate();
            IPoint iPoint2 = new IPoint();
            FPoint fPoint = new FPoint();
            MapProjection.lonlat2Geo(d2, d, iPoint2);
            mapProjection.geo2Map(iPoint2.x, iPoint2.y, fPoint);
            mapProjection.map2Win(fPoint.x, fPoint.y, iPoint);
            mapProjection.recycle();
        }
    }

    private LatLng ak() {
        if (!this.aJ) {
            return null;
        }
        DPoint dPoint = new DPoint();
        IPoint iPoint = new IPoint();
        this.J.getGeoCenter(iPoint);
        MapProjection.geo2LonLat(iPoint.x, iPoint.y, dPoint);
        return new LatLng(dPoint.y, dPoint.x, false);
    }

    public CameraPosition n(boolean z) {
        if (!this.aJ) {
            return null;
        }
        LatLng latLng;
        if (z) {
            DPoint dPoint = new DPoint();
            a(this.bt, this.bu, dPoint);
            latLng = new LatLng(dPoint.y, dPoint.x, false);
        } else {
            latLng = ak();
        }
        return CameraPosition.builder().target(latLng).bearing(this.J.getMapAngle()).tilt(this.J.getCameraHeaderAngle()).zoom(this.J.getMapZoomer()).build();
    }

    private void al() {
        if (this.bo) {
            this.bo = false;
        }
        if (this.bk) {
            this.bk = false;
            p a = p.a();
            a.p = true;
            this.e.a(a);
        }
        if (this.bf) {
            this.bf = false;
            a = p.a();
            a.p = true;
            this.e.a(a);
        }
        this.bg = false;
        this.bh = false;
        if (this.Y != null && this.bi != null) {
            this.Y.onMarkerDragEnd(this.bi);
            this.bi = null;
        }
    }

    private void b(MotionEvent motionEvent) throws RemoteException {
        if (this.bh && this.bi != null) {
            int x = (int) motionEvent.getX();
            int y = (int) (motionEvent.getY() - 60.0f);
            LatLng g = this.bj.g();
            LatLng e = this.bj.e();
            DPoint dPoint = new DPoint();
            a(x, y, dPoint);
            this.bi.setPosition(new LatLng((e.latitude + dPoint.y) - g.latitude, (dPoint.x + e.longitude) - g.longitude));
            this.Y.onMarkerDrag(this.bi);
        }
    }

    public boolean a(MotionEvent motionEvent) {
        if (!this.aK) {
            return false;
        }
        f(false);
        if (motionEvent.getAction() == 261) {
            this.bn = motionEvent.getPointerCount();
        }
        this.K.onTouchEvent(motionEvent);
        this.ar.a(motionEvent);
        this.L.onTouchEvent(motionEvent);
        this.M.a(motionEvent);
        if (motionEvent.getAction() == 2) {
            try {
                b(motionEvent);
            } catch (Throwable e) {
                ce.a(e, "AMapDelegateImpGLSurfaceView", "onDragMarker");
                e.printStackTrace();
            }
        }
        if (motionEvent.getAction() == 1) {
            al();
        }
        f(false);
        if (this.ac != null) {
            this.bv.removeMessages(1);
            Message obtainMessage = this.bv.obtainMessage();
            obtainMessage.what = 1;
            obtainMessage.obj = MotionEvent.obtain(motionEvent);
            obtainMessage.sendToTarget();
        }
        return true;
    }

    public void a(ah ahVar) throws RemoteException {
        int i = -2;
        if (ahVar != null) {
            E();
            if (!((ahVar.i() == null && ahVar.j() == null) || this.ah == null)) {
                this.ak = ahVar;
                if (this.aK) {
                    int i2;
                    Marker marker = new Marker(ahVar);
                    this.aj = this.ah.getInfoWindow(marker);
                    try {
                        if (this.ay == null) {
                            this.ay = bd.a(this.H, "infowindow_bg.9.png");
                        }
                    } catch (Throwable th) {
                        ce.a(th, "AMapDelegateImpGLSurfaceView", "showInfoWindow decodeDrawableFromAsset");
                        th.printStackTrace();
                    }
                    if (this.aj == null) {
                        this.aj = this.ah.getInfoContents(marker);
                    }
                    View linearLayout = new LinearLayout(this.H);
                    if (this.aj == null) {
                        linearLayout.setBackgroundDrawable(this.ay);
                        View textView = new TextView(this.H);
                        textView.setText(ahVar.i());
                        textView.setTextColor(-16777216);
                        View textView2 = new TextView(this.H);
                        textView2.setTextColor(-16777216);
                        textView2.setText(ahVar.j());
                        linearLayout.setOrientation(1);
                        linearLayout.addView(textView);
                        linearLayout.addView(textView2);
                    } else {
                        if (this.aj.getBackground() == null) {
                            this.aj.setBackgroundDrawable(this.ay);
                        }
                        linearLayout.addView(this.aj);
                    }
                    this.aj = linearLayout;
                    LayoutParams layoutParams = this.aj.getLayoutParams();
                    this.aj.setDrawingCacheEnabled(true);
                    this.aj.setDrawingCacheQuality(0);
                    ahVar.d();
                    int D = ahVar.D() + ahVar.B();
                    int E = (ahVar.E() + ahVar.C()) + 2;
                    if (layoutParams == null) {
                        i2 = -2;
                    } else {
                        i2 = layoutParams.width;
                        i = layoutParams.height;
                    }
                    layoutParams = new com.amap.api.mapcore.az.a(i2, i, ahVar.f(), D, E, 81);
                    Bitmap a;
                    BitmapDescriptor fromBitmap;
                    if (this.al != null) {
                        this.al.a(ahVar.f());
                        this.al.b(D, E);
                        a = bj.a(this.aj);
                        fromBitmap = BitmapDescriptorFactory.fromBitmap(a);
                        a.recycle();
                        this.al.a(fromBitmap);
                    } else {
                        a = bj.a(this.aj);
                        fromBitmap = BitmapDescriptorFactory.fromBitmap(a);
                        a.recycle();
                        this.al = new bh(this, new MarkerOptions().icon(fromBitmap), this) {
                            final /* synthetic */ AMapDelegateImp a;

                            public void a() {
                                this.a.aC.removeCallbacks(this.a.bw);
                                this.a.aC.post(this.a.bx);
                            }
                        };
                        this.al.a(ahVar.f());
                        this.al.b(D, E);
                    }
                    this.O.addView(this.aj, layoutParams);
                    ahVar.b(true);
                } else {
                    this.aC.postDelayed(new Runnable(this) {
                        final /* synthetic */ AMapDelegateImp a;

                        {
                            this.a = r1;
                        }

                        public void run() {
                            try {
                                this.a.a(this.a.ak);
                            } catch (Throwable th) {
                                ce.a(th, "AMapDelegateImpGLSurfaceView", "showInfoWindow postDelayed");
                                th.printStackTrace();
                            }
                        }
                    }, 100);
                }
            }
        }
    }

    public void E() {
        if (this.aj != null) {
            this.aj.clearFocus();
            this.O.removeView(this.aj);
            bj.a(this.aj.getBackground());
            bj.a(this.ay);
            if (this.al != null) {
                this.al.c(false);
            }
            this.aj = null;
        }
        this.ak = null;
    }

    public float F() {
        return this.J.getMapZoomer();
    }

    void G() {
        this.l.obtainMessage(18).sendToTarget();
    }

    public LatLngBounds H() {
        return this.bq;
    }

    public LatLngBounds a(LatLng latLng, float f) {
        int n = n();
        int o = o();
        if (n <= 0 || o <= 0) {
            return null;
        }
        IPoint iPoint = new IPoint();
        MapProjection.lonlat2Geo(latLng.longitude, latLng.latitude, iPoint);
        MapProjection mapProjection = new MapProjection(this.G);
        mapProjection.setCameraHeaderAngle(0.0f);
        mapProjection.setMapAngle(0.0f);
        mapProjection.setGeoCenter(iPoint.x, iPoint.y);
        mapProjection.setMapZoomer(f);
        mapProjection.recalculate();
        DPoint dPoint = new DPoint();
        a(mapProjection, 0, 0, dPoint);
        LatLng latLng2 = new LatLng(dPoint.y, dPoint.x, false);
        a(mapProjection, n, o, dPoint);
        LatLng latLng3 = new LatLng(dPoint.y, dPoint.x, false);
        mapProjection.recycle();
        return LatLngBounds.builder().include(latLng3).include(latLng2).build();
    }

    public Point I() {
        if (this.P != null) {
            return this.P.c();
        }
        return null;
    }

    public static Bitmap a(int i, int i2, int i3, int i4, GL10 gl10) {
        try {
            int[] iArr = new int[(i3 * i4)];
            int[] iArr2 = new int[(i3 * i4)];
            Buffer wrap = IntBuffer.wrap(iArr);
            wrap.position(0);
            gl10.glReadPixels(i, i2, i3, i4, 6408, 5121, wrap);
            for (int i5 = 0; i5 < i4; i5++) {
                for (int i6 = 0; i6 < i3; i6++) {
                    int i7 = iArr[(i5 * i3) + i6];
                    iArr2[(((i4 - i5) - 1) * i3) + i6] = ((i7 & -16711936) | ((i7 << 16) & 16711680)) | ((i7 >> 16) & 255);
                }
            }
            Bitmap createBitmap = Bitmap.createBitmap(i3, i4, Config.ARGB_8888);
            createBitmap.setPixels(iArr2, 0, i3, 0, 0, i3, i4);
            return createBitmap;
        } catch (Throwable th) {
            ce.a(th, "AMapDelegateImpGLSurfaceView", "SavePixels");
            th.printStackTrace();
            return null;
        }
    }

    public void a(onMapPrintScreenListener onmapprintscreenlistener) {
        this.aA = onmapprintscreenlistener;
        this.aS = true;
        f(false);
    }

    public void a(OnMapScreenShotListener onMapScreenShotListener) {
        this.aB = onMapScreenShotListener;
        this.aS = true;
        f(false);
    }

    public void d(int i) {
        if (this.P != null) {
            this.P.a(i);
            this.P.invalidate();
            if (this.S.getVisibility() == 0) {
                this.S.invalidate();
            }
        }
    }

    public void e(int i) {
        if (this.f != null) {
            this.f.a(i);
        }
    }

    public float J() {
        try {
            LatLng latLng = r().target;
            float f = this.a;
            if (this.aJ) {
                f = this.J.getMapZoomer();
            }
            return (float) ((((Math.cos((latLng.latitude * 3.141592653589793d) / 180.0d) * 2.0d) * 3.141592653589793d) * 6378137.0d) / (Math.pow(2.0d, (double) f) * 256.0d));
        } catch (Throwable th) {
            ce.a(th, "AMapDelegateImpGLSurfaceView", "getScalePerPixel");
            th.printStackTrace();
            return 0.0f;
        }
    }

    void o(boolean z) {
        int i;
        Handler handler = this.l;
        if (z) {
            i = 1;
        } else {
            i = 0;
        }
        handler.obtainMessage(20, i, 0).sendToTarget();
    }

    protected void a(boolean z, CameraPosition cameraPosition) {
        if (this.aa != null && this.at.a() && this.j.isEnabled()) {
            if (cameraPosition == null) {
                try {
                    cameraPosition = r();
                } catch (Throwable e) {
                    ce.a(e, "AMapDelegateImpGLSurfaceView", "cameraChangeFinish");
                    e.printStackTrace();
                }
            }
            this.aa.onCameraChangeFinish(cameraPosition);
        }
    }

    public void f(int i) {
        if (this.B.contains(Integer.valueOf(i))) {
            this.A.add(Integer.valueOf(i));
            this.B.remove(this.B.indexOf(Integer.valueOf(i)));
        }
    }

    public int K() {
        Integer valueOf = Integer.valueOf(0);
        if (this.A.size() > 0) {
            valueOf = (Integer) this.A.get(0);
            this.A.remove(0);
            this.B.add(valueOf);
        }
        return valueOf.intValue();
    }

    public List<Marker> L() {
        boolean z = false;
        if (n() > 0 && o() > 0) {
            z = true;
        }
        au.a(z, (Object) "地图未初始化完成！");
        return this.d.f();
    }

    public void M() {
        this.h.b();
    }

    public void N() {
        this.br = true;
    }

    public boolean O() {
        return this.br;
    }

    public void P() {
        if (this.d != null) {
            this.d.g();
        }
        this.br = false;
    }

    public void a(int i, int i2) {
        if (this.I != null) {
            this.bs = true;
            this.I.a(i, i2);
            this.bt = i;
            this.bu = i2;
        }
    }

    public void g(int i) {
        this.ax = i;
    }

    public int Q() {
        return this.ax;
    }

    public boolean R() {
        return this.aK;
    }

    public o S() {
        return this.at;
    }

    public void p(final boolean z) throws RemoteException {
        a(new Runnable(this) {
            final /* synthetic */ AMapDelegateImp b;

            public void run() {
                int i;
                MapCore g = this.b.G;
                if (z) {
                    i = 1;
                } else {
                    i = 0;
                }
                g.setParameter(2601, i, 0, 0, 0);
            }
        });
    }

    public void T() {
        a(null);
    }

    public void a(OnCacheRemoveListener onCacheRemoveListener) {
        if (this.aV != null) {
            try {
                this.G.setParameter(2601, 0, 0, 0, 0);
                Runnable iVar = new i(this, this.H, onCacheRemoveListener);
                this.aV.removeCallbacks(iVar);
                this.aV.post(iVar);
            } catch (Throwable th) {
                ce.a(th, "AMapDelegateImpGLSurfaceView", "removecache");
                th.printStackTrace();
            }
        }
    }

    private boolean a(File file) throws IOException, Exception {
        if (file == null || !file.exists()) {
            return false;
        }
        File[] listFiles = file.listFiles();
        if (listFiles != null) {
            for (int i = 0; i < listFiles.length; i++) {
                if (listFiles[i].isFile()) {
                    if (!listFiles[i].delete()) {
                        return false;
                    }
                } else if (!a(listFiles[i])) {
                    return false;
                } else {
                    listFiles[i].delete();
                }
            }
        }
        return true;
    }

    public void U() {
        if (this.h != null) {
            this.h.c();
        }
        if (this.d != null) {
            this.d.b();
        }
        if (this.m != null) {
            this.m.OnMapReferencechanged();
        }
    }

    public void h(int i) {
        this.j.setVisibility(i);
    }

    public void a(com.amap.api.mapcore.util.f fVar) throws RemoteException {
        if (!this.u) {
            return;
        }
        if (fVar != null) {
            if (this.aD == null || !this.aD.poiid.equals(fVar.poiid) || !this.U.d()) {
                if (this.aD == null || !this.aD.poiid.equals(fVar.poiid) || this.aD.g == null) {
                    this.aD = fVar;
                    this.aD.g = new IPoint();
                    this.J.getGeoCenter(this.aD.g);
                }
                if (this.ag != null) {
                    this.ag.OnIndoorBuilding(fVar);
                }
                s.f = 20.0f;
                if (this.an.c()) {
                    this.l.sendEmptyMessage(21);
                }
                if (this.an.a() && !this.U.d()) {
                    this.an.a(true);
                    this.l.post(new Runnable(this) {
                        final /* synthetic */ AMapDelegateImp a;

                        {
                            this.a = r1;
                        }

                        public void run() {
                            try {
                                this.a.U.a(this.a.aD.floor_names);
                                this.a.U.a(this.a.aD.activeFloorName);
                            } catch (Throwable th) {
                                th.printStackTrace();
                            }
                        }
                    });
                } else if (!this.an.a() && this.U.d()) {
                    this.an.a(false);
                }
            }
        } else if (!ag()) {
            if (this.ag != null) {
                this.ag.OnIndoorBuilding(fVar);
            }
            if (this.aD != null) {
                this.aD.g = null;
            }
            if (this.U.d()) {
                this.l.post(new Runnable(this) {
                    final /* synthetic */ AMapDelegateImp a;

                    {
                        this.a = r1;
                    }

                    public void run() {
                        this.a.U.setVisibility(8);
                    }
                });
            }
            s.f = 19.0f;
            if (this.an.c()) {
                this.l.sendEmptyMessage(21);
            }
        }
    }

    public void b(com.amap.api.mapcore.util.f fVar) throws RemoteException {
        if (fVar != null && fVar.activeFloorName != null && fVar.poiid != null) {
            this.aD = fVar;
            f(false);
            a(new Runnable(this) {
                final /* synthetic */ AMapDelegateImp a;

                {
                    this.a = r1;
                }

                public void run() {
                    this.a.G.setIndoorBuildingToBeActive(this.a.aD.activeFloorName, this.a.aD.activeFloorIndex, this.a.aD.poiid);
                }
            });
        }
    }

    private Poi a(int i, int i2, int i3) {
        if (!this.aK) {
            return null;
        }
        try {
            SelectedMapPoi GetSelectedMapPoi = this.G.GetSelectedMapPoi(i, i2, i3);
            if (GetSelectedMapPoi == null) {
                return null;
            }
            DPoint dPoint = new DPoint();
            MapProjection.geo2LonLat(GetSelectedMapPoi.mapx, GetSelectedMapPoi.mapy, dPoint);
            return new Poi(GetSelectedMapPoi.name, new LatLng(dPoint.y, dPoint.x, false), GetSelectedMapPoi.poiid);
        } catch (Throwable th) {
            return null;
        }
    }

    public void a(CustomRenderer customRenderer) {
        this.m = customRenderer;
    }

    public Context V() {
        return this.H;
    }

    public void a(Runnable runnable) {
        if (this.j != null) {
            this.j.queueEvent(runnable);
        }
    }

    public void onSurfaceCreated(GL10 gl10, EGLConfig eGLConfig) {
        try {
            if (!this.aJ) {
                ad();
            }
            this.be = false;
            this.G.setGL(gl10);
            aj();
            this.G.surfaceCreate(gl10);
            if (this.q != null) {
                if (!this.q.isRecycled()) {
                    if (this.r == null || this.r.isRecycled()) {
                        this.r = bj.a(this.H, "lineDashTexture.png");
                    }
                    this.aU = false;
                    this.n = bj.a(gl10, this.q);
                    this.o = bj.a(gl10, this.r, true);
                    this.q = null;
                    this.d.i();
                    this.h.e();
                    this.g.f();
                    if (this.al != null) {
                        this.al.J();
                    }
                    ah();
                    f(false);
                    if (!this.aL) {
                        this.bp.setName("AuthThread");
                        this.bp.start();
                        this.aL = true;
                    }
                    if (this.m == null) {
                        this.m.onSurfaceCreated(gl10, eGLConfig);
                    }
                }
            }
            this.q = bj.a(this.H, "lineTexture.png");
            if (this.r == null) {
                this.aU = false;
                this.n = bj.a(gl10, this.q);
                this.o = bj.a(gl10, this.r, true);
                this.q = null;
                this.d.i();
                this.h.e();
                this.g.f();
                if (this.al != null) {
                    this.al.J();
                }
                ah();
                f(false);
                if (this.aL) {
                    this.bp.setName("AuthThread");
                    this.bp.start();
                    this.aL = true;
                }
                if (this.m == null) {
                    this.m.onSurfaceCreated(gl10, eGLConfig);
                }
            }
            this.r = bj.a(this.H, "lineDashTexture.png");
            this.aU = false;
            this.n = bj.a(gl10, this.q);
            this.o = bj.a(gl10, this.r, true);
            this.q = null;
            this.d.i();
            this.h.e();
            this.g.f();
            if (this.al != null) {
                this.al.J();
            }
            ah();
            f(false);
            if (this.aL) {
                this.bp.setName("AuthThread");
                this.bp.start();
                this.aL = true;
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        if (this.m == null) {
            this.m.onSurfaceCreated(gl10, eGLConfig);
        }
    }

    public void onSurfaceChanged(GL10 gl10, int i, int i2) {
        int i3 = 120;
        int i4 = 1;
        this.ap = new Rect(0, 0, i, i2);
        try {
            this.G.setGL(gl10);
            this.G.surfaceChange(gl10, i, i2);
            int i5 = this.H.getResources().getDisplayMetrics().densityDpi;
            float f = this.H.getResources().getDisplayMetrics().density;
            int i6 = 100;
            if (i5 <= 120) {
                i5 = 50;
            } else if (i5 <= 160) {
                int i7;
                if (Math.max(i, i2) > 480) {
                    i7 = 100;
                    i3 = 160;
                } else {
                    i7 = 120;
                }
                i5 = i3;
                i6 = i7;
            } else if (i5 > 240) {
                if (i5 <= 320) {
                    i5 = 180;
                    i4 = 3;
                    i6 = 50;
                } else if (i5 > 480) {
                    i5 = 360;
                    i4 = 4;
                    i6 = 40;
                } else {
                    i5 = VTMCDataCache.MAX_EXPIREDTIME;
                    i4 = 3;
                    i6 = 50;
                }
            } else if (Math.min(i, i2) < 1000) {
                i5 = 150;
                i4 = 2;
                i6 = 70;
            } else {
                i5 = 200;
                i4 = 2;
                i6 = 60;
            }
            this.G.setParameter(2051, i6, i5, (int) (f * 100.0f), i4);
            this.z = ((float) i6) / 100.0f;
            this.G.setParameter(1001, 0, 0, 0, 0);
            this.G.setParameter(Place.TYPE_SUBLOCALITY_LEVEL_1, 1, 0, 0, 0);
            f(false);
            if (this.m != null) {
                this.m.onSurfaceChanged(gl10, i, i2);
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public float W() {
        return this.z;
    }

    public void X() {
        this.aK = false;
        g();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        f();
    }

    public MapViewTime Y() {
        return this.C;
    }

    public MapViewMode Z() {
        return this.D;
    }

    public MapViewModeState aa() {
        return this.E;
    }

    private void am() {
        if (this.D == MapViewMode.SATELLITE || this.C == MapViewTime.NIGHT) {
            this.P.a(true);
        } else {
            this.P.a(false);
        }
    }

    public void i(int i) {
        try {
            this.p = Math.max(10, Math.min(i, 40));
            this.l.sendEmptyMessage(22);
        } catch (Throwable th) {
        }
    }

    public boolean ab() {
        return this.aM;
    }
}
