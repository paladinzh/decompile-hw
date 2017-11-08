package com.amap.api.mapcore;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.model.CameraPosition;

/* compiled from: MapFragmentDelegateImp */
public class at implements ag {
    public static volatile Context a;
    public static int c = 0;
    public static int d = 1;
    public int b = 0;
    private ab e;
    private int f = 0;
    private AMapOptions g;

    public at(int i) {
        int i2 = 0;
        if (i > 0) {
            i2 = 1;
        }
        this.f = i2;
    }

    public void a(Context context) {
        if (context != null) {
            a = context.getApplicationContext();
        }
    }

    public void a(AMapOptions aMapOptions) {
        this.g = aMapOptions;
    }

    public ab a() throws RemoteException {
        if (this.e == null) {
            if (a != null) {
                int i = a.getResources().getDisplayMetrics().densityDpi;
                if (i <= 120) {
                    s.a = 0.5f;
                } else if (i <= 160) {
                    s.a = 0.8f;
                } else if (i <= 240) {
                    s.a = 0.87f;
                } else if (i <= 320) {
                    s.a = 1.0f;
                } else if (i <= 480) {
                    s.a = 1.5f;
                } else if (i > 640) {
                    s.a = 0.9f;
                } else {
                    s.a = 1.8f;
                }
                if (this.f != c) {
                    this.e = new k(a).a();
                } else {
                    this.e = new j(a).a();
                }
            } else {
                throw new NullPointerException("Context 为 null 请在地图调用之前 使用 MapsInitializer.initialize(Context paramContext) 来设置Context");
            }
        }
        return this.e;
    }

    public void a(Activity activity, AMapOptions aMapOptions, Bundle bundle) throws RemoteException {
        a = activity.getApplicationContext();
        this.g = aMapOptions;
    }

    public void a(Bundle bundle) throws RemoteException {
    }

    public View a(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) throws RemoteException {
        if (this.e == null) {
            if (a == null && layoutInflater != null) {
                a = layoutInflater.getContext().getApplicationContext();
            }
            if (a != null) {
                int i = a.getResources().getDisplayMetrics().densityDpi;
                if (i <= 120) {
                    s.a = 0.5f;
                } else if (i <= 160) {
                    s.a = 0.6f;
                } else if (i <= 240) {
                    s.a = 0.87f;
                } else if (i <= 320) {
                    s.a = 1.0f;
                } else if (i <= 480) {
                    s.a = 1.5f;
                } else if (i > 640) {
                    s.a = 0.9f;
                } else {
                    s.a = 1.8f;
                }
                if (this.f != c) {
                    this.e = new k(a).a();
                } else {
                    this.e = new j(a).a();
                }
                this.e.h(this.b);
            } else {
                throw new NullPointerException("Context 为 null 请在地图调用之前 使用 MapsInitializer.initialize(Context paramContext) 来设置Context");
            }
        }
        try {
            if (this.g == null && bundle != null) {
                byte[] byteArray = bundle.getByteArray("MapOptions");
                if (byteArray != null) {
                    Parcel obtain = Parcel.obtain();
                    obtain.unmarshall(byteArray, 0, byteArray.length);
                    obtain.setDataPosition(0);
                    this.g = AMapOptions.CREATOR.createFromParcel(obtain);
                }
            }
            b(this.g);
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return this.e.D();
    }

    void b(AMapOptions aMapOptions) throws RemoteException {
        if (aMapOptions != null && this.e != null) {
            CameraPosition camera = aMapOptions.getCamera();
            if (camera != null) {
                this.e.a(p.a(camera.target, camera.zoom, camera.bearing, camera.tilt));
            }
            aq A = this.e.A();
            A.i(aMapOptions.getRotateGesturesEnabled().booleanValue());
            A.f(aMapOptions.getScrollGesturesEnabled().booleanValue());
            A.h(aMapOptions.getTiltGesturesEnabled().booleanValue());
            A.c(aMapOptions.getZoomControlsEnabled().booleanValue());
            A.g(aMapOptions.getZoomGesturesEnabled().booleanValue());
            A.d(aMapOptions.getCompassEnabled().booleanValue());
            A.b(aMapOptions.getScaleControlsEnabled().booleanValue());
            A.a(aMapOptions.getLogoPosition());
            this.e.b(aMapOptions.getMapType());
            this.e.g(aMapOptions.getZOrderOnTop().booleanValue());
        }
    }

    public void b() throws RemoteException {
        if (this.e != null) {
            this.e.d();
        }
    }

    public void c() throws RemoteException {
        if (this.e != null) {
            this.e.e();
        }
    }

    public void d() throws RemoteException {
    }

    public void e() throws RemoteException {
        if (this.e != null) {
            this.e.v();
            this.e.h();
            this.e = null;
        }
    }

    public void f() throws RemoteException {
        Log.d("onLowMemory", "onLowMemory run");
    }

    public void b(Bundle bundle) throws RemoteException {
        if (this.e != null) {
            if (this.g == null) {
                this.g = new AMapOptions();
            }
            try {
                Parcel obtain = Parcel.obtain();
                this.g = this.g.camera(a().n(false));
                this.g.writeToParcel(obtain, 0);
                bundle.putByteArray("MapOptions", obtain.marshall());
            } catch (Throwable th) {
            }
        }
    }

    public void a(int i) {
        this.b = i;
        if (this.e != null) {
            this.e.h(i);
        }
    }
}
