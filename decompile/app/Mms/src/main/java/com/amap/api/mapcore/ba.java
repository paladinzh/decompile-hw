package com.amap.api.mapcore;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.opengl.GLES10;
import android.os.Build.VERSION;
import android.os.RemoteException;
import android.util.Log;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.amap.api.mapcore.util.bj;
import com.amap.api.mapcore.util.ce;
import com.amap.api.mapcore.util.dq;
import com.amap.api.maps.AMapException;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.autonavi.amap.mapcore.DPoint;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapProjection;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: MarkerDelegateImp */
class ba implements ah {
    private static int a = 0;
    private boolean A = false;
    private boolean B = true;
    private aw C;
    private FloatBuffer D;
    private Object E;
    private boolean F = false;
    private CopyOnWriteArrayList<BitmapDescriptor> G = null;
    private boolean H = false;
    private boolean I = false;
    private boolean J = true;
    private int K = 0;
    private int L = 20;
    private boolean M = false;
    private int N;
    private int O;
    private long P = 0;
    private boolean b = false;
    private boolean c = false;
    private boolean d = false;
    private float e = 0.0f;
    private float f = 0.0f;
    private boolean g = false;
    private int h = 0;
    private int i = 0;
    private int j = 0;
    private int k = 0;
    private int l;
    private int m;
    private FPoint n = new FPoint();
    private float[] o;
    private int[] p = null;
    private float q = 0.0f;
    private boolean r = false;
    private FloatBuffer s = null;
    private String t;
    private LatLng u;
    private LatLng v;
    private String w;
    private String x;
    private float y = 0.5f;
    private float z = ContentUtil.FONT_SIZE_NORMAL;

    private static String c(String str) {
        a++;
        return str + a;
    }

    public void a(float f) {
        this.f = f;
        this.e = (((-f) % 360.0f) + 360.0f) % 360.0f;
        if (n()) {
            this.C.e(this);
            this.C.d(this);
        }
        N();
    }

    public boolean x() {
        return this.r;
    }

    public void y() {
        if (this.r) {
            try {
                if (this.G != null) {
                    Iterator it = this.G.iterator();
                    while (it.hasNext()) {
                        ((BitmapDescriptor) it.next()).recycle();
                    }
                    this.G = null;
                }
                if (this.D != null) {
                    this.D.clear();
                    this.D = null;
                }
                if (this.s != null) {
                    this.s.clear();
                    this.s = null;
                }
                this.u = null;
                this.E = null;
                this.p = null;
            } catch (Throwable th) {
                ce.a(th, "MarkerDelegateImp", "realdestroy");
                th.printStackTrace();
                Log.d("destroy erro", "MarkerDelegateImp destroy");
            }
        }
    }

    public void p() {
        try {
            int i;
            this.r = true;
            b();
            if (this.C != null) {
                this.C.a.N();
                i = 0;
                while (this.p != null && i < this.p.length) {
                    this.C.a(Integer.valueOf(this.p[i]));
                    this.C.a(this.p[i]);
                    i++;
                }
            }
            i = 0;
            while (this.G != null && i < this.G.size()) {
                ((BitmapDescriptor) this.G.get(i)).recycle();
                i++;
            }
        } catch (Throwable th) {
            ce.a(th, "MarkerDelegateImp", "destroy");
            th.printStackTrace();
            Log.d("destroy erro", "MarkerDelegateImp destroy");
        }
    }

    synchronized void a() {
        if (this.G != null) {
            this.G.clear();
        } else {
            this.G = new CopyOnWriteArrayList();
        }
    }

    public synchronized void b(ArrayList<BitmapDescriptor> arrayList) {
        a();
        if (arrayList != null) {
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                BitmapDescriptor bitmapDescriptor = (BitmapDescriptor) it.next();
                if (bitmapDescriptor != null) {
                    this.G.add(bitmapDescriptor);
                }
            }
        }
    }

    public ba(MarkerOptions markerOptions, aw awVar) {
        this.C = awVar;
        this.u = markerOptions.getPosition();
        IPoint iPoint = new IPoint();
        this.H = markerOptions.isGps();
        if (markerOptions.getPosition() != null) {
            if (this.H) {
                try {
                    double[] a = dq.a(markerOptions.getPosition().longitude, markerOptions.getPosition().latitude);
                    this.v = new LatLng(a[1], a[0]);
                    MapProjection.lonlat2Geo(a[0], a[1], iPoint);
                } catch (Throwable th) {
                    ce.a(th, "MarkerDelegateImp", "create");
                    this.v = markerOptions.getPosition();
                }
            } else {
                MapProjection.lonlat2Geo(this.u.longitude, this.u.latitude, iPoint);
            }
        }
        this.l = iPoint.x;
        this.m = iPoint.y;
        this.y = markerOptions.getAnchorU();
        this.z = markerOptions.getAnchorV();
        this.h = markerOptions.getInfoWindowOffsetX();
        this.i = markerOptions.getInfoWindowOffsetY();
        this.L = markerOptions.getPeriod();
        this.q = markerOptions.getZIndex();
        r();
        b(markerOptions.getIcons());
        this.B = markerOptions.isVisible();
        this.x = markerOptions.getSnippet();
        this.w = markerOptions.getTitle();
        this.A = markerOptions.isDraggable();
        this.t = h();
        this.F = markerOptions.isPerspective();
        this.g = markerOptions.isFlat();
    }

    public int K() {
        try {
            return M().getWidth();
        } catch (Throwable th) {
            return 0;
        }
    }

    public int L() {
        try {
            return M().getHeight();
        } catch (Throwable th) {
            return 0;
        }
    }

    public Rect d() {
        if (this.o == null) {
            return new Rect(0, 0, 0, 0);
        }
        try {
            Rect rect;
            MapProjection c = this.C.a.c();
            int K = K();
            int L = L();
            IPoint iPoint = new IPoint();
            IPoint iPoint2 = new IPoint();
            c.map2Win(this.n.x, this.n.y, iPoint);
            if (this.g) {
                c.map2Win(this.o[0], this.o[1], iPoint2);
                rect = new Rect(iPoint2.x, iPoint2.y, iPoint2.x, iPoint2.y);
                c.map2Win(this.o[3], this.o[4], iPoint2);
                rect.union(iPoint2.x, iPoint2.y);
                c.map2Win(this.o[6], this.o[7], iPoint2);
                rect.union(iPoint2.x, iPoint2.y);
                c.map2Win(this.o[9], this.o[10], iPoint2);
                rect.union(iPoint2.x, iPoint2.y);
            } else {
                a((-this.y) * ((float) K), (this.z - ContentUtil.FONT_SIZE_NORMAL) * ((float) L), iPoint2);
                rect = new Rect(iPoint.x + iPoint2.x, iPoint.y - iPoint2.y, iPoint.x + iPoint2.x, iPoint.y - iPoint2.y);
                a((-this.y) * ((float) K), this.z * ((float) L), iPoint2);
                rect.union(iPoint.x + iPoint2.x, iPoint.y - iPoint2.y);
                a((ContentUtil.FONT_SIZE_NORMAL - this.y) * ((float) K), this.z * ((float) L), iPoint2);
                rect.union(iPoint.x + iPoint2.x, iPoint.y - iPoint2.y);
                a((ContentUtil.FONT_SIZE_NORMAL - this.y) * ((float) K), (this.z - ContentUtil.FONT_SIZE_NORMAL) * ((float) L), iPoint2);
                rect.union(iPoint.x + iPoint2.x, iPoint.y - iPoint2.y);
            }
            this.j = rect.centerX() - iPoint.x;
            this.k = rect.top - iPoint.y;
            return rect;
        } catch (Throwable th) {
            ce.a(th, "MarkerDelegateImp", "getRect");
            th.printStackTrace();
            return new Rect(0, 0, 0, 0);
        }
    }

    public boolean b() {
        N();
        this.B = false;
        if (this.C == null) {
            return false;
        }
        return this.C.b((ah) this);
    }

    private void N() {
        if (this.C.a != null) {
            this.C.a.f(false);
        }
    }

    public LatLng e() {
        if (!this.M || this.n == null) {
            return this.u;
        }
        DPoint dPoint = new DPoint();
        IPoint iPoint = new IPoint();
        r();
        this.C.a.a(this.n.x, this.n.y, iPoint);
        MapProjection.geo2LonLat(iPoint.x, iPoint.y, dPoint);
        return new LatLng(dPoint.y, dPoint.x);
    }

    public String h() {
        if (this.t == null) {
            this.t = c("Marker");
        }
        return this.t;
    }

    public void a(LatLng latLng) {
        if (latLng != null) {
            this.u = latLng;
            IPoint iPoint = new IPoint();
            if (this.H) {
                try {
                    double[] a = dq.a(latLng.longitude, latLng.latitude);
                    this.v = new LatLng(a[1], a[0]);
                    MapProjection.lonlat2Geo(a[0], a[1], iPoint);
                } catch (Throwable th) {
                    this.v = latLng;
                }
            } else {
                MapProjection.lonlat2Geo(latLng.longitude, latLng.latitude, iPoint);
            }
            this.l = iPoint.x;
            this.m = iPoint.y;
            this.M = false;
            r();
            N();
            return;
        }
        ce.a(new AMapException("非法坐标值 latlng is null"), "setPosition", "Marker");
    }

    public void a(String str) {
        this.w = str;
        N();
    }

    public String i() {
        return this.w;
    }

    public void b(String str) {
        this.x = str;
        N();
    }

    public String j() {
        return this.x;
    }

    public void a(boolean z) {
        this.A = z;
        N();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void a(ArrayList<BitmapDescriptor> arrayList) {
        if (arrayList != null) {
            try {
                if (this.G != null) {
                    b((ArrayList) arrayList);
                    this.I = false;
                    this.b = false;
                    if (this.D != null) {
                        this.D.clear();
                        this.D = null;
                    }
                    this.p = null;
                    if (n()) {
                        this.C.e(this);
                        this.C.d(this);
                    }
                    N();
                }
            } catch (Throwable th) {
                ce.a(th, "MarkerDelegateImp", "setIcons");
                th.printStackTrace();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized ArrayList<BitmapDescriptor> w() {
        if (this.G != null) {
            if (this.G.size() > 0) {
                ArrayList<BitmapDescriptor> arrayList = new ArrayList();
                Iterator it = this.G.iterator();
                while (it.hasNext()) {
                    arrayList.add((BitmapDescriptor) it.next());
                }
                return arrayList;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void a(BitmapDescriptor bitmapDescriptor) {
        if (bitmapDescriptor != null) {
            try {
                if (this.G != null) {
                    this.G.clear();
                    this.G.add(bitmapDescriptor);
                    this.I = false;
                    this.b = false;
                    this.p = null;
                    if (this.D != null) {
                        this.D.clear();
                        this.D = null;
                    }
                    if (n()) {
                        this.C.e(this);
                        this.C.d(this);
                    }
                    N();
                }
            } catch (Throwable th) {
                ce.a(th, "MarkerDelegateImp", "setIcon");
                th.printStackTrace();
            }
        }
    }

    public synchronized BitmapDescriptor M() {
        try {
            if (this.G != null) {
                if (this.G.size() != 0) {
                    if (this.G.get(0) == null) {
                        this.G.clear();
                        return M();
                    }
                    return (BitmapDescriptor) this.G.get(0);
                }
            }
            a();
            this.G.add(BitmapDescriptorFactory.defaultMarker());
            return (BitmapDescriptor) this.G.get(0);
        } catch (Throwable th) {
            ce.a(th, "MarkerDelegateImp", "getBitmapDescriptor");
            th.printStackTrace();
            return null;
        }
    }

    public boolean k() {
        return this.A;
    }

    public void l() {
        if (this.B) {
            this.C.d(this);
            N();
        }
    }

    public void m() {
        if (n()) {
            this.C.e(this);
            N();
            this.c = false;
        }
        this.d = false;
    }

    public void b(boolean z) {
        this.c = z;
        if (this.c && this.M) {
            this.d = true;
        }
    }

    public boolean n() {
        return this.c;
    }

    public void c(boolean z) {
        if (this.B != z) {
            this.B = z;
            if (!z && n()) {
                this.C.e(this);
            }
            N();
        }
    }

    public boolean o() {
        return this.B;
    }

    public void a(float f, float f2) {
        if (this.y != f || this.z != f2) {
            this.y = f;
            this.z = f2;
            if (n()) {
                this.C.e(this);
                this.C.d(this);
            }
            N();
        }
    }

    public boolean a(ah ahVar) throws RemoteException {
        if (equals(ahVar) || ahVar.h().equals(h())) {
            return true;
        }
        return false;
    }

    public int q() {
        return super.hashCode();
    }

    public boolean r() {
        if (this.M) {
            this.C.a.c().win2Map(this.N, this.O, this.n);
        } else {
            this.C.a.c().geo2Map(this.l, this.m, this.n);
        }
        return true;
    }

    private void a(ab abVar) throws RemoteException {
        float[] a = bj.a(abVar, !this.g ? 0 : 1, this.n, this.e, K(), L(), this.y, this.z);
        this.o = (float[]) a.clone();
        if (this.s != null) {
            this.s = bj.a(a, this.s);
        } else {
            this.s = bj.a(a);
        }
        if (this.G != null && this.G.size() > 0) {
            this.K++;
            if (this.K >= this.L * this.G.size()) {
                this.K = 0;
            }
            int i = this.K / this.L;
            if (!this.J) {
                N();
            }
            if (this.p != null && this.p.length > 0) {
                a(this.p[i % this.G.size()], this.s, this.D);
            }
        }
    }

    private void a(float f, float f2, IPoint iPoint) {
        float f3 = (float) ((((double) this.e) * 3.141592653589793d) / 180.0d);
        iPoint.x = (int) ((((double) f) * Math.cos((double) f3)) + (((double) f2) * Math.sin((double) f3)));
        iPoint.y = (int) ((((double) f2) * Math.cos((double) f3)) - (Math.sin((double) f3) * ((double) f)));
    }

    private void a(int i, FloatBuffer floatBuffer, FloatBuffer floatBuffer2) {
        if (i != 0 && floatBuffer != null && floatBuffer2 != null) {
            GLES10.glEnable(3042);
            GLES10.glBlendFunc(1, 771);
            GLES10.glColor4f(ContentUtil.FONT_SIZE_NORMAL, ContentUtil.FONT_SIZE_NORMAL, ContentUtil.FONT_SIZE_NORMAL, ContentUtil.FONT_SIZE_NORMAL);
            GLES10.glEnable(3553);
            GLES10.glEnableClientState(32884);
            GLES10.glEnableClientState(32888);
            GLES10.glBindTexture(3553, i);
            GLES10.glVertexPointer(3, 5126, 0, floatBuffer);
            GLES10.glTexCoordPointer(2, 5126, 0, floatBuffer2);
            GLES10.glDrawArrays(6, 0, 4);
            GLES10.glDisableClientState(32884);
            GLES10.glDisableClientState(32888);
            GLES10.glDisable(3553);
            GLES10.glDisable(3042);
        }
    }

    public void a(GL10 gl10, ab abVar) {
        Object obj = 1;
        if (this.B && !this.r) {
            if (this.u != null || this.M) {
                int i;
                int i2;
                int i3;
                BitmapDescriptor bitmapDescriptor;
                if (M() == null) {
                    if (this.G != null) {
                    }
                }
                if (!this.I) {
                    try {
                        if (this.G != null) {
                            Object obj2;
                            this.p = new int[this.G.size()];
                            if (VERSION.SDK_INT < 12) {
                                obj2 = null;
                            } else {
                                i = 1;
                            }
                            Iterator it = this.G.iterator();
                            i2 = 0;
                            i3 = 0;
                            while (it.hasNext()) {
                                int i4;
                                bitmapDescriptor = (BitmapDescriptor) it.next();
                                if (obj2 != null) {
                                    i2 = this.C.a(bitmapDescriptor);
                                }
                                if (i2 != 0) {
                                    i4 = i2;
                                } else {
                                    Bitmap bitmap = bitmapDescriptor.getBitmap();
                                    if (bitmap == null || bitmap.isRecycled()) {
                                        i4 = i2;
                                    } else {
                                        i2 = a(gl10);
                                        if (obj2 != null) {
                                            this.C.a(new be(bitmapDescriptor, i2));
                                        }
                                        bj.b(gl10, i2, bitmap, false);
                                        i4 = i2;
                                    }
                                }
                                this.p[i3] = i4;
                                i3++;
                                i2 = i4;
                            }
                            if (this.G.size() != 1) {
                                this.J = false;
                            } else {
                                this.J = true;
                            }
                            this.I = true;
                        }
                    } catch (Throwable th) {
                        ce.a(th, "MarkerDelegateImp", "loadtexture");
                        return;
                    }
                }
                try {
                    if (!this.b) {
                        if (this.D == null) {
                            bitmapDescriptor = M();
                            if (bitmapDescriptor != null) {
                                i = bitmapDescriptor.getWidth();
                                i2 = bitmapDescriptor.getHeight();
                                i3 = bitmapDescriptor.getBitmap().getHeight();
                                float width = ((float) i) / ((float) bitmapDescriptor.getBitmap().getWidth());
                                float f = ((float) i2) / ((float) i3);
                                this.D = bj.a(new float[]{0.0f, f, width, f, width, 0.0f, 0.0f, 0.0f});
                            } else {
                                return;
                            }
                        }
                        r();
                        this.P = System.currentTimeMillis();
                        this.b = true;
                    }
                    if (this.M) {
                        abVar.a(this.N, this.O, this.n);
                    }
                    a(abVar);
                    if (this.d && n()) {
                        this.C.j();
                        if (System.currentTimeMillis() - this.P > 1000) {
                            obj = null;
                        }
                        if (obj == null) {
                            this.d = false;
                        }
                    }
                } catch (Throwable th2) {
                    ce.a(th2, "MarkerDelegateImp", "drawMarker");
                }
            }
        }
    }

    private int a(GL10 gl10) {
        int K = this.C.a.K();
        if (K != 0) {
            return K;
        }
        int[] iArr = new int[]{0};
        gl10.glGenTextures(1, iArr, 0);
        return iArr[0];
    }

    public boolean c() {
        return this.J;
    }

    public void a(int i) {
        if (i > 1) {
            this.L = i;
        } else {
            this.L = 1;
        }
    }

    public void a(Object obj) {
        this.E = obj;
    }

    public Object s() {
        return this.E;
    }

    public void d(boolean z) {
        this.F = z;
    }

    public boolean t() {
        return this.F;
    }

    public int v() {
        return this.L;
    }

    public LatLng g() {
        if (this.M) {
            this.C.a.c().win2Map(this.N, this.O, this.n);
            DPoint dPoint = new DPoint();
            this.C.a.a(this.N, this.O, dPoint);
            return new LatLng(dPoint.y, dPoint.y);
        }
        LatLng latLng;
        if (this.H) {
            latLng = this.v;
        } else {
            latLng = this.u;
        }
        return latLng;
    }

    public void z() {
        this.C.c(this);
    }

    public void e(boolean z) throws RemoteException {
        this.g = z;
        N();
    }

    public boolean A() {
        return this.g;
    }

    public float u() {
        return this.f;
    }

    public int B() {
        return this.h;
    }

    public int C() {
        return this.i;
    }

    public void a(int i, int i2) {
        int i3 = 0;
        this.N = i;
        this.O = i2;
        this.M = true;
        r();
        try {
            ab abVar = this.C.a;
            if (this.g) {
                i3 = 1;
            }
            this.o = bj.a(abVar, i3, this.n, this.e, K(), L(), this.y, this.z);
        } catch (Throwable th) {
            ce.a(th, "MarkerDelegateImp", "setPositionByPixels");
        }
        N();
        if (n()) {
            l();
        }
    }

    public int D() {
        return this.j;
    }

    public int E() {
        return this.k;
    }

    public FPoint f() {
        return this.n;
    }

    public boolean F() {
        return this.M;
    }

    public void b(float f) {
        this.q = f;
        this.C.h();
    }

    public float G() {
        return this.q;
    }

    public boolean H() {
        Rect k = this.C.a.k();
        if (this.M || k == null) {
            return true;
        }
        IPoint iPoint = new IPoint();
        if (this.H && this.v != null) {
            this.C.a.b(this.v.latitude, this.v.longitude, iPoint);
        } else if (this.u != null) {
            this.C.a.b(this.u.latitude, this.u.longitude, iPoint);
        }
        return k.contains(iPoint.x, iPoint.y);
    }

    public void a(IPoint iPoint) {
        this.M = false;
        this.l = iPoint.x;
        this.m = iPoint.y;
        DPoint dPoint = new DPoint();
        MapProjection.geo2LonLat(this.l, this.m, dPoint);
        this.u = new LatLng(dPoint.y, dPoint.x, false);
        this.C.a.c().geo2Map(this.l, this.m, this.n);
    }

    public IPoint I() {
        IPoint iPoint = new IPoint();
        if (!this.M) {
            return new IPoint(this.l, this.m);
        }
        this.C.a.a(this.N, this.O, iPoint);
        return iPoint;
    }

    public void J() {
        this.I = false;
        if (this.p != null) {
            Arrays.fill(this.p, 0);
        }
    }
}
