package com.amap.api.maps;

import android.graphics.Point;
import com.amap.api.mapcore.p;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapProjection;

public final class CameraUpdateFactory {
    public static CameraUpdate zoomIn() {
        return new CameraUpdate(p.b());
    }

    public static CameraUpdate zoomOut() {
        return new CameraUpdate(p.c());
    }

    public static CameraUpdate scrollBy(float f, float f2) {
        return new CameraUpdate(p.a(f, f2));
    }

    public static CameraUpdate zoomTo(float f) {
        return new CameraUpdate(p.a(f));
    }

    public static CameraUpdate zoomBy(float f) {
        return new CameraUpdate(p.b(f));
    }

    public static CameraUpdate zoomBy(float f, Point point) {
        return new CameraUpdate(p.a(f, point));
    }

    public static CameraUpdate newCameraPosition(CameraPosition cameraPosition) {
        return new CameraUpdate(p.a(cameraPosition));
    }

    public static CameraUpdate newLatLng(LatLng latLng) {
        return new CameraUpdate(p.a(latLng));
    }

    public static CameraUpdate newLatLngZoom(LatLng latLng, float f) {
        return new CameraUpdate(p.a(latLng, f));
    }

    public static CameraUpdate newLatLngBounds(LatLngBounds latLngBounds, int i) {
        return new CameraUpdate(p.a(latLngBounds, i));
    }

    public static CameraUpdate changeLatLng(LatLng latLng) {
        IPoint iPoint = new IPoint();
        MapProjection.lonlat2Geo(latLng.longitude, latLng.latitude, iPoint);
        return new CameraUpdate(p.a(iPoint));
    }

    public static CameraUpdate changeBearing(float f) {
        return new CameraUpdate(p.d(f % 360.0f));
    }

    public static CameraUpdate changeBearingGeoCenter(float f, IPoint iPoint) {
        return new CameraUpdate(p.a(f % 360.0f, iPoint));
    }

    public static CameraUpdate changeTilt(float f) {
        return new CameraUpdate(p.c(f));
    }

    public static CameraUpdate newLatLngBounds(LatLngBounds latLngBounds, int i, int i2, int i3) {
        return new CameraUpdate(p.a(latLngBounds, i, i2, i3));
    }
}
