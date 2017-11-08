package com.amap.api.mapcore.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import com.amap.api.maps.AMap.CancelableCallback;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.MultiPositionInfoWindowAdapter;
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
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CustomRenderer;
import com.amap.api.maps.InfoWindowAnimationManager;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.Projection;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.AMapGestureListener;
import com.amap.api.maps.model.Arc;
import com.amap.api.maps.model.ArcOptions;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.GroundOverlay;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.IndoorBuildingInfo;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.LatLngBounds.Builder;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.MyTrafficStyle;
import com.amap.api.maps.model.NavigateArrow;
import com.amap.api.maps.model.NavigateArrowOptions;
import com.amap.api.maps.model.Poi;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.Text;
import com.amap.api.maps.model.TextOptions;
import com.amap.api.maps.model.TileOverlay;
import com.amap.api.maps.model.TileOverlayOptions;
import com.amap.api.maps.model.WeightedLatLng;
import com.amap.api.maps.model.animation.Animation.AnimationListener;
import com.autonavi.amap.mapcore.DPoint;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.FileUtil;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapConfig;
import com.autonavi.amap.mapcore.MapCore;
import com.autonavi.amap.mapcore.MapMessage;
import com.autonavi.amap.mapcore.MapProjection;
import com.autonavi.amap.mapcore.VMapDataCache;
import com.autonavi.amap.mapcore.VirtualEarthProjection;
import com.autonavi.amap.mapcore.interfaces.IArc;
import com.autonavi.amap.mapcore.interfaces.ICircle;
import com.autonavi.amap.mapcore.interfaces.IGroundOverlay;
import com.autonavi.amap.mapcore.interfaces.IMarker;
import com.autonavi.amap.mapcore.interfaces.IMarkerAction;
import com.autonavi.amap.mapcore.interfaces.INavigateArrow;
import com.autonavi.amap.mapcore.interfaces.IPolygon;
import com.autonavi.amap.mapcore.interfaces.IPolyline;
import com.huawei.watermark.manager.parse.WMElement;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: AMapDelegateImp */
public class b implements Renderer, l {
    private static boolean ap = false;
    private int A;
    private int B;
    private MyTrafficStyle C = null;
    private com.amap.api.mapcore.util.i.c D = com.amap.api.mapcore.util.i.c.DAY;
    private com.amap.api.mapcore.util.i.a E = com.amap.api.mapcore.util.i.a.NORAML;
    private com.amap.api.mapcore.util.i.b F = com.amap.api.mapcore.util.i.b.NORMAL;
    private int G = -1;
    private CopyOnWriteArrayList<s> H = new CopyOnWriteArrayList();
    private int I = 1;
    private MapCore J;
    private Context K;
    private a L = null;
    private MapProjection M;
    private de N;
    private OnMyLocationChangeListener O;
    private OnMarkerClickListener P;
    private OnPolylineClickListener Q;
    private OnMarkerDragListener R;
    private OnMapLoadedListener S;
    private OnCameraChangeListener T;
    private OnMapClickListener U;
    private OnMapTouchListener V;
    private OnPOIClickListener W;
    private OnMapLongClickListener X;
    private OnInfoWindowClickListener Y;
    private OnIndoorBuildingActiveListener Z;
    int a = -1;
    private boolean aA = false;
    private Marker aB = null;
    private cr aC = null;
    private boolean aD = false;
    private boolean aE = false;
    private Thread aF;
    private a aG = new a(this) {
        final /* synthetic */ b a;

        {
            this.a = r2;
        }

        public void run() {
            super.run();
            this.a.a(this.c, this.d, this.e);
        }
    };
    private e aa;
    private onMapPrintScreenListener ab = null;
    private OnMapScreenShotListener ac = null;
    private AMapGestureListener ad;
    private n ae;
    private o af;
    private LocationSource ag;
    private Rect ah = new Rect();
    private da ai;
    private CancelableCallback aj = null;
    private int ak = 0;
    private Location al;
    private boolean am = true;
    private boolean an = false;
    private boolean ao = false;
    private boolean aq = false;
    private Boolean ar = Boolean.valueOf(false);
    private boolean as = false;
    private boolean at = false;
    private boolean au = false;
    private MapConfig av = new MapConfig(true);
    private dp aw;
    private final r ax;
    private volatile boolean ay = false;
    private boolean az = false;
    j b = null;
    i c = null;
    m d = null;
    q e;
    v f;
    em g;
    h h = null;
    aq i = null;
    cq j = null;
    int k = 10;
    int l = 10;
    Runnable m;
    final Handler n = new Handler(this) {
        final /* synthetic */ b a;

        {
            this.a = r1;
        }

        public void handleMessage(Message message) {
            boolean z = false;
            if (message != null && !this.a.ar.booleanValue()) {
                try {
                    this.a.K();
                    CameraPosition cameraPosition;
                    switch (message.what) {
                        case 2:
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("Key验证失败：[");
                            if (message.obj == null) {
                                stringBuilder.append(ez.b);
                            } else {
                                stringBuilder.append(message.obj);
                            }
                            stringBuilder.append("]");
                            Log.w("amapsdk", stringBuilder.toString());
                            break;
                        case 10:
                            cameraPosition = (CameraPosition) message.obj;
                            if (!(cameraPosition == null || this.a.T == null)) {
                                this.a.T.onCameraChange(cameraPosition);
                                break;
                            }
                        case 11:
                            af afVar = (af) message.obj;
                            if (afVar != null) {
                                this.a.J.addMessage(afVar);
                                break;
                            }
                            break;
                        case 12:
                            try {
                                cameraPosition = this.a.getCameraPosition();
                                if (cameraPosition != null) {
                                    if (this.a.g != null) {
                                        this.a.g.a(cameraPosition);
                                    }
                                }
                                this.a.g(true);
                                if (this.a.au) {
                                    this.a.l();
                                    this.a.au = false;
                                }
                                this.a.a(true, cameraPosition);
                                if (this.a.aj != null) {
                                    this.a.aj.onFinish();
                                }
                                if (!this.a.aq) {
                                    this.a.aj = null;
                                    break;
                                } else {
                                    this.a.aq = false;
                                    break;
                                }
                            } catch (Throwable th) {
                                fo.b(th, "AMapDelegateImp", "CameraUpdateFinish");
                                break;
                            }
                        case 14:
                            if (this.a.g != null) {
                                this.a.g.a(this.a.o());
                                break;
                            }
                            break;
                        case 15:
                            if (this.a.g != null) {
                                ej h = this.a.g.h();
                                if (h != null) {
                                    h.a();
                                    break;
                                }
                                return;
                            }
                            break;
                        case 16:
                            try {
                                if (this.a.V != null) {
                                    this.a.V.onTouch((MotionEvent) message.obj);
                                    break;
                                }
                            } catch (Throwable th2) {
                                fo.b(th2, "AMapDelegateImp", "onTouchHandler");
                                th2.printStackTrace();
                                break;
                            }
                            break;
                        case 17:
                            Bitmap bitmap = (Bitmap) message.obj;
                            int i = message.arg1;
                            if (bitmap == null || this.a.g == null) {
                                if (this.a.ab != null) {
                                    this.a.ab.onMapPrint(null);
                                }
                                if (this.a.ac != null) {
                                    this.a.ac.onMapScreenShot(null);
                                    this.a.ac.onMapScreenShot(null, i);
                                }
                            } else {
                                Canvas canvas = new Canvas(bitmap);
                                eo i2 = this.a.g.i();
                                if (i2 != null) {
                                    i2.onDraw(canvas);
                                }
                                this.a.g.a(canvas);
                                if (this.a.ab != null) {
                                    this.a.ab.onMapPrint(new BitmapDrawable(this.a.K.getResources(), bitmap));
                                }
                                if (this.a.ac != null) {
                                    this.a.ac.onMapScreenShot(bitmap);
                                    this.a.ac.onMapScreenShot(bitmap, i);
                                }
                            }
                            this.a.ab = null;
                            this.a.ac = null;
                            break;
                        case 18:
                            if (this.a.S != null) {
                                this.a.S.onMapLoaded();
                                break;
                            }
                            break;
                        case 19:
                            if (this.a.J.getAnimateionsCount() == 0 && this.a.f != null) {
                                this.a.f.b(false);
                            }
                            if (this.a.f != null) {
                                v vVar = this.a.f;
                                if (message.arg1 != 0) {
                                    z = true;
                                }
                                vVar.a(z);
                                break;
                            }
                            break;
                        case 21:
                            if (this.a.j != null) {
                                this.a.j.e();
                                break;
                            }
                            break;
                    }
                } catch (Throwable th22) {
                    fo.b(th22, "AMapDelegateImp", "handleMessage");
                    th22.printStackTrace();
                }
                this.a.K();
            }
        }
    };
    CustomRenderer o;
    private int p = -1;
    private int q = -1;
    private Bitmap r = null;
    private Bitmap s = null;
    private float t = WMElement.CAMERASIZEVALUE1B1;
    private int u = 0;
    private boolean v;
    private boolean w;
    private boolean x = false;
    private boolean y = false;
    private boolean z = false;

    /* compiled from: AMapDelegateImp */
    private static abstract class a implements Runnable {
        boolean b;
        com.amap.api.mapcore.util.i.a c;
        com.amap.api.mapcore.util.i.c d;
        com.amap.api.mapcore.util.i.b e;

        private a() {
            this.b = false;
        }

        public void run() {
            this.b = false;
        }
    }

    /* compiled from: AMapDelegateImp */
    private class b implements com.amap.api.mapcore.util.ek.a {
        final /* synthetic */ b a;

        private b(b bVar) {
            this.a = bVar;
        }

        public void a(int i) {
            if (this.a.i != null) {
                this.a.i.activeFloorIndex = this.a.i.floor_indexs[i];
                this.a.i.activeFloorName = this.a.i.floor_names[i];
                try {
                    this.a.setIndoorBuildingInfo(this.a.i);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /* compiled from: AMapDelegateImp */
    private class c implements Runnable {
        final /* synthetic */ b a;
        private Context b;
        private OnCacheRemoveListener c;

        public c(b bVar, Context context, OnCacheRemoveListener onCacheRemoveListener) {
            this.a = bVar;
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
                String b = eh.b(applicationContext);
                String a = eh.a(applicationContext);
                boolean z4 = FileUtil.deleteFile(new File(b));
                if (z4) {
                    try {
                        if (FileUtil.deleteFile(new File(a))) {
                            z3 = true;
                            this.a.J.setParameter(2601, 1, 0, 0, 0);
                            if (this.c == null) {
                                this.c.onRemoveCacheFinish(z3);
                            }
                        }
                    } catch (Throwable th3) {
                        th2 = th3;
                        z = z4;
                        this.a.J.setParameter(2601, 1, 0, 0, 0);
                        if (this.c != null) {
                            this.c.onRemoveCacheFinish(z);
                        }
                        throw th2;
                    }
                }
                z3 = false;
                try {
                    this.a.J.setParameter(2601, 1, 0, 0, 0);
                    if (this.c == null) {
                        this.c.onRemoveCacheFinish(z3);
                    }
                } catch (Throwable th4) {
                    th4.printStackTrace();
                }
            } catch (Throwable th5) {
                th4 = th5;
                fo.b(th4, "AMapDelegateImp", "RemoveCacheRunnable");
                this.a.J.setParameter(2601, 1, 0, 0, 0);
                if (this.c != null) {
                    this.c.onRemoveCacheFinish(false);
                }
            }
        }

        public boolean equals(Object obj) {
            return obj instanceof c;
        }
    }

    public b(m mVar, Context context, AttributeSet attributeSet) {
        g.b = ey.c(context);
        this.d = mVar;
        this.K = context.getApplicationContext();
        this.aw = new dp(mVar);
        this.af = new w(this);
        this.J = new MapCore(this.K, this);
        this.L = new a(this);
        this.J.setMapCallback(this.L);
        this.d.setRenderer(this);
        C();
        this.c = new i(this, context);
        this.ae = new t(this);
        this.h = new h(this.K, this);
        this.aa = new e(this);
        this.g = new em(this.K, this);
        this.g.f().a(new b());
        this.aF = new f(this.K, this);
        this.b = new j(this);
        this.e = new q(this.K, this);
        this.f = new v(this.K, this);
        this.ai = new da(this, context);
        this.j = this.g;
        this.N = new de(this, context);
        this.ax = new r();
    }

    public void setOnMyLocationChangeListener(OnMyLocationChangeListener onMyLocationChangeListener) {
        this.O = onMyLocationChangeListener;
    }

    public MapCore a() {
        return this.J;
    }

    public int b() {
        return this.p;
    }

    public MapProjection c() {
        if (this.M == null) {
            this.M = this.J.getMapstate();
        }
        return this.M;
    }

    public void onActivityResume() {
        this.w = false;
    }

    public void onActivityPause() {
        B();
        this.w = true;
    }

    public void d() {
        if (this.u != 1) {
            this.u = 1;
            this.v = false;
            if (!this.an) {
                a(new Runnable(this) {
                    final /* synthetic */ b a;

                    {
                        this.a = r1;
                    }

                    public void run() {
                        try {
                            this.a.C();
                            this.a.L();
                            if (this.a.L != null) {
                                this.a.L.onResume(this.a.J);
                                this.a.K();
                            }
                            if (this.a.f != null) {
                                this.a.f.d();
                            }
                            if (this.a.ai != null) {
                                this.a.ai.a();
                            }
                        } catch (Throwable th) {
                            th.printStackTrace();
                        }
                    }
                });
            }
            if (this.d instanceof c) {
                ((c) this.d).onResume();
            } else {
                ((d) this.d).c();
            }
        }
    }

    public void e() {
        if (this.u == 1) {
            this.u = -1;
            B();
            this.v = true;
            try {
                ei c = this.g.c();
                if (c != null) {
                    c.a(true);
                }
                if (this.L != null) {
                    this.L.destoryMap(this.J);
                }
                M();
                if (this.d instanceof c) {
                    ((c) this.d).onPause();
                } else {
                    ((d) this.d).b();
                }
                D();
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    private void B() {
        IPoint iPoint = new IPoint();
        if (this.M != null) {
            this.M.recalculate();
            this.M.getGeoCenter(iPoint);
            this.av.setS_x(iPoint.x);
            this.av.setS_y(iPoint.y);
            this.av.setS_z(this.M.getMapZoomer());
            this.av.setS_c(this.M.getCameraHeaderAngle());
            this.av.setS_r(this.M.getMapAngle());
        }
    }

    private void C() {
        if (!this.an) {
            try {
                this.J.newMap();
                this.L.onResume(this.J);
                this.M = this.J.getMapstate();
                this.M.setGeoCenter(this.av.getS_x(), this.av.getS_y());
                this.M.setMapAngle(this.av.getS_r());
                this.M.setMapZoomer(this.av.getS_z());
                this.M.setCameraHeaderAngle(this.av.getS_c());
                this.J.setMapstate(this.M);
                if (this.av.getLimitIPoints() != null) {
                    X();
                }
                this.an = true;
                this.aw.a(15);
                this.d.setRenderMode(0);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    private void D() {
        a(new Runnable(this) {
            final /* synthetic */ b a;

            {
                this.a = r1;
            }

            public void run() {
                if (this.a.an) {
                    try {
                        this.a.J.destroy();
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                    VMapDataCache.getInstance().reset();
                    this.a.an = false;
                }
            }
        });
    }

    private void E() {
        int i = 0;
        try {
            setIndoorEnabled(this.av.isIndoorEnable());
            set3DBuildingEnabled(this.av.isBuildingEnable());
            setMapTextEnable(this.av.isMapTextEnable());
            setTrafficEnabled(this.av.isTrafficEnabled());
            setMyTrafficStyle(this.C);
            MapCore mapCore = this.J;
            if (this.E == com.amap.api.mapcore.util.i.a.SATELLITE) {
                i = 1;
            }
            mapCore.setParameter(2011, i, 0, 0, 0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void setMyLocationStyle(MyLocationStyle myLocationStyle) {
        if (this.ai != null) {
            this.ai.a(myLocationStyle);
        }
    }

    public void setMyLocationType(int i) {
        if (this.ai != null) {
            this.ai.a(i);
        }
    }

    public void setMyLocationRotateAngle(float f) throws RemoteException {
        if (this.ai != null) {
            this.ai.a(f);
        }
    }

    public void a(Location location) throws RemoteException {
        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            try {
                if (this.am && this.ag != null) {
                    if (this.ai != null) {
                        if (this.al != null) {
                            this.ai.a(location);
                            if (this.O != null) {
                                if (this.al == null || this.al.equals(location)) {
                                    this.O.onMyLocationChange(location);
                                }
                            }
                            this.al = new Location(location);
                            K();
                            return;
                        }
                    }
                    if (this.ai == null) {
                        this.ai = new da(this, this.K);
                    }
                    if (this.M != null) {
                        a(ag.a(latLng, this.M.getMapZoomer()));
                    }
                    this.ai.a(location);
                    if (this.O != null) {
                        if (this.al == null) {
                        }
                        this.O.onMyLocationChange(location);
                    }
                    this.al = new Location(location);
                    K();
                    return;
                }
                if (this.ai != null) {
                    this.ai.b();
                }
                this.ai = null;
            } catch (Throwable e) {
                fo.b(e, "AMapDelegateImp", "showMyLocationOverlay");
                e.printStackTrace();
            }
        }
    }

    public void a(boolean z) {
        if (!this.ar.booleanValue()) {
            this.g.b(z);
        }
    }

    public void b(boolean z) {
        if (!this.ar.booleanValue()) {
            this.g.a(z);
        }
    }

    public boolean f() {
        if (!(this.M == null || this.M.getMapZoomer() < 17.0f || this.i == null || this.i.g == null)) {
            IPoint iPoint = new IPoint();
            b(this.i.g.x, this.i.g.y, iPoint);
            if (this.ah.contains(iPoint.x, iPoint.y)) {
                return true;
            }
        }
        return false;
    }

    public void destroy() {
        this.ar = Boolean.valueOf(true);
        try {
            M();
            if (this.s != null) {
                this.s.recycle();
                this.s = null;
            }
            if (this.r != null) {
                this.r.recycle();
                this.r = null;
            }
            if (!(this.n == null || this.m == null)) {
                this.n.removeCallbacks(this.m);
                this.m = null;
            }
            if (this.b != null) {
                this.b.b();
            }
            if (this.e != null) {
                this.e.e();
            }
            if (this.f != null) {
                this.f.f();
            }
            V();
            if (this.aF != null) {
                this.aF.interrupt();
                this.aF = null;
            }
            if (this.L != null) {
                this.L.OnMapDestory(this.J);
                this.J.setMapCallback(null);
                this.L = null;
            }
            if (this.J != null) {
                a(new Runnable(this) {
                    final /* synthetic */ b a;

                    {
                        this.a = r1;
                    }

                    public void run() {
                        try {
                            this.a.J.destroy();
                            this.a.J = null;
                        } catch (Throwable th) {
                            th.printStackTrace();
                        }
                    }
                });
                Thread.sleep(200);
            }
            if (this.g != null) {
                this.g.j();
                this.g = null;
            }
            if (this.ai != null) {
                this.ai.b();
                this.ai = null;
            }
            this.ag = null;
            F();
            this.C = null;
            fo.b();
        } catch (Throwable th) {
            fo.b(th, "AMapDelegateImp", "destroy");
            th.printStackTrace();
        }
    }

    private void F() {
        this.O = null;
        this.P = null;
        this.Q = null;
        this.R = null;
        this.S = null;
        this.T = null;
        this.U = null;
        this.V = null;
        this.W = null;
        this.X = null;
        this.Y = null;
        this.Z = null;
        this.aa = null;
        this.ab = null;
        this.ac = null;
    }

    public void c(boolean z) {
        if (!this.ar.booleanValue()) {
            this.g.c(z);
        }
    }

    public void d(boolean z) {
        if (!this.ar.booleanValue()) {
            this.g.d(z);
        }
    }

    public void a(cr crVar) throws RemoteException {
        if (crVar != null) {
            if (!((crVar.getTitle() == null && crVar.getSnippet() == null) || this.j == null)) {
                this.j.a(crVar);
            }
        }
    }

    void g() {
        this.n.obtainMessage(15).sendToTarget();
    }

    void h() {
        this.n.obtainMessage(14).sendToTarget();
    }

    public void e(boolean z) {
        if (!this.ar.booleanValue()) {
            this.g.e(z);
        }
    }

    void i() {
        this.n.post(new Runnable(this) {
            final /* synthetic */ b a;

            {
                this.a = r1;
            }

            public void run() {
                if (this.a.g != null) {
                    this.a.g.b().a();
                }
            }
        });
    }

    public boolean a(String str) throws RemoteException {
        K();
        return this.b.d(str);
    }

    public synchronized void setRunLowFrame(boolean z) {
        if (!z) {
            G();
        }
    }

    private void G() {
        if (!(!this.an || this.aw == null || this.aw.c())) {
            this.d.requestRender();
        }
    }

    public void onDrawFrame(GL10 gl10) {
        if (!this.ar.booleanValue() && !this.v) {
            try {
                if (this.an) {
                    gl10.glColor4f(WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, 0.5f);
                    gl10.glClear(16640);
                    J();
                    this.J.setGL(gl10);
                    this.J.drawFrame(gl10);
                    this.f.a(gl10);
                    if (this.G == 1) {
                        if (this.ax != null) {
                            this.ax.a(gl10, this.ah.width(), this.ah.height());
                        }
                    }
                    this.b.a(gl10, false, this.ak);
                    if (this.G == 0 && this.ax != null) {
                        this.e.b(gl10);
                        this.ax.a(gl10, this.ah.width(), this.ah.height());
                        this.b.a(gl10);
                    }
                    this.e.a(gl10);
                    if (!(this.N == null || this.M == null)) {
                        this.N.a(gl10, this.M, getMapWidth(), getMapHeight());
                    }
                    a(gl10);
                    H();
                    j();
                } else {
                    gl10.glClearColor(0.9453125f, 0.93359f, 0.9101f, WMElement.CAMERASIZEVALUE1B1);
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    private void a(GL10 gl10) {
        if (this.as) {
            int i = !this.J.canStopRenderMap() ? 0 : 1;
            Message obtainMessage = this.n.obtainMessage(17, eh.a(0, 0, getMapWidth(), getMapHeight(), gl10));
            obtainMessage.arg1 = i;
            obtainMessage.sendToTarget();
            this.as = false;
        }
    }

    private void H() {
        final ei c = this.g.c();
        if (c != null && c.getVisibility() != 8) {
            if (!this.ao) {
                this.n.sendEmptyMessage(18);
                this.ao = true;
                g(true);
            }
            this.n.post(new Runnable(this) {
                final /* synthetic */ b b;

                public void run() {
                    if (!this.b.v) {
                        try {
                            if (this.b.i != null) {
                                this.b.setIndoorBuildingInfo(this.b.i);
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        c.a(false);
                    }
                }
            });
        }
    }

    private void I() {
        this.av.setMapRect(eh.a((l) this, true));
    }

    private void J() {
        if (this.a != -1) {
            this.aw.a(this.a);
            K();
        } else if (this.J.getAnimateionsCount() > 0) {
            this.aw.a(30);
            K();
        } else if (this.k <= this.l) {
            this.k++;
            this.aw.a(30);
            this.aw.b(3);
        } else {
            this.aw.a(15);
        }
    }

    public void j() {
        if (this.av.getMapRect() == null) {
            I();
        }
        MapProjection mapstate = this.J.getMapstate();
        if (mapstate != null) {
            IPoint iPoint = new IPoint();
            mapstate.getGeoCenter(iPoint);
            this.av.setS_x(iPoint.x);
            this.av.setS_y(iPoint.y);
            this.av.setS_z(mapstate.getMapZoomer());
            this.av.setS_c(mapstate.getCameraHeaderAngle());
            this.av.setS_r(mapstate.getMapAngle());
            if (this.a != -1) {
                this.aw.a(this.a);
            } else if (this.J.getAnimateionsCount() == 0) {
                this.aw.a(15);
            }
            if (this.av.isMapStateChange()) {
                if (this.M != null) {
                    this.av.setMapPerPixelUnitLength(this.M.getMapLenWithWin(1));
                }
                DPoint dPoint = new DPoint();
                MapProjection.geo2LonLat(iPoint.x, iPoint.y, dPoint);
                CameraPosition cameraPosition = new CameraPosition(new LatLng(dPoint.y, dPoint.x, false), this.av.getS_z(), this.av.getS_c(), this.av.getS_r());
                Message obtainMessage = this.n.obtainMessage();
                obtainMessage.what = 10;
                obtainMessage.obj = cameraPosition;
                this.n.sendMessage(obtainMessage);
                l();
                I();
                try {
                    if (this.af.isZoomControlsEnabled()) {
                        if (this.av.isNeedUpdateZoomControllerState()) {
                            h();
                        }
                    }
                    if (this.av.getChangeGridRatio() != WeightedLatLng.DEFAULT_INTENSITY) {
                        g(true);
                    }
                    if (this.af.isCompassEnabled()) {
                        if (this.av.isTiltChanged() || this.av.isBearingChanged()) {
                            g();
                        }
                    }
                    if (this.af.isScaleControlsEnabled()) {
                        i();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int getMapWidth() {
        return this.d.getWidth();
    }

    public int getMapHeight() {
        return this.d.getHeight();
    }

    public void setMyTrafficStyle(MyTrafficStyle myTrafficStyle) {
        if (!(this.ar.booleanValue() || !this.an || myTrafficStyle == null)) {
            this.C = myTrafficStyle;
            this.J.setParameter(2201, 1, 1, 1, 1);
            this.J.setParameter(2202, myTrafficStyle.getSmoothColor(), myTrafficStyle.getSlowColor(), myTrafficStyle.getCongestedColor(), myTrafficStyle.getSeriousCongestedColor());
        }
    }

    private void K() {
        if (this.aw != null) {
            this.aw.b(2);
        }
    }

    private synchronized void L() {
        this.aw.b();
    }

    private synchronized void M() {
        this.aw.a();
    }

    private synchronized void N() {
        try {
            if (!this.ay) {
                this.c.a();
                this.c.a(true);
                this.c.b(true);
                this.c.e(true);
                this.c.d(true);
                this.c.c(true);
                this.ay = true;
            }
        } catch (Throwable e) {
            fo.b(e, "AMapDelegateImp", "setInternaltexture");
            e.printStackTrace();
        } catch (Throwable e2) {
            fo.b(e2, "AMapDelegateImp", "setInternaltexture");
            e2.printStackTrace();
        }
    }

    public int k() {
        return this.q;
    }

    public void l() {
        if (this.an) {
            this.n.sendEmptyMessage(21);
        }
    }

    public void setZOrderOnTop(boolean z) {
        this.d.setZOrderOnTop(z);
    }

    public CameraPosition getCameraPosition() throws RemoteException {
        return f(this.z);
    }

    public float getMaxZoomLevel() {
        return this.av.getMaxZoomLevel();
    }

    public float getMinZoomLevel() {
        return this.av.getMinZoomLevel();
    }

    public void moveCamera(CameraUpdate cameraUpdate) throws RemoteException {
        if (cameraUpdate != null) {
            a(cameraUpdate.getCameraUpdateFactoryDelegate());
        }
    }

    public void a(MapMessage mapMessage) throws RemoteException {
        if (this.J != null && !this.ar.booleanValue()) {
            if (this.v || this.w) {
                if (this.J.getStateMessageCount() > 0) {
                    MapMessage c = ag.c();
                    c.a = com.amap.api.mapcore.util.af.a.changeGeoCenterZoomTiltBearing;
                    c.geoPoint = new IPoint(this.av.getS_x(), this.av.getS_y());
                    c.zoom = this.av.getS_z();
                    c.bearing = this.av.getS_r();
                    c.tilt = this.av.getS_c();
                    this.J.addMessage(mapMessage);
                    while (this.J.getStateMessageCount() > 0) {
                        if (((af) this.J.getStateMessage()) != null) {
                            mapMessage.mergeCameraUpdateDelegate(c);
                        }
                    }
                    mapMessage = c;
                }
            }
            K();
            this.J.clearAnimations();
            mapMessage.isChangeFinished = true;
            c(mapMessage);
            this.J.addMessage(mapMessage);
        }
    }

    public void animateCamera(CameraUpdate cameraUpdate) throws RemoteException {
        animateCameraWithCallback(cameraUpdate, null);
    }

    public void b(MapMessage mapMessage) throws RemoteException {
        a(mapMessage, 250, null);
    }

    public void animateCameraWithCallback(CameraUpdate cameraUpdate, CancelableCallback cancelableCallback) throws RemoteException {
        animateCameraWithDurationAndCallback(cameraUpdate, 250, cancelableCallback);
    }

    public void animateCameraWithDurationAndCallback(CameraUpdate cameraUpdate, long j, CancelableCallback cancelableCallback) throws RemoteException {
        if (cameraUpdate != null) {
            a(cameraUpdate.getCameraUpdateFactoryDelegate(), j, cancelableCallback);
        }
    }

    public void a(MapMessage mapMessage, long j, CancelableCallback cancelableCallback) throws RemoteException {
        if (this.v || this.w) {
            a(mapMessage);
            return;
        }
        if (this.J.getAnimateionsCount() > 0 && this.aj != null) {
            try {
                this.aj.onCancel();
            } catch (Throwable th) {
                fo.b(th, "AMapDelegateImp", "CancelableCallback.onCancel");
                th.printStackTrace();
            }
        }
        K();
        if (cancelableCallback != null) {
            this.aj = cancelableCallback;
        }
        c(mapMessage);
        this.J.clearAnimations();
        MapProjection mapProjection = new MapProjection(this.J);
        this.J.addMapAnimation(mapMessage.generateMapAnimation(mapProjection, (int) j));
        mapProjection.recycle();
    }

    private void c(MapMessage mapMessage) {
        mapMessage.isUseAnchor = this.z;
        if (this.z) {
            mapMessage.anchorX = this.A;
            mapMessage.anchorY = this.B;
        }
        if (mapMessage.width == 0) {
            mapMessage.width = getMapWidth();
        }
        if (mapMessage.height == 0) {
            mapMessage.height = getMapHeight();
        }
        mapMessage.mapConfig = this.av;
    }

    public void stopAnimation() throws RemoteException {
        if (this.J != null && this.J.getAnimateionsCount() > 0) {
            a(true, null);
            this.J.clearAnimations();
            if (this.aj != null) {
                try {
                    this.aj.onCancel();
                } catch (Throwable th) {
                    fo.b(th, "AMapDelegateImp", "CancelableCallback.onCancel");
                    th.printStackTrace();
                }
            }
            this.aj = null;
        }
        K();
    }

    public Polyline addPolyline(PolylineOptions polylineOptions) throws RemoteException {
        K();
        IPolyline a = this.b.a(polylineOptions);
        if (a == null) {
            return null;
        }
        return new Polyline(a);
    }

    public NavigateArrow addNavigateArrow(NavigateArrowOptions navigateArrowOptions) throws RemoteException {
        K();
        INavigateArrow a = this.b.a(navigateArrowOptions);
        if (a == null) {
            return null;
        }
        return new NavigateArrow(a);
    }

    public Polygon addPolygon(PolygonOptions polygonOptions) throws RemoteException {
        K();
        IPolygon a = this.b.a(polygonOptions);
        if (a == null) {
            return null;
        }
        return new Polygon(a);
    }

    public Circle addCircle(CircleOptions circleOptions) throws RemoteException {
        K();
        ICircle a = this.b.a(circleOptions);
        if (a == null) {
            return null;
        }
        return new Circle(a);
    }

    public Arc addArc(ArcOptions arcOptions) throws RemoteException {
        K();
        IArc a = this.b.a(arcOptions);
        if (a == null) {
            return null;
        }
        return new Arc(a);
    }

    public GroundOverlay addGroundOverlay(GroundOverlayOptions groundOverlayOptions) throws RemoteException {
        K();
        IGroundOverlay a = this.b.a(groundOverlayOptions);
        if (a == null) {
            return null;
        }
        return new GroundOverlay(a);
    }

    public Marker addMarker(MarkerOptions markerOptions) throws RemoteException {
        K();
        return this.e.a(markerOptions);
    }

    public Text addText(TextOptions textOptions) throws RemoteException {
        K();
        return this.e.a(textOptions);
    }

    public ArrayList<Marker> addMarkers(ArrayList<MarkerOptions> arrayList, boolean z) throws RemoteException {
        K();
        return this.e.a((ArrayList) arrayList, z);
    }

    public TileOverlay addTileOverlay(TileOverlayOptions tileOverlayOptions) throws RemoteException {
        return this.f.a(tileOverlayOptions);
    }

    public void clear() throws RemoteException {
        try {
            clear(false);
        } catch (Throwable th) {
            fo.b(th, "AMapDelegateImp", "clear");
            th.printStackTrace();
        }
    }

    public void clear(boolean z) throws RemoteException {
        String str = null;
        try {
            String str2;
            n();
            if (this.ai == null) {
                str2 = null;
            } else if (z) {
                str2 = this.ai.c();
                str = this.ai.d();
            } else {
                this.ai.e();
                str2 = null;
            }
            this.b.b(str);
            this.f.b();
            this.e.a(str2);
            if (this.g != null) {
                this.g.k();
            }
            K();
        } catch (Throwable th) {
            fo.b(th, "AMapDelegateImp", "clear");
            th.printStackTrace();
        }
    }

    public int getMapType() throws RemoteException {
        return this.I;
    }

    public void setMapType(int i) throws RemoteException {
        this.I = i;
        if (i == 1) {
            a(com.amap.api.mapcore.util.i.a.NORAML, com.amap.api.mapcore.util.i.c.DAY);
        } else if (i == 2) {
            a(com.amap.api.mapcore.util.i.a.SATELLITE, com.amap.api.mapcore.util.i.c.DAY);
        } else if (i == 3) {
            a(com.amap.api.mapcore.util.i.a.NORAML, com.amap.api.mapcore.util.i.c.NIGHT, com.amap.api.mapcore.util.i.b.NAVI_CAR);
        } else if (i != 4) {
            try {
                this.I = 1;
                a(com.amap.api.mapcore.util.i.a.NORAML, com.amap.api.mapcore.util.i.c.DAY);
            } catch (Throwable th) {
                fo.b(th, "AMapDelegateImp", "setMaptype");
                th.printStackTrace();
                return;
            }
        } else {
            a(com.amap.api.mapcore.util.i.a.NORAML, com.amap.api.mapcore.util.i.c.DAY, com.amap.api.mapcore.util.i.b.NAVI_CAR);
        }
        K();
    }

    public void a(com.amap.api.mapcore.util.i.a aVar, com.amap.api.mapcore.util.i.c cVar) {
        a(aVar, cVar, com.amap.api.mapcore.util.i.b.NORMAL);
    }

    public void a(com.amap.api.mapcore.util.i.a aVar, com.amap.api.mapcore.util.i.c cVar, com.amap.api.mapcore.util.i.b bVar) {
        if (this.D != cVar || this.E != aVar || this.F != bVar) {
            if (ap) {
                final com.amap.api.mapcore.util.i.c cVar2 = this.D;
                final com.amap.api.mapcore.util.i.a aVar2 = this.E;
                if (this.ay && this.an) {
                    final com.amap.api.mapcore.util.i.c cVar3 = cVar;
                    final com.amap.api.mapcore.util.i.a aVar3 = aVar;
                    final com.amap.api.mapcore.util.i.b bVar2 = bVar;
                    a(new Runnable(this) {
                        final /* synthetic */ b f;

                        public void run() {
                            int i;
                            String b = this.f.c.b();
                            String c = this.f.c.c();
                            this.f.D = cVar3;
                            this.f.E = aVar3;
                            this.f.F = bVar2;
                            String b2 = this.f.c.b();
                            String c2 = this.f.c.c();
                            if (this.f.E == com.amap.api.mapcore.util.i.a.SATELLITE || this.f.D == com.amap.api.mapcore.util.i.c.NIGHT || cVar2 == com.amap.api.mapcore.util.i.c.NIGHT || aVar2 == com.amap.api.mapcore.util.i.a.SATELLITE) {
                                this.f.n.post(new Runnable(this) {
                                    final /* synthetic */ AnonymousClass18 a;

                                    {
                                        this.a = r1;
                                    }

                                    public void run() {
                                        this.a.f.R();
                                    }
                                });
                            }
                            this.f.J.setParameter(2501, 0, 0, 0, 0);
                            if (!b.equals(b2)) {
                                this.f.c.a();
                            }
                            if (this.f.E == com.amap.api.mapcore.util.i.a.SATELLITE || aVar2 == com.amap.api.mapcore.util.i.a.SATELLITE) {
                                MapCore d = this.f.J;
                                if (this.f.E != com.amap.api.mapcore.util.i.a.SATELLITE) {
                                    i = 0;
                                } else {
                                    i = 1;
                                }
                                d.setParameter(2011, i, 0, 0, 0);
                            }
                            if (this.f.D == com.amap.api.mapcore.util.i.c.NIGHT || cVar2 == com.amap.api.mapcore.util.i.c.NIGHT) {
                                d = this.f.J;
                                if (this.f.D != com.amap.api.mapcore.util.i.c.NIGHT) {
                                    i = 0;
                                } else {
                                    i = 1;
                                }
                                d.setParameter(2401, i, 0, 0, 0);
                                this.f.c.d(true);
                                this.f.c.c(true);
                            }
                            if (!c.equals(c2)) {
                                this.f.c.a(true);
                            }
                            this.f.c.b(true);
                            if (this.f.F != null) {
                                this.f.J.setParameter(2013, this.f.E.ordinal(), this.f.D.ordinal(), this.f.F.ordinal(), 0);
                            }
                            this.f.J.setParameter(2501, 1, 1, 0, 0);
                        }
                    });
                } else {
                    this.aG.c = aVar;
                    this.aG.d = cVar;
                    this.aG.b = true;
                }
            } else {
                this.D = cVar;
                this.E = aVar;
                this.F = bVar;
            }
        }
    }

    public boolean isTrafficEnabled() throws RemoteException {
        return this.av.isTrafficEnabled();
    }

    public void setTrafficEnabled(final boolean z) throws RemoteException {
        if (!this.ar.booleanValue()) {
            this.av.setTrafficEnabled(z);
            K();
            a(new Runnable(this) {
                final /* synthetic */ b b;

                public void run() {
                    if (this.b.J != null) {
                        this.b.J.setParameter(2010, !z ? 0 : 1, 0, 0, 0);
                    }
                }
            });
        }
    }

    public void setMapTextEnable(final boolean z) throws RemoteException {
        if (!this.ar.booleanValue()) {
            this.av.setMapTextEnable(z);
            K();
            a(new Runnable(this) {
                final /* synthetic */ b b;

                public void run() {
                    if (this.b.J != null) {
                        this.b.J.setParameter(1024, !z ? 0 : 1, 0, 0, 0);
                    }
                }
            });
        }
    }

    public boolean isIndoorEnabled() throws RemoteException {
        return this.av.isIndoorEnable();
    }

    public void setIndoorEnabled(final boolean z) throws RemoteException {
        if (!this.ar.booleanValue()) {
            this.av.setIndoorEnable(z);
            K();
            if (z) {
                this.J.setParameter(1026, 1, 0, 0, 0);
            } else {
                this.J.setParameter(1026, 0, 0, 0, 0);
                this.av.maxZoomLevel = !this.av.isSetLimitZoomLevel() ? MapConfig.MAX_ZOOM : this.av.getMaxZoomLevel();
                if (this.af.isZoomControlsEnabled()) {
                    h();
                }
            }
            if (this.af.isIndoorSwitchEnabled()) {
                this.n.post(new Runnable(this) {
                    final /* synthetic */ b b;

                    public void run() {
                        if (z) {
                            this.b.b(true);
                        } else if (this.b.g != null && this.b.g.f() != null) {
                            this.b.g.f().a(false);
                        }
                    }
                });
            }
        }
    }

    public void set3DBuildingEnabled(final boolean z) throws RemoteException {
        if (!this.ar.booleanValue()) {
            this.av.setBuildingEnable(z);
            K();
            a(new Runnable(this) {
                final /* synthetic */ b b;

                public void run() {
                    if (this.b.J != null) {
                        this.b.J.setParameter(1021, !z ? 0 : 1, 0, 0, 0);
                    }
                }
            });
        }
    }

    public boolean isMyLocationEnabled() throws RemoteException {
        return this.am;
    }

    public void setMyLocationEnabled(boolean z) throws RemoteException {
        try {
            if (this.g != null) {
                el g = this.g.g();
                if (this.ag == null) {
                    g.a(false);
                } else if (z) {
                    this.ag.activate(this.aa);
                    g.a(true);
                    if (this.ai == null) {
                        this.ai = new da(this, this.K);
                    }
                } else {
                    if (this.ai != null) {
                        this.ai.b();
                        this.ai = null;
                    }
                    this.al = null;
                    this.ag.deactivate();
                }
            }
            if (!z) {
                this.af.setMyLocationButtonEnabled(z);
            }
            this.am = z;
            K();
        } catch (Throwable th) {
            fo.b(th, "AMapDelegateImp", "setMyLocationEnabled");
            th.printStackTrace();
        }
    }

    public Location getMyLocation() throws RemoteException {
        if (this.ag == null) {
            return null;
        }
        return this.aa.a;
    }

    public void setLocationSource(LocationSource locationSource) throws RemoteException {
        try {
            if (!this.ar.booleanValue()) {
                this.ag = locationSource;
                if (locationSource == null) {
                    this.g.g().a(false);
                } else {
                    this.g.g().a(true);
                }
            }
        } catch (Throwable th) {
            fo.b(th, "AMapDelegateImp", "setLocationSource");
            th.printStackTrace();
        }
    }

    public o m() throws RemoteException {
        return this.af;
    }

    public UiSettings getAMapUiSettings() throws RemoteException {
        return new UiSettings(this.af);
    }

    public Projection getAMapProjection() throws RemoteException {
        return new Projection(this.ae);
    }

    public void setOnCameraChangeListener(OnCameraChangeListener onCameraChangeListener) throws RemoteException {
        this.T = onCameraChangeListener;
    }

    public void setOnMapClickListener(OnMapClickListener onMapClickListener) throws RemoteException {
        this.U = onMapClickListener;
    }

    public void setOnMapTouchListener(OnMapTouchListener onMapTouchListener) throws RemoteException {
        this.V = onMapTouchListener;
    }

    public void setOnPOIClickListener(OnPOIClickListener onPOIClickListener) throws RemoteException {
        this.W = onPOIClickListener;
    }

    public void setOnMapLongClickListener(OnMapLongClickListener onMapLongClickListener) throws RemoteException {
        this.X = onMapLongClickListener;
    }

    public void setOnMarkerClickListener(OnMarkerClickListener onMarkerClickListener) throws RemoteException {
        this.P = onMarkerClickListener;
    }

    public void setOnPolylineClickListener(OnPolylineClickListener onPolylineClickListener) throws RemoteException {
        this.Q = onPolylineClickListener;
    }

    public void setOnMarkerDragListener(OnMarkerDragListener onMarkerDragListener) throws RemoteException {
        this.R = onMarkerDragListener;
    }

    public void setOnMaploadedListener(OnMapLoadedListener onMapLoadedListener) throws RemoteException {
        this.S = onMapLoadedListener;
    }

    public void setOnInfoWindowClickListener(OnInfoWindowClickListener onInfoWindowClickListener) throws RemoteException {
        this.Y = onInfoWindowClickListener;
    }

    public void setOnIndoorBuildingActiveListener(OnIndoorBuildingActiveListener onIndoorBuildingActiveListener) throws RemoteException {
        this.Z = onIndoorBuildingActiveListener;
    }

    public void setInfoWindowAdapter(InfoWindowAdapter infoWindowAdapter) throws RemoteException {
        if (!this.ar.booleanValue()) {
            if (infoWindowAdapter instanceof MultiPositionInfoWindowAdapter) {
                if (this.j != null) {
                    this.j.d();
                }
                this.j = this.N;
                this.N.a((MultiPositionInfoWindowAdapter) infoWindowAdapter);
            } else {
                this.N.destroy();
                this.j = this.g;
                this.g.a(infoWindowAdapter);
            }
        }
    }

    public View getView() throws RemoteException {
        return this.g;
    }

    public float a(int i) {
        if (this.an && this.M != null) {
            return this.M.getMapLenWithWin(i);
        }
        return 0.0f;
    }

    public void a(int i, int i2, DPoint dPoint) {
        if (this.M != null) {
            a(this.M, i, i2, dPoint);
        }
    }

    private void a(MapProjection mapProjection, int i, int i2, DPoint dPoint) {
        if (this.an) {
            FPoint fPoint = new FPoint();
            mapProjection.win2Map(i, i2, fPoint);
            IPoint iPoint = new IPoint();
            mapProjection.map2Geo(fPoint.x, fPoint.y, iPoint);
            MapProjection.geo2LonLat(iPoint.x, iPoint.y, dPoint);
        }
    }

    public void a(int i, int i2, IPoint iPoint) {
        if (this.an && this.M != null) {
            FPoint fPoint = new FPoint();
            this.M.win2Map(i, i2, fPoint);
            this.M.map2Geo(fPoint.x, fPoint.y, iPoint);
        }
    }

    public void b(int i, int i2, IPoint iPoint) {
        if (this.an && this.M != null) {
            FPoint fPoint = new FPoint();
            this.M.geo2Map(i, i2, fPoint);
            this.M.map2Win(fPoint.x, fPoint.y, iPoint);
        }
    }

    public void a(double d, double d2, FPoint fPoint) {
        if (this.an && this.M != null) {
            IPoint iPoint = new IPoint();
            MapProjection.lonlat2Geo(d2, d, iPoint);
            this.M.geo2Map(iPoint.x, iPoint.y, fPoint);
        }
    }

    public void a(double d, double d2, IPoint iPoint) {
        MapProjection.lonlat2Geo(d2, d, iPoint);
    }

    public void a(int i, int i2, FPoint fPoint) {
        if (this.an && this.M != null) {
            this.M.win2Map(i, i2, fPoint);
        }
    }

    public void b(int i, int i2, FPoint fPoint) {
        if (this.an && this.M != null) {
            this.M.geo2Map(i2, i, fPoint);
        }
    }

    public void a(float f, float f2, IPoint iPoint) {
        if (this.an && this.M != null) {
            this.M.map2Geo(f, f2, iPoint);
        }
    }

    public void b(int i, int i2, DPoint dPoint) {
        MapProjection.geo2LonLat(i, i2, dPoint);
    }

    public void b(double d, double d2, IPoint iPoint) {
        if (this.an) {
            MapProjection mapProjection = new MapProjection(this.J);
            mapProjection.recalculate();
            IPoint iPoint2 = new IPoint();
            FPoint fPoint = new FPoint();
            MapProjection.lonlat2Geo(d2, d, iPoint2);
            mapProjection.geo2Map(iPoint2.x, iPoint2.y, fPoint);
            mapProjection.map2Win(fPoint.x, fPoint.y, iPoint);
            mapProjection.recycle();
        }
    }

    private LatLng O() {
        if (!this.an || this.M == null) {
            return null;
        }
        DPoint dPoint = new DPoint();
        IPoint iPoint = new IPoint();
        this.M.getGeoCenter(iPoint);
        MapProjection.geo2LonLat(iPoint.x, iPoint.y, dPoint);
        return new LatLng(dPoint.y, dPoint.x, false);
    }

    public CameraPosition f(boolean z) {
        if (this.v || this.M == null) {
            DPoint dPoint = new DPoint();
            b(this.av.getS_x(), this.av.getS_y(), dPoint);
            return CameraPosition.builder().target(new LatLng(dPoint.y, dPoint.x)).bearing(this.av.getS_r()).tilt(this.av.getS_c()).zoom(this.av.getS_z()).build();
        }
        LatLng latLng;
        if (z) {
            dPoint = new DPoint();
            a(this.A, this.B, dPoint);
            latLng = new LatLng(dPoint.y, dPoint.x, false);
        } else {
            latLng = O();
        }
        return CameraPosition.builder().target(latLng).bearing(this.M.getMapAngle()).tilt(this.M.getCameraHeaderAngle()).zoom(this.M.getMapZoomer()).build();
    }

    private void P() {
        if (this.aE) {
            this.aE = false;
        }
        if (this.aD) {
            this.aD = false;
            MapMessage c = ag.c();
            c.isChangeFinished = true;
            this.J.addMessage(c);
        }
        if (this.az) {
            this.az = false;
            c = ag.c();
            c.isChangeFinished = true;
            this.J.addMessage(c);
        }
        this.aA = false;
        if (this.R != null && this.aB != null) {
            try {
                this.R.onMarkerDragEnd(this.aB);
            } catch (Throwable th) {
                fo.b(th, "AMapDelegateImp", "OnMarkerDragListener.onMarkerDragEnd");
                th.printStackTrace();
            }
            this.aB = null;
        }
    }

    private void e(MotionEvent motionEvent) throws RemoteException {
        if (this.aA && this.aB != null && this.aC != null) {
            int x = (int) motionEvent.getX();
            int y = (int) (motionEvent.getY() - BitmapDescriptorFactory.HUE_YELLOW);
            LatLng b = this.aC.b();
            LatLng position = this.aC.getPosition();
            DPoint dPoint = new DPoint();
            a(x, y, dPoint);
            this.aB.setPosition(new LatLng((position.latitude + dPoint.y) - b.latitude, (dPoint.x + position.longitude) - b.longitude));
            this.R.onMarkerDrag(this.aB);
        }
    }

    public boolean a(MotionEvent motionEvent) {
        if (!this.ao) {
            return false;
        }
        K();
        switch (motionEvent.getAction() & 255) {
            case 0:
                this.y = true;
                T();
                break;
            case 1:
                this.y = false;
                U();
                P();
                break;
        }
        if (motionEvent.getAction() == 2 && this.aA) {
            try {
                e(motionEvent);
            } catch (Throwable e) {
                fo.b(e, "AMapDelegateImp", "onDragMarker");
                e.printStackTrace();
            }
            return true;
        }
        if (this.x) {
            try {
                this.h.a(motionEvent);
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
            }
        }
        if (this.V != null) {
            this.n.removeMessages(16);
            Message obtainMessage = this.n.obtainMessage();
            obtainMessage.what = 16;
            obtainMessage.obj = MotionEvent.obtain(motionEvent);
            obtainMessage.sendToTarget();
        }
        return true;
    }

    public void onFling() {
        if (this.f != null) {
            this.f.b(true);
        }
        this.au = true;
    }

    public void b(MotionEvent motionEvent) {
        try {
            this.aD = false;
            this.aC = this.e.a(motionEvent);
            if (this.R != null && this.aC != null && this.aC.isDraggable()) {
                this.aB = new Marker(this.aC);
                LatLng position = this.aB.getPosition();
                LatLng b = this.aC.b();
                IPoint iPoint = new IPoint();
                b(b.latitude, b.longitude, iPoint);
                iPoint.y -= 60;
                DPoint dPoint = new DPoint();
                a(iPoint.x, iPoint.y, dPoint);
                this.aB.setPosition(new LatLng((position.latitude + dPoint.y) - b.latitude, (dPoint.x + position.longitude) - b.longitude));
                this.e.b(this.aC);
                this.R.onMarkerDragStart(this.aB);
                this.aA = true;
            } else if (this.X != null) {
                DPoint dPoint2 = new DPoint();
                a((int) motionEvent.getX(), (int) motionEvent.getY(), dPoint2);
                this.X.onMapLongClick(new LatLng(dPoint2.y, dPoint2.x));
                this.aE = true;
            }
        } catch (Throwable th) {
            fo.b(th, "AMapDelegateImp", "onLongPress");
            th.printStackTrace();
        }
    }

    public boolean c(MotionEvent motionEvent) {
        this.aD = false;
        if (this.aE) {
            this.aE = false;
            return true;
        }
        try {
            if (j(motionEvent) || i(motionEvent) || h(motionEvent)) {
                return true;
            }
            f(motionEvent);
            return true;
        } catch (Throwable th) {
            fo.b(th, "AMapDelegateImp", "onSingleTapUp");
            th.printStackTrace();
            return true;
        }
    }

    private void f(final MotionEvent motionEvent) {
        a(new Runnable(this) {
            final /* synthetic */ b b;

            public void run() {
                if (this.b.W == null) {
                    this.b.g(motionEvent);
                    return;
                }
                final Poi poiItem = this.b.J.getPoiItem((int) motionEvent.getX(), (int) motionEvent.getY(), 25);
                if (poiItem == null) {
                    this.b.g(motionEvent);
                } else {
                    this.b.n.post(new Runnable(this) {
                        final /* synthetic */ AnonymousClass3 b;

                        public void run() {
                            try {
                                this.b.b.W.onPOIClick(poiItem);
                            } catch (Throwable th) {
                                fo.b(th, "AMapDelegateImp", "OnPOIClickListener.onPOIClick");
                                th.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }

    private void g(final MotionEvent motionEvent) {
        this.n.post(new Runnable(this) {
            final /* synthetic */ b b;

            public void run() {
                if (this.b.U != null) {
                    DPoint dPoint = new DPoint();
                    this.b.a((int) motionEvent.getX(), (int) motionEvent.getY(), dPoint);
                    try {
                        this.b.U.onMapClick(new LatLng(dPoint.y, dPoint.x));
                    } catch (Throwable th) {
                        fo.b(th, "AMapDelegateImp", "OnMapClickListener.onMapClick");
                        th.printStackTrace();
                    }
                }
            }
        });
    }

    private boolean h(MotionEvent motionEvent) {
        if (this.Q != null) {
            DPoint dPoint = new DPoint();
            a((int) motionEvent.getX(), (int) motionEvent.getY(), dPoint);
            LatLng latLng = new LatLng(dPoint.y, dPoint.x);
            if (latLng != null) {
                ct a = this.b.a(latLng);
                if (a != null) {
                    this.Q.onPolylineClick(new Polyline((cw) a));
                    return true;
                }
            }
        }
        return false;
    }

    private boolean i(MotionEvent motionEvent) throws RemoteException {
        if (this.e.b(motionEvent)) {
            cu d = this.e.d();
            if (d == null || !d.isVisible()) {
                return true;
            }
            try {
                Marker marker = new Marker(d);
                if (this.P != null) {
                    if (this.P.onMarkerClick(marker) || this.e.b() <= 0) {
                        this.e.b(d);
                        return true;
                    }
                }
                a((cr) d);
                if (!d.g()) {
                    LatLng b = d.b();
                    if (b != null) {
                        IPoint iPoint = new IPoint();
                        a(b.latitude, b.longitude, iPoint);
                        a(ag.a(iPoint));
                    }
                }
                this.e.b(d);
                return true;
            } catch (Throwable th) {
                fo.b(th, "AMapDelegateImp", "onMarkerTap");
                th.printStackTrace();
            }
        }
        return false;
    }

    private boolean j(MotionEvent motionEvent) throws RemoteException {
        if (this.j == null || !this.j.a(motionEvent)) {
            return false;
        }
        if (this.Y != null) {
            IMarker d = this.e.d();
            if (!d.isVisible()) {
                return true;
            }
            this.Y.onInfoWindowClick(new Marker(d));
        }
        return true;
    }

    public boolean d(MotionEvent motionEvent) {
        if ((this.M == null ? this.av.getS_z() : this.M.getMapZoomer()) >= getMaxZoomLevel()) {
            return true;
        }
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        try {
            if (this.af.isZoomInByScreenCenter()) {
                x = getMapWidth() / 2;
                y = getMapHeight() / 2;
            }
            b(ag.a((float) WMElement.CAMERASIZEVALUE1B1, new Point(x, y)));
        } catch (Throwable th) {
            fo.b(th, "AMapDelegateImp", "onDoubleTap");
            th.printStackTrace();
        }
        return true;
    }

    public void n() {
        if (this.j != null) {
            this.j.d();
        }
    }

    public float o() {
        if (this.M == null) {
            return this.av.getS_z();
        }
        return this.M.getMapZoomer();
    }

    public FPoint[] p() {
        if (this.av.getMapRect() == null) {
            this.av.setMapRect(eh.a((l) this, true));
        }
        return this.av.getMapRect();
    }

    public LatLngBounds a(LatLng latLng, float f, float f2, float f3) {
        int mapWidth = getMapWidth();
        int mapHeight = getMapHeight();
        if (mapWidth <= 0 || mapHeight <= 0 || this.ar.booleanValue()) {
            return null;
        }
        float a = eh.a(this.av, f);
        MapProjection mapProjection = new MapProjection(this.J);
        if (latLng != null) {
            IPoint iPoint = new IPoint();
            MapProjection.lonlat2Geo(latLng.longitude, latLng.latitude, iPoint);
            mapProjection.setCameraHeaderAngle(0.0f);
            mapProjection.setMapAngle(0.0f);
            mapProjection.setGeoCenter(iPoint.x, iPoint.y);
            mapProjection.setMapZoomer(a);
            mapProjection.recalculate();
        }
        DPoint dPoint = new DPoint();
        a(mapProjection, 0, 0, dPoint);
        LatLng latLng2 = new LatLng(dPoint.y, dPoint.x, false);
        a(mapProjection, mapWidth, mapHeight, dPoint);
        LatLng latLng3 = new LatLng(dPoint.y, dPoint.x, false);
        mapProjection.recycle();
        return LatLngBounds.builder().include(latLng3).include(latLng2).build();
    }

    public Point q() {
        if (this.g == null) {
            return new Point();
        }
        return this.g.a();
    }

    public void getMapPrintScreen(onMapPrintScreenListener onmapprintscreenlistener) {
        this.ab = onmapprintscreenlistener;
        this.as = true;
        K();
    }

    public void getMapScreenShot(OnMapScreenShotListener onMapScreenShotListener) {
        this.ac = onMapScreenShotListener;
        this.as = true;
        K();
    }

    public void b(int i) {
        if (this.g != null) {
            this.g.b(i);
        }
    }

    public void c(int i) {
        if (this.g != null) {
            this.g.c(i);
        }
    }

    public void d(int i) {
        if (this.g != null) {
            this.g.d(i);
        }
    }

    public float e(int i) {
        if (this.g == null) {
            return 0.0f;
        }
        return this.g.e(i);
    }

    public void a(int i, float f) {
        if (this.g != null) {
            this.g.a(i, f);
        }
    }

    public void f(int i) {
        if (this.g != null) {
            this.g.a(i);
        }
    }

    public float getScalePerPixel() {
        try {
            LatLng latLng = getCameraPosition().target;
            float s_z = this.av.getS_z();
            if (this.an && this.M != null) {
                s_z = this.M.getMapZoomer();
            }
            return ((float) ((((Math.cos((latLng.latitude * 3.141592653589793d) / VirtualEarthProjection.MaxLongitude) * 2.0d) * 3.141592653589793d) * 6378137.0d) / (Math.pow(2.0d, (double) s_z) * 256.0d))) * u();
        } catch (Throwable th) {
            fo.b(th, "AMapDelegateImp", "getScalePerPixel");
            th.printStackTrace();
            return 0.0f;
        }
    }

    void g(boolean z) {
        int i;
        Handler handler = this.n;
        if (z) {
            i = 1;
        } else {
            i = 0;
        }
        handler.obtainMessage(19, i, 0).sendToTarget();
    }

    protected void a(boolean z, CameraPosition cameraPosition) {
        if (this.av != null && this.av.getChangedCounter() != 0) {
            this.av.resetChangedCounter();
            if (this.ad != null) {
                this.ad.onMapStable();
            }
            if (this.T != null && this.d.isEnabled()) {
                if (cameraPosition == null) {
                    try {
                        cameraPosition = getCameraPosition();
                    } catch (Throwable e) {
                        fo.b(e, "AMapDelegateImp", "cameraChangeFinish");
                        e.printStackTrace();
                    }
                }
                this.T.onCameraChangeFinish(cameraPosition);
            }
        }
    }

    public List<Marker> getMapScreenMarkers() {
        if (eh.a(getMapWidth(), getMapHeight())) {
            return this.e.f();
        }
        return new ArrayList();
    }

    public void r() {
        this.b.c();
    }

    public void setCenterToPixel(int i, int i2) {
        if (this.L != null) {
            this.z = true;
            this.A = i;
            this.B = i2;
        }
    }

    public void setMapTextZIndex(int i) {
        this.ak = i;
    }

    public int getMapTextZIndex() {
        return this.ak;
    }

    public boolean isMaploaded() {
        return this.ao;
    }

    public void setLoadOfflineData(final boolean z) throws RemoteException {
        a(new Runnable(this) {
            final /* synthetic */ b b;

            public void run() {
                int i;
                MapCore d = this.b.J;
                if (z) {
                    i = 1;
                } else {
                    i = 0;
                }
                d.setParameter(2601, i, 0, 0, 0);
            }
        });
    }

    public void removecache() {
        removecache(null);
    }

    public void removecache(OnCacheRemoveListener onCacheRemoveListener) {
        if (this.n != null) {
            try {
                this.J.setParameter(2601, 0, 0, 0, 0);
                Runnable cVar = new c(this, this.K, onCacheRemoveListener);
                this.n.removeCallbacks(cVar);
                this.n.post(cVar);
            } catch (Throwable th) {
                fo.b(th, "AMapDelegateImp", "removecache");
                th.printStackTrace();
            }
        }
    }

    public void s() {
        if (this.b != null) {
            this.b.d();
        }
        if (this.e != null) {
            this.e.c();
        }
        if (this.o != null) {
            this.o.OnMapReferencechanged();
        }
    }

    public void setVisibilityEx(int i) {
        this.d.setVisibility(i);
    }

    public void a(aq aqVar) throws RemoteException {
        if (this.av.isIndoorEnable()) {
            final ek f = this.g.f();
            if (aqVar != null) {
                if (this.i == null || !this.i.poiid.equals(aqVar.poiid) || !f.c()) {
                    if (this.i == null || !this.i.poiid.equals(aqVar.poiid) || this.i.g == null) {
                        this.i = aqVar;
                        this.i.g = new IPoint();
                        if (this.M != null) {
                            this.M.getGeoCenter(this.i.g);
                        }
                    }
                    if (this.Z != null) {
                        this.Z.OnIndoorBuilding(aqVar);
                    }
                    this.av.maxZoomLevel = !this.av.isSetLimitZoomLevel() ? MapConfig.MAX_ZOOM_INDOOR : this.av.getMaxZoomLevel();
                    if (this.af.isZoomControlsEnabled()) {
                        h();
                    }
                    if (this.af.isIndoorSwitchEnabled() && !f.c()) {
                        this.af.setIndoorSwitchEnabled(true);
                        this.n.post(new Runnable(this) {
                            final /* synthetic */ b b;

                            public void run() {
                                try {
                                    f.a(this.b.i.floor_names);
                                    f.a(this.b.i.activeFloorName);
                                } catch (Throwable th) {
                                    th.printStackTrace();
                                }
                            }
                        });
                    } else if (!this.af.isIndoorSwitchEnabled() && f.c()) {
                        this.af.setIndoorSwitchEnabled(false);
                    }
                }
            } else if (!f()) {
                if (this.Z != null) {
                    this.Z.OnIndoorBuilding(aqVar);
                }
                if (this.i != null) {
                    this.i.g = null;
                }
                if (f.c()) {
                    this.n.post(new Runnable(this) {
                        final /* synthetic */ b b;

                        public void run() {
                            f.setVisibility(8);
                        }
                    });
                }
                this.av.maxZoomLevel = !this.av.isSetLimitZoomLevel() ? MapConfig.MAX_ZOOM : this.av.getMaxZoomLevel();
                if (this.af.isZoomControlsEnabled()) {
                    h();
                }
            }
        }
    }

    public void setIndoorBuildingInfo(IndoorBuildingInfo indoorBuildingInfo) throws RemoteException {
        if (!this.ar.booleanValue() && indoorBuildingInfo != null && indoorBuildingInfo.activeFloorName != null && indoorBuildingInfo.poiid != null) {
            this.i = (aq) indoorBuildingInfo;
            K();
            this.k = 0;
            a(new Runnable(this) {
                final /* synthetic */ b a;

                {
                    this.a = r1;
                }

                public void run() {
                    this.a.J.setIndoorBuildingToBeActive(this.a.i.activeFloorName, this.a.i.activeFloorIndex, this.a.i.poiid);
                }
            });
        }
    }

    public void setCustomRenderer(CustomRenderer customRenderer) {
        this.o = customRenderer;
    }

    public Context t() {
        return this.K;
    }

    public void a(Runnable runnable) {
        if (this.d != null) {
            this.d.queueEvent(runnable);
        }
    }

    public void onSurfaceCreated(GL10 gl10, EGLConfig eGLConfig) {
        try {
            if (!this.an) {
                C();
            }
            this.ay = false;
            this.J.setGL(gl10);
            N();
            if (this.D != com.amap.api.mapcore.util.i.c.NIGHT) {
                this.g.c().a(ei.a);
            } else {
                this.J.setParameter(2401, 1, 0, 0, 0);
                this.g.c().a(ei.b);
            }
            this.J.surfaceCreate(gl10);
            b(gl10);
            this.at = false;
            S();
            L();
            K();
            Q();
            if (this.o != null) {
                this.o.onSurfaceCreated(gl10, eGLConfig);
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public void onSurfaceChanged(GL10 gl10, int i, int i2) {
        try {
            this.ah = new Rect(0, 0, i, i2);
            this.J.setGL(gl10);
            this.J.surfaceChange(gl10, i, i2);
            if (this.av != null) {
                this.av.updateMapRectNextFrame(true);
            }
            E();
            if (this.aG.b) {
                if (this.n == null) {
                    this.aG.run();
                } else {
                    this.n.post(this.aG);
                }
            }
            K();
            if (this.o != null) {
                this.o.onSurfaceChanged(gl10, i, i2);
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private void b(GL10 gl10) {
        if (this.r == null || this.r.isRecycled()) {
            this.r = eh.a(this.K, "amap_sdk_lineTexture.png");
        }
        if (this.s == null || this.s.isRecycled()) {
            this.s = eh.a(this.K, "amap_sdk_lineDashTexture.png");
        }
        this.p = eh.a(gl10, this.r);
        this.q = eh.a(gl10, this.s, true);
    }

    private void Q() {
        if (!ap) {
            try {
                this.aF.setName("AuthThread");
                this.aF.start();
                ap = true;
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    public float u() {
        return this.t;
    }

    public com.amap.api.mapcore.util.i.c v() {
        return this.D;
    }

    public com.amap.api.mapcore.util.i.a w() {
        return this.E;
    }

    public com.amap.api.mapcore.util.i.b x() {
        return this.F;
    }

    private void R() {
        boolean z = false;
        if (!this.ar.booleanValue()) {
            if (!(this.E == com.amap.api.mapcore.util.i.a.SATELLITE || this.D == com.amap.api.mapcore.util.i.c.NIGHT)) {
                z = true;
            }
            this.g.f(z);
        }
    }

    public void setRenderFps(int i) {
        try {
            this.a = Math.max(10, Math.min(i, 40));
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public void setZoomScaleParam(float f) {
        this.t = f;
    }

    public float getCameraAngle() {
        return this.av.getS_c();
    }

    public float y() {
        return this.av.getS_r();
    }

    public Handler getMainHandler() {
        return this.n;
    }

    public MapConfig getMapConfig() {
        return this.av;
    }

    public void reloadMap() {
        this.ao = false;
        e();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        d();
    }

    private void S() {
        if (this.H != null) {
            this.H.clear();
        }
        this.e.h();
        this.b.f();
        this.f.g();
    }

    private void T() {
        if (this.an) {
            this.h.a();
            this.x = true;
            this.J.clearAllMessages();
            this.J.clearAnimations();
            a(new Runnable(this) {
                final /* synthetic */ b a;

                {
                    this.a = r1;
                }

                public void run() {
                    this.a.J.setParameter(4001, 1, 0, 0, 0);
                }
            });
        }
    }

    private void U() {
        this.x = true;
        a(new Runnable(this) {
            final /* synthetic */ b a;

            {
                this.a = r1;
            }

            public void run() {
                this.a.J.setParameter(4001, 0, 0, 0, 0);
            }
        });
    }

    public View z() {
        return (View) this.d;
    }

    public void onChangeFinish() {
        K();
        Message obtainMessage = this.n.obtainMessage();
        obtainMessage.what = 12;
        this.n.sendMessage(obtainMessage);
    }

    private synchronized void V() {
        Iterator it = this.H.iterator();
        while (it.hasNext()) {
            ((s) it.next()).a().recycle();
        }
        this.H.clear();
    }

    public synchronized void a(s sVar) {
        if (sVar == null) {
            return;
        }
        if (sVar.b() != 0) {
            this.H.add(sVar);
        }
    }

    public synchronized void g(int i) {
        Iterator it = this.H.iterator();
        while (it.hasNext()) {
            s sVar = (s) it.next();
            if (sVar.b() == i) {
                this.H.remove(sVar);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized int a(BitmapDescriptor bitmapDescriptor) {
        if (bitmapDescriptor != null) {
            if (!(bitmapDescriptor.getBitmap() == null || bitmapDescriptor.getBitmap().isRecycled())) {
                for (int i = 0; i < this.H.size(); i++) {
                    s sVar = (s) this.H.get(i);
                    if (sVar.a().equals(bitmapDescriptor)) {
                        return sVar.b();
                    }
                }
                return 0;
            }
        }
    }

    public int a(IMarkerAction iMarkerAction, Rect rect) {
        if (this.e == null || iMarkerAction == null || rect == null) {
            return 0;
        }
        return this.e.a(iMarkerAction, rect);
    }

    public InfoWindowAnimationManager getInfoWindowAnimationManager() {
        return new InfoWindowAnimationManager(this.N);
    }

    public float getZoomToSpanLevel(LatLng latLng, LatLng latLng2) {
        MapConfig mapConfig = getMapConfig();
        if (latLng == null || latLng2 == null || !this.an || this.ar.booleanValue()) {
            return mapConfig.getS_z();
        }
        Builder builder = new Builder();
        builder.include(latLng);
        builder.include(latLng2);
        MapProjection mapProjection = new MapProjection(this.J);
        Pair a = eh.a(mapProjection, mapConfig, 0, 0, 0, 0, builder.build(), getMapWidth(), getMapHeight());
        mapProjection.recycle();
        if (a == null) {
            return mapProjection.getMapZoomer();
        }
        return ((Float) a.first).floatValue();
    }

    public Pair<Float, LatLng> calculateZoomToSpanLevel(int i, int i2, int i3, int i4, LatLng latLng, LatLng latLng2) {
        MapConfig mapConfig = getMapConfig();
        if (latLng == null || latLng2 == null || !this.an || this.ar.booleanValue()) {
            DPoint dPoint = new DPoint();
            MapProjection.geo2LonLat(mapConfig.getS_x(), mapConfig.getS_y(), dPoint);
            return new Pair(Float.valueOf(mapConfig.getS_z()), new LatLng(dPoint.y, dPoint.x));
        }
        Builder builder = new Builder();
        builder.include(latLng);
        builder.include(latLng2);
        MapProjection mapProjection = new MapProjection(this.J);
        Pair a = eh.a(mapProjection, mapConfig, i, i2, i3, i4, builder.build(), getMapWidth(), getMapHeight());
        mapProjection.recycle();
        if (a == null) {
            return null;
        }
        DPoint dPoint2 = new DPoint();
        MapProjection.geo2LonLat(((IPoint) a.second).x, ((IPoint) a.second).y, dPoint2);
        return new Pair(a.first, new LatLng(dPoint2.y, dPoint2.x));
    }

    public void setAMapGestureListener(AMapGestureListener aMapGestureListener) {
        this.ad = aMapGestureListener;
        this.h.a(aMapGestureListener);
    }

    public void setMaskLayerParams(int i, int i2, int i3, int i4, final int i5, long j) {
        try {
            if (this.ax != null) {
                dh dhVar;
                float f = ((float) i4) / 255.0f;
                if (i5 != -1) {
                    this.G = i5;
                    dhVar = new dh(0.0f, f);
                    if (((double) f) > 0.2d) {
                        if (this.g != null) {
                            this.g.i().setVisibility(4);
                        }
                    } else if (this.g != null) {
                        this.g.i().setVisibility(0);
                    }
                } else {
                    dhVar = new dh(f, 0.0f);
                    dhVar.a(new AnimationListener(this) {
                        final /* synthetic */ b b;

                        public void onAnimationStart() {
                        }

                        public void onAnimationEnd() {
                            this.b.n.post(new Runnable(this) {
                                final /* synthetic */ AnonymousClass14 a;

                                {
                                    this.a = r1;
                                }

                                public void run() {
                                    this.a.b.G = i5;
                                    if (this.a.b.g != null) {
                                        this.a.b.g.i().setVisibility(0);
                                    }
                                }
                            });
                        }
                    });
                }
                dhVar.a(new LinearInterpolator());
                dhVar.a(j);
                this.ax.a(i, i2, i3, i4);
                this.ax.a(dhVar);
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public int A() {
        return this.G;
    }

    public void setMaxZoomLevel(float f) {
        this.av.setMaxZoomLevel(f);
    }

    public void setMinZoomLevel(float f) {
        this.av.setMinZoomLevel(f);
    }

    public void setMapStatusLimits(LatLngBounds latLngBounds) {
        this.av.setLimitLatLngBounds(latLngBounds);
        W();
    }

    public void resetMinMaxZoomPreference() {
        this.av.resetMinMaxZoomPreference();
    }

    private void W() {
        LatLngBounds limitLatLngBounds = this.av.getLimitLatLngBounds();
        if (limitLatLngBounds == null || this.J == null) {
            this.av.setLimitIPoints(null);
            this.av.setLimitZoomLevel(0.0f);
            return;
        }
        MapProjection mapProjection = new MapProjection(this.J);
        MapProjection.lonlat2Geo(limitLatLngBounds.northeast.longitude, limitLatLngBounds.northeast.latitude, new IPoint());
        MapProjection.lonlat2Geo(limitLatLngBounds.southwest.longitude, limitLatLngBounds.southwest.latitude, new IPoint());
        this.av.setLimitIPoints(new IPoint[]{r2, r3});
        mapProjection.recycle();
        X();
    }

    private void X() {
        if (this.av.getLimitIPoints() != null && this.av.getLimitIPoints().length == 2) {
            IPoint iPoint = this.av.getLimitIPoints()[0];
            IPoint iPoint2 = this.av.getLimitIPoints()[1];
            MapProjection mapProjection = new MapProjection(this.J);
            float a = eh.a(mapProjection, getMapConfig(), iPoint, iPoint2, getMapWidth(), getMapHeight());
            mapProjection.recycle();
            setMinZoomLevel(a);
            this.av.setLimitZoomLevel(a);
        }
    }
}
