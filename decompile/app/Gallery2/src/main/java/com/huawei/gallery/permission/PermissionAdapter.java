package com.huawei.gallery.permission;

import android.app.Activity;
import android.os.Build.VERSION;
import com.android.gallery3d.util.GalleryLog;

public class PermissionAdapter {
    private static String TAG = "PermissionAdapter";

    public static boolean hasPermission(Activity activity, String permission) {
        boolean z = true;
        if (VERSION.SDK_INT < 23 || activity == null || permission == null) {
            return true;
        }
        try {
            if (activity.checkSelfPermission(permission) != 0) {
                z = false;
            }
            return z;
        } catch (Exception e) {
            GalleryLog.w(TAG, "No checkSelfPermission method!");
            return true;
        }
    }

    public static void requestPermissions(Activity activity, String[] permissions, int requestCode) {
        if (VERSION.SDK_INT >= 23 && activity != null && permissions != null) {
            try {
                activity.requestPermissions(permissions, requestCode);
            } catch (Exception e) {
                GalleryLog.w(TAG, "No requestPermissions method!");
            }
        }
    }

    public static boolean shouldShowRequestPermissionRationale(Activity activity, String permission) {
        if (VERSION.SDK_INT < 23 || activity == null || permission == null) {
            return true;
        }
        try {
            return activity.shouldShowRequestPermissionRationale(permission);
        } catch (Exception e) {
            GalleryLog.w(TAG, "No shouldShowRequestPermissionRationale method!");
            return true;
        }
    }
}
