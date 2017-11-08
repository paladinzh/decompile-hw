package com.amap.api.maps;

import android.graphics.Point;
import android.util.Log;
import com.amap.api.mapcore.util.ag;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapProjection;

public final class CameraUpdateFactory {
    public static CameraUpdate zoomIn() {
        return new CameraUpdate(ag.a());
    }

    public static CameraUpdate zoomOut() {
        return new CameraUpdate(ag.b());
    }

    public static CameraUpdate scrollBy(float f, float f2) {
        return new CameraUpdate(ag.a(f, f2));
    }

    public static CameraUpdate zoomTo(float f) {
        return new CameraUpdate(ag.a(f));
    }

    public static CameraUpdate zoomBy(float f) {
        return new CameraUpdate(ag.b(f));
    }

    public static CameraUpdate zoomBy(float f, Point point) {
        return new CameraUpdate(ag.a(f, point));
    }

    public static CameraUpdate newCameraPosition(CameraPosition cameraPosition) {
        if (cameraPosition != null) {
            return new CameraUpdate(ag.a(cameraPosition));
        }
        Log.w("CameraUpdateFactory", "cameraPosition is null");
        return new CameraUpdate(ag.c());
    }

    public static CameraUpdate newLatLng(LatLng latLng) {
        if (latLng != null) {
            return new CameraUpdate(ag.a(latLng));
        }
        Log.w("CameraUpdateFactory", "latLng is null");
        return new CameraUpdate(ag.c());
    }

    public static CameraUpdate newLatLngZoom(LatLng latLng, float f) {
        if (latLng != null) {
            return new CameraUpdate(ag.a(latLng, f));
        }
        Log.w("CameraUpdateFactory", "target is null");
        return new CameraUpdate(ag.c());
    }

    public static CameraUpdate newLatLngBounds(LatLngBounds latLngBounds, int i) {
        if (latLngBounds != null) {
            return new CameraUpdate(ag.a(latLngBounds, i));
        }
        Log.w("CameraUpdateFactory", "bounds is null");
        return new CameraUpdate(ag.c());
    }

    public static CameraUpdate changeLatLng(LatLng latLng) {
        if (latLng != null) {
            IPoint iPoint = new IPoint();
            MapProjection.lonlat2Geo(latLng.longitude, latLng.latitude, iPoint);
            return new CameraUpdate(ag.a(iPoint));
        }
        Log.w("CameraUpdateFactory", "target is null");
        return new CameraUpdate(ag.c());
    }

    public static CameraUpdate changeBearing(float f) {
        return new CameraUpdate(ag.d(f % 360.0f));
    }

    public static CameraUpdate changeBearingGeoCenter(float f, IPoint iPoint) {
        if (iPoint != null) {
            return new CameraUpdate(ag.a(f % 360.0f, iPoint));
        }
        Log.w("CameraUpdateFactory", "geoPoint is null");
        return new CameraUpdate(ag.c());
    }

    public static CameraUpdate changeTilt(float f) {
        return new CameraUpdate(ag.c(f));
    }

    public static CameraUpdate newLatLngBounds(LatLngBounds latLngBounds, int i, int i2, int i3) {
        if (latLngBounds != null) {
            return new CameraUpdate(ag.a(latLngBounds, i, i2, i3));
        }
        Log.w("CameraUpdateFactory", "bounds is null");
        return new CameraUpdate(ag.c());
    }

    public static CameraUpdate newLatLngBoundsRect(LatLngBounds latLngBounds, int i, int i2, int i3, int i4) {
        if (latLngBounds != null) {
            return new CameraUpdate(ag.a(latLngBounds, i, i2, i3, i4));
        }
        Log.w("CameraUpdateFactory", "bounds is null");
        return new CameraUpdate(ag.c());
    }
}
