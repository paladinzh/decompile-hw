package com.amap.api.mapcore.util;

import android.graphics.Point;
import com.amap.api.mapcore.util.af.a;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapProjection;
import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: CameraUpdateFactoryDelegate */
public class ag {
    public static af a() {
        af ahVar = new ah();
        ahVar.a = a.zoomBy;
        ahVar.d = WMElement.CAMERASIZEVALUE1B1;
        return ahVar;
    }

    public static af b() {
        af ahVar = new ah();
        ahVar.a = a.zoomBy;
        ahVar.d = GroundOverlayOptions.NO_DIMENSION;
        return ahVar;
    }

    public static af a(float f, float f2) {
        af aeVar = new ae();
        aeVar.a = a.scrollBy;
        aeVar.b = f;
        aeVar.c = f2;
        return aeVar;
    }

    public static af a(float f) {
        af adVar = new ad();
        adVar.a = a.newCameraPosition;
        adVar.zoom = f;
        return adVar;
    }

    public static af b(float f) {
        return a(f, null);
    }

    public static af a(float f, Point point) {
        af ahVar = new ah();
        ahVar.a = a.zoomBy;
        ahVar.d = f;
        ahVar.g = point;
        return ahVar;
    }

    public static af a(CameraPosition cameraPosition) {
        af adVar = new ad();
        adVar.a = a.newCameraPosition;
        if (cameraPosition == null || cameraPosition.target == null) {
            return adVar;
        }
        IPoint iPoint = new IPoint();
        MapProjection.lonlat2Geo(cameraPosition.target.longitude, cameraPosition.target.latitude, iPoint);
        adVar.geoPoint = iPoint;
        adVar.zoom = cameraPosition.zoom;
        adVar.bearing = cameraPosition.bearing;
        adVar.tilt = cameraPosition.tilt;
        adVar.e = cameraPosition;
        return adVar;
    }

    public static af a(IPoint iPoint) {
        af adVar = new ad();
        adVar.a = a.newCameraPosition;
        adVar.geoPoint = iPoint;
        return adVar;
    }

    public static af c(float f) {
        af adVar = new ad();
        adVar.a = a.newCameraPosition;
        adVar.tilt = f;
        return adVar;
    }

    public static af d(float f) {
        af adVar = new ad();
        adVar.a = a.newCameraPosition;
        adVar.bearing = f;
        return adVar;
    }

    public static af a(float f, IPoint iPoint) {
        af adVar = new ad();
        adVar.a = a.newCameraPosition;
        adVar.geoPoint = iPoint;
        adVar.bearing = f;
        return adVar;
    }

    public static af a(LatLng latLng) {
        return a(CameraPosition.builder().target(latLng).build());
    }

    public static af a(LatLng latLng, float f) {
        return a(CameraPosition.builder().target(latLng).zoom(f).build());
    }

    public static af a(LatLngBounds latLngBounds, int i) {
        af acVar = new ac();
        acVar.a = a.newLatLngBounds;
        acVar.f = latLngBounds;
        acVar.h = i;
        acVar.i = i;
        acVar.j = i;
        acVar.k = i;
        return acVar;
    }

    public static af a(LatLngBounds latLngBounds, int i, int i2, int i3) {
        af acVar = new ac();
        acVar.a = a.newLatLngBoundsWithSize;
        acVar.f = latLngBounds;
        acVar.h = i3;
        acVar.i = i3;
        acVar.j = i3;
        acVar.k = i3;
        acVar.width = i;
        acVar.height = i2;
        return acVar;
    }

    public static af a(LatLngBounds latLngBounds, int i, int i2, int i3, int i4) {
        af acVar = new ac();
        acVar.a = a.newLatLngBounds;
        acVar.f = latLngBounds;
        acVar.h = i;
        acVar.i = i2;
        acVar.j = i3;
        acVar.k = i4;
        return acVar;
    }

    public static af c() {
        return new ad();
    }
}
