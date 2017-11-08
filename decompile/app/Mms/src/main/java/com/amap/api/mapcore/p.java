package com.amap.api.mapcore;

import android.graphics.Point;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.autonavi.amap.mapcore.IPoint;

/* compiled from: CameraUpdateFactoryDelegate */
public class p {
    a a = a.none;
    float b;
    float c;
    float d;
    float e;
    float f;
    float g;
    CameraPosition h;
    LatLngBounds i;
    int j;
    int k;
    int l;
    Point m = null;
    boolean n = false;
    IPoint o;
    boolean p = false;

    /* compiled from: CameraUpdateFactoryDelegate */
    enum a {
        none,
        zoomIn,
        changeCenter,
        changeTilt,
        changeBearing,
        changeBearingGeoCenter,
        changeGeoCenterZoom,
        zoomOut,
        zoomTo,
        zoomBy,
        scrollBy,
        newCameraPosition,
        newLatLngBounds,
        newLatLngBoundsWithSize,
        changeGeoCenterZoomTiltBearing
    }

    private p() {
    }

    public static p a() {
        return new p();
    }

    public static p b() {
        p a = a();
        a.a = a.zoomIn;
        return a;
    }

    public static p c() {
        p a = a();
        a.a = a.zoomOut;
        return a;
    }

    public static p a(float f, float f2) {
        p a = a();
        a.a = a.scrollBy;
        a.b = f;
        a.c = f2;
        return a;
    }

    public static p a(float f) {
        p a = a();
        a.a = a.zoomTo;
        a.d = f;
        return a;
    }

    public static p b(float f) {
        return a(f, null);
    }

    public static p a(float f, Point point) {
        p a = a();
        a.a = a.zoomBy;
        a.e = f;
        a.m = point;
        return a;
    }

    public static p a(CameraPosition cameraPosition) {
        p a = a();
        a.a = a.newCameraPosition;
        a.h = cameraPosition;
        return a;
    }

    public static p a(IPoint iPoint) {
        p a = a();
        a.a = a.changeCenter;
        a.o = iPoint;
        return a;
    }

    public static p c(float f) {
        p a = a();
        a.a = a.changeTilt;
        a.f = f;
        return a;
    }

    public static p d(float f) {
        p a = a();
        a.a = a.changeBearing;
        a.g = f;
        return a;
    }

    public static p a(float f, IPoint iPoint) {
        p a = a();
        a.a = a.changeBearingGeoCenter;
        a.g = f;
        a.o = iPoint;
        return a;
    }

    public static p a(LatLng latLng) {
        return a(CameraPosition.builder().target(latLng).build());
    }

    public static p a(LatLng latLng, float f) {
        return a(CameraPosition.builder().target(latLng).zoom(f).build());
    }

    public static p a(LatLng latLng, float f, float f2, float f3) {
        return a(CameraPosition.builder().target(latLng).zoom(f).bearing(f2).tilt(f3).build());
    }

    static p a(IPoint iPoint, float f, float f2, float f3) {
        p a = a();
        a.a = a.changeGeoCenterZoomTiltBearing;
        a.o = iPoint;
        a.d = f;
        a.g = f2;
        a.f = f3;
        return a;
    }

    public static p a(LatLngBounds latLngBounds, int i) {
        p a = a();
        a.a = a.newLatLngBounds;
        a.i = latLngBounds;
        a.j = i;
        return a;
    }

    public static p a(LatLngBounds latLngBounds, int i, int i2, int i3) {
        p a = a();
        a.a = a.newLatLngBoundsWithSize;
        a.i = latLngBounds;
        a.j = i3;
        a.k = i;
        a.l = i2;
        return a;
    }
}
