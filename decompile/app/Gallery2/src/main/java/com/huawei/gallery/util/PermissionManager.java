package com.huawei.gallery.util;

import android.app.Activity;
import android.content.Intent;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.permission.PermissionAdapter;
import java.util.ArrayList;
import java.util.List;

public class PermissionManager {
    private static final String[] PERMISSIONS_LOCATION = new String[]{"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"};
    private static final String[] PERMISSIONS_STORAGE = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};
    private static final String[] PERMISSION_CLOUD_INVITE_BY_CONTACTS = new String[]{"android.permission.READ_CONTACTS", "android.permission.READ_CALL_LOG"};
    private static PermissionManager sInstance = null;

    private PermissionManager() {
    }

    public static synchronized PermissionManager getInstance() {
        PermissionManager permissionManager;
        synchronized (PermissionManager.class) {
            if (sInstance == null) {
                sInstance = new PermissionManager();
            }
            permissionManager = sInstance;
        }
        return permissionManager;
    }

    public static List<String> getRequestPermissionList(Activity activity, String[] permissionList) {
        List<String> requestPermissionList = new ArrayList();
        for (String permission : permissionList) {
            if (!PermissionAdapter.hasPermission(activity, permission)) {
                requestPermissionList.add(permission);
            }
        }
        return requestPermissionList;
    }

    public static boolean checkHasPermissions(Activity activity, String[] permissionList) {
        return getRequestPermissionList(activity, permissionList).isEmpty();
    }

    public static boolean requestPermissionsIfNeed(Activity activity, String[] permissionList, int requestCode) {
        List<String> requestPermissionList = getRequestPermissionList(activity, permissionList);
        if (requestPermissionList.isEmpty()) {
            return false;
        }
        getInstance().requestPermissions(activity, (String[]) requestPermissionList.toArray(new String[requestPermissionList.size()]), requestCode);
        return true;
    }

    public synchronized void requestPermissions(Activity activity, String[] permissions, int requestCode) {
        PermissionAdapter.requestPermissions(activity, permissions, requestCode);
    }

    public static boolean isAllGranted(int[] grantResult) {
        for (int result : grantResult) {
            if (result != 0) {
                return false;
            }
        }
        return true;
    }

    public static String[] getPermissionsStorage() {
        return (String[]) PERMISSIONS_STORAGE.clone();
    }

    public static String[] getPermissionsLocation() {
        return (String[]) PERMISSIONS_LOCATION.clone();
    }

    public static String[] getPermissionCloudInviteByContacts() {
        return (String[]) PERMISSION_CLOUD_INVITE_BY_CONTACTS.clone();
    }

    public static boolean showTipsWithHwSystemManager(Activity activity, String action, String[] permissions, int requestCode) {
        if (activity == null || action == null || permissions == null) {
            return false;
        }
        Intent intent = new Intent(action);
        intent.setPackage("com.huawei.systemmanager");
        intent.putExtra("KEY_HW_PERMISSION_ARRAY", permissions);
        intent.putExtra("KEY_HW_PERMISSION_PKG", activity.getPackageName());
        try {
            activity.startActivityForResult(intent, requestCode);
            return true;
        } catch (Exception e) {
            GalleryLog.w("PermissionManager", "showTipsWithHwSystemManager Exception");
            return false;
        }
    }

    public static boolean showTipsGotoPermissionSettingsIfNeed(Activity activity, String[] permissions, int requestCode) {
        ArrayList<String> deniedPermissions = new ArrayList();
        for (String permission : permissions) {
            if (!(PermissionAdapter.hasPermission(activity, permission) || PermissionAdapter.shouldShowRequestPermissionRationale(activity, permission))) {
                deniedPermissions.add(permission);
            }
        }
        if (deniedPermissions.isEmpty()) {
            return false;
        }
        return showTipsWithHwSystemManager(activity, "huawei.intent.action.REQUEST_PERMISSIONS", (String[]) deniedPermissions.toArray(new String[deniedPermissions.size()]), requestCode);
    }
}
