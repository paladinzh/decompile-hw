package com.android.util;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build.VERSION;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class CompatUtils {
    private static final String TAG = CompatUtils.class.getSimpleName();
    public static final int sAPIleve = VERSION.SDK_INT;

    public static void localRequestPermissions(Object activity, String[] permissions, int requestCode) {
        if (activity == null) {
            Log.w(TAG, "localRequestPermissions is not be executed because activity is NULL");
            return;
        }
        try {
            if (sAPIleve >= 22) {
                activity.getClass().getMethod("requestPermissions", new Class[]{String[].class, Integer.TYPE}).invoke(activity, new Object[]{permissions, Integer.valueOf(requestCode)});
            }
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "Android API = " + sAPIleve + " and Exception in localRequestPermissions: " + e.getCause());
        } catch (IllegalAccessException e2) {
            Log.e(TAG, "Android API = " + sAPIleve + " and Exception in localRequestPermissions: " + e2.getCause());
        } catch (IllegalArgumentException e3) {
            Log.e(TAG, "Android API = " + sAPIleve + " and Exception in localRequestPermissions: " + e3.getCause());
        } catch (InvocationTargetException e4) {
            Log.e(TAG, "Android API = " + sAPIleve + " and Exception in localRequestPermissions: " + e4.getCause());
        } catch (SecurityException e5) {
            Log.e(TAG, "Android API = " + sAPIleve + " and Exception in localRequestPermissions: " + e5.getCause());
        }
    }

    public static boolean hasPermission(Context context, String permission) {
        if (context.getPackageManager().checkPermission(permission, context.getPackageName()) == 0) {
            return true;
        }
        return false;
    }

    public static void openPhoneManager(Object activity, String[] permissions, int requestCode) {
        localRequestPermissions(activity, permissions, requestCode);
    }

    public static ArrayList<String> grantPermissionsByManager(Object object, int requestCode) {
        Context activity = null;
        if (object instanceof Fragment) {
            activity = ((Fragment) object).getActivity();
        } else if (object instanceof Activity) {
            Activity activity2 = (Activity) object;
        }
        ArrayList<String> permissions = new ArrayList();
        if (activity == null) {
            return permissions;
        }
        SharedPreferences preference = Utils.getDefaultSharedPreferences(activity);
        boolean isFirstTime = preference.getBoolean("first_time", true);
        Log.iRelease(TAG, "isFirstTime = " + isFirstTime);
        if (!hasPermission(activity, "android.permission.READ_EXTERNAL_STORAGE")) {
            permissions.add("android.permission.READ_EXTERNAL_STORAGE");
        }
        if (permissions.size() == 0) {
            Log.iRelease(TAG, "grantPermissions->has all permissions");
            preference.edit().putBoolean("first_time", false).commit();
            return permissions;
        }
        String[] permissonsArray = new String[permissions.size()];
        if (isFirstTime) {
            preference.edit().putBoolean("first_time", false).commit();
            if (!requestPermissionsFirst(activity, (String[]) permissions.toArray(permissonsArray), requestCode)) {
                openPhoneManager(object, (String[]) permissions.toArray(permissonsArray), requestCode);
            }
        } else {
            openPhoneManager(object, (String[]) permissions.toArray(permissonsArray), requestCode);
        }
        return permissions;
    }

    private static boolean requestPermissionsFirst(Activity activity, String[] permissions, int requestCode) {
        Intent intent = new Intent("huawei.intent.action.REQUEST_MULTI_PERMISSIONS");
        intent.setPackage("com.huawei.systemmanager");
        intent.putExtra("KEY_HW_PERMISSION_ARRAY", permissions);
        try {
            activity.startActivityForResult(intent, requestCode);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "requestPermissionsFirst : Exception");
            return false;
        }
    }
}
