package com.huawei.gallery.map.app;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.watermark.manager.parse.WMElement;

public abstract class MapUtils {
    public static int getBestZoomScale(int showWidthPx, int showHeightPx, double widthInLatLng, double heightInLatLng, double[] meterPerPxArray, int defaultValue) {
        for (int i = 1; i < meterPerPxArray.length; i++) {
            if (meterPerPxArray[i] != -1.0d) {
                double showableWidthLatlng = (((double) showWidthPx) * meterPerPxArray[i]) / 111000.0d;
                if ((((double) showHeightPx) * meterPerPxArray[i]) / 111000.0d <= heightInLatLng || showableWidthLatlng <= widthInLatLng) {
                    return i;
                }
            }
        }
        return defaultValue;
    }

    public static double getMapClusterRadius(double markRadiusInPX, double[] meterPerPxArray, float zoomLevel) {
        float margin = zoomLevel - ((float) ((int) zoomLevel));
        return (markRadiusInPX * ((meterPerPxArray[(int) zoomLevel] * ((double) (WMElement.CAMERASIZEVALUE1B1 - margin))) + (meterPerPxArray[Math.min(((int) zoomLevel) + 1, meterPerPxArray.length - 1)] * ((double) margin)))) / 111000.0d;
    }

    public static boolean isPackagesExist(Context context, String... pkgs) {
        if (pkgs == null) {
            return false;
        }
        try {
            for (String pkg : pkgs) {
                context.getPackageManager().getPackageGids(pkg);
            }
            return true;
        } catch (NameNotFoundException e) {
            GalleryLog.w("MapUtil", "package is missing." + e.getMessage());
            return false;
        }
    }

    public static boolean isMapOverRanged(double lat, double lng) {
        GalleryLog.d("MapUtil", String.format("[isMapOverRanged] lat=%s,lng=%s)", new Object[]{Double.valueOf(lat), Double.valueOf(lng)}));
        if (lat > 45.0d || lng > 90.0d) {
            return true;
        }
        return false;
    }
}
