package com.huawei.permissionmanager.ui;

import android.content.pm.PackageManager;

/* compiled from: Permission */
class OrCheckPackagePermission implements CheckPackagePermissionInterface {
    OrCheckPackagePermission() {
    }

    public boolean isAppRequestPermission(String packageName, PackageManager pakcageManager, Permission permissionObject) {
        boolean result = false;
        for (String permissionName : permissionObject.getAndroidPermissionSet()) {
            result = result || pakcageManager.checkPermission(permissionName, packageName) == 0;
        }
        return result;
    }
}
