package com.amap.api.mapcore;

import android.content.Context;
import android.graphics.Point;
import android.os.Message;
import android.os.RemoteException;
import com.amap.api.mapcore.util.bj;
import com.amap.api.mapcore.util.ce;
import com.amap.api.mapcore.util.f;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.WeightedLatLng;
import com.autonavi.amap.mapcore.BaseMapCallImplement;
import com.autonavi.amap.mapcore.Convert;
import com.autonavi.amap.mapcore.DPoint;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapCore;
import com.autonavi.amap.mapcore.MapProjection;
import com.autonavi.amap.mapcore.MapSourceGridData;
import java.util.ArrayList;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: AMapCallback */
class a extends BaseMapCallImplement {
    IPoint a = new IPoint();
    float b;
    float c;
    float d;
    IPoint e = new IPoint();
    private AMapDelegateImp f;
    private float g = -1.0f;
    private int h;
    private int i;

    public String getMapSvrAddress() {
        return "http://mps.amap.com";
    }

    public a(AMapDelegateImp aMapDelegateImp) {
        this.f = aMapDelegateImp;
    }

    public void OnMapSurfaceCreate(GL10 gl10, MapCore mapCore) {
        super.OnMapSurfaceCreate(mapCore);
    }

    public void OnMapSurfaceRenderer(GL10 gl10, MapCore mapCore, int i) {
        super.OnMapSurfaceRenderer(gl10, mapCore, i);
        if (i == 3) {
            this.f.h.a(gl10, true, this.f.Q());
            if (this.f.m != null) {
                this.f.m.onDrawFrame(gl10);
            }
        }
    }

    public void OnMapSufaceChanged(GL10 gl10, MapCore mapCore, int i, int i2) {
    }

    public void OnMapProcessEvent(MapCore mapCore) {
        float f = 0.0f;
        if (this.f != null && this.f.O()) {
            this.f.P();
        }
        if (this.f != null) {
            float F = this.f.F();
            a(mapCore);
            while (true) {
                au a = this.f.e.a();
                if (a == null) {
                    break;
                } else if (a.a == 2) {
                    if (a.a()) {
                        mapCore.setParameter(2010, 4, 0, 0, 0);
                    } else {
                        mapCore.setParameter(2010, 0, 0, 0, 0);
                    }
                }
            }
            mapCore.setMapstate(this.f.c());
            if (this.b < this.f.t() || this.g == F) {
                f = F;
            } else if (this.f.l == null) {
                f = F;
            } else {
                this.f.l.obtainMessage(21).sendToTarget();
                f = F;
            }
        }
        this.g = f;
    }

    void a(MapCore mapCore) {
        Object obj = null;
        MapProjection c = this.f.c();
        float mapZoomer = c.getMapZoomer();
        float cameraHeaderAngle = c.getCameraHeaderAngle();
        float mapAngle = c.getMapAngle();
        c.getGeoCenter(this.e);
        int i = 0;
        while (this.f.ab()) {
            p c2 = this.f.e.c();
            if (c2 == null) {
                break;
            }
            try {
                a(c2);
                i |= c2.p;
            } catch (Throwable e) {
                ce.a(e, "AMapCallback", "runMessage");
                e.printStackTrace();
            }
        }
        this.b = c.getMapZoomer();
        this.c = c.getCameraHeaderAngle();
        this.d = c.getMapAngle();
        c.getGeoCenter(this.a);
        if (mapZoomer != this.b || this.c != cameraHeaderAngle || this.d != mapAngle || this.a.x != this.e.x || this.a.y != this.e.y) {
            obj = 1;
        }
        if (obj == null) {
            try {
                this.f.f(true);
            } catch (Throwable e2) {
                ce.a(e2, "AMapCallback", "runMessage cameraChange");
                e2.printStackTrace();
                return;
            }
        }
        this.f.f(false);
        if (this.f.C() != null) {
            DPoint dPoint = new DPoint();
            MapProjection.geo2LonLat(this.a.x, this.a.y, dPoint);
            this.f.a(new CameraPosition(new LatLng(dPoint.y, dPoint.x, false), this.b, this.c, this.d));
        }
        this.f.G();
        if (i != 0) {
            if (i == 0) {
                this.f.o(false);
            } else {
                this.f.o(true);
            }
            Message message = new Message();
            message.what = 17;
            this.f.l.sendMessage(message);
        }
        if (!(this.c == cameraHeaderAngle && this.d == mapAngle) && this.f.A().d()) {
            this.f.i();
        }
        if (this.f.A().b()) {
            this.f.j();
        }
    }

    private void b(p pVar) {
        MapCore a = this.f.a();
        MapProjection c = this.f.c();
        LatLngBounds latLngBounds = pVar.i;
        int i = pVar.k;
        int i2 = pVar.l;
        int i3 = pVar.j;
        IPoint iPoint = new IPoint();
        IPoint iPoint2 = new IPoint();
        MapProjection.lonlat2Geo(latLngBounds.northeast.longitude, latLngBounds.northeast.latitude, iPoint);
        MapProjection.lonlat2Geo(latLngBounds.southwest.longitude, latLngBounds.southwest.latitude, iPoint2);
        int i4 = iPoint2.y - iPoint.y;
        i -= i3 * 2;
        i2 -= i3 * 2;
        if (iPoint.x - iPoint2.x >= 0 || i4 >= 0) {
            if (i <= 0) {
                i = 1;
            }
            if (i2 <= 0) {
                i2 = 1;
            }
            float a2 = a(latLngBounds.northeast, latLngBounds.southwest, i, i2);
            i2 = (iPoint.x + iPoint2.x) / 2;
            int i5 = (iPoint.y + iPoint2.y) / 2;
            c.setMapZoomer(a2);
            c.setGeoCenter(i2, i5);
            c.setCameraHeaderAngle(0.0f);
            c.setMapAngle(0.0f);
            a.setMapstate(c);
        }
    }

    private float a(LatLng latLng, LatLng latLng2, int i, int i2) {
        float a;
        MapProjection c = this.f.c();
        c.setMapAngle(0.0f);
        c.setCameraHeaderAngle(0.0f);
        c.recalculate();
        IPoint iPoint = new IPoint();
        IPoint iPoint2 = new IPoint();
        this.f.b(latLng.latitude, latLng.longitude, iPoint);
        this.f.b(latLng2.latitude, latLng2.longitude, iPoint2);
        double d = (double) (iPoint.x - iPoint2.x);
        double d2 = (double) (iPoint2.y - iPoint.y);
        if (d <= 0.0d) {
            d = WeightedLatLng.DEFAULT_INTENSITY;
        }
        if (d2 <= 0.0d) {
            d2 = WeightedLatLng.DEFAULT_INTENSITY;
        }
        d = Math.log(((double) i) / d) / Math.log(2.0d);
        d2 = Math.min(d, Math.log(((double) i2) / d2) / Math.log(2.0d));
        Object obj = Math.abs(d2 - d) < 1.0E-7d ? 1 : null;
        float a2 = bj.a((float) (Math.floor(d2) + ((double) c.getMapZoomer())));
        while (true) {
            a = bj.a((float) (((double) a2) + 0.1d));
            c.setMapZoomer(a);
            c.recalculate();
            this.f.b(latLng.latitude, latLng.longitude, iPoint);
            this.f.b(latLng2.latitude, latLng2.longitude, iPoint2);
            d2 = (double) (iPoint.x - iPoint2.x);
            double d3 = (double) (iPoint2.y - iPoint.y);
            if (obj == null) {
                if (d3 >= ((double) i2)) {
                    break;
                }
                if (a >= s.f) {
                    return a;
                }
                a2 = a;
            } else {
                if (d2 >= ((double) i)) {
                    break;
                }
                if (a >= s.f) {
                    return a;
                }
                a2 = a;
            }
        }
        return (float) (((double) a) - 0.1d);
    }

    void a(p pVar) throws RemoteException {
        MapCore a = this.f.a();
        MapProjection c = this.f.c();
        pVar.d = this.f.b(pVar.d);
        float b;
        switch (b.a[pVar.a.ordinal()]) {
            case 1:
                if (pVar.n) {
                    a(c, pVar.o);
                } else {
                    c.setGeoCenter(pVar.o.x, pVar.o.y);
                }
                a.setMapstate(c);
                return;
            case 2:
                if (pVar.n) {
                    d(c, pVar);
                } else {
                    c.setMapAngle(pVar.g);
                }
                a.setMapstate(c);
                return;
            case 3:
                if (pVar.n) {
                    a(c, pVar);
                } else {
                    c.setMapAngle(pVar.g);
                    c.setGeoCenter(pVar.o.x, pVar.o.y);
                }
                a.setMapstate(c);
                return;
            case 4:
                pVar.f = bj.a(pVar.f, c.getMapZoomer());
                if (pVar.n) {
                    c(c, pVar);
                } else {
                    c.setCameraHeaderAngle(pVar.f);
                }
                a.setMapstate(c);
                return;
            case 5:
                if (pVar.n) {
                    b(c, pVar);
                } else {
                    c.setGeoCenter(pVar.o.x, pVar.o.y);
                    c.setMapZoomer(pVar.d);
                }
                a.setMapstate(c);
                return;
            case 6:
                LatLng latLng = pVar.h.target;
                IPoint iPoint = new IPoint();
                MapProjection.lonlat2Geo(latLng.longitude, latLng.latitude, iPoint);
                float a2 = bj.a(pVar.h.zoom);
                float f = pVar.h.bearing;
                float a3 = bj.a(pVar.h.tilt, a2);
                if (pVar.n) {
                    a(c, iPoint, a2, f, a3);
                } else {
                    c.setGeoCenter(iPoint.x, iPoint.y);
                    c.setMapZoomer(a2);
                    c.setMapAngle(f);
                    c.setCameraHeaderAngle(a3);
                }
                a.setMapstate(c);
                return;
            case 7:
                b = this.f.b(c.getMapZoomer() + 1.0f);
                if (pVar.n) {
                    a(c, b);
                } else {
                    c.setMapZoomer(b);
                }
                a.setMapstate(c);
                return;
            case 8:
                b = this.f.b(c.getMapZoomer() - 1.0f);
                if (pVar.n) {
                    a(c, b);
                } else {
                    c.setMapZoomer(b);
                }
                c.setMapZoomer(b);
                a.setMapstate(c);
                return;
            case 9:
                b = pVar.d;
                if (pVar.n) {
                    a(c, b);
                } else {
                    c.setMapZoomer(b);
                }
                a.setMapstate(c);
                return;
            case 10:
                b = this.f.b(c.getMapZoomer() + pVar.e);
                Point point = pVar.m;
                if (point != null) {
                    a(c, b, point.x, point.y);
                } else if (pVar.n) {
                    a(c, b);
                } else {
                    c.setMapZoomer(b);
                }
                a.setMapstate(c);
                return;
            case 11:
                b = pVar.b;
                b += ((float) this.f.n()) / 2.0f;
                float o = pVar.c + (((float) this.f.o()) / 2.0f);
                IPoint iPoint2 = new IPoint();
                this.f.a((int) b, (int) o, iPoint2);
                c.setGeoCenter(iPoint2.x, iPoint2.y);
                a.setMapstate(c);
                return;
            case 12:
                pVar.a = a.newLatLngBoundsWithSize;
                pVar.k = this.f.n();
                pVar.l = this.f.o();
                b(pVar);
                return;
            case 13:
                b(pVar);
                return;
            case 14:
                pVar.f = bj.a(pVar.f, pVar.d);
                if (pVar.n) {
                    a(c, pVar.o, pVar.d, pVar.g, pVar.f);
                } else {
                    c.setGeoCenter(pVar.o.x, pVar.o.y);
                    c.setMapZoomer(pVar.d);
                    c.setMapAngle(pVar.g);
                    c.setCameraHeaderAngle(pVar.f);
                }
                a.setMapstate(c);
                return;
            default:
                a.setMapstate(c);
                return;
        }
    }

    private void a(MapProjection mapProjection, p pVar) {
        mapProjection.setMapAngle(pVar.g);
        a(mapProjection, pVar.o);
    }

    private void a(MapProjection mapProjection, float f) {
        a(mapProjection, f, this.h, this.i);
    }

    private void a(MapProjection mapProjection, float f, int i, int i2) {
        IPoint a = a(mapProjection, i, i2);
        mapProjection.setMapZoomer(f);
        a(mapProjection, a, i, i2);
    }

    private void a(MapProjection mapProjection, IPoint iPoint, float f, float f2, float f3) {
        mapProjection.setMapZoomer(f);
        mapProjection.setMapAngle(f2);
        mapProjection.setCameraHeaderAngle(f3);
        a(mapProjection, iPoint);
    }

    private void b(MapProjection mapProjection, p pVar) {
        mapProjection.setMapZoomer(pVar.d);
        a(mapProjection, pVar.o);
    }

    private void c(MapProjection mapProjection, p pVar) {
        IPoint a = a(mapProjection);
        mapProjection.setCameraHeaderAngle(pVar.f);
        a(mapProjection, a);
    }

    private void d(MapProjection mapProjection, p pVar) {
        IPoint a = a(mapProjection);
        mapProjection.setMapAngle(pVar.g);
        a(mapProjection, a);
    }

    private void a(MapProjection mapProjection, IPoint iPoint) {
        a(mapProjection, iPoint, this.h, this.i);
    }

    private void a(MapProjection mapProjection, IPoint iPoint, int i, int i2) {
        mapProjection.recalculate();
        IPoint a = a(mapProjection, i, i2);
        IPoint iPoint2 = new IPoint();
        mapProjection.getGeoCenter(iPoint2);
        mapProjection.setGeoCenter((iPoint2.x + iPoint.x) - a.x, (iPoint2.y + iPoint.y) - a.y);
    }

    private IPoint a(MapProjection mapProjection) {
        return a(mapProjection, this.h, this.i);
    }

    private IPoint a(MapProjection mapProjection, int i, int i2) {
        FPoint fPoint = new FPoint();
        mapProjection.win2Map(i, i2, fPoint);
        IPoint iPoint = new IPoint();
        mapProjection.map2Geo(fPoint.x, fPoint.y, iPoint);
        return iPoint;
    }

    public void OnMapDestory(GL10 gl10, MapCore mapCore) {
        super.OnMapDestory(mapCore);
    }

    public void OnMapReferencechanged(MapCore mapCore, String str, String str2) {
        try {
            if (this.f.A().d()) {
                this.f.i();
            }
            if (this.f.A().b()) {
                this.f.j();
            }
            this.f.o(true);
        } catch (Throwable e) {
            ce.a(e, "AMapCallback", "OnMapReferencechanged");
            e.printStackTrace();
        }
        this.f.U();
    }

    public Context getContext() {
        return this.f.V();
    }

    public boolean isMapEngineValid() {
        if (this.f.a() == null) {
            return false;
        }
        return this.f.a().isMapEngineValid();
    }

    public void OnMapLoaderError(int i) {
    }

    public void a(int i, int i2) {
        this.h = i;
        this.i = i2;
    }

    public void requestRender() {
        this.f.f(false);
    }

    public void onIndoorBuildingActivity(MapCore mapCore, byte[] bArr) {
        f fVar = null;
        if (bArr != null) {
            f fVar2 = new f();
            byte b = bArr[0];
            fVar2.a = new String(bArr, 1, b);
            int i = b + 1;
            int i2 = i + 1;
            b = bArr[i];
            fVar2.b = new String(bArr, i2, b);
            i = b + i2;
            i2 = i + 1;
            b = bArr[i];
            fVar2.activeFloorName = new String(bArr, i2, b);
            i = b + i2;
            fVar2.activeFloorIndex = Convert.getInt(bArr, i);
            i += 4;
            i2 = i + 1;
            b = bArr[i];
            fVar2.poiid = new String(bArr, i2, b);
            i = b + i2;
            fVar2.c = Convert.getInt(bArr, i);
            i += 4;
            fVar2.floor_indexs = new int[fVar2.c];
            fVar2.floor_names = new String[fVar2.c];
            fVar2.d = new String[fVar2.c];
            for (int i3 = 0; i3 < fVar2.c; i3++) {
                fVar2.floor_indexs[i3] = Convert.getInt(bArr, i);
                i2 = i + 4;
                i = i2 + 1;
                byte b2 = bArr[i2];
                if (b2 <= (byte) 0) {
                    i2 = i;
                } else {
                    fVar2.floor_names[i3] = new String(bArr, i, b2);
                    i2 = i + b2;
                }
                i = i2 + 1;
                b2 = bArr[i2];
                if (b2 > (byte) 0) {
                    fVar2.d[i3] = new String(bArr, i, b2);
                    i += b2;
                }
            }
            fVar2.e = Convert.getInt(bArr, i);
            i += 4;
            if (fVar2.e <= 0) {
                fVar = fVar2;
            } else {
                fVar2.f = new int[fVar2.e];
                int i4 = i;
                for (i = 0; i < fVar2.e; i++) {
                    fVar2.f[i] = Convert.getInt(bArr, i4);
                    i4 += 4;
                }
                fVar = fVar2;
            }
        }
        try {
            this.f.a(fVar);
        } catch (Throwable th) {
            th.printStackTrace();
            ce.a(th, "AMapCallback", "onIndoorBuildingActivity");
        }
    }

    public void onIndoorDataRequired(MapCore mapCore, int i, String[] strArr, int[] iArr, int[] iArr2) {
        if (strArr != null && strArr.length != 0) {
            ArrayList reqGridList = getReqGridList(i);
            if (reqGridList != null) {
                reqGridList.clear();
                for (int i2 = 0; i2 < strArr.length; i2++) {
                    reqGridList.add(new MapSourceGridData(strArr[i2], i, iArr[i2], iArr2[i2]));
                }
                if (i != 5) {
                    proccessRequiredData(mapCore, reqGridList, i);
                }
            }
        }
    }
}
