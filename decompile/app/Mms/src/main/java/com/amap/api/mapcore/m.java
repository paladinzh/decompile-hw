package com.amap.api.mapcore;

import android.graphics.Color;
import android.os.RemoteException;
import android.util.Log;
import com.amap.api.mapcore.util.ce;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.WeightedLatLng;
import com.autonavi.amap.mapcore.AMapNativeRenderer;
import com.autonavi.amap.mapcore.DPoint;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapProjection;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: ArcDelegateImp */
class m implements ac {
    float a;
    float b;
    float c;
    float d;
    private LatLng e;
    private LatLng f;
    private LatLng g;
    private float h = 10.0f;
    private int i = -16777216;
    private float j = 0.0f;
    private boolean k = true;
    private String l;
    private ab m;
    private float[] n;
    private int o = 0;
    private boolean p = false;
    private double q = 0.0d;
    private double r = 0.0d;
    private double s = 0.0d;

    public m(ab abVar) {
        this.m = abVar;
        try {
            this.l = c();
        } catch (Throwable e) {
            ce.a(e, "ArcDelegateImp", "create");
            e.printStackTrace();
        }
    }

    public boolean a() {
        return true;
    }

    public void b() throws RemoteException {
        this.m.a(c());
        this.m.f(false);
    }

    public String c() throws RemoteException {
        if (this.l == null) {
            this.l = w.a("Arc");
        }
        return this.l;
    }

    public void a(float f) throws RemoteException {
        this.j = f;
        this.m.M();
        this.m.f(false);
    }

    public float d() throws RemoteException {
        return this.j;
    }

    public void a(boolean z) throws RemoteException {
        this.k = z;
        this.m.f(false);
    }

    public boolean e() throws RemoteException {
        return this.k;
    }

    public boolean a(aj ajVar) throws RemoteException {
        if (equals(ajVar) || ajVar.c().equals(c())) {
            return true;
        }
        return false;
    }

    public int f() throws RemoteException {
        return 0;
    }

    public void g() throws RemoteException {
        int i = 0;
        if (this.e != null && this.f != null && this.g != null && this.k) {
            try {
                this.p = false;
                MapProjection c = this.m.c();
                FPoint fPoint;
                if (l()) {
                    DPoint m = m();
                    int abs = (int) ((Math.abs(this.s - this.r) * 180.0d) / 3.141592653589793d);
                    double d = (this.s - this.r) / ((double) abs);
                    FPoint[] fPointArr = new FPoint[(abs + 1)];
                    this.n = new float[(fPointArr.length * 3)];
                    for (int i2 = 0; i2 <= abs; i2++) {
                        MapProjection mapProjection;
                        if (i2 != abs) {
                            mapProjection = c;
                            fPointArr[i2] = a(mapProjection, (((double) i2) * d) + this.r, m.x, m.y);
                        } else {
                            fPoint = new FPoint();
                            this.m.a(this.g.latitude, this.g.longitude, fPoint);
                            fPointArr[i2] = fPoint;
                        }
                        mapProjection = c;
                        fPointArr[i2] = a(mapProjection, (((double) i2) * d) + this.r, m.x, m.y);
                        this.n[i2 * 3] = fPointArr[i2].x;
                        this.n[(i2 * 3) + 1] = fPointArr[i2].y;
                        this.n[(i2 * 3) + 2] = 0.0f;
                    }
                    this.o = fPointArr.length;
                    return;
                }
                FPoint[] fPointArr2 = new FPoint[3];
                this.n = new float[(fPointArr2.length * 3)];
                fPoint = new FPoint();
                this.m.a(this.e.latitude, this.e.longitude, fPoint);
                fPointArr2[0] = fPoint;
                fPoint = new FPoint();
                this.m.a(this.f.latitude, this.f.longitude, fPoint);
                fPointArr2[1] = fPoint;
                fPoint = new FPoint();
                this.m.a(this.g.latitude, this.g.longitude, fPoint);
                fPointArr2[2] = fPoint;
                while (i < 3) {
                    this.n[i * 3] = fPointArr2[i].x;
                    this.n[(i * 3) + 1] = fPointArr2[i].y;
                    this.n[(i * 3) + 2] = 0.0f;
                    i++;
                }
                this.o = fPointArr2.length;
            } catch (Throwable th) {
                ce.a(th, "ArcDelegateImp", "calMapFPoint");
                th.printStackTrace();
            }
        }
    }

    private FPoint a(MapProjection mapProjection, double d, double d2, double d3) {
        int cos = (int) ((Math.cos(d) * this.q) + d2);
        int i = (int) (((-Math.sin(d)) * this.q) + d3);
        FPoint fPoint = new FPoint();
        mapProjection.geo2Map(cos, i, fPoint);
        return fPoint;
    }

    private boolean l() {
        if (Math.abs(((this.e.latitude - this.f.latitude) * (this.f.longitude - this.g.longitude)) - ((this.e.longitude - this.f.longitude) * (this.f.latitude - this.g.latitude))) < 1.0E-6d) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private DPoint m() {
        IPoint iPoint = new IPoint();
        this.m.a(this.e.latitude, this.e.longitude, iPoint);
        IPoint iPoint2 = new IPoint();
        this.m.a(this.f.latitude, this.f.longitude, iPoint2);
        IPoint iPoint3 = new IPoint();
        this.m.a(this.g.latitude, this.g.longitude, iPoint3);
        double d = (double) iPoint.x;
        double d2 = (double) iPoint.y;
        double d3 = (double) iPoint2.x;
        double d4 = (double) iPoint2.y;
        double d5 = (double) iPoint3.x;
        double d6 = (double) iPoint3.y;
        double d7 = (((d6 - d2) * ((((d4 * d4) - (d2 * d2)) + (d3 * d3)) - (d * d))) + ((d4 - d2) * ((((d2 * d2) - (d6 * d6)) + (d * d)) - (d5 * d5)))) / ((((d3 - d) * 2.0d) * (d6 - d2)) - (((d5 - d) * 2.0d) * (d4 - d2)));
        double d8 = (((d5 - d) * ((((d3 * d3) - (d * d)) + (d4 * d4)) - (d2 * d2))) + ((d3 - d) * ((((d * d) - (d5 * d5)) + (d2 * d2)) - (d6 * d6)))) / ((((d4 - d2) * 2.0d) * (d5 - d)) - (((d6 - d2) * 2.0d) * (d3 - d)));
        this.q = Math.sqrt(((d - d7) * (d - d7)) + ((d2 - d8) * (d2 - d8)));
        this.r = a(d7, d8, d, d2);
        d = a(d7, d8, d3, d4);
        this.s = a(d7, d8, d5, d6);
        if (this.r < this.s) {
            if (d > this.r) {
            }
            this.s -= 6.283185307179586d;
        } else {
            if (d > this.s) {
            }
            this.s += 6.283185307179586d;
        }
        return new DPoint(d7, d8);
    }

    private double a(double d, double d2, double d3, double d4) {
        double d5 = (d2 - d4) / this.q;
        if (Math.abs(d5) > WeightedLatLng.DEFAULT_INTENSITY) {
            d5 = Math.signum(d5);
        }
        d5 = Math.asin(d5);
        if (d5 >= 0.0d) {
            if (d3 < d) {
                return 3.141592653589793d - Math.abs(d5);
            }
            return d5;
        } else if (d3 < d) {
            return 3.141592653589793d - d5;
        } else {
            return d5 + 6.283185307179586d;
        }
    }

    public void a(GL10 gl10) throws RemoteException {
        if (this.e != null && this.f != null && this.g != null && this.k) {
            if (this.n == null || this.o == 0) {
                g();
            }
            if (this.n != null && this.o > 0) {
                float mapLenWithWin = this.m.c().getMapLenWithWin((int) this.h);
                this.m.c().getMapLenWithWin(1);
                AMapNativeRenderer.nativeDrawLineByTextureID(this.n, this.n.length, mapLenWithWin, this.m.b(), this.b, this.c, this.d, this.a, 0.0f, false, true, false);
            }
            this.p = true;
        }
    }

    public void b(float f) throws RemoteException {
        this.h = f;
        this.m.f(false);
    }

    public float h() throws RemoteException {
        return this.h;
    }

    public void a(int i) throws RemoteException {
        this.i = i;
        this.a = ((float) Color.alpha(i)) / 255.0f;
        this.b = ((float) Color.red(i)) / 255.0f;
        this.c = ((float) Color.green(i)) / 255.0f;
        this.d = ((float) Color.blue(i)) / 255.0f;
        this.m.f(false);
    }

    public int i() throws RemoteException {
        return this.i;
    }

    public void j() {
        try {
            this.e = null;
            this.f = null;
            this.g = null;
        } catch (Throwable th) {
            ce.a(th, "ArcDelegateImp", "destroy");
            th.printStackTrace();
            Log.d("destroy erro", "ArcDelegateImp destroy");
        }
    }

    public boolean k() {
        return this.p;
    }

    public void a(LatLng latLng) {
        this.e = latLng;
    }

    public void b(LatLng latLng) {
        this.f = latLng;
    }

    public void c(LatLng latLng) {
        this.g = latLng;
    }
}
