package com.amap.api.mapcore.util;

import android.graphics.Color;
import android.os.RemoteException;
import android.util.Log;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.LatLngBounds.Builder;
import com.autonavi.amap.mapcore.AMapNativeRenderer;
import com.autonavi.amap.mapcore.DPoint;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.FPointBounds;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.interfaces.IOverlay;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: NavigateArrowDelegateImp */
public class db implements cs {
    float a;
    float b;
    float c;
    float d;
    float e;
    float f;
    float g;
    float h;
    FPointBounds i = null;
    float[] j;
    private l k;
    private float l = 10.0f;
    private int m = -16777216;
    private int n = -16777216;
    private float o = 0.0f;
    private boolean p = true;
    private String q;
    private CopyOnWriteArrayList<IPoint> r = new CopyOnWriteArrayList();
    private int s = 0;
    private boolean t = false;
    private LatLngBounds u = null;

    public db(l lVar) {
        this.k = lVar;
        try {
            this.q = getId();
        } catch (Throwable e) {
            fo.b(e, "NavigateArrowDelegateImp", "create");
            e.printStackTrace();
        }
    }

    void a(List<LatLng> list) throws RemoteException {
        Builder builder = LatLngBounds.builder();
        this.r.clear();
        if (list != null) {
            Object obj = null;
            for (LatLng latLng : list) {
                if (!(latLng == null || latLng.equals(r1))) {
                    IPoint iPoint = new IPoint();
                    this.k.a(latLng.latitude, latLng.longitude, iPoint);
                    this.r.add(iPoint);
                    builder.include(latLng);
                    obj = latLng;
                }
            }
        }
        this.u = builder.build();
        this.s = 0;
        this.k.setRunLowFrame(false);
    }

    public void remove() throws RemoteException {
        this.k.a(getId());
        this.k.setRunLowFrame(false);
    }

    public String getId() throws RemoteException {
        if (this.q == null) {
            this.q = j.a("NavigateArrow");
        }
        return this.q;
    }

    public void setPoints(List<LatLng> list) throws RemoteException {
        a((List) list);
    }

    public List<LatLng> getPoints() throws RemoteException {
        return d();
    }

    private List<LatLng> d() throws RemoteException {
        if (this.r == null) {
            return null;
        }
        List<LatLng> arrayList = new ArrayList();
        Iterator it = this.r.iterator();
        while (it.hasNext()) {
            IPoint iPoint = (IPoint) it.next();
            if (iPoint != null) {
                DPoint dPoint = new DPoint();
                this.k.b(iPoint.x, iPoint.y, dPoint);
                arrayList.add(new LatLng(dPoint.y, dPoint.x));
            }
        }
        return arrayList;
    }

    public void setWidth(float f) throws RemoteException {
        this.l = f;
        this.k.setRunLowFrame(false);
    }

    public float getWidth() throws RemoteException {
        return this.l;
    }

    public void setTopColor(int i) throws RemoteException {
        this.m = i;
        this.a = ((float) Color.alpha(i)) / 255.0f;
        this.b = ((float) Color.red(i)) / 255.0f;
        this.c = ((float) Color.green(i)) / 255.0f;
        this.d = ((float) Color.blue(i)) / 255.0f;
        this.k.setRunLowFrame(false);
    }

    public int getTopColor() throws RemoteException {
        return this.m;
    }

    public void setSideColor(int i) throws RemoteException {
        this.n = i;
        this.e = ((float) Color.alpha(i)) / 255.0f;
        this.f = ((float) Color.red(i)) / 255.0f;
        this.g = ((float) Color.green(i)) / 255.0f;
        this.h = ((float) Color.blue(i)) / 255.0f;
        this.k.setRunLowFrame(false);
    }

    public int getSideColor() throws RemoteException {
        return this.n;
    }

    public void setZIndex(float f) throws RemoteException {
        this.o = f;
        this.k.r();
        this.k.setRunLowFrame(false);
    }

    public float getZIndex() throws RemoteException {
        return this.o;
    }

    public void setVisible(boolean z) throws RemoteException {
        this.p = z;
        this.k.setRunLowFrame(false);
    }

    public boolean isVisible() throws RemoteException {
        return this.p;
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
        if (this.i == null) {
            return false;
        }
        if (eh.a(this.i.northeast, this.k.p()) || eh.a(this.i.southwest, this.k.p())) {
            return true;
        }
        return false;
    }

    public boolean b() throws RemoteException {
        this.t = false;
        FPoint fPoint = new FPoint();
        FPointBounds.Builder builder = new FPointBounds.Builder();
        this.j = new float[(this.r.size() * 3)];
        Iterator it = this.r.iterator();
        int i = 0;
        while (it.hasNext()) {
            IPoint iPoint = (IPoint) it.next();
            this.k.b(iPoint.y, iPoint.x, fPoint);
            this.j[i * 3] = fPoint.x;
            this.j[(i * 3) + 1] = fPoint.y;
            this.j[(i * 3) + 2] = 0.0f;
            int i2 = i + 1;
            builder.include(fPoint);
            i = i2;
        }
        this.i = builder.build();
        this.s = this.r.size();
        return true;
    }

    public void a(GL10 gl10) throws RemoteException {
        if (this.r != null && this.r.size() != 0 && this.l > 0.0f) {
            if (this.s == 0) {
                b();
            }
            if (this.j != null && this.s > 0) {
                float mapLenWithWin = this.k.c().getMapLenWithWin((int) this.l);
                this.k.c().getMapLenWithWin(1);
                AMapNativeRenderer.nativeDrawLineByTextureID(this.j, this.j.length, mapLenWithWin, this.k.b(), this.b, this.c, this.d, this.a, 0.0f, false, true, true);
            }
            this.t = true;
        }
    }

    public void destroy() {
        try {
            if (this.j != null) {
                this.j = null;
            }
        } catch (Throwable th) {
            fo.b(th, "NavigateArrowDelegateImp", "destroy");
            th.printStackTrace();
            Log.d("destroy erro", "NavigateArrowDelegateImp destroy");
        }
    }

    public boolean c() {
        return this.t;
    }

    public boolean isAboveMaskLayer() {
        return false;
    }

    public void setAboveMaskLayer(boolean z) {
    }
}
