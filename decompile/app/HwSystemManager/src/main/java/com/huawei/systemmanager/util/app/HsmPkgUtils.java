package com.huawei.systemmanager.util.app;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import com.huawei.systemmanager.util.HwLog;

public class HsmPkgUtils {
    private static final String TAG = "HsmPkgUtils";

    public static int getPackageUid(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            HwLog.w(TAG, "getPackageUid : Invalid package name");
            return -1;
        }
        try {
            return HsmPackageManager.getInstance().getPkgInfo(packageName, 0).mUid;
        } catch (NameNotFoundException e) {
            return -1;
        }
    }

    public static String getLableFromPm(Context context, String pkgName) {
        if (context == null || pkgName == null) {
            HwLog.e(TAG, "getLableFromPm,but context or pkgname is null.");
            return pkgName;
        }
        try {
            PackageManager pm = context.getPackageManager();
            return pm.getApplicationInfo(pkgName, 8192).loadLabel(pm).toString().trim();
        } catch (NameNotFoundException e) {
            HwLog.w(TAG, "can't get application info:" + pkgName, e);
            return pkgName;
        } catch (Exception e2) {
            HwLog.w(TAG, "can't get application info:" + pkgName, e2);
            return pkgName;
        }
    }
}
