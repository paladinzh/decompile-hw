package com.amap.api.mapcore.util;

import android.os.RemoteException;
import android.util.Log;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.LatLngBounds.Builder;
import com.amap.api.services.poisearch.PoiSearch.SearchBound;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.FPointBounds;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.interfaces.IOverlay;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: PolygonDelegateImp */
public class dc implements cv {
    private static float u = 1.0E10f;
    FPointBounds a = null;
    private l b;
    private float c = 0.0f;
    private boolean d = true;
    private String e;
    private float f;
    private int g;
    private int h;
    private List<LatLng> i;
    private CopyOnWriteArrayList<IPoint> j = new CopyOnWriteArrayList();
    private List<FPoint> k = new ArrayList();
    private FloatBuffer l;
    private FloatBuffer m;
    private int n = 0;
    private int o = 0;
    private LatLngBounds p = null;
    private boolean q = false;
    private float r = 0.0f;
    private Object s = new Object();
    private float t = 0.0f;

    public dc(l lVar) {
        this.b = lVar;
        try {
            this.e = getId();
        } catch (Throwable e) {
            fo.b(e, "PolygonDelegateImp", "create");
            e.printStackTrace();
        }
    }

    public void remove() throws RemoteException {
        this.b.a(getId());
        this.b.setRunLowFrame(false);
    }

    public String getId() throws RemoteException {
        if (this.e == null) {
            this.e = j.a(SearchBound.POLYGON_SHAPE);
        }
        return this.e;
    }

    public void setPoints(List<LatLng> list) throws RemoteException {
        synchronized (this.s) {
            this.i = list;
            a((List) list);
            b();
            this.b.setRunLowFrame(false);
        }
    }

    public List<LatLng> getPoints() throws RemoteException {
        return this.i;
    }

    public void setZIndex(float f) throws RemoteException {
        this.c = f;
        this.b.r();
        this.b.setRunLowFrame(false);
    }

    public float getZIndex() throws RemoteException {
        return this.c;
    }

    public void setVisible(boolean z) throws RemoteException {
        this.d = z;
        this.b.setRunLowFrame(false);
    }

    public boolean isVisible() throws RemoteException {
        return this.d;
    }

    public boolean equalsRemote(IOverlay iOverlay) throws RemoteException {
        if (equals(iOverlay) || iOverlay.getId().equals(getId())) {
            return true;
        }
        return false;
    }

    void a(List<LatLng> list) throws RemoteException {
        Builder builder = LatLngBounds.builder();
        this.j.clear();
        if (list != null) {
            Object obj = null;
            for (LatLng latLng : list) {
                if (!latLng.equals(obj)) {
                    IPoint iPoint = new IPoint();
                    this.b.a(latLng.latitude, latLng.longitude, iPoint);
                    this.j.add(iPoint);
                    builder.include(latLng);
                    obj = latLng;
                }
            }
            int size = this.j.size();
            if (size > 1) {
                IPoint iPoint2 = (IPoint) this.j.get(0);
                IPoint iPoint3 = (IPoint) this.j.get(size - 1);
                if (iPoint2.x == iPoint3.x && iPoint2.y == iPoint3.y) {
                    this.j.remove(size - 1);
                }
            }
        }
        this.p = builder.build();
        if (this.l != null) {
            this.l.clear();
        }
        if (this.m != null) {
            this.m.clear();
        }
        this.n = 0;
        this.o = 0;
        this.b.setRunLowFrame(false);
    }

    public boolean b() throws RemoteException {
        synchronized (this.s) {
            this.k.clear();
            this.q = false;
            FPointBounds.Builder builder = new FPointBounds.Builder();
            Iterator it = this.j.iterator();
            while (it.hasNext()) {
                IPoint iPoint = (IPoint) it.next();
                FPoint fPoint = new FPoint();
                this.b.b(iPoint.y, iPoint.x, fPoint);
                this.k.add(fPoint);
                builder.include(fPoint);
            }
            this.a = builder.build();
            d();
        }
        return true;
    }

    public int hashCodeRemote() throws RemoteException {
        return super.hashCode();
    }

    public boolean a() {
        return true;
    }

    public void a(GL10 gl10) throws RemoteException {
        if (this.j != null && this.j.size() != 0) {
            if (this.l == null || this.m == null || this.n == 0 || this.o == 0) {
                b();
            }
            List list = this.k;
            FPoint[] p = this.b.p();
            if (b(p)) {
                synchronized (this.s) {
                    list = eh.a(p, this.k, true);
                }
            }
            if (list.size() > 2) {
                b(list);
                if (this.l != null && this.m != null && this.n > 0 && this.o > 0) {
                    du.a(gl10, this.g, this.h, this.l, this.f, this.m, this.n, this.o);
                }
            }
            this.q = true;
        }
    }

    private boolean b(FPoint[] fPointArr) {
        if (this.b == null) {
            return false;
        }
        this.t = this.b.o();
        d();
        if (this.t <= 10.0f) {
            return false;
        }
        try {
            if (eh.a(this.a.northeast, fPointArr) && eh.a(this.a.southwest, fPointArr)) {
                return false;
            }
            return true;
        } catch (Throwable th) {
            return false;
        }
    }

    private void b(List<FPoint> list) throws RemoteException {
        int i = 0;
        d();
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
                if (u == 1.0E10f) {
                    u = 1.0E8f;
                } else {
                    u = 1.0E10f;
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
            this.n = fPointArr.length;
            this.o = a.length;
            this.l = eh.a(fArr);
            this.m = eh.a(fArr2);
        }
    }

    private boolean a(FPoint fPoint, FPoint fPoint2) {
        boolean z;
        if (Math.abs(fPoint2.x - fPoint.x) >= this.r) {
            z = true;
        } else {
            z = false;
        }
        if (z || Math.abs(fPoint2.y - fPoint.y) >= this.r) {
            return true;
        }
        return false;
    }

    private void d() {
        float o = this.b.o();
        if (this.j.size() > 5000 && o <= 12.0f) {
            o = (o / 2.0f) + (this.f / 2.0f);
            if (o > 200.0f) {
                o = 200.0f;
            }
            this.r = this.b.c().getMapLenWithWin((int) o);
            return;
        }
        this.r = this.b.c().getMapLenWithWin(10);
    }

    public void setStrokeWidth(float f) throws RemoteException {
        this.f = f;
        this.b.setRunLowFrame(false);
    }

    public float getStrokeWidth() throws RemoteException {
        return this.f;
    }

    public void setFillColor(int i) throws RemoteException {
        this.g = i;
        this.b.setRunLowFrame(false);
    }

    public int getFillColor() throws RemoteException {
        return this.g;
    }

    public void setStrokeColor(int i) throws RemoteException {
        this.h = i;
        this.b.setRunLowFrame(false);
    }

    public int getStrokeColor() throws RemoteException {
        return this.h;
    }

    static FPoint[] a(FPoint[] fPointArr) {
        int i = 0;
        int length = fPointArr.length;
        float[] fArr = new float[(length * 2)];
        for (int i2 = 0; i2 < length; i2++) {
            fArr[i2 * 2] = fPointArr[i2].x * u;
            fArr[(i2 * 2) + 1] = fPointArr[i2].y * u;
        }
        eg a = new dt().a(fArr);
        length = a.b;
        FPoint[] fPointArr2 = new FPoint[length];
        while (i < length) {
            fPointArr2[i] = new FPoint();
            fPointArr2[i].x = fArr[a.a(i) * 2] / u;
            fPointArr2[i].y = fArr[(a.a(i) * 2) + 1] / u;
            i++;
        }
        return fPointArr2;
    }

    public void destroy() {
        try {
            if (this.l != null) {
                this.l.clear();
                this.l = null;
            }
            if (this.m != null) {
                this.m = null;
            }
        } catch (Throwable th) {
            fo.b(th, "PolygonDelegateImp", "destroy");
            th.printStackTrace();
            Log.d("destroy erro", "PolygonDelegateImp destroy");
        }
    }

    public boolean contains(LatLng latLng) throws RemoteException {
        try {
            return eh.a(latLng, getPoints());
        } catch (Throwable th) {
            fo.b(th, "PolygonDelegateImp", "contains");
            th.printStackTrace();
            return false;
        }
    }

    public boolean c() {
        return this.q;
    }

    public boolean isAboveMaskLayer() {
        return false;
    }

    public void setAboveMaskLayer(boolean z) {
    }
}
