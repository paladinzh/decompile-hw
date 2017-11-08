package com.huawei.permissionmanager.ui;

import android.content.pm.PackageManager;

public interface CheckPackagePermissionInterface {
    boolean isAppRequestPermission(String str, PackageManager packageManager, Permission permission);
}
