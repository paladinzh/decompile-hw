package com.avast.android.sdk.engine;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build.VERSION;

/* compiled from: Unknown */
public final class UsageStatsPermissionHelper {
    private UsageStatsPermissionHelper() {
    }

    @TargetApi(21)
    public static void askForPermission(Activity activity) {
        activity.startActivity(new Intent("android.settings.USAGE_ACCESS_SETTINGS"));
    }

    public static boolean hasPermission(Context context) {
        if (VERSION.SDK_INT < 21) {
            return true;
        }
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            return ((AppOpsManager) context.getSystemService("appops")).checkOpNoThrow("android:get_usage_stats", applicationInfo.uid, applicationInfo.packageName) == 0;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    @TargetApi(21)
    public static boolean hasUsageStatsStrippedOut(Context context) {
        return new Intent("android.settings.USAGE_ACCESS_SETTINGS").resolveActivity(context.getPackageManager()) == null;
    }
}
