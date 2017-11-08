package com.amap.api.mapcore.util;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.opengl.GLES10;
import android.os.Build.VERSION;
import android.os.RemoteException;
import android.util.Log;
import android.view.animation.AnimationUtils;
import com.amap.api.maps.AMapException;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.Animation.AnimationListener;
import com.autonavi.amap.mapcore.DPoint;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapProjection;
import com.autonavi.amap.mapcore.VirtualEarthProjection;
import com.autonavi.amap.mapcore.interfaces.IAnimation;
import com.autonavi.amap.mapcore.interfaces.IMarkerAction;
import com.autonavi.amap.mapcore.interfaces.IOverlayImage;
import com.huawei.watermark.manager.parse.WMElement;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: MarkerDelegateImp */
public class cz implements cr, IAnimation, IMarkerAction {
    private static CopyOnWriteArrayList<Float> D = new CopyOnWriteArrayList();
    private static CopyOnWriteArrayList<Float> E = new CopyOnWriteArrayList();
    private static int b = 0;
    private boolean A = true;
    private boolean B = false;
    private boolean C = true;
    private FPoint F = new FPoint();
    private float G;
    private float H;
    private boolean I = false;
    private FloatBuffer J = null;
    private String K;
    private LatLng L;
    private LatLng M;
    private String N;
    private String O;
    private float P = 0.5f;
    private float Q = WMElement.CAMERASIZEVALUE1B1;
    private boolean R = false;
    private boolean S = true;
    private q T;
    private FloatBuffer U;
    private Object V;
    private boolean W = false;
    private CopyOnWriteArrayList<BitmapDescriptor> X = null;
    private boolean Y = false;
    private boolean Z = false;
    di a;
    private boolean aa = true;
    private int ab = 0;
    private int ac = 20;
    private boolean ad = false;
    private int ae;
    private int af;
    private long ag = 0;
    private float ah = Float.MAX_VALUE;
    private float ai = Float.MIN_VALUE;
    private float aj = Float.MAX_VALUE;
    private float ak = Float.MIN_VALUE;
    private boolean c = false;
    private boolean d = false;
    private boolean e = false;
    private float f = 0.0f;
    private float g = 0.0f;
    private boolean h = false;
    private int i = 0;
    private int j = 0;
    private int k = 0;
    private int l = 0;
    private int m;
    private int n;
    private FPoint o = new FPoint();
    private float[] p;
    private int[] q = null;
    private float r = 0.0f;
    private float s = WMElement.CAMERASIZEVALUE1B1;
    private float t = WMElement.CAMERASIZEVALUE1B1;
    private float u = WMElement.CAMERASIZEVALUE1B1;
    private MarkerOptions v;
    private boolean w = false;
    private boolean x = true;
    private int y = 5;
    private boolean z = true;

    private static String a(String str) {
        b++;
        return str + b;
    }

    public void setRotateAngle(float f) {
        this.v.rotateAngle(f);
        this.g = f;
        this.f = (((-f) % 360.0f) + 360.0f) % 360.0f;
        this.e = true;
        s();
    }

    public void destroy() {
        try {
            int i;
            this.I = true;
            remove();
            if (this.T != null) {
                i = 0;
                while (this.q != null && i < this.q.length) {
                    this.T.a(Integer.valueOf(this.q[i]));
                    this.T.a().g(this.q[i]);
                    i++;
                }
            }
            i = 0;
            while (this.X != null && i < this.X.size()) {
                ((BitmapDescriptor) this.X.get(i)).recycle();
                i++;
            }
            if (this.U != null) {
                this.U.clear();
                this.U = null;
            }
            if (this.J != null) {
                this.J.clear();
                this.J = null;
            }
            this.L = null;
            this.V = null;
            this.q = null;
        } catch (Throwable th) {
            fo.b(th, "MarkerDelegateImp", "destroy");
            th.printStackTrace();
            Log.d("destroy erro", "MarkerDelegateImp destroy");
        }
    }

    synchronized void n() {
        if (this.X != null) {
            this.X.clear();
        } else {
            this.X = new CopyOnWriteArrayList();
        }
    }

    public synchronized void a(ArrayList<BitmapDescriptor> arrayList) {
        n();
        if (arrayList != null) {
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                BitmapDescriptor bitmapDescriptor = (BitmapDescriptor) it.next();
                if (bitmapDescriptor != null) {
                    this.X.add(bitmapDescriptor);
                }
            }
        }
    }

    public cz(MarkerOptions markerOptions, q qVar) {
        this.T = qVar;
        setMarkerOptions(markerOptions);
    }

    public int o() {
        try {
            return q().getWidth();
        } catch (Throwable th) {
            return 0;
        }
    }

    public int p() {
        try {
            return q().getHeight();
        } catch (Throwable th) {
            return 0;
        }
    }

    public Rect h() {
        if (this.p == null) {
            return new Rect(0, 0, 0, 0);
        }
        try {
            Rect rect;
            MapProjection c = this.T.a().c();
            int o = o();
            int p = p();
            IPoint iPoint = new IPoint();
            IPoint iPoint2 = new IPoint();
            c.map2Win(this.o.x, this.o.y, iPoint);
            if (this.h) {
                c.map2Win(this.p[0], this.p[1], iPoint2);
                rect = new Rect(iPoint2.x, iPoint2.y, iPoint2.x, iPoint2.y);
                c.map2Win(this.p[3], this.p[4], iPoint2);
                rect.union(iPoint2.x, iPoint2.y);
                c.map2Win(this.p[6], this.p[7], iPoint2);
                rect.union(iPoint2.x, iPoint2.y);
                c.map2Win(this.p[9], this.p[10], iPoint2);
                rect.union(iPoint2.x, iPoint2.y);
            } else {
                a((-this.P) * ((float) o), (this.Q - WMElement.CAMERASIZEVALUE1B1) * ((float) p), iPoint2);
                rect = new Rect(iPoint.x + iPoint2.x, iPoint.y - iPoint2.y, iPoint.x + iPoint2.x, iPoint.y - iPoint2.y);
                a((-this.P) * ((float) o), this.Q * ((float) p), iPoint2);
                rect.union(iPoint.x + iPoint2.x, iPoint.y - iPoint2.y);
                a((WMElement.CAMERASIZEVALUE1B1 - this.P) * ((float) o), this.Q * ((float) p), iPoint2);
                rect.union(iPoint.x + iPoint2.x, iPoint.y - iPoint2.y);
                a((WMElement.CAMERASIZEVALUE1B1 - this.P) * ((float) o), (this.Q - WMElement.CAMERASIZEVALUE1B1) * ((float) p), iPoint2);
                rect.union(iPoint.x + iPoint2.x, iPoint.y - iPoint2.y);
            }
            this.k = rect.centerX() - iPoint.x;
            this.l = rect.top - iPoint.y;
            return rect;
        } catch (Throwable th) {
            fo.b(th, "MarkerDelegateImp", "getRect");
            th.printStackTrace();
            return new Rect(0, 0, 0, 0);
        }
    }

    public boolean remove() {
        s();
        this.S = false;
        if (this.T == null) {
            return false;
        }
        return this.T.a((cu) this);
    }

    private void s() {
        if (this.T.a() != null) {
            this.T.a().setRunLowFrame(false);
        }
    }

    public LatLng getPosition() {
        if (!this.ad || this.o == null) {
            return this.L;
        }
        DPoint dPoint = new DPoint();
        IPoint iPoint = new IPoint();
        i();
        this.T.a().a(this.o.x, this.o.y, iPoint);
        MapProjection.geo2LonLat(iPoint.x, iPoint.y, dPoint);
        return new LatLng(dPoint.y, dPoint.x);
    }

    public String getId() {
        if (this.K == null) {
            this.K = a("Marker");
        }
        return this.K;
    }

    public void setPosition(LatLng latLng) {
        if (latLng != null) {
            this.L = latLng;
            IPoint iPoint = new IPoint();
            if (this.Y) {
                try {
                    double[] a = hn.a(latLng.longitude, latLng.latitude);
                    this.M = new LatLng(a[1], a[0]);
                    MapProjection.lonlat2Geo(a[0], a[1], iPoint);
                } catch (Throwable th) {
                    this.M = latLng;
                }
            } else {
                MapProjection.lonlat2Geo(latLng.longitude, latLng.latitude, iPoint);
            }
            this.m = iPoint.x;
            this.n = iPoint.y;
            this.ad = false;
            i();
            s();
            this.e = true;
            return;
        }
        fo.b(new AMapException("非法坐标值 latlng is null"), "setPosition", "Marker");
    }

    public void setTitle(String str) {
        this.N = str;
        s();
        this.v.title(str);
    }

    public String getTitle() {
        return this.N;
    }

    public void setSnippet(String str) {
        this.O = str;
        s();
        this.v.snippet(str);
    }

    public String getSnippet() {
        return this.O;
    }

    public void setDraggable(boolean z) {
        this.R = z;
        this.v.draggable(z);
        s();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setIcons(ArrayList<BitmapDescriptor> arrayList) {
        if (arrayList != null) {
            try {
                if (this.X != null) {
                    a((ArrayList) arrayList);
                    this.Z = false;
                    this.c = false;
                    t();
                    s();
                    this.e = true;
                }
            } catch (Throwable th) {
                fo.b(th, "MarkerDelegateImp", "setIcons");
                th.printStackTrace();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized ArrayList<BitmapDescriptor> getIcons() {
        if (this.X != null) {
            if (this.X.size() > 0) {
                ArrayList<BitmapDescriptor> arrayList = new ArrayList();
                Iterator it = this.X.iterator();
                while (it.hasNext()) {
                    arrayList.add((BitmapDescriptor) it.next());
                }
                return arrayList;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setIcon(BitmapDescriptor bitmapDescriptor) {
        if (bitmapDescriptor != null) {
            try {
                if (this.X != null) {
                    this.X.clear();
                    this.X.add(bitmapDescriptor);
                    t();
                    this.Z = false;
                    this.c = false;
                    s();
                    this.e = true;
                }
            } catch (Throwable th) {
                fo.b(th, "MarkerDelegateImp", "setIcon");
                th.printStackTrace();
            }
        }
    }

    private void t() {
        if (this.U != null) {
            this.U.clear();
        }
        try {
            BitmapDescriptor q = q();
            if (q != null) {
                int width = q.getWidth();
                int height = q.getHeight();
                int height2 = q.getBitmap().getHeight();
                float width2 = ((float) width) / ((float) q.getBitmap().getWidth());
                float f = ((float) height) / ((float) height2);
                this.U = eh.a(new float[]{0.0f, f, width2, f, width2, 0.0f, 0.0f, 0.0f});
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public synchronized BitmapDescriptor q() {
        try {
            if (this.X != null) {
                if (this.X.size() != 0) {
                    if (this.X.get(0) == null) {
                        this.X.clear();
                        return q();
                    }
                    return (BitmapDescriptor) this.X.get(0);
                }
            }
            n();
            this.X.add(BitmapDescriptorFactory.defaultMarker());
            return (BitmapDescriptor) this.X.get(0);
        } catch (Throwable th) {
            fo.b(th, "MarkerDelegateImp", "getBitmapDescriptor");
            th.printStackTrace();
            return null;
        }
    }

    public boolean isDraggable() {
        return this.R;
    }

    public void showInfoWindow() {
        if (this.S) {
            this.T.a((cr) this);
            s();
        }
    }

    public void hideInfoWindow() {
        if (isInfoWindowShown()) {
            this.T.c(this);
            s();
            this.d = false;
        }
        this.e = false;
    }

    public void a(boolean z) {
        this.d = z;
        this.e = true;
    }

    public boolean isInfoWindowShown() {
        return this.d;
    }

    public void setVisible(boolean z) {
        if (this.S != z) {
            this.v.visible(z);
            this.S = z;
            if (!z && isInfoWindowShown()) {
                this.T.c(this);
            }
            i();
            s();
        }
    }

    public boolean isVisible() {
        return this.S;
    }

    public void setAnchor(float f, float f2) {
        if (this.P != f || this.Q != f2) {
            this.v.anchor(f, f2);
            this.P = f;
            this.Q = f2;
            this.e = true;
            s();
        }
    }

    public float getAnchorU() {
        return this.P;
    }

    public float getAnchorV() {
        return this.Q;
    }

    public boolean equalsRemote(IOverlayImage iOverlayImage) throws RemoteException {
        if (equals(iOverlayImage) || iOverlayImage.getId().equals(getId())) {
            return true;
        }
        return false;
    }

    public int hashCodeRemote() {
        return super.hashCode();
    }

    public boolean i() {
        try {
            if (this.T == null || this.T.a() == null || this.T.a().c() == null) {
                return false;
            }
            if (this.o == null) {
                this.o = new FPoint();
            }
            if (this.ad) {
                this.T.a().a(this.ae, this.af, this.o);
            } else {
                this.T.a().b(this.n, this.m, this.o);
            }
            return true;
        } catch (Throwable th) {
            th.printStackTrace();
            return false;
        }
    }

    private void a(l lVar) throws RemoteException {
        float[] a = eh.a(lVar, !this.h ? 0 : 1, this.o, this.f, (int) (this.s * ((float) o())), (int) (this.t * ((float) p())), this.P, this.Q);
        this.p = (float[]) a.clone();
        if (this.J != null) {
            this.J = eh.a(a, this.J);
        } else {
            this.J = eh.a(a);
        }
        if (this.X != null && this.X.size() > 0) {
            this.ab++;
            if (this.ab >= this.ac * this.X.size()) {
                this.ab = 0;
            }
            if (this.ac == 0) {
                this.ac = 1;
            }
            int i = this.ab / this.ac;
            if (!this.aa) {
                s();
            }
            if (this.q != null && this.q.length > 0) {
                a(this.q[i % this.X.size()], this.J, this.U);
            }
        }
    }

    private void a(float f, float f2, IPoint iPoint) {
        float f3 = (float) ((((double) this.f) * 3.141592653589793d) / VirtualEarthProjection.MaxLongitude);
        iPoint.x = (int) ((((double) f) * Math.cos((double) f3)) + (((double) f2) * Math.sin((double) f3)));
        iPoint.y = (int) ((((double) f2) * Math.cos((double) f3)) - (Math.sin((double) f3) * ((double) f)));
    }

    private void a(int i, FloatBuffer floatBuffer, FloatBuffer floatBuffer2) {
        if (i != 0 && floatBuffer != null && floatBuffer2 != null) {
            GLES10.glEnable(3042);
            GLES10.glTexEnvf(8960, 8704, 8448.0f);
            GLES10.glBlendFunc(770, 771);
            GLES10.glColor4f(WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, this.u);
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

    public void a(GL10 gl10, l lVar) {
        Object obj = 1;
        if (this.S && !this.I) {
            if (this.L != null || this.ad) {
                if (q() == null) {
                    if (this.X != null) {
                    }
                }
                if (!this.Z) {
                    synchronized (this) {
                        try {
                            if (this.X != null) {
                                Object obj2;
                                this.q = new int[this.X.size()];
                                if (VERSION.SDK_INT < 12) {
                                    obj2 = null;
                                } else {
                                    int i = 1;
                                }
                                Iterator it = this.X.iterator();
                                int i2 = 0;
                                int i3 = 0;
                                while (it.hasNext()) {
                                    int i4;
                                    BitmapDescriptor bitmapDescriptor = (BitmapDescriptor) it.next();
                                    if (obj2 != null) {
                                        i2 = lVar.a(bitmapDescriptor);
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
                                                lVar.a(new s(bitmapDescriptor, i2));
                                            }
                                            eh.b(gl10, i2, bitmap, false);
                                            i4 = i2;
                                        }
                                    }
                                    this.q[i3] = i4;
                                    i3++;
                                    i2 = i4;
                                }
                                if (this.X.size() != 1) {
                                    this.aa = false;
                                } else {
                                    this.aa = true;
                                }
                                this.Z = true;
                            }
                            i();
                        } catch (Throwable th) {
                            fo.b(th, "MarkerDelegateImp", "loadtexture");
                            return;
                        }
                    }
                }
                try {
                    if (!this.c) {
                        this.ag = System.currentTimeMillis();
                        this.c = true;
                    }
                    if (this.ad) {
                        if (this.A) {
                            lVar.a(this.ae, this.af, this.o);
                        }
                    }
                    u();
                    a(lVar);
                    if (this.e && isInfoWindowShown()) {
                        this.T.a().l();
                        if (System.currentTimeMillis() - this.ag > 1000) {
                            obj = null;
                        }
                        if (obj == null) {
                            this.e = false;
                        }
                    }
                } catch (Throwable th2) {
                    fo.b(th2, "MarkerDelegateImp", "drawMarker");
                }
            }
        }
    }

    private void u() {
        if (this.C || this.a == null || this.a.l()) {
            this.s = WMElement.CAMERASIZEVALUE1B1;
            this.t = WMElement.CAMERASIZEVALUE1B1;
            this.C = true;
            return;
        }
        s();
        dn dnVar = new dn();
        this.a.a(AnimationUtils.currentAnimationTimeMillis(), dnVar);
        if (dnVar != null) {
            if (!(Double.isNaN(dnVar.e) || Double.isNaN(dnVar.f))) {
                this.s = (float) dnVar.e;
                this.t = (float) dnVar.f;
            }
            if (!Double.isNaN(dnVar.d)) {
                setRotateAngle((float) dnVar.d);
            }
            if (!(Double.isNaN(dnVar.a) || Double.isNaN(dnVar.b))) {
                double d = dnVar.a;
                double d2 = dnVar.b;
                if (this.ad) {
                    FPoint fPoint = new FPoint();
                    this.T.a().c().win2Map((int) d, (int) d2, fPoint);
                    IPoint iPoint = new IPoint();
                    this.T.a().c().map2Geo(fPoint.x, fPoint.y, iPoint);
                    a(iPoint.x, iPoint.y);
                    this.ad = true;
                } else {
                    a((int) d, (int) d2);
                }
            }
            if (!Double.isNaN(dnVar.c)) {
                this.u = (float) dnVar.c;
            }
        }
    }

    private int a(GL10 gl10) {
        int[] iArr = new int[]{0};
        gl10.glGenTextures(1, iArr, 0);
        return iArr[0];
    }

    public boolean j() {
        return this.aa;
    }

    public void setPeriod(int i) {
        if (i > 1) {
            this.ac = i;
        } else {
            this.ac = 1;
        }
    }

    public void setObject(Object obj) {
        this.V = obj;
    }

    public Object getObject() {
        return this.V;
    }

    public void setPerspective(boolean z) {
        this.W = z;
    }

    public boolean isPerspective() {
        return this.W;
    }

    public int getPeriod() {
        return this.ac;
    }

    public LatLng b() {
        if (this.ad) {
            this.T.a().c().win2Map(this.ae, this.af, this.o);
            DPoint dPoint = new DPoint();
            this.T.a().a(this.ae, this.af, dPoint);
            return new LatLng(dPoint.y, dPoint.y);
        }
        LatLng latLng;
        if (this.Y) {
            latLng = this.M;
        } else {
            latLng = this.L;
        }
        return latLng;
    }

    public void set2Top() {
        this.T.b((cu) this);
    }

    public void setFlat(boolean z) throws RemoteException {
        this.h = z;
        s();
        this.v.setFlat(z);
    }

    public boolean isFlat() {
        return this.h;
    }

    public float getRotateAngle() {
        return this.g;
    }

    public int c() {
        return this.i;
    }

    public int d() {
        return this.j;
    }

    public void setPositionByPixels(int i, int i2) {
        int i3 = 0;
        this.ae = i;
        this.af = i2;
        this.ad = true;
        i();
        try {
            l a = this.T.a();
            if (this.h) {
                i3 = 1;
            }
            this.p = eh.a(a, i3, this.o, this.f, o(), p(), this.P, this.Q);
        } catch (Throwable th) {
            fo.b(th, "MarkerDelegateImp", "setPositionByPixels");
        }
        s();
        this.e = true;
    }

    public int e() {
        return this.k;
    }

    public int f() {
        return this.l;
    }

    public FPoint a() {
        return this.o;
    }

    public boolean g() {
        return this.ad;
    }

    public void setZIndex(float f) {
        this.r = f;
        this.v.zIndex(f);
        this.T.g();
    }

    public float getZIndex() {
        return this.r;
    }

    public boolean k() {
        if (this.ad) {
            return true;
        }
        if (this.o != null) {
            this.F.x = this.o.x;
            this.F.y = this.o.y;
            FPoint[] mapRect = this.T.a().getMapConfig().getMapRect();
            a(mapRect);
            if (eh.a(this.F, mapRect)) {
                return true;
            }
        }
        return false;
    }

    public void setGeoPoint(IPoint iPoint) {
        this.ad = false;
        a(iPoint.x, iPoint.y);
    }

    private void a(int i, int i2) {
        this.m = i;
        this.n = i2;
        DPoint dPoint = new DPoint();
        MapProjection.geo2LonLat(this.m, this.n, dPoint);
        this.L = new LatLng(dPoint.y, dPoint.x, false);
        this.T.a().c().geo2Map(this.m, this.n, this.o);
    }

    public IPoint getGeoPoint() {
        IPoint iPoint = new IPoint();
        if (!this.ad) {
            return new IPoint(this.m, this.n);
        }
        this.T.a().a(this.ae, this.af, iPoint);
        return iPoint;
    }

    public void l() {
        this.Z = false;
        if (this.q != null) {
            Arrays.fill(this.q, 0);
        }
    }

    public void setAnimation(Animation animation) {
        di diVar = null;
        IAnimation r = r();
        if (r != null) {
            if (animation != null) {
                diVar = animation.glAnimation;
            }
            r.setAnimation(diVar);
        }
    }

    public void setAnimation(di diVar) {
        if (diVar != null) {
            this.a = diVar;
        }
    }

    public boolean startAnimation() {
        if (this.a != null) {
            if (this.a instanceof dj) {
                dj djVar = (dj) this.a;
                for (di diVar : djVar.n()) {
                    a(diVar);
                    diVar.a(djVar.f());
                }
            } else {
                a(this.a);
            }
            this.C = false;
            this.a.c();
            s();
        }
        return false;
    }

    private void a(di diVar) {
        if (diVar instanceof do) {
            if (this.ad) {
                this.L = getPosition();
                setPosition(this.L);
                this.ad = true;
            }
            if (this.ad) {
                ((do) diVar).a = (double) this.ae;
                ((do) diVar).b = (double) this.af;
                IPoint iPoint = new IPoint();
                this.T.a().b(((do) diVar).w, ((do) diVar).c, iPoint);
                ((do) diVar).c = (double) iPoint.x;
                ((do) diVar).w = (double) iPoint.y;
                return;
            }
            ((do) diVar).a = (double) this.m;
            ((do) diVar).b = (double) this.n;
            IPoint iPoint2 = new IPoint();
            MapProjection.lonlat2Geo(((do) diVar).c, ((do) diVar).w, iPoint2);
            ((do) diVar).c = (double) iPoint2.x;
            ((do) diVar).w = (double) iPoint2.y;
        }
    }

    public void setAnimationListener(AnimationListener animationListener) {
        if (this.a != null) {
            this.a.a(animationListener);
        }
    }

    public IAnimation r() {
        return this;
    }

    public IMarkerAction getIMarkerAction() {
        return this;
    }

    public float getAlpha() {
        return this.u;
    }

    public void setAlpha(float f) {
        this.u = f;
        this.v.alpha(f);
    }

    public int getDisplayLevel() {
        return this.y;
    }

    public MarkerOptions getOptions() {
        return this.v;
    }

    public void setMarkerOptions(MarkerOptions markerOptions) {
        if (markerOptions != null) {
            this.v = markerOptions;
            this.L = this.v.getPosition();
            IPoint iPoint = new IPoint();
            this.Y = this.v.isGps();
            if (this.v.getPosition() != null) {
                if (this.Y) {
                    try {
                        double[] a = hn.a(this.v.getPosition().longitude, this.v.getPosition().latitude);
                        this.M = new LatLng(a[1], a[0]);
                        MapProjection.lonlat2Geo(a[0], a[1], iPoint);
                    } catch (Throwable th) {
                        fo.b(th, "MarkerDelegateImp", "create");
                        this.M = this.v.getPosition();
                    }
                } else {
                    MapProjection.lonlat2Geo(this.L.longitude, this.L.latitude, iPoint);
                }
            }
            this.m = iPoint.x;
            this.n = iPoint.y;
            this.P = this.v.getAnchorU();
            this.Q = this.v.getAnchorV();
            this.i = this.v.getInfoWindowOffsetX();
            this.j = this.v.getInfoWindowOffsetY();
            this.ac = this.v.getPeriod();
            this.r = this.v.getZIndex();
            this.B = this.v.isBelowMaskLayer();
            i();
            a(this.v.getIcons());
            t();
            this.S = this.v.isVisible();
            this.O = this.v.getSnippet();
            this.N = this.v.getTitle();
            this.R = this.v.isDraggable();
            this.K = getId();
            this.W = this.v.isPerspective();
            this.h = this.v.isFlat();
            this.B = this.v.isBelowMaskLayer();
            this.u = this.v.getAlpha();
            setRotateAngle(this.v.getRotateAngle());
            this.y = this.v.getDisplayLevel();
            this.w = this.v.isInfoWindowAutoOverturn();
            this.x = this.v.isInfoWindowEnable();
        }
    }

    public boolean isClickable() {
        return this.z;
    }

    public boolean isInfoWindowAutoOverturn() {
        return this.w;
    }

    public boolean isInfoWindowEnable() {
        return this.x;
    }

    public void setInfoWindowEnable(boolean z) {
        this.x = z;
        this.v.infoWindowEnable(z);
    }

    public void setAutoOverturnInfoWindow(boolean z) {
        this.w = z;
        this.v.autoOverturnInfoWindow(z);
    }

    public void setClickable(boolean z) {
        this.z = z;
    }

    public void setDisplayLevel(int i) {
        this.y = i;
        this.v.displayLevel(i);
    }

    public void setFixingPointEnable(boolean z) {
        boolean z2 = false;
        this.A = z;
        if (!z) {
            if (this.ad) {
                z2 = true;
            }
            this.L = getPosition();
            setPosition(this.L);
            if (z2) {
                this.ad = true;
            }
        } else if (this.ad && this.L != null) {
            IPoint iPoint = new IPoint();
            this.T.a().c().map2Win(this.o.x, this.o.y, iPoint);
            this.ae = iPoint.x;
            this.af = iPoint.y;
        }
    }

    public void setPositionNotUpdate(LatLng latLng) {
        setPosition(latLng);
    }

    public void setRotateAngleNotUpdate(float f) {
    }

    public void setBelowMaskLayer(boolean z) {
        this.B = z;
    }

    public boolean m() {
        return this.B;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void a(FPoint[] fPointArr) {
        if (fPointArr != null) {
            v();
            if (this.G > 0.0f && this.H > 0.0f) {
                for (FPoint fPoint : fPointArr) {
                    if (fPoint.x > this.ai) {
                        this.ai = fPoint.x;
                    }
                    if (fPoint.x < this.ah) {
                        this.ah = fPoint.x;
                    }
                    if (fPoint.y > this.ak) {
                        this.ak = fPoint.y;
                    }
                    if (fPoint.y < this.aj) {
                        this.aj = fPoint.y;
                    }
                }
                if (this.o.x < (this.ah + this.ai) / 2.0f) {
                    this.F.x = this.o.x + (this.G / 2.0f);
                } else {
                    this.F.x = this.o.x - (this.G / 2.0f);
                }
                if (this.o.y < (this.ak + this.aj) / 2.0f) {
                    this.F.y = this.o.y;
                } else {
                    this.F.y = this.o.y - this.H;
                }
            }
        }
    }

    private void v() {
        if (this.T.a() != null && this.T.a().getMapConfig() != null) {
            this.G = this.T.a().getMapConfig().getMapPerPixelUnitLength() * ((float) o());
            this.H = this.T.a().getMapConfig().getMapPerPixelUnitLength() * ((float) p());
        }
    }
}
