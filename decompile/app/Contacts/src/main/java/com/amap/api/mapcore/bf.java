package com.amap.api.mapcore;

import android.graphics.Rect;
import android.os.RemoteException;
import android.util.Log;
import com.amap.api.mapcore.util.az;
import com.amap.api.mapcore.util.bi;
import com.amap.api.mapcore.util.bj;
import com.amap.api.mapcore.util.ce;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.LatLngBounds.Builder;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.IPoint;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: PolygonDelegateImp */
class bf implements ak {
    private static float s = 1.0E10f;
    private ab a;
    private float b = 0.0f;
    private boolean c = true;
    private String d;
    private float e;
    private int f;
    private int g;
    private List<LatLng> h;
    private CopyOnWriteArrayList<IPoint> i = new CopyOnWriteArrayList();
    private List<FPoint> j = new ArrayList();
    private FloatBuffer k;
    private FloatBuffer l;
    private int m = 0;
    private int n = 0;
    private LatLngBounds o = null;
    private boolean p = false;
    private float q = 0.0f;
    private Object r = new Object();

    public bf(ab abVar) {
        this.a = abVar;
        try {
            this.d = c();
        } catch (Throwable e) {
            ce.a(e, "PolygonDelegateImp", "create");
            e.printStackTrace();
        }
    }

    public void b() throws RemoteException {
        this.a.a(c());
        this.a.f(false);
    }

    public String c() throws RemoteException {
        if (this.d == null) {
            this.d = w.a("Polygon");
        }
        return this.d;
    }

    public void a(List<LatLng> list) throws RemoteException {
        synchronized (this.r) {
            this.h = list;
            b((List) list);
            g();
            this.a.f(false);
        }
    }

    public List<LatLng> l() throws RemoteException {
        return this.h;
    }

    public void a(float f) throws RemoteException {
        this.b = f;
        this.a.M();
        this.a.f(false);
    }

    public float d() throws RemoteException {
        return this.b;
    }

    public void a(boolean z) throws RemoteException {
        this.c = z;
        this.a.f(false);
    }

    public boolean e() throws RemoteException {
        return this.c;
    }

    public boolean a(aj ajVar) throws RemoteException {
        if (equals(ajVar) || ajVar.c().equals(c())) {
            return true;
        }
        return false;
    }

    void b(List<LatLng> list) throws RemoteException {
        Builder builder = LatLngBounds.builder();
        this.i.clear();
        if (list != null) {
            Object obj = null;
            for (LatLng latLng : list) {
                if (!latLng.equals(obj)) {
                    IPoint iPoint = new IPoint();
                    this.a.a(latLng.latitude, latLng.longitude, iPoint);
                    this.i.add(iPoint);
                    builder.include(latLng);
                    obj = latLng;
                }
            }
            int size = this.i.size();
            if (size > 1) {
                IPoint iPoint2 = (IPoint) this.i.get(0);
                IPoint iPoint3 = (IPoint) this.i.get(size - 1);
                if (iPoint2.x == iPoint3.x && iPoint2.y == iPoint3.y) {
                    this.i.remove(size - 1);
                }
            }
        }
        this.o = builder.build();
        if (this.k != null) {
            this.k.clear();
        }
        if (this.l != null) {
            this.l.clear();
        }
        this.m = 0;
        this.n = 0;
        this.a.f(false);
    }

    public void g() throws RemoteException {
        synchronized (this.r) {
            this.j.clear();
            this.p = false;
            Iterator it = this.i.iterator();
            while (it.hasNext()) {
                IPoint iPoint = (IPoint) it.next();
                FPoint fPoint = new FPoint();
                this.a.b(iPoint.y, iPoint.x, fPoint);
                this.j.add(fPoint);
            }
            o();
        }
    }

    public int f() throws RemoteException {
        return super.hashCode();
    }

    public boolean a() {
        boolean z = false;
        if (this.o == null) {
            return false;
        }
        LatLngBounds H = this.a.H();
        if (H == null) {
            return true;
        }
        if (this.o.contains(H) || this.o.intersects(H)) {
            z = true;
        }
        return z;
    }

    public void a(GL10 gl10) throws RemoteException {
        if (this.i != null && this.i.size() != 0) {
            if (this.k == null || this.l == null || this.m == 0 || this.n == 0) {
                g();
            }
            List list = this.j;
            if (n()) {
                synchronized (this.r) {
                    list = bj.a(this.a, this.j, true);
                }
            }
            if (list.size() > 2) {
                c(list);
                if (this.k != null && this.l != null && this.m > 0 && this.n > 0) {
                    u.a(gl10, this.f, this.g, this.k, this.e, this.l, this.m, this.n);
                }
            }
            this.p = true;
        }
    }

    private boolean n() {
        boolean z = false;
        float F = this.a.F();
        o();
        if (F <= 10.0f) {
            return false;
        }
        try {
            if (this.a != null) {
                Rect rect = new Rect(-100, -100, this.a.l() + 100, this.a.m() + 100);
                LatLng latLng = this.o.northeast;
                LatLng latLng2 = this.o.southwest;
                IPoint iPoint = new IPoint();
                this.a.b(latLng.latitude, latLng2.longitude, iPoint);
                IPoint iPoint2 = new IPoint();
                this.a.b(latLng.latitude, latLng.longitude, iPoint2);
                IPoint iPoint3 = new IPoint();
                this.a.b(latLng2.latitude, latLng.longitude, iPoint3);
                IPoint iPoint4 = new IPoint();
                this.a.b(latLng2.latitude, latLng2.longitude, iPoint4);
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

    private void c(List<FPoint> list) throws RemoteException {
        int i = 0;
        o();
        ArrayList arrayList = new ArrayList();
        int size = list.size();
        if (size >= 2) {
            FPoint fPoint = (FPoint) list.get(0);
            arrayList.add(fPoint);
            int i2 = 1;
            FPoint fPoint2 = fPoint;
            while (i2 < size - 1) {
                fPoint = (FPoint) list.get(i2);
                if (a(fPoint2, fPoint)) {
                    arrayList.add(fPoint);
                } else {
                    fPoint = fPoint2;
                }
                i2++;
                fPoint2 = fPoint;
            }
            arrayList.add(list.get(size - 1));
            float[] fArr = new float[(arrayList.size() * 3)];
            FPoint[] fPointArr = new FPoint[arrayList.size()];
            Iterator it = arrayList.iterator();
            int i3 = 0;
            while (it.hasNext()) {
                fPoint = (FPoint) it.next();
                fArr[i3 * 3] = fPoint.x;
                fArr[(i3 * 3) + 1] = fPoint.y;
                fArr[(i3 * 3) + 2] = 0.0f;
                fPointArr[i3] = fPoint;
                i3++;
            }
            FPoint[] a = a(fPointArr);
            if (a.length == 0) {
                if (s == 1.0E10f) {
                    s = 1.0E8f;
                } else {
                    s = 1.0E10f;
                }
                a = a(fPointArr);
            }
            float[] fArr2 = new float[(a.length * 3)];
            int length = a.length;
            i3 = 0;
            while (i < length) {
                FPoint fPoint3 = a[i];
                fArr2[i3 * 3] = fPoint3.x;
                fArr2[(i3 * 3) + 1] = fPoint3.y;
                fArr2[(i3 * 3) + 2] = 0.0f;
                i3++;
                i++;
            }
            this.m = fPointArr.length;
            this.n = a.length;
            this.k = bj.a(fArr);
            this.l = bj.a(fArr2);
        }
    }

    private boolean a(FPoint fPoint, FPoint fPoint2) {
        boolean z;
        if (Math.abs(fPoint2.x - fPoint.x) >= this.q) {
            z = true;
        } else {
            z = false;
        }
        if (z || Math.abs(fPoint2.y - fPoint.y) >= this.q) {
            return true;
        }
        return false;
    }

    private void o() {
        float F = this.a.F();
        if (this.i.size() > 5000 && F <= 12.0f) {
            F = (F / 2.0f) + (this.e / 2.0f);
            if (F > 200.0f) {
                F = 200.0f;
            }
            this.q = this.a.c().getMapLenWithWin((int) F);
            return;
        }
        this.q = this.a.c().getMapLenWithWin(10);
    }

    public void b(float f) throws RemoteException {
        this.e = f;
        this.a.f(false);
    }

    public float h() throws RemoteException {
        return this.e;
    }

    public void a(int i) throws RemoteException {
        this.f = i;
        this.a.f(false);
    }

    public int i() throws RemoteException {
        return this.f;
    }

    public void b(int i) throws RemoteException {
        this.g = i;
        this.a.f(false);
    }

    public int m() throws RemoteException {
        return this.g;
    }

    static FPoint[] a(FPoint[] fPointArr) {
        int i = 0;
        int length = fPointArr.length;
        float[] fArr = new float[(length * 2)];
        for (int i2 = 0; i2 < length; i2++) {
            fArr[i2 * 2] = fPointArr[i2].x * s;
            fArr[(i2 * 2) + 1] = fPointArr[i2].y * s;
        }
        bi a = new az().a(fArr);
        length = a.b;
        FPoint[] fPointArr2 = new FPoint[length];
        while (i < length) {
            fPointArr2[i] = new FPoint();
            fPointArr2[i].x = fArr[a.a(i) * 2] / s;
            fPointArr2[i].y = fArr[(a.a(i) * 2) + 1] / s;
            i++;
        }
        return fPointArr2;
    }

    public void j() {
        try {
            if (this.k != null) {
                this.k.clear();
                this.k = null;
            }
            if (this.l != null) {
                this.l = null;
            }
        } catch (Throwable th) {
            ce.a(th, "PolygonDelegateImp", "destroy");
            th.printStackTrace();
            Log.d("destroy erro", "PolygonDelegateImp destroy");
        }
    }

    public boolean a(LatLng latLng) throws RemoteException {
        try {
            return bj.a(latLng, l());
        } catch (Throwable th) {
            ce.a(th, "PolygonDelegateImp", "contains");
            th.printStackTrace();
            return false;
        }
    }

    public boolean k() {
        return this.p;
    }
}
