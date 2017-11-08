package com.amap.api.mapcore.util;

import android.os.RemoteException;
import android.util.Log;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.WeightedLatLng;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapProjection;
import com.autonavi.amap.mapcore.VirtualEarthProjection;
import com.autonavi.amap.mapcore.interfaces.IOverlay;
import java.nio.FloatBuffer;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: CircleDelegateImp */
public class ck implements co {
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
    private l i;
    private FloatBuffer j;
    private int k = 0;
    private boolean l = false;

    public ck(l lVar) {
        this.i = lVar;
        try {
            this.h = getId();
        } catch (Throwable e) {
            fo.b(e, "CircleDelegateImp", "create");
            e.printStackTrace();
        }
    }

    public boolean a() {
        return true;
    }

    public void remove() throws RemoteException {
        this.i.a(getId());
        this.i.setRunLowFrame(false);
    }

    public String getId() throws RemoteException {
        if (this.h == null) {
            this.h = j.a("Circle");
        }
        return this.h;
    }

    public void setZIndex(float f) throws RemoteException {
        this.f = f;
        this.i.r();
        this.i.setRunLowFrame(false);
    }

    public float getZIndex() throws RemoteException {
        return this.f;
    }

    public void setVisible(boolean z) throws RemoteException {
        this.g = z;
        this.i.setRunLowFrame(false);
    }

    public boolean isVisible() throws RemoteException {
        return this.g;
    }

    public boolean equalsRemote(IOverlay iOverlay) throws RemoteException {
        if (equals(iOverlay) || iOverlay.getId().equals(getId())) {
            return true;
        }
        return false;
    }

    public int hashCodeRemote() throws RemoteException {
        return 0;
    }

    public boolean b() throws RemoteException {
        int i = 0;
        this.l = false;
        LatLng latLng = this.a;
        if (latLng != null) {
            FPoint[] fPointArr = new FPoint[360];
            float[] fArr = new float[(fPointArr.length * 3)];
            double b = b(this.a.latitude) * this.b;
            IPoint iPoint = new IPoint();
            MapProjection c = this.i.c();
            MapProjection.lonlat2Geo(latLng.longitude, latLng.latitude, iPoint);
            while (i < 360) {
                double d = (((double) i) * 3.141592653589793d) / VirtualEarthProjection.MaxLongitude;
                double sin = Math.sin(d) * b;
                int i2 = (int) (sin + ((double) iPoint.x));
                int cos = (int) ((Math.cos(d) * b) + ((double) iPoint.y));
                FPoint fPoint = new FPoint();
                c.geo2Map(i2, cos, fPoint);
                fPointArr[i] = fPoint;
                fArr[i * 3] = fPointArr[i].x;
                fArr[(i * 3) + 1] = fPointArr[i].y;
                fArr[(i * 3) + 2] = 0.0f;
                i++;
            }
            this.k = fPointArr.length;
            this.j = eh.a(fArr);
        }
        return true;
    }

    public void a(GL10 gl10) throws RemoteException {
        boolean z = false;
        if (this.a != null) {
            if (this.b <= 0.0d) {
                z = true;
            }
            if (!z && this.g) {
                if (this.j == null || this.k == 0) {
                    b();
                }
                if (this.j != null && this.k > 0) {
                    du.b(gl10, this.e, this.d, this.j, this.c, this.k);
                }
                this.l = true;
            }
        }
    }

    void d() {
        this.k = 0;
        if (this.j != null) {
            this.j.clear();
        }
        this.i.setRunLowFrame(false);
    }

    public void setCenter(LatLng latLng) throws RemoteException {
        this.a = latLng;
        d();
    }

    public LatLng getCenter() throws RemoteException {
        return this.a;
    }

    public void setRadius(double d) throws RemoteException {
        this.b = d;
        d();
    }

    public double getRadius() throws RemoteException {
        return this.b;
    }

    public void setStrokeWidth(float f) throws RemoteException {
        this.c = f;
        this.i.setRunLowFrame(false);
    }

    public float getStrokeWidth() throws RemoteException {
        return this.c;
    }

    public void setStrokeColor(int i) throws RemoteException {
        this.d = i;
    }

    public int getStrokeColor() throws RemoteException {
        return this.d;
    }

    public void setFillColor(int i) throws RemoteException {
        this.e = i;
        this.i.setRunLowFrame(false);
    }

    public int getFillColor() throws RemoteException {
        return this.e;
    }

    private float a(double d) {
        return (float) ((Math.cos((3.141592653589793d * d) / VirtualEarthProjection.MaxLongitude) * ((double) m)) / ((double) (n << o)));
    }

    private double b(double d) {
        return WeightedLatLng.DEFAULT_INTENSITY / ((double) a(d));
    }

    public void destroy() {
        try {
            this.a = null;
            if (this.j != null) {
                this.j.clear();
                this.j = null;
            }
        } catch (Throwable th) {
            fo.b(th, "CircleDelegateImp", "destroy");
            th.printStackTrace();
            Log.d("destroy erro", "CircleDelegateImp destroy");
        }
    }

    public boolean contains(LatLng latLng) throws RemoteException {
        if (this.b >= ((double) AMapUtils.calculateLineDistance(this.a, latLng))) {
            return true;
        }
        return false;
    }

    public boolean c() {
        return this.l;
    }

    public boolean isAboveMaskLayer() {
        return false;
    }

    public void setAboveMaskLayer(boolean z) {
    }
}
