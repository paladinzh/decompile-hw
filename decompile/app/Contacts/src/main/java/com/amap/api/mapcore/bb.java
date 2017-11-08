package com.amap.api.mapcore;

import android.content.Context;
import android.location.Location;
import android.os.RemoteException;
import com.amap.api.mapcore.util.ce;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapProjection;

/* compiled from: MyLocationOverlay */
class bb {
    private ab a;
    private Marker b;
    private ad c;
    private MyLocationStyle d;
    private LatLng e;
    private double f;
    private Context g;
    private bk h;
    private int i = 1;
    private boolean j = false;
    private final String k = "location_map_gps_locked.png";
    private final String l = "location_map_gps_3d.png";
    private boolean m = false;

    bb(ab abVar, Context context) {
        this.g = context;
        this.a = abVar;
        this.h = new bk(this.g, abVar);
    }

    public void a(MyLocationStyle myLocationStyle) {
        try {
            this.d = myLocationStyle;
            if (this.b != null || this.c != null) {
                k();
                this.h.a(this.b);
                j();
            }
        } catch (Throwable th) {
            ce.a(th, "MyLocationOverlay", "setMyLocationStyle");
            th.printStackTrace();
        }
    }

    public void a(int i) {
        this.i = i;
        this.j = false;
        switch (this.i) {
            case 1:
                f();
                return;
            case 2:
                g();
                return;
            case 3:
                h();
                return;
            default:
                return;
        }
    }

    public void a() {
        if (this.i == 3 && this.h != null) {
            this.h.a();
        }
    }

    private void f() {
        if (this.b != null) {
            c(0.0f);
            this.h.b();
            if (!this.m) {
                this.b.setIcon(BitmapDescriptorFactory.fromAsset("location_map_gps_locked.png"));
            }
            this.b.setFlat(false);
            b(0.0f);
        }
    }

    private void g() {
        if (this.b != null) {
            c(0.0f);
            this.h.b();
            if (!this.m) {
                this.b.setIcon(BitmapDescriptorFactory.fromAsset("location_map_gps_locked.png"));
            }
            this.b.setFlat(false);
            b(0.0f);
        }
    }

    private void h() {
        if (this.b != null) {
            this.b.setRotateAngle(0.0f);
            this.h.a();
            if (!this.m) {
                this.b.setIcon(BitmapDescriptorFactory.fromAsset("location_map_gps_3d.png"));
            }
            this.b.setFlat(true);
            try {
                this.a.a(p.a(17.0f));
                b(45.0f);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void b(float f) {
        if (this.a != null) {
            try {
                this.a.a(p.c(f));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void c(float f) {
        if (this.a != null) {
            try {
                this.a.a(p.d(f));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void a(Location location) {
        if (location != null) {
            this.e = new LatLng(location.getLatitude(), location.getLongitude());
            this.f = (double) location.getAccuracy();
            if (this.b == null && this.c == null) {
                j();
            }
            if (this.b != null) {
                this.b.setPosition(this.e);
            }
            if (this.c != null) {
                try {
                    this.c.a(this.e);
                    if (this.f != -1.0d) {
                        this.c.a(this.f);
                    }
                } catch (Throwable e) {
                    ce.a(e, "MyLocationOverlay", "setCentAndRadius");
                    e.printStackTrace();
                }
                i();
                if (this.i != 3) {
                    b(location);
                }
                this.j = true;
            }
        }
    }

    private void b(Location location) {
        float bearing = location.getBearing() % 360.0f;
        if (bearing > 180.0f) {
            bearing -= 360.0f;
        } else if (bearing < -180.0f) {
            bearing += 360.0f;
        }
        if (this.b != null) {
            this.b.setRotateAngle(-bearing);
        }
    }

    private void i() {
        if (this.i != 1 || !this.j) {
            try {
                IPoint iPoint = new IPoint();
                MapProjection.lonlat2Geo(this.e.longitude, this.e.latitude, iPoint);
                this.a.b(p.a(iPoint));
            } catch (Throwable e) {
                ce.a(e, "MyLocationOverlay", "locaitonFollow");
                e.printStackTrace();
            }
        }
    }

    private void j() {
        if (this.d != null) {
            this.m = true;
            l();
            return;
        }
        this.d = new MyLocationStyle();
        this.d.myLocationIcon(BitmapDescriptorFactory.fromAsset("location_map_gps_locked.png"));
        l();
    }

    public void b() throws RemoteException {
        k();
        if (this.h != null) {
            this.h.b();
            this.h = null;
        }
    }

    private void k() {
        if (this.c != null) {
            try {
                this.a.a(this.c.c());
            } catch (Throwable e) {
                ce.a(e, "MyLocationOverlay", "locationIconRemove");
                e.printStackTrace();
            }
            this.c = null;
        }
        if (this.b != null) {
            this.b.remove();
            this.b.destroy();
            this.b = null;
            this.h.a(null);
        }
    }

    private void l() {
        try {
            this.c = this.a.a(new CircleOptions().strokeWidth(this.d.getStrokeWidth()).fillColor(this.d.getRadiusFillColor()).strokeColor(this.d.getStrokeColor()).center(new LatLng(0.0d, 0.0d)).zIndex(1.0f));
            if (this.e != null) {
                this.c.a(this.e);
            }
            this.c.a(this.f);
            this.b = this.a.a(new MarkerOptions().visible(false).anchor(this.d.getAnchorU(), this.d.getAnchorV()).icon(this.d.getMyLocationIcon()).position(new LatLng(0.0d, 0.0d)));
            a(this.i);
            if (this.e != null) {
                this.b.setPosition(this.e);
                this.b.setVisible(true);
            }
            this.h.a(this.b);
        } catch (Throwable e) {
            ce.a(e, "MyLocationOverlay", "myLocStyle");
            e.printStackTrace();
        }
    }

    public void a(float f) {
        if (this.b != null) {
            this.b.setRotateAngle(f);
        }
    }

    public String c() {
        if (this.b == null) {
            return null;
        }
        return this.b.getId();
    }

    public String d() throws RemoteException {
        if (this.c == null) {
            return null;
        }
        return this.c.c();
    }

    public void e() {
        this.c = null;
        this.b = null;
    }
}
