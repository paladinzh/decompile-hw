package com.huawei.gallery.media;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.android.gallery3d.util.GalleryUtils;

public class LocationFailedRecordUtils {
    public static String getLocalLocationKey(long locationKey, String language) {
        return String.valueOf(locationKey) + language;
    }

    public static void rememberFailedLocationInfo(String key) {
        long value = getPreferenceValue(key);
        if (value < 3) {
            value++;
        }
        if (value >= 3) {
            value = System.currentTimeMillis();
        }
        setPreferenceValue(key, value);
    }

    public static boolean skipAnalysisFailedLocation(String key) {
        if (isWifiConnected()) {
            return false;
        }
        long value = getPreferenceValue(key);
        if (value == -1) {
            return true;
        }
        if (value < 3) {
            return false;
        }
        long currentMillis = System.currentTimeMillis();
        return currentMillis >= value && currentMillis <= 172800000 + value;
    }

    public static synchronized long getPreferenceValue(String key) {
        synchronized (LocationFailedRecordUtils.class) {
            if (GalleryUtils.getContext() == null) {
                return -1;
            }
            long j = GalleryUtils.getContext().getSharedPreferences("failedLocationRecord", 0).getLong(key, 0);
            return j;
        }
    }

    private static synchronized void setPreferenceValue(String key, long value) {
        synchronized (LocationFailedRecordUtils.class) {
            if (GalleryUtils.getContext() == null) {
                return;
            }
            Editor editor = GalleryUtils.getContext().getSharedPreferences("failedLocationRecord", 0).edit();
            editor.putLong(key, value);
            editor.apply();
        }
    }

    private static boolean isWifiConnected() {
        Context context = GalleryUtils.getContext();
        if (context == null) {
            return false;
        }
        NetworkInfo activeNetInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        return activeNetInfo != null && activeNetInfo.getType() == 1;
    }
}
