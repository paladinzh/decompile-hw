package com.amap.api.mapcore;

import android.graphics.Color;
import android.os.RemoteException;
import android.util.Log;
import com.amap.api.mapcore.util.ce;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.LatLngBounds.Builder;
import com.autonavi.amap.mapcore.AMapNativeRenderer;
import com.autonavi.amap.mapcore.DPoint;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.IPoint;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: NavigateArrowDelegateImp */
class bc implements ai {
    float a;
    float b;
    float c;
    float d;
    float e;
    float f;
    float g;
    float h;
    float[] i;
    private ab j;
    private float k = 10.0f;
    private int l = -16777216;
    private int m = -16777216;
    private float n = 0.0f;
    private boolean o = true;
    private String p;
    private CopyOnWriteArrayList<IPoint> q = new CopyOnWriteArrayList();
    private int r = 0;
    private boolean s = false;
    private LatLngBounds t = null;

    public bc(ab abVar) {
        this.j = abVar;
        try {
            this.p = c();
        } catch (Throwable e) {
            ce.a(e, "NavigateArrowDelegateImp", "create");
            e.printStackTrace();
        }
    }

    void b(List<LatLng> list) throws RemoteException {
        Builder builder = LatLngBounds.builder();
        this.q.clear();
        if (list != null) {
            Object obj = null;
            for (LatLng latLng : list) {
                if (!(latLng == null || latLng.equals(r1))) {
                    IPoint iPoint = new IPoint();
                    this.j.a(latLng.latitude, latLng.longitude, iPoint);
                    this.q.add(iPoint);
                    builder.include(latLng);
                    obj = latLng;
                }
            }
        }
        this.t = builder.build();
        this.r = 0;
        this.j.f(false);
    }

    public void b() throws RemoteException {
        this.j.a(c());
        this.j.f(false);
    }

    public String c() throws RemoteException {
        if (this.p == null) {
            this.p = w.a("NavigateArrow");
        }
        return this.p;
    }

    public void a(List<LatLng> list) throws RemoteException {
        b((List) list);
    }

    public List<LatLng> m() throws RemoteException {
        return n();
    }

    private List<LatLng> n() throws RemoteException {
        if (this.q == null) {
            return null;
        }
        List<LatLng> arrayList = new ArrayList();
        Iterator it = this.q.iterator();
        while (it.hasNext()) {
            IPoint iPoint = (IPoint) it.next();
            if (iPoint != null) {
                DPoint dPoint = new DPoint();
                this.j.b(iPoint.x, iPoint.y, dPoint);
                arrayList.add(new LatLng(dPoint.y, dPoint.x));
            }
        }
        return arrayList;
    }

    public void b(float f) throws RemoteException {
        this.k = f;
        this.j.f(false);
    }

    public float h() throws RemoteException {
        return this.k;
    }

    public void a(int i) throws RemoteException {
        this.l = i;
        this.a = ((float) Color.alpha(i)) / 255.0f;
        this.b = ((float) Color.red(i)) / 255.0f;
        this.c = ((float) Color.green(i)) / 255.0f;
        this.d = ((float) Color.blue(i)) / 255.0f;
        this.j.f(false);
    }

    public int i() throws RemoteException {
        return this.l;
    }

    public void b(int i) throws RemoteException {
        this.m = i;
        this.e = ((float) Color.alpha(i)) / 255.0f;
        this.f = ((float) Color.red(i)) / 255.0f;
        this.g = ((float) Color.green(i)) / 255.0f;
        this.h = ((float) Color.blue(i)) / 255.0f;
        this.j.f(false);
    }

    public int l() throws RemoteException {
        return this.m;
    }

    public void a(float f) throws RemoteException {
        this.n = f;
        this.j.M();
        this.j.f(false);
    }

    public float d() throws RemoteException {
        return this.n;
    }

    public void a(boolean z) throws RemoteException {
        this.o = z;
        this.j.f(false);
    }

    public boolean e() throws RemoteException {
        return this.o;
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
        boolean z = false;
        if (this.t == null) {
            return false;
        }
        LatLngBounds H = this.j.H();
        if (H == null) {
            return true;
        }
        if (H.contains(this.t) || this.t.intersects(H)) {
            z = true;
        }
        return z;
    }

    public void g() throws RemoteException {
        this.s = false;
        FPoint fPoint = new FPoint();
        this.i = new float[(this.q.size() * 3)];
        Iterator it = this.q.iterator();
        int i = 0;
        while (it.hasNext()) {
            IPoint iPoint = (IPoint) it.next();
            this.j.b(iPoint.y, iPoint.x, fPoint);
            this.i[i * 3] = fPoint.x;
            this.i[(i * 3) + 1] = fPoint.y;
            this.i[(i * 3) + 2] = 0.0f;
            i++;
        }
        this.r = this.q.size();
    }

    public void a(GL10 gl10) throws RemoteException {
        if (this.q != null && this.q.size() != 0 && this.k > 0.0f) {
            if (this.r == 0) {
                g();
            }
            if (this.i != null && this.r > 0) {
                float mapLenWithWin = this.j.c().getMapLenWithWin((int) this.k);
                this.j.c().getMapLenWithWin(1);
                AMapNativeRenderer.nativeDrawLineByTextureID(this.i, this.i.length, mapLenWithWin, this.j.b(), this.b, this.c, this.d, this.a, 0.0f, false, true, true);
            }
            this.s = true;
        }
    }

    public void j() {
        try {
            if (this.i != null) {
                this.i = null;
            }
        } catch (Throwable th) {
            ce.a(th, "NavigateArrowDelegateImp", "destroy");
            th.printStackTrace();
            Log.d("destroy erro", "NavigateArrowDelegateImp destroy");
        }
    }

    public boolean k() {
        return this.s;
    }
}
