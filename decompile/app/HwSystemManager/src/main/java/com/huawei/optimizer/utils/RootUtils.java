package com.huawei.optimizer.utils;

import android.content.Context;

public class RootUtils {
    private static final boolean DEBUG = false;
    private static final String TAG = "RootUtil";

    public static boolean isRooted(Context context) {
        return GlobalStateMgr.getInstance().isRooted();
    }

    public static boolean hasRootPermission() {
        return false;
    }

    public static boolean doInstallApp(String apkFilePath) {
        return false;
    }

    public static boolean doUninstallApp(String pkgName) {
        return false;
    }

    public static boolean clearCache(String pkgName) {
        return false;
    }
}
