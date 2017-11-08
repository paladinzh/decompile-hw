package com.amap.api.mapcore;

import android.os.RemoteException;
import android.util.Log;
import com.amap.api.mapcore.util.bj;
import com.amap.api.mapcore.util.ce;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.WeightedLatLng;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapProjection;
import java.nio.FloatBuffer;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: CircleDelegateImp */
class q implements ad {
    private static float m = 4.0075016E7f;
    private static int n = 256;
    private static int o = 20;
    private LatLng a = null;
    private double b = 0.0d;
    private float c = 10.0f;
    private int d = -16777216;
    private int e = 0;
    private float f = 0.0f;
    private boolean g = true;
    private String h;
    private ab i;
    private FloatBuffer j;
    private int k = 0;
    private boolean l = false;

    public q(ab abVar) {
        this.i = abVar;
        try {
            this.h = c();
        } catch (Throwable e) {
            ce.a(e, "CircleDelegateImp", "create");
            e.printStackTrace();
        }
    }

    public boolean a() {
        return true;
    }

    public void b() throws RemoteException {
        this.i.a(c());
        this.i.f(false);
    }

    public String c() throws RemoteException {
        if (this.h == null) {
            this.h = w.a("Circle");
        }
        return this.h;
    }

    public void a(float f) throws RemoteException {
        this.f = f;
        this.i.M();
        this.i.f(false);
    }

    public float d() throws RemoteException {
        return this.f;
    }

    public void a(boolean z) throws RemoteException {
        this.g = z;
        this.i.f(false);
    }

    public boolean e() throws RemoteException {
        return this.g;
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
        this.l = false;
        LatLng latLng = this.a;
        if (latLng != null) {
            FPoint[] fPointArr = new FPoint[360];
            float[] fArr = new float[(fPointArr.length * 3)];
            double c = c(this.a.latitude) * this.b;
            IPoint iPoint = new IPoint();
            MapProjection c2 = this.i.c();
            MapProjection.lonlat2Geo(latLng.longitude, latLng.latitude, iPoint);
            while (i < 360) {
                double d = (((double) i) * 3.141592653589793d) / 180.0d;
                double sin = Math.sin(d) * c;
                int i2 = (int) (sin + ((double) iPoint.x));
                int cos = (int) ((Math.cos(d) * c) + ((double) iPoint.y));
                FPoint fPoint = new FPoint();
                c2.geo2Map(i2, cos, fPoint);
                fPointArr[i] = fPoint;
                fArr[i * 3] = fPointArr[i].x;
                fArr[(i * 3) + 1] = fPointArr[i].y;
                fArr[(i * 3) + 2] = 0.0f;
                i++;
            }
            this.k = fPointArr.length;
            this.j = bj.a(fArr);
        }
    }

    public void a(GL10 gl10) throws RemoteException {
        boolean z = false;
        if (this.a != null) {
            if (this.b <= 0.0d) {
                z = true;
            }
            if (!z && this.g) {
                if (this.j == null || this.k == 0) {
                    g();
                }
                if (this.j != null && this.k > 0) {
                    u.b(gl10, this.e, this.d, this.j, this.c, this.k);
                }
                this.l = true;
            }
        }
    }

    void h() {
        this.k = 0;
        if (this.j != null) {
            this.j.clear();
        }
        this.i.f(false);
    }

    public void a(LatLng latLng) throws RemoteException {
        this.a = latLng;
        h();
    }

    public LatLng i() throws RemoteException {
        return this.a;
    }

    public void a(double d) throws RemoteException {
        this.b = d;
        h();
    }

    public double l() throws RemoteException {
        return this.b;
    }

    public void b(float f) throws RemoteException {
        this.c = f;
        this.i.f(false);
    }

    public float m() throws RemoteException {
        return this.c;
    }

    public void a(int i) throws RemoteException {
        this.d = i;
    }

    public int n() throws RemoteException {
        return this.d;
    }

    public void b(int i) throws RemoteException {
        this.e = i;
        this.i.f(false);
    }

    public int o() throws RemoteException {
        return this.e;
    }

    private float b(double d) {
        return (float) ((Math.cos((3.141592653589793d * d) / 180.0d) * ((double) m)) / ((double) (n << o)));
    }

    private double c(double d) {
        return WeightedLatLng.DEFAULT_INTENSITY / ((double) b(d));
    }

    public void j() {
        try {
            this.a = null;
            if (this.j != null) {
                this.j.clear();
                this.j = null;
            }
        } catch (Throwable th) {
            ce.a(th, "CircleDelegateImp", "destroy");
            th.printStackTrace();
            Log.d("destroy erro", "CircleDelegateImp destroy");
        }
    }

    public boolean b(LatLng latLng) throws RemoteException {
        if (this.b >= ((double) AMapUtils.calculateLineDistance(this.a, latLng))) {
            return true;
        }
        return false;
    }

    public boolean k() {
        return this.l;
    }
}
