package com.amap.api.mapcore.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.RemoteException;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.LatLngBounds.Builder;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.WeightedLatLng;
import com.autonavi.amap.mapcore.AMapNativeRenderer;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.FPoint3;
import com.autonavi.amap.mapcore.FPointBounds;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapConfig;
import com.autonavi.amap.mapcore.VirtualEarthProjection;
import com.autonavi.amap.mapcore.interfaces.IOverlay;
import com.huawei.watermark.manager.parse.WMElement;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: PolylineDelegateImp */
public class dd implements cw {
    private int A = 0;
    private int B = 0;
    private int C = -16777216;
    private int D = 0;
    private float E = 10.0f;
    private float F = 0.0f;
    private float G = 0.0f;
    private float H;
    private float I;
    private float J;
    private float K;
    private float L = WMElement.CAMERASIZEVALUE1B1;
    private float M = 0.0f;
    private float[] N;
    private int[] O;
    private int[] P;
    private double Q = 5.0d;
    private boolean R = false;
    private final int S = 2;
    private FPointBounds T = null;
    private PolylineOptions U;
    private int V = 0;
    private int W = 2;
    int a = 0;
    int b = 0;
    ArrayList<FPoint> c = new ArrayList();
    long d = 0;
    private j e;
    private String f;
    private List<IPoint> g = new ArrayList();
    private List<FPoint> h = new ArrayList();
    private List<LatLng> i = new ArrayList();
    private List<BitmapDescriptor> j = new ArrayList();
    private List<Integer> k = new ArrayList();
    private List<Integer> l = new ArrayList();
    private List<Integer> m = new ArrayList();
    private FloatBuffer n;
    private BitmapDescriptor o = null;
    private LatLngBounds p = null;
    private Object q = new Object();
    private boolean r = true;
    private boolean s = true;
    private boolean t = false;
    private boolean u = false;
    private boolean v = false;
    private boolean w = true;
    private boolean x = false;
    private boolean y = false;
    private boolean z = true;

    public void a(boolean z) {
        this.z = z;
        this.e.e().setRunLowFrame(false);
    }

    public void setGeodesic(boolean z) throws RemoteException {
        this.t = z;
        this.e.e().setRunLowFrame(false);
    }

    public boolean isGeodesic() {
        return this.t;
    }

    public void setDottedLine(boolean z) {
        if (this.A == 2 || this.A == 0) {
            this.u = z;
            if (z && this.s) {
                this.A = 2;
            }
            this.e.e().setRunLowFrame(false);
        }
    }

    public boolean isDottedLine() {
        return this.u;
    }

    public dd(j jVar, PolylineOptions polylineOptions) {
        this.e = jVar;
        setOptions(polylineOptions);
        try {
            this.f = getId();
        } catch (Throwable e) {
            fo.b(e, "PolylineDelegateImp", "create");
            e.printStackTrace();
        }
    }

    void a(List<LatLng> list) throws RemoteException {
        List arrayList = new ArrayList();
        Builder builder = LatLngBounds.builder();
        if (list != null) {
            LatLng latLng = null;
            for (LatLng latLng2 : list) {
                IPoint iPoint;
                if (!this.t) {
                    iPoint = new IPoint();
                    this.e.e().a(latLng2.latitude, latLng2.longitude, iPoint);
                    arrayList.add(iPoint);
                    builder.include(latLng2);
                } else if (latLng != null) {
                    if (Math.abs(latLng2.longitude - latLng.longitude) < 0.01d) {
                        iPoint = new IPoint();
                        this.e.e().a(latLng.latitude, latLng.longitude, iPoint);
                        arrayList.add(iPoint);
                        builder.include(latLng);
                        iPoint = new IPoint();
                        this.e.e().a(latLng2.latitude, latLng2.longitude, iPoint);
                        arrayList.add(iPoint);
                        builder.include(latLng2);
                    } else {
                        a(latLng, latLng2, arrayList, builder);
                    }
                }
                latLng = latLng2;
            }
        }
        this.g = arrayList;
        this.D = 0;
        if (this.g.size() > 0) {
            this.p = builder.build();
        }
        this.e.e().setRunLowFrame(false);
    }

    IPoint a(IPoint iPoint, IPoint iPoint2, IPoint iPoint3, double d, int i) {
        IPoint iPoint4 = new IPoint();
        double d2 = (double) (iPoint2.x - iPoint.x);
        double d3 = (double) (iPoint2.y - iPoint.y);
        iPoint4.y = (int) (((((double) i) * d) / Math.sqrt(((d3 * d3) / (d2 * d2)) + WeightedLatLng.DEFAULT_INTENSITY)) + ((double) iPoint3.y));
        iPoint4.x = (int) (((d3 * ((double) (iPoint3.y - iPoint4.y))) / d2) + ((double) iPoint3.x));
        return iPoint4;
    }

    void a(List<IPoint> list, List<IPoint> list2, double d) {
        if (list.size() == 3) {
            for (int i = 0; i <= 10; i = (int) (((float) i) + WMElement.CAMERASIZEVALUE1B1)) {
                float f = ((float) i) / 10.0f;
                IPoint iPoint = new IPoint();
                double d2 = ((((WeightedLatLng.DEFAULT_INTENSITY - ((double) f)) * (WeightedLatLng.DEFAULT_INTENSITY - ((double) f))) * ((double) ((IPoint) list.get(0)).x)) + (((((double) (2.0f * f)) * (WeightedLatLng.DEFAULT_INTENSITY - ((double) f))) * ((double) ((IPoint) list.get(1)).x)) * d)) + ((double) (((float) ((IPoint) list.get(2)).x) * (f * f)));
                double d3 = ((((WeightedLatLng.DEFAULT_INTENSITY - ((double) f)) * (WeightedLatLng.DEFAULT_INTENSITY - ((double) f))) * ((double) ((IPoint) list.get(0)).y)) + (((((double) (2.0f * f)) * (WeightedLatLng.DEFAULT_INTENSITY - ((double) f))) * ((double) ((IPoint) list.get(1)).y)) * d)) + ((double) (((float) ((IPoint) list.get(2)).y) * (f * f)));
                double d4 = (((WeightedLatLng.DEFAULT_INTENSITY - ((double) f)) * (WeightedLatLng.DEFAULT_INTENSITY - ((double) f))) + ((((double) (2.0f * f)) * (WeightedLatLng.DEFAULT_INTENSITY - ((double) f))) * d)) + ((double) (f * f));
                iPoint.x = (int) (d2 / ((((WeightedLatLng.DEFAULT_INTENSITY - ((double) f)) * (WeightedLatLng.DEFAULT_INTENSITY - ((double) f))) + ((((double) (2.0f * f)) * (WeightedLatLng.DEFAULT_INTENSITY - ((double) f))) * d)) + ((double) (f * f))));
                iPoint.y = (int) (d3 / d4);
                list2.add(iPoint);
            }
        }
    }

    void a(LatLng latLng, LatLng latLng2, List<IPoint> list, Builder builder) {
        double abs = (Math.abs(latLng.longitude - latLng2.longitude) * 3.141592653589793d) / VirtualEarthProjection.MaxLongitude;
        LatLng latLng3 = new LatLng((latLng2.latitude + latLng.latitude) / 2.0d, (latLng2.longitude + latLng.longitude) / 2.0d, false);
        builder.include(latLng).include(latLng3).include(latLng2);
        int i = latLng3.latitude > 0.0d ? -1 : 1;
        IPoint iPoint = new IPoint();
        this.e.e().a(latLng.latitude, latLng.longitude, iPoint);
        IPoint iPoint2 = new IPoint();
        this.e.e().a(latLng2.latitude, latLng2.longitude, iPoint2);
        IPoint iPoint3 = new IPoint();
        this.e.e().a(latLng3.latitude, latLng3.longitude, iPoint3);
        double cos = Math.cos(0.5d * abs);
        IPoint a = a(iPoint, iPoint2, iPoint3, (Math.hypot((double) (iPoint.x - iPoint2.x), (double) (iPoint.y - iPoint2.y)) * 0.5d) * Math.tan(0.5d * abs), i);
        List arrayList = new ArrayList();
        arrayList.add(iPoint);
        arrayList.add(a);
        arrayList.add(iPoint2);
        a(arrayList, (List) list, cos);
    }

    public void remove() throws RemoteException {
        this.e.d(getId());
        setVisible(false);
        this.e.e().setRunLowFrame(false);
    }

    public String getId() throws RemoteException {
        if (this.f == null) {
            this.f = j.a("Polyline");
        }
        return this.f;
    }

    public void setPoints(List<LatLng> list) throws RemoteException {
        try {
            this.i = list;
            synchronized (this.q) {
                a((List) list);
            }
            this.w = true;
            this.e.e().setRunLowFrame(false);
            this.U.setPoints(list);
        } catch (Throwable th) {
            fo.b(th, "PolylineDelegateImp", "setPoints");
            this.g.clear();
            th.printStackTrace();
        }
    }

    public List<LatLng> getPoints() throws RemoteException {
        return this.i;
    }

    public void setWidth(float f) throws RemoteException {
        this.E = f;
        this.e.e().setRunLowFrame(false);
        this.U.width(f);
    }

    public float getWidth() throws RemoteException {
        return this.E;
    }

    public void setColor(int i) {
        if (this.A == 0 || this.A == 2) {
            this.C = i;
            this.H = ((float) Color.alpha(i)) / 255.0f;
            this.I = ((float) Color.red(i)) / 255.0f;
            this.J = ((float) Color.green(i)) / 255.0f;
            this.K = ((float) Color.blue(i)) / 255.0f;
            if (this.s) {
                this.A = 0;
            }
            this.e.e().setRunLowFrame(false);
        }
        this.U.color(i);
    }

    public int getColor() throws RemoteException {
        return this.C;
    }

    public void setZIndex(float f) throws RemoteException {
        this.F = f;
        this.e.c();
        this.e.e().setRunLowFrame(false);
        this.U.zIndex(f);
    }

    public float getZIndex() throws RemoteException {
        return this.F;
    }

    public void setVisible(boolean z) throws RemoteException {
        this.r = z;
        this.e.e().setRunLowFrame(false);
        this.U.visible(z);
    }

    public boolean isVisible() throws RemoteException {
        return this.r;
    }

    public boolean equalsRemote(IOverlay iOverlay) throws RemoteException {
        if (equals(iOverlay) || iOverlay.getId().equals(getId())) {
            return true;
        }
        return false;
    }

    public int hashCodeRemote() throws RemoteException {
        return super.hashCode();
    }

    public boolean a() {
        return true;
    }

    public boolean b() throws RemoteException {
        synchronized (this.q) {
            FPointBounds.Builder builder = new FPointBounds.Builder();
            this.h.clear();
            this.y = false;
            this.N = new float[(this.g.size() * 3)];
            this.a = this.N.length;
            int i = 0;
            for (IPoint iPoint : this.g) {
                FPoint fPoint3 = new FPoint3();
                this.e.e().b(iPoint.y, iPoint.x, fPoint3);
                this.N[i * 3] = fPoint3.x;
                this.N[(i * 3) + 1] = fPoint3.y;
                this.N[(i * 3) + 2] = 0.0f;
                if (this.k != null && this.k.size() > i) {
                    fPoint3.setColorIndex(((Integer) this.k.get(i)).intValue());
                }
                this.h.add(fPoint3);
                builder.include(fPoint3);
                i++;
            }
            this.T = builder.build();
        }
        if (!this.z) {
            this.n = eh.a(this.N);
        }
        this.D = this.g.size();
        e();
        return true;
    }

    private void e() {
        if (this.D > 5000 && this.G <= 12.0f) {
            float f = (this.E / 2.0f) + (this.G / 2.0f);
            if (f > 200.0f) {
                f = 200.0f;
            }
            this.M = this.e.e().c().getMapLenWithWin((int) f);
            return;
        }
        this.M = this.e.e().c().getMapLenWithWin(10);
    }

    private void e(List<FPoint> list) throws RemoteException {
        int i = 0;
        this.c.clear();
        int size = list.size();
        if (size >= 2) {
            Iterator it;
            Object obj;
            List arrayList;
            int i2;
            FPoint3 fPoint3;
            int i3;
            FPoint fPoint = (FPoint) list.get(0);
            this.c.add(fPoint);
            int i4 = 1;
            FPoint fPoint2 = fPoint;
            while (i4 < size - 1) {
                fPoint = (FPoint) list.get(i4);
                if (a(fPoint2, fPoint)) {
                    this.c.add(fPoint);
                } else {
                    fPoint = fPoint2;
                }
                i4++;
                fPoint2 = fPoint;
            }
            this.c.add(list.get(size - 1));
            int size2 = this.c.size() * 3;
            this.a = size2;
            if (this.N != null) {
                if (this.N.length < this.a) {
                }
                if (this.A == 5) {
                    it = this.c.iterator();
                    while (it.hasNext()) {
                        fPoint = (FPoint) it.next();
                        this.N[i * 3] = fPoint.x;
                        this.N[(i * 3) + 1] = fPoint.y;
                        this.N[(i * 3) + 2] = 0.0f;
                        i++;
                    }
                } else {
                    obj = new int[this.c.size()];
                    arrayList = new ArrayList();
                    i4 = 0;
                    size = 0;
                    for (i2 = 0; i2 < size2 / 3; i2++) {
                        fPoint3 = (FPoint3) this.c.get(i2);
                        this.N[i2 * 3] = fPoint3.x;
                        this.N[(i2 * 3) + 1] = fPoint3.y;
                        this.N[(i2 * 3) + 2] = 0.0f;
                        i3 = fPoint3.colorIndex;
                        if (i2 != 0) {
                            arrayList.add(Integer.valueOf(i3));
                            size = i3;
                        } else if (i3 == size) {
                        } else {
                            if (i3 != -1) {
                                size = i3;
                            }
                            arrayList.add(Integer.valueOf(size));
                        }
                        obj[i4] = i2;
                        i4++;
                    }
                    this.O = new int[arrayList.size()];
                    System.arraycopy(obj, 0, this.O, 0, this.O.length);
                    this.m = arrayList;
                }
            }
            this.N = new float[size2];
            if (this.A == 5) {
                obj = new int[this.c.size()];
                arrayList = new ArrayList();
                i4 = 0;
                size = 0;
                for (i2 = 0; i2 < size2 / 3; i2++) {
                    fPoint3 = (FPoint3) this.c.get(i2);
                    this.N[i2 * 3] = fPoint3.x;
                    this.N[(i2 * 3) + 1] = fPoint3.y;
                    this.N[(i2 * 3) + 2] = 0.0f;
                    i3 = fPoint3.colorIndex;
                    if (i2 != 0) {
                        arrayList.add(Integer.valueOf(i3));
                        size = i3;
                    } else if (i3 == size) {
                    } else {
                        if (i3 != -1) {
                            size = i3;
                        }
                        arrayList.add(Integer.valueOf(size));
                    }
                    obj[i4] = i2;
                    i4++;
                }
                this.O = new int[arrayList.size()];
                System.arraycopy(obj, 0, this.O, 0, this.O.length);
                this.m = arrayList;
            } else {
                it = this.c.iterator();
                while (it.hasNext()) {
                    fPoint = (FPoint) it.next();
                    this.N[i * 3] = fPoint.x;
                    this.N[(i * 3) + 1] = fPoint.y;
                    this.N[(i * 3) + 2] = 0.0f;
                    i++;
                }
            }
        }
    }

    private boolean a(FPoint fPoint, FPoint fPoint2) {
        boolean z;
        if (Math.abs(fPoint2.x - fPoint.x) >= this.M) {
            z = true;
        } else {
            z = false;
        }
        if (z || Math.abs(fPoint2.y - fPoint.y) >= this.M) {
            return true;
        }
        return false;
    }

    public void setCustomTexture(BitmapDescriptor bitmapDescriptor) {
        Object obj = 1;
        long nanoTime = System.nanoTime();
        if (nanoTime - this.d < 16) {
            obj = null;
        }
        if (obj != null) {
            this.d = nanoTime;
            if (bitmapDescriptor != null) {
                synchronized (this) {
                    if (!(this.o == null || bitmapDescriptor == null || !this.o.equals(bitmapDescriptor))) {
                        this.o.recycle();
                    }
                    this.s = false;
                    this.v = false;
                    this.A = 1;
                    this.o = bitmapDescriptor;
                    this.e.e().setRunLowFrame(false);
                    this.U.setCustomTexture(bitmapDescriptor);
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void a(GL10 gl10) throws RemoteException {
        if (this.g != null && this.g.size() != 0 && this.E > 0.0f && this.e.e() != null) {
            if (this.w) {
                b();
                this.w = false;
            }
            if (this.N != null && this.D > 0) {
                if (this.z) {
                    b(gl10);
                } else {
                    if (this.n == null) {
                        this.n = eh.a(this.N);
                    }
                    du.a(gl10, 3, this.C, this.n, this.E, this.D);
                }
            }
            this.y = true;
        }
    }

    private void b(GL10 gl10) {
        float mapLenWithWin = this.e.e().c().getMapLenWithWin((int) this.E);
        switch (this.A) {
            case 0:
                f(gl10, mapLenWithWin);
                return;
            case 1:
                d(gl10, mapLenWithWin);
                return;
            case 2:
                e(gl10, mapLenWithWin);
                return;
            case 3:
                c(gl10, mapLenWithWin);
                return;
            case 4:
                b(gl10, mapLenWithWin);
                return;
            case 5:
                a(gl10, mapLenWithWin);
                return;
            default:
                return;
        }
    }

    private void a(GL10 gl10, float f) {
        int i = 0;
        if (!this.v) {
            try {
                if (this.j != null) {
                    this.P = new int[this.j.size()];
                    boolean z = VERSION.SDK_INT >= 12;
                    int i2 = 0;
                    int i3 = 0;
                    for (BitmapDescriptor a : this.j) {
                        int a2 = a(gl10, i2, z, a);
                        this.P[i3] = a2;
                        i3++;
                        i2 = a2;
                    }
                    this.v = true;
                }
            } catch (Throwable th) {
                fo.b(th, "MarkerDelegateImp", "loadtexture");
                return;
            }
        }
        FPoint[] mapRect = this.e.e().getMapConfig().getMapRect();
        try {
            List list = this.h;
            if (a(mapRect)) {
                synchronized (this.q) {
                    list = eh.b(mapRect, this.h, false);
                }
            }
            if (list.size() >= 2) {
                e(list);
                int[] iArr = new int[this.m.size()];
                for (int i4 = 0; i4 < iArr.length; i4++) {
                    a2 = ((Integer) this.m.get(i4)).intValue();
                    if (a2 < 0) {
                        a2 = 0;
                    }
                    iArr[i4] = this.P[a2];
                }
                if (iArr == null) {
                    a2 = 0;
                } else {
                    a2 = 1;
                }
                if (this.O != null) {
                    i = 1;
                }
                if ((a2 & i) != 0) {
                    AMapNativeRenderer.nativeDrawLineByMultiTextureID(this.N, this.a, f, iArr, iArr.length, this.O, this.O.length, WMElement.CAMERASIZEVALUE1B1 - this.L);
                }
            }
        } catch (Throwable th2) {
            th2.printStackTrace();
        }
    }

    private int a(GL10 gl10, int i, boolean z, BitmapDescriptor bitmapDescriptor) {
        int a;
        if (z) {
            a = this.e.e().a(bitmapDescriptor);
        } else {
            a = i;
        }
        if (a == 0) {
            Bitmap bitmap = bitmapDescriptor.getBitmap();
            if (!(bitmap == null || bitmap.isRecycled())) {
                a = c(gl10);
                if (z) {
                    this.e.e().a(new s(bitmapDescriptor, a));
                }
                eh.b(gl10, a, bitmap, true);
            }
        }
        return a;
    }

    private void b(GL10 gl10, float f) {
        int[] iArr = new int[this.l.size()];
        for (int i = 0; i < this.l.size(); i++) {
            iArr[i] = ((Integer) this.l.get(i)).intValue();
        }
        AMapNativeRenderer.nativeDrawGradientColorLine(this.N, this.N.length, f, iArr, this.l.size(), this.O, this.O.length, this.e.e().b());
    }

    private void c(GL10 gl10, float f) {
        int[] iArr = new int[this.l.size()];
        for (int i = 0; i < this.l.size(); i++) {
            iArr[i] = ((Integer) this.l.get(i)).intValue();
        }
        AMapNativeRenderer.nativeDrawLineByMultiColor(this.N, this.N.length, f, this.e.e().b(), iArr, this.l.size(), this.O, this.O.length);
    }

    private void d(GL10 gl10, float f) {
        boolean z = false;
        if (!this.v) {
            synchronized (this) {
                try {
                    if (this.o != null) {
                        int[] iArr = new int[]{0};
                        if (VERSION.SDK_INT >= 12) {
                            z = true;
                        }
                        iArr[0] = a(gl10, 0, z, this.o);
                        this.B = iArr[0];
                        this.v = true;
                    }
                } catch (Throwable th) {
                    fo.b(th, "MarkerDelegateImp", "loadtexture");
                    return;
                }
            }
        }
        try {
            MapConfig mapConfig = this.e.e().getMapConfig();
            if (mapConfig.getChangeRatio() == 0.0d && this.N != null) {
                this.V++;
                if (this.V > this.W) {
                    AMapNativeRenderer.nativeDrawLineByTextureID(this.N, this.a, f, this.B, this.I, this.J, this.K, this.H, WMElement.CAMERASIZEVALUE1B1 - this.L, false, false, false);
                    return;
                }
            }
            this.V = 0;
            FPoint[] mapRect = mapConfig.getMapRect();
            List list = this.h;
            if (a(mapRect)) {
                synchronized (this.q) {
                    list = eh.a(mapRect, this.h, false);
                }
            }
            if (list.size() >= 2) {
                e(list);
                AMapNativeRenderer.nativeDrawLineByTextureID(this.N, this.a, f, this.B, this.I, this.J, this.K, this.H, WMElement.CAMERASIZEVALUE1B1 - this.L, false, false, false);
            }
        } catch (Throwable th2) {
        }
    }

    private void e(GL10 gl10, float f) {
        AMapNativeRenderer.nativeDrawLineByTextureID(this.N, this.N.length, f, this.e.e().k(), this.I, this.J, this.K, this.H, 0.0f, true, true, false);
    }

    private void f(GL10 gl10, float f) {
        try {
            List list = this.h;
            if (this.e.e() != null) {
                if (this.e.e().getMapConfig().getChangeRatio() == 0.0d && this.N != null) {
                    this.V++;
                    if (this.V > this.W) {
                        AMapNativeRenderer.nativeDrawLineByTextureID(this.N, this.a, f, this.e.e().b(), this.I, this.J, this.K, this.H, 0.0f, false, true, false);
                        return;
                    }
                }
                this.V = 0;
                FPoint[] p = this.e.e().p();
                if (a(p)) {
                    synchronized (this.q) {
                        list = eh.a(p, this.h, false);
                    }
                }
                if (list.size() >= 2) {
                    e(list);
                    AMapNativeRenderer.nativeDrawLineByTextureID(this.N, this.a, f, this.e.e().b(), this.I, this.J, this.K, this.H, 0.0f, false, true, false);
                }
            }
        } catch (Throwable th) {
        }
    }

    private int c(GL10 gl10) {
        int[] iArr = new int[]{0};
        gl10.glGenTextures(1, iArr, 0);
        return iArr[0];
    }

    private boolean a(FPoint[] fPointArr) {
        this.G = this.e.e().o();
        e();
        if (this.G <= 10.0f) {
            return false;
        }
        try {
            if (this.e.e() == null) {
                return false;
            }
            if (eh.a(this.T.northeast, fPointArr) && eh.a(this.T.southwest, fPointArr)) {
                return false;
            }
            return true;
        } catch (Throwable th) {
            return false;
        }
    }

    public void destroy() {
        try {
            remove();
            if (this.P != null && this.P.length > 0) {
                for (int i = 0; i < this.P.length; i++) {
                    this.e.a(Integer.valueOf(this.P[i]));
                    this.e.e().g(this.P[i]);
                }
            }
            if (this.B > 0) {
                this.e.a(Integer.valueOf(this.B));
                this.e.e().g(this.B);
            }
            if (this.N != null) {
                this.N = null;
            }
            if (this.n != null) {
                this.n.clear();
                this.n = null;
            }
            if (this.j != null && this.j.size() > 0) {
                for (BitmapDescriptor recycle : this.j) {
                    recycle.recycle();
                }
            }
            if (this.o != null) {
                this.o.recycle();
            }
            if (this.l != null) {
                this.l.clear();
                this.l = null;
            }
            if (this.k != null) {
                this.k.clear();
                this.k = null;
            }
            if (this.i != null) {
                this.i.clear();
                this.i = null;
            }
            this.U = null;
        } catch (Throwable th) {
            fo.b(th, "PolylineDelegateImp", "destroy");
            th.printStackTrace();
        }
    }

    public boolean c() {
        return this.y;
    }

    public LatLng getNearestLatLng(LatLng latLng) {
        if (latLng == null || this.i == null || this.i.size() == 0) {
            return null;
        }
        float f = 0.0f;
        int i = 0;
        int i2 = 0;
        while (i2 < this.i.size()) {
            try {
                if (i2 != 0) {
                    float calculateLineDistance = AMapUtils.calculateLineDistance(latLng, (LatLng) this.i.get(i2));
                    if (f > calculateLineDistance) {
                        f = calculateLineDistance;
                        i = i2;
                    }
                } else {
                    f = AMapUtils.calculateLineDistance(latLng, (LatLng) this.i.get(i2));
                }
                i2++;
            } catch (Throwable th) {
                fo.b(th, "PolylineDelegateImp", "getNearestLatLng");
                th.printStackTrace();
                return null;
            }
        }
        return (LatLng) this.i.get(i);
    }

    public boolean a(LatLng latLng) {
        Object obj = new float[this.N.length];
        System.arraycopy(this.N, 0, obj, 0, this.N.length);
        if (obj.length / 3 < 2) {
            return false;
        }
        try {
            ArrayList f = f();
            if (f == null || f.size() < 1) {
                return false;
            }
            double mapLenWithWin = (double) this.e.e().c().getMapLenWithWin(((int) this.E) / 4);
            double mapLenWithWin2 = (double) this.e.e().c().getMapLenWithWin((int) this.Q);
            FPoint b = b(latLng);
            FPoint fPoint = null;
            for (int i = 0; i < f.size() - 1; i++) {
                FPoint fPoint2;
                if (i != 0) {
                    fPoint2 = fPoint;
                } else {
                    fPoint2 = (FPoint) f.get(i);
                }
                fPoint = (FPoint) f.get(i + 1);
                if ((mapLenWithWin2 + mapLenWithWin) - a(b, fPoint2, fPoint) >= 0.0d) {
                    f.clear();
                    return true;
                }
            }
            f.clear();
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private ArrayList<FPoint> f() {
        ArrayList<FPoint> arrayList = new ArrayList();
        int i = 0;
        while (i < this.N.length) {
            float f = this.N[i];
            i++;
            float f2 = this.N[i];
            i++;
            arrayList.add(new FPoint(f, f2));
            i++;
        }
        return arrayList;
    }

    private double a(FPoint fPoint, FPoint fPoint2, FPoint fPoint3) {
        return a((double) fPoint.x, (double) fPoint.y, (double) fPoint2.x, (double) fPoint2.y, (double) fPoint3.x, (double) fPoint3.y);
    }

    private FPoint b(LatLng latLng) {
        IPoint iPoint = new IPoint();
        this.e.e().a(latLng.latitude, latLng.longitude, iPoint);
        FPoint fPoint = new FPoint();
        this.e.e().b(iPoint.y, iPoint.x, fPoint);
        return fPoint;
    }

    private double a(double d, double d2, double d3, double d4, double d5, double d6) {
        double d7 = ((d5 - d3) * (d - d3)) + ((d6 - d4) * (d2 - d4));
        if (d7 <= 0.0d) {
            return Math.sqrt(((d - d3) * (d - d3)) + ((d2 - d4) * (d2 - d4)));
        }
        double d8 = ((d5 - d3) * (d5 - d3)) + ((d6 - d4) * (d6 - d4));
        if (d7 >= d8) {
            return Math.sqrt(((d - d5) * (d - d5)) + ((d2 - d6) * (d2 - d6)));
        }
        d7 /= d8;
        d8 = ((d5 - d3) * d7) + d3;
        d7 = (d7 * (d6 - d4)) + d4;
        return Math.sqrt(((d7 - d2) * (d7 - d2)) + ((d - d8) * (d - d8)));
    }

    public void setTransparency(float f) {
        this.L = f;
        this.e.e().setRunLowFrame(false);
    }

    public void b(List<BitmapDescriptor> list) {
        if (list != null && list.size() != 0) {
            if (list.size() <= 1) {
                setCustomTexture((BitmapDescriptor) list.get(0));
            } else {
                this.s = false;
                this.A = 5;
                this.j = list;
                this.e.e().setRunLowFrame(false);
            }
        }
    }

    public void c(List<Integer> list) {
        if (list != null && list.size() != 0) {
            this.k = list;
            this.m = f(list);
        }
    }

    public void d(List<Integer> list) {
        if (list != null && list.size() != 0) {
            if (list.size() <= 1) {
                setColor(((Integer) list.get(0)).intValue());
            } else {
                this.s = false;
                this.l = f(list);
                this.A = 3;
                this.e.e().setRunLowFrame(false);
            }
        }
    }

    public void b(boolean z) {
        if (z && this.l != null && this.l.size() > 1) {
            this.x = z;
            this.A = 4;
            this.e.e().setRunLowFrame(false);
        }
    }

    private List<Integer> f(List<Integer> list) {
        Object obj = new int[list.size()];
        List<Integer> arrayList = new ArrayList();
        int i = 0;
        int i2 = 0;
        for (int i3 = 0; i3 < list.size(); i3++) {
            int intValue = ((Integer) list.get(i3)).intValue();
            if (i3 == 0) {
                arrayList.add(Integer.valueOf(intValue));
            } else if (intValue != i2) {
                arrayList.add(Integer.valueOf(intValue));
            } else {
            }
            obj[i] = i3;
            i++;
            i2 = intValue;
        }
        this.O = new int[arrayList.size()];
        System.arraycopy(obj, 0, this.O, 0, this.O.length);
        return arrayList;
    }

    public void d() {
        this.v = false;
        this.B = 0;
        if (this.P != null) {
            Arrays.fill(this.P, 0);
        }
    }

    public void setAboveMaskLayer(boolean z) {
        this.R = z;
    }

    public boolean isAboveMaskLayer() {
        return this.R;
    }

    public void setOptions(PolylineOptions polylineOptions) {
        if (polylineOptions != null) {
            this.U = polylineOptions;
            try {
                setColor(polylineOptions.getColor());
                setGeodesic(polylineOptions.isGeodesic());
                setDottedLine(polylineOptions.isDottedLine());
                setAboveMaskLayer(polylineOptions.isAboveMaskLayer());
                setVisible(polylineOptions.isVisible());
                setWidth(polylineOptions.getWidth());
                setZIndex(polylineOptions.getZIndex());
                a(polylineOptions.isUseTexture());
                setTransparency(polylineOptions.getTransparency());
                if (polylineOptions.getColorValues() != null) {
                    d(polylineOptions.getColorValues());
                    b(polylineOptions.isUseGradient());
                }
                if (polylineOptions.getCustomTexture() != null) {
                    setCustomTexture(polylineOptions.getCustomTexture());
                    d();
                }
                if (polylineOptions.getCustomTextureList() != null) {
                    b(polylineOptions.getCustomTextureList());
                    c(polylineOptions.getCustomTextureIndex());
                    d();
                }
                setPoints(polylineOptions.getPoints());
            } catch (Throwable e) {
                fo.b(e, "PolylineDelegateImp", "setOptions");
                e.printStackTrace();
            }
        }
    }

    public PolylineOptions getOptions() {
        return this.U;
    }
}
