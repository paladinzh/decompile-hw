package com.amap.api.mapcore.util;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.WeightedLatLng;
import com.amap.api.trace.TraceLocation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/* compiled from: TracePointReducer */
public class ew {
    public static List<TraceLocation> a(List<TraceLocation> list, float f) {
        if (list == null) {
            return null;
        }
        if (list.size() <= 2) {
            return list;
        }
        double d = 0.0d;
        List<TraceLocation> arrayList = new ArrayList();
        TraceLocation traceLocation = (TraceLocation) list.get(0);
        TraceLocation traceLocation2 = (TraceLocation) list.get(list.size() - 1);
        int i = 0;
        for (int i2 = 1; i2 < list.size() - 1; i2++) {
            double a = a((TraceLocation) list.get(i2), traceLocation, traceLocation2);
            if (a > d) {
                i = i2;
                d = a;
            }
        }
        if (d < ((double) f)) {
            arrayList.add(traceLocation);
            arrayList.add(traceLocation2);
            return arrayList;
        }
        Collection a2 = a(list.subList(0, i + 1), f);
        Collection a3 = a(list.subList(i, list.size()), f);
        arrayList.addAll(a2);
        arrayList.remove(arrayList.size() - 1);
        arrayList.addAll(a3);
        return arrayList;
    }

    private static double a(TraceLocation traceLocation, TraceLocation traceLocation2, TraceLocation traceLocation3) {
        double longitude;
        double latitude;
        double longitude2 = traceLocation3.getLongitude() - traceLocation2.getLongitude();
        double latitude2 = traceLocation3.getLatitude() - traceLocation2.getLatitude();
        double longitude3 = (((traceLocation.getLongitude() - traceLocation2.getLongitude()) * longitude2) + ((traceLocation.getLatitude() - traceLocation2.getLatitude()) * latitude2)) / ((longitude2 * longitude2) + (latitude2 * latitude2));
        if ((longitude3 < 0.0d ? 1 : null) != null || (traceLocation2.getLongitude() == traceLocation3.getLongitude() && traceLocation2.getLatitude() == traceLocation3.getLatitude())) {
            longitude = traceLocation2.getLongitude();
            latitude = traceLocation2.getLatitude();
        } else if (longitude3 > WeightedLatLng.DEFAULT_INTENSITY) {
            longitude = traceLocation3.getLongitude();
            latitude = traceLocation3.getLatitude();
        } else {
            longitude = (longitude3 * longitude2) + traceLocation2.getLongitude();
            latitude = traceLocation2.getLatitude() + (longitude3 * latitude2);
        }
        return (double) AMapUtils.calculateLineDistance(new LatLng(traceLocation.getLatitude(), traceLocation.getLongitude()), new LatLng(latitude, longitude));
    }
}
