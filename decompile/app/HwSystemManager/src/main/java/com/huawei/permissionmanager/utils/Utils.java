package com.huawei.permissionmanager.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.ArraySet;
import android.util.Log;
import com.huawei.permission.MPermissionUtil;
import com.huawei.permissionmanager.model.AppPermissionGroup;
import com.huawei.permissionmanager.model.PermissionApps.PermissionApp;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;

public class Utils {
    private static final Intent LAUNCHER_INTENT = new Intent("android.intent.action.MAIN", null).addCategory("android.intent.category.LAUNCHER");
    private static final String LOG_TAG = "Utils";
    public static final String[] MODERN_PERMISSION_GROUPS = new String[]{MPermissionUtil.GROUP_CALENDAR, MPermissionUtil.GROUP_CAMERA, MPermissionUtil.GROUP_CONTACTS, "android.permission-group.LOCATION", MPermissionUtil.GROUP_SENSORS, MPermissionUtil.GROUP_SMS, MPermissionUtil.GROUP_PHONE, MPermissionUtil.GROUP_MICROPHONE, MPermissionUtil.GROUP_STORAGE};
    public static final String OS_PKG = "android";

    private Utils() {
    }

    public static Drawable loadDrawable(PackageManager pm, String pkg, int resId) {
        try {
            return pm.getResourcesForApplication(pkg).getDrawable(resId, null);
        } catch (Exception e) {
            Log.d(LOG_TAG, "Couldn't get resource", e);
            return null;
        }
    }

    public static boolean isModernPermissionGroup(String name) {
        for (String modernGroup : MODERN_PERMISSION_GROUPS) {
            if (modernGroup.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static boolean shouldShowPermission(AppPermissionGroup group, String packageName) {
        if (group.isSystemFixed() && !LocationUtils.isLocationGroupAndProvider(group.getName(), packageName)) {
            return false;
        }
        if (!group.getDeclaringPackage().equals("android") || isModernPermissionGroup(group.getName())) {
            return true;
        }
        return false;
    }

    @Deprecated
    public static boolean shouldShowPermission(AppPermissionGroup group) {
        if (group.isSystemFixed()) {
            return false;
        }
        if (!group.hasRuntimePermission() && !group.hasAppOpPermission()) {
            return false;
        }
        if (!group.getDeclaringPackage().equals("android") || isModernPermissionGroup(group.getName())) {
            return true;
        }
        return false;
    }

    public static boolean shouldShowPermission(PermissionApp app) {
        if (!app.isSystemFixed() || LocationUtils.isLocationGroupAndProvider(app.getPermissionGroup().getName(), app.getPackageName())) {
            return true;
        }
        return false;
    }

    public static ArraySet<String> getLauncherPackages(Context context) {
        ArraySet<String> launcherPkgs = new ArraySet();
        for (ResolveInfo info : PackageManagerWrapper.queryIntentActivities(context.getPackageManager(), LAUNCHER_INTENT, 0)) {
            launcherPkgs.add(info.activityInfo.packageName);
        }
        return launcherPkgs;
    }

    public static boolean isSystem(PermissionApp app, ArraySet<String> launcherPkgs) {
        ApplicationInfo info = app.getAppInfo();
        if (info.isSystemApp() && (info.flags & 128) == 0 && !launcherPkgs.contains(info.packageName)) {
            return true;
        }
        return false;
    }

    public static boolean isTelevision(Context context) {
        return (context.getResources().getConfiguration().uiMode & 15) == 4;
    }
}
