package com.huawei.systemmanager.startupmgr.comm;

import android.app.ActivityManager;
import android.content.Context;
import com.huawei.systemmanager.comm.misc.Utility;

public class SysCallUtils {
    public static final String ANDROID_PACKAGE_NAME = "android";
    private static final long SYSTEM_UID = 1000;

    public static String getPackageByPidUid(Context ctx, int pid, int uid) {
        if (((long) uid) == 1000) {
            return "android";
        }
        String[] pkgs = ctx.getPackageManager().getPackagesForUid(uid);
        if (pkgs == null || pkgs.length <= 0) {
            return null;
        }
        return pkgs[0];
    }

    public static void forceStopForbidStartupPackage(Context ctx, String pkgName) {
        ((ActivityManager) ctx.getSystemService("activity")).forceStopPackage(pkgName);
    }

    public static boolean checkUser() {
        if (Utility.isOwnerUser(false)) {
            return true;
        }
        return false;
    }
}
