package com.amap.api.mapcore;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.RemoteException;
import android.util.Log;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.amap.api.mapcore.util.bj;
import com.amap.api.mapcore.util.ce;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.LatLngBounds.Builder;
import com.amap.api.maps.model.WeightedLatLng;
import com.autonavi.amap.mapcore.AMapNativeRenderer;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.IPoint;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: PolylineDelegateImp */
class bg implements al {
    private float A = 0.0f;
    private float B = 0.0f;
    private float C;
    private float D;
    private float E;
    private float F;
    private float G = 0.0f;
    private float H = 0.0f;
    private float[] I;
    private int[] J;
    private int[] K;
    private double L = 5.0d;
    private w a;
    private String b;
    private List<IPoint> c = new ArrayList();
    private List<FPoint> d = new ArrayList();
    private List<LatLng> e = new ArrayList();
    private List<BitmapDescriptor> f = new ArrayList();
    private List<Integer> g = new ArrayList();
    private List<Integer> h = new ArrayList();
    private FloatBuffer i;
    private BitmapDescriptor j = null;
    private LatLngBounds k = null;
    private Object l = new Object();
    private boolean m = true;
    private boolean n = true;
    private boolean o = false;
    private boolean p = false;
    private boolean q = false;
    private boolean r = true;
    private boolean s = false;
    private boolean t = false;
    private boolean u = true;
    private int v = 0;
    private int w = 0;
    private int x = -16777216;
    private int y = 0;
    private float z = 10.0f;

    public void d(boolean z) {
        this.u = z;
        this.a.a.f(false);
    }

    public void b(boolean z) throws RemoteException {
        this.o = z;
        this.a.a.f(false);
    }

    public boolean m() {
        return this.o;
    }

    public void c(boolean z) {
        if (this.v == 2 || this.v == 0) {
            this.p = z;
            if (z && this.n) {
                this.v = 2;
            }
            this.a.a.f(false);
        }
    }

    public boolean n() {
        return this.p;
    }

    public bg(w wVar) {
        this.a = wVar;
        try {
            this.b = c();
        } catch (Throwable e) {
            ce.a(e, "PolylineDelegateImp", "create");
            e.printStackTrace();
        }
    }

    void b(List<LatLng> list) throws RemoteException {
        Object arrayList = new ArrayList();
        Builder builder = LatLngBounds.builder();
        if (list != null) {
            LatLng latLng = null;
            for (LatLng latLng2 : list) {
                if (!(latLng2 == null || latLng2.equals(latLng))) {
                    IPoint iPoint;
                    if (!this.o) {
                        iPoint = new IPoint();
                        this.a.a.a(latLng2.latitude, latLng2.longitude, iPoint);
                        arrayList.add(iPoint);
                        builder.include(latLng2);
                    } else if (latLng != null) {
                        if (Math.abs(latLng2.longitude - latLng.longitude) < 0.01d) {
                            iPoint = new IPoint();
                            this.a.a.a(latLng.latitude, latLng.longitude, iPoint);
                            arrayList.add(iPoint);
                            builder.include(latLng);
                            iPoint = new IPoint();
                            this.a.a.a(latLng2.latitude, latLng2.longitude, iPoint);
                            arrayList.add(iPoint);
                            builder.include(latLng2);
                        } else {
                            a(latLng, latLng2, arrayList, builder);
                        }
                    }
                    latLng = latLng2;
                }
            }
        }
        this.c = arrayList;
        this.y = 0;
        if (this.c.size() > 0) {
            this.k = builder.build();
        }
        this.a.a.f(false);
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
            for (int i = 0; i <= 10; i = (int) (((float) i) + ContentUtil.FONT_SIZE_NORMAL)) {
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
        double abs = (Math.abs(latLng.longitude - latLng2.longitude) * 3.141592653589793d) / 180.0d;
        LatLng latLng3 = new LatLng((latLng2.latitude + latLng.latitude) / 2.0d, (latLng2.longitude + latLng.longitude) / 2.0d, false);
        builder.include(latLng).include(latLng3).include(latLng2);
        int i = latLng3.latitude > 0.0d ? -1 : 1;
        IPoint iPoint = new IPoint();
        this.a.a.a(latLng.latitude, latLng.longitude, iPoint);
        IPoint iPoint2 = new IPoint();
        this.a.a.a(latLng2.latitude, latLng2.longitude, iPoint2);
        IPoint iPoint3 = new IPoint();
        this.a.a.a(latLng3.latitude, latLng3.longitude, iPoint3);
        double cos = Math.cos(0.5d * abs);
        IPoint a = a(iPoint, iPoint2, iPoint3, (Math.hypot((double) (iPoint.x - iPoint2.x), (double) (iPoint.y - iPoint2.y)) * 0.5d) * Math.tan(0.5d * abs), i);
        List arrayList = new ArrayList();
        arrayList.add(iPoint);
        arrayList.add(a);
        arrayList.add(iPoint2);
        a(arrayList, (List) list, cos);
    }

    public void b() throws RemoteException {
        this.a.c(c());
        if (this.K != null && this.K.length > 0) {
            for (int valueOf : this.K) {
                this.a.a(Integer.valueOf(valueOf));
            }
            this.K = null;
        }
        if (this.w > 0) {
            this.a.a(Integer.valueOf(this.w));
            this.w = 0;
        }
        this.a.a.f(false);
    }

    public String c() throws RemoteException {
        if (this.b == null) {
            this.b = w.a("Polyline");
        }
        return this.b;
    }

    public void a(List<LatLng> list) throws RemoteException {
        try {
            this.e = list;
            synchronized (this.l) {
                b((List) list);
            }
            this.r = true;
            this.a.a.f(false);
        } catch (Throwable th) {
            ce.a(th, "PolylineDelegateImp", "setPoints");
            this.c.clear();
            th.printStackTrace();
        }
    }

    public List<LatLng> l() throws RemoteException {
        return this.e;
    }

    public void b(float f) throws RemoteException {
        this.z = f;
        this.a.a.f(false);
    }

    public float h() throws RemoteException {
        return this.z;
    }

    public void a(int i) {
        if (this.v == 0 || this.v == 2) {
            this.x = i;
            this.C = ((float) Color.alpha(i)) / 255.0f;
            this.D = ((float) Color.red(i)) / 255.0f;
            this.E = ((float) Color.green(i)) / 255.0f;
            this.F = ((float) Color.blue(i)) / 255.0f;
            if (this.n) {
                this.v = 0;
            }
            this.a.a.f(false);
        }
    }

    public int i() throws RemoteException {
        return this.x;
    }

    public void a(float f) throws RemoteException {
        this.A = f;
        this.a.b();
        this.a.a.f(false);
    }

    public float d() throws RemoteException {
        return this.A;
    }

    public void a(boolean z) throws RemoteException {
        this.m = z;
        this.a.a.f(false);
    }

    public boolean e() throws RemoteException {
        return this.m;
    }

    public boolean a(aj ajVar) throws RemoteException {
        if (equals(ajVar) || ajVar.c().equals(c())) {
            return true;
        }
        return false;
    }

    public int f() throws RemoteException {
        return super.hashCode();
    }

    public boolean a() {
        return true;
    }

    public void g() throws RemoteException {
        synchronized (this.l) {
            this.d.clear();
            this.t = false;
            this.I = new float[(this.c.size() * 3)];
            int i = 0;
            for (IPoint iPoint : this.c) {
                FPoint fPoint = new FPoint();
                this.a.a.b(iPoint.y, iPoint.x, fPoint);
                this.I[i * 3] = fPoint.x;
                this.I[(i * 3) + 1] = fPoint.y;
                this.I[(i * 3) + 2] = 0.0f;
                this.d.add(fPoint);
                i++;
            }
        }
        if (!this.u) {
            this.i = bj.a(this.I);
        }
        this.y = this.c.size();
        p();
    }

    private void p() {
        if (this.y > 5000 && this.B <= 12.0f) {
            float f = (this.z / 2.0f) + (this.B / 2.0f);
            if (f > 200.0f) {
                f = 200.0f;
            }
            this.H = this.a.a.c().getMapLenWithWin((int) f);
            return;
        }
        this.H = this.a.a.c().getMapLenWithWin(10);
    }

    private void f(List<FPoint> list) throws RemoteException {
        ArrayList arrayList = new ArrayList();
        int size = list.size();
        if (size >= 2) {
            FPoint fPoint = (FPoint) list.get(0);
            arrayList.add(fPoint);
            int i = 1;
            FPoint fPoint2 = fPoint;
            while (i < size - 1) {
                fPoint = (FPoint) list.get(i);
                if (a(fPoint2, fPoint)) {
                    arrayList.add(fPoint);
                } else {
                    fPoint = fPoint2;
                }
                i++;
                fPoint2 = fPoint;
            }
            arrayList.add(list.get(size - 1));
            this.I = new float[(arrayList.size() * 3)];
            this.J = null;
            this.J = new int[arrayList.size()];
            Iterator it = arrayList.iterator();
            int i2 = 0;
            while (it.hasNext()) {
                fPoint = (FPoint) it.next();
                this.J[i2] = i2;
                this.I[i2 * 3] = fPoint.x;
                this.I[(i2 * 3) + 1] = fPoint.y;
                this.I[(i2 * 3) + 2] = 0.0f;
                i2++;
            }
        }
    }

    private boolean a(FPoint fPoint, FPoint fPoint2) {
        boolean z;
        if (Math.abs(fPoint2.x - fPoint.x) >= this.H) {
            z = true;
        } else {
            z = false;
        }
        if (z || Math.abs(fPoint2.y - fPoint.y) >= this.H) {
            return true;
        }
        return false;
    }

    public void a(BitmapDescriptor bitmapDescriptor) {
        this.n = false;
        this.v = 1;
        this.j = bitmapDescriptor;
        this.a.a.f(false);
    }

    public void a(GL10 gl10) throws RemoteException {
        if (this.c != null && this.c.size() != 0 && this.z > 0.0f) {
            if (this.r) {
                g();
                this.r = false;
            }
            if (this.I != null && this.y > 0) {
                if (this.u) {
                    b(gl10);
                } else {
                    if (this.i == null) {
                        this.i = bj.a(this.I);
                    }
                    u.a(gl10, 3, this.x, this.i, this.z, this.y);
                }
            }
            this.t = true;
        }
    }

    private void b(GL10 gl10) {
        float mapLenWithWin = this.a.a.c().getMapLenWithWin((int) this.z);
        switch (this.v) {
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
        if (!this.q) {
            this.K = new int[this.f.size()];
            for (int i2 = 0; i2 < this.K.length; i2++) {
                int i3;
                int K = this.a.a.K();
                if (K != 0) {
                    i3 = K;
                } else {
                    int[] iArr = new int[]{0};
                    gl10.glGenTextures(1, iArr, 0);
                    i3 = iArr[0];
                }
                bj.b(gl10, i3, ((BitmapDescriptor) this.f.get(i2)).getBitmap(), true);
                this.K[i2] = i3;
            }
            this.q = true;
        }
        int[] iArr2 = new int[this.g.size()];
        while (i < iArr2.length) {
            iArr2[i] = this.K[((Integer) this.g.get(i)).intValue()];
            i++;
        }
        AMapNativeRenderer.nativeDrawLineByMultiTextureID(this.I, this.I.length, f, iArr2, iArr2.length, this.J, this.J.length, this.G);
    }

    private void b(GL10 gl10, float f) {
        int[] iArr = new int[this.h.size()];
        for (int i = 0; i < this.h.size(); i++) {
            iArr[i] = ((Integer) this.h.get(i)).intValue();
        }
        AMapNativeRenderer.nativeDrawGradientColorLine(this.I, this.I.length, f, iArr, this.h.size(), this.J, this.J.length, this.a.a.b());
    }

    private void c(GL10 gl10, float f) {
        int[] iArr = new int[this.h.size()];
        for (int i = 0; i < this.h.size(); i++) {
            iArr[i] = ((Integer) this.h.get(i)).intValue();
        }
        AMapNativeRenderer.nativeDrawLineByMultiColor(this.I, this.I.length, f, this.a.a.b(), iArr, this.h.size(), this.J, this.J.length);
    }

    private void d(GL10 gl10, float f) {
        if (!this.q) {
            this.w = this.a.a.K();
            if (this.w == 0) {
                int[] iArr = new int[]{0};
                gl10.glGenTextures(1, iArr, 0);
                this.w = iArr[0];
            }
            if (this.j != null) {
                bj.b(gl10, this.w, this.j.getBitmap(), true);
            }
            this.q = true;
        }
        try {
            List list = this.d;
            if (q()) {
                synchronized (this.l) {
                    list = bj.a(this.a.a, this.d, false);
                }
            }
            f(list);
        } catch (Throwable th) {
            th.printStackTrace();
        }
        AMapNativeRenderer.nativeDrawLineByTextureID(this.I, this.I.length, f, this.w, this.D, this.E, this.F, this.C, this.G, false, false, false);
    }

    private void e(GL10 gl10, float f) {
        AMapNativeRenderer.nativeDrawLineByTextureID(this.I, this.I.length, f, this.a.a.p(), this.D, this.E, this.F, this.C, 0.0f, true, true, false);
    }

    private void f(GL10 gl10, float f) {
        try {
            List list = this.d;
            if (q()) {
                synchronized (this.l) {
                    list = bj.a(this.a.a, this.d, false);
                }
            }
            f(list);
            AMapNativeRenderer.nativeDrawLineByTextureID(this.I, this.I.length, f, this.a.a.b(), this.D, this.E, this.F, this.C, 0.0f, false, true, false);
        } catch (Throwable th) {
        }
    }

    private boolean q() {
        try {
            this.B = this.a.a.r().zoom;
            p();
            if ((this.B <= 10.0f ? 1 : null) != null || this.v > 2) {
                return false;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        try {
            boolean z;
            if (this.a.a == null) {
                z = false;
            } else {
                Rect rect = new Rect(-100, -100, this.a.a.l() + 100, this.a.a.m() + 100);
                LatLng latLng = this.k.northeast;
                LatLng latLng2 = this.k.southwest;
                IPoint iPoint = new IPoint();
                this.a.a.b(latLng.latitude, latLng2.longitude, iPoint);
                IPoint iPoint2 = new IPoint();
                this.a.a.b(latLng.latitude, latLng.longitude, iPoint2);
                IPoint iPoint3 = new IPoint();
                this.a.a.b(latLng2.latitude, latLng.longitude, iPoint3);
                IPoint iPoint4 = new IPoint();
                this.a.a.b(latLng2.latitude, latLng2.longitude, iPoint4);
                if (rect.contains(iPoint.x, iPoint.y) && rect.contains(iPoint2.x, iPoint2.y) && rect.contains(iPoint3.x, iPoint3.y) && rect.contains(iPoint4.x, iPoint4.y)) {
                    z = false;
                } else {
                    z = true;
                }
            }
            return z;
        } catch (Throwable th) {
            return false;
        }
    }

    public void j() {
        try {
            b();
            if (this.I != null) {
                this.I = null;
            }
            if (this.i != null) {
                this.i.clear();
                this.i = null;
            }
            if (this.f != null && this.f.size() > 0) {
                for (BitmapDescriptor recycle : this.f) {
                    recycle.recycle();
                }
            }
            if (this.j != null) {
                this.j.recycle();
            }
            if (this.h != null) {
                this.h.clear();
                this.h = null;
            }
            if (this.g != null) {
                this.g.clear();
                this.g = null;
            }
        } catch (Throwable th) {
            ce.a(th, "PolylineDelegateImp", "destroy");
            th.printStackTrace();
            Log.d("destroy erro", "PolylineDelegateImp destroy");
        }
    }

    public boolean k() {
        return this.t;
    }

    public LatLng a(LatLng latLng) {
        if (latLng == null || this.e == null || this.e.size() == 0) {
            return null;
        }
        float f = 0.0f;
        int i = 0;
        int i2 = 0;
        while (i2 < this.e.size()) {
            try {
                if (i2 != 0) {
                    float calculateLineDistance = AMapUtils.calculateLineDistance(latLng, (LatLng) this.e.get(i2));
                    if (f > calculateLineDistance) {
                        f = calculateLineDistance;
                        i = i2;
                    }
                } else {
                    f = AMapUtils.calculateLineDistance(latLng, (LatLng) this.e.get(i2));
                }
                i2++;
            } catch (Throwable th) {
                ce.a(th, "PolylineDelegateImp", "getNearestLatLng");
                th.printStackTrace();
                return null;
            }
        }
        return (LatLng) this.e.get(i);
    }

    public boolean b(LatLng latLng) {
        Object obj = new float[this.I.length];
        System.arraycopy(this.I, 0, obj, 0, this.I.length);
        if (obj.length / 3 < 2) {
            return false;
        }
        try {
            ArrayList r = r();
            if (r == null || r.size() < 1) {
                return false;
            }
            double mapLenWithWin = (double) this.a.a.c().getMapLenWithWin(((int) this.z) / 4);
            double mapLenWithWin2 = (double) this.a.a.c().getMapLenWithWin((int) this.L);
            FPoint c = c(latLng);
            FPoint fPoint = null;
            for (int i = 0; i < r.size() - 1; i++) {
                FPoint fPoint2;
                if (i != 0) {
                    fPoint2 = fPoint;
                } else {
                    fPoint2 = (FPoint) r.get(i);
                }
                fPoint = (FPoint) r.get(i + 1);
                if ((mapLenWithWin2 + mapLenWithWin) - a(c, fPoint2, fPoint) >= 0.0d) {
                    r.clear();
                    return true;
                }
            }
            r.clear();
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private ArrayList<FPoint> r() {
        ArrayList<FPoint> arrayList = new ArrayList();
        int i = 0;
        while (i < this.I.length) {
            float f = this.I[i];
            i++;
            float f2 = this.I[i];
            i++;
            arrayList.add(new FPoint(f, f2));
            i++;
        }
        return arrayList;
    }

    private double a(FPoint fPoint, FPoint fPoint2, FPoint fPoint3) {
        return a((double) fPoint.x, (double) fPoint.y, (double) fPoint2.x, (double) fPoint2.y, (double) fPoint3.x, (double) fPoint3.y);
    }

    private FPoint c(LatLng latLng) {
        IPoint iPoint = new IPoint();
        this.a.a.a(latLng.latitude, latLng.longitude, iPoint);
        FPoint fPoint = new FPoint();
        this.a.a.b(iPoint.y, iPoint.x, fPoint);
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

    public void c(float f) {
        this.G = f;
        this.a.a.f(false);
    }

    public void c(List<BitmapDescriptor> list) {
        if (list != null && list.size() != 0) {
            if (list.size() <= 1) {
                a((BitmapDescriptor) list.get(0));
            } else {
                this.n = false;
                this.v = 5;
                this.f = list;
                this.a.a.f(false);
            }
        }
    }

    public void d(List<Integer> list) {
        if (list != null && list.size() != 0) {
            this.g = g(list);
        }
    }

    public void e(List<Integer> list) {
        if (list != null && list.size() != 0) {
            if (list.size() <= 1) {
                a(((Integer) list.get(0)).intValue());
            } else {
                this.n = false;
                this.h = g(list);
                this.v = 3;
                this.a.a.f(false);
            }
        }
    }

    public void e(boolean z) {
        if (z && this.h != null && this.h.size() > 1) {
            this.s = z;
            this.v = 4;
            this.a.a.f(false);
        }
    }

    private List<Integer> g(List<Integer> list) {
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
        this.J = new int[arrayList.size()];
        System.arraycopy(obj, 0, this.J, 0, this.J.length);
        return arrayList;
    }

    public void o() {
        this.q = false;
        this.w = 0;
        if (this.K != null) {
            Arrays.fill(this.K, 0);
        }
    }
}
