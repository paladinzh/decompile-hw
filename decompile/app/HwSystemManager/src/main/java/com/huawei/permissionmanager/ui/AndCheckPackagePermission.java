package com.huawei.permissionmanager.ui;

import android.content.pm.PackageManager;

/* compiled from: Permission */
class AndCheckPackagePermission implements CheckPackagePermissionInterface {
    AndCheckPackagePermission() {
    }

    public boolean isAppRequestPermission(String packageName, PackageManager packageManager, Permission permissionObject) {
        boolean result = true;
        for (String permissionName : permissionObject.getAndroidPermissionSet()) {
            result = result && packageManager.checkPermission(permissionName, packageName) == 0;
        }
        return result;
    }
}
