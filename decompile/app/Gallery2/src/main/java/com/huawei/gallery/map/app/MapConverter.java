package com.huawei.gallery.map.app;

import com.amap.api.maps.model.WeightedLatLng;
import com.autonavi.amap.mapcore.VirtualEarthProjection;
import com.huawei.gallery.map.data.MapLatLng;

public class MapConverter {
    private static double a = 6378245.0d;
    private static double ee = 0.006693421622965943d;

    public static MapLatLng transform(double wgLat, double wgLon, MapLatLng point) {
        if (useGps(wgLat, wgLon)) {
            point.latitude = wgLat;
            point.longitude = wgLon;
            return point;
        }
        double dLat = transformLat(wgLon - 105.0d, wgLat - 35.0d);
        double dLon = transformLon(wgLon - 105.0d, wgLat - 35.0d);
        double radLat = (wgLat / VirtualEarthProjection.MaxLongitude) * 3.141592653589793d;
        double magic = Math.sin(radLat);
        magic = WeightedLatLng.DEFAULT_INTENSITY - ((ee * magic) * magic);
        double sqrtMagic = Math.sqrt(magic);
        dLon = (VirtualEarthProjection.MaxLongitude * dLon) / (((a / sqrtMagic) * Math.cos(radLat)) * 3.141592653589793d);
        point.latitude = wgLat + ((VirtualEarthProjection.MaxLongitude * dLat) / (((a * (WeightedLatLng.DEFAULT_INTENSITY - ee)) / (magic * sqrtMagic)) * 3.141592653589793d));
        point.longitude = wgLon + dLon;
        return point;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean useGps(double lat, double lon) {
        if (lon < 72.004d || lon > 137.8347d || lat < 0.8293d || lat > 55.8271d) {
            return true;
        }
        return false;
    }

    private static double transformLat(double x, double y) {
        return ((((((((2.0d * x) - 0.044921875d) + (3.0d * y)) + ((0.2d * y) * y)) + ((0.1d * x) * y)) + (Math.sqrt(Math.abs(x)) * 0.2d)) + ((((Math.sin((6.0d * x) * 3.141592653589793d) * 20.0d) + (Math.sin((2.0d * x) * 3.141592653589793d) * 20.0d)) * 2.0d) / 3.0d)) + ((((Math.sin(3.141592653589793d * y) * 20.0d) + (Math.sin((y / 3.0d) * 3.141592653589793d) * 40.0d)) * 2.0d) / 3.0d)) + ((((Math.sin((y / 12.0d) * 3.141592653589793d) * 160.0d) + (Math.sin((3.141592653589793d * y) / 30.0d) * 320.0d)) * 2.0d) / 3.0d);
    }

    private static double transformLon(double x, double y) {
        return (((((((300.0d + x) + (2.0d * y)) + ((0.1d * x) * x)) + ((0.1d * x) * y)) + (Math.sqrt(Math.abs(x)) * 0.1d)) + ((((Math.sin((6.0d * x) * 3.141592653589793d) * 20.0d) + (Math.sin((2.0d * x) * 3.141592653589793d) * 20.0d)) * 2.0d) / 3.0d)) + ((((Math.sin(3.141592653589793d * x) * 20.0d) + (Math.sin((x / 3.0d) * 3.141592653589793d) * 40.0d)) * 2.0d) / 3.0d)) + ((((Math.sin((x / 12.0d) * 3.141592653589793d) * 150.0d) + (Math.sin((x / 30.0d) * 3.141592653589793d) * 300.0d)) * 2.0d) / 3.0d);
    }
}
