package com.amap.api.mapcore.util;

import android.graphics.Point;
import android.graphics.Rect;
import android.location.Location;
import android.os.RemoteException;
import android.view.MotionEvent;
import android.view.View;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.autonavi.amap.mapcore.DPoint;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapCore;
import com.autonavi.amap.mapcore.MapMessage;
import com.autonavi.amap.mapcore.MapProjection;
import com.autonavi.amap.mapcore.interfaces.IAMap;
import com.autonavi.amap.mapcore.interfaces.IMarkerAction;

/* compiled from: IAMapDelegate */
public interface l extends IAMap {
    int A();

    float a(int i);

    int a(BitmapDescriptor bitmapDescriptor);

    int a(IMarkerAction iMarkerAction, Rect rect);

    LatLngBounds a(LatLng latLng, float f, float f2, float f3);

    MapCore a();

    void a(double d, double d2, FPoint fPoint);

    void a(double d, double d2, IPoint iPoint);

    void a(float f, float f2, IPoint iPoint);

    void a(int i, float f);

    void a(int i, int i2, DPoint dPoint);

    void a(int i, int i2, FPoint fPoint);

    void a(int i, int i2, IPoint iPoint);

    void a(Location location) throws RemoteException;

    void a(cr crVar) throws RemoteException;

    void a(s sVar);

    void a(MapMessage mapMessage) throws RemoteException;

    void a(boolean z);

    boolean a(MotionEvent motionEvent);

    boolean a(String str) throws RemoteException;

    int b();

    void b(double d, double d2, IPoint iPoint);

    void b(int i);

    void b(int i, int i2, DPoint dPoint);

    void b(int i, int i2, FPoint fPoint);

    void b(MotionEvent motionEvent);

    void b(MapMessage mapMessage) throws RemoteException;

    void b(boolean z);

    MapProjection c();

    void c(int i);

    void c(boolean z);

    boolean c(MotionEvent motionEvent);

    void d();

    void d(int i);

    void d(boolean z);

    boolean d(MotionEvent motionEvent);

    float e(int i);

    void e();

    void e(boolean z);

    void f(int i);

    boolean f();

    void g(int i);

    int k();

    void l();

    o m() throws RemoteException;

    void n();

    float o();

    FPoint[] p();

    Point q();

    void r();

    float u();

    float y();

    View z();
}
