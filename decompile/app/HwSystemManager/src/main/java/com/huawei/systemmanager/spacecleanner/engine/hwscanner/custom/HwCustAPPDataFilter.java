package com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom;

import android.content.pm.PackageInfo;
import android.text.TextUtils;
import com.huawei.systemmanager.filterrule.util.BaseSignatures;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPkgInfo;

public class HwCustAPPDataFilter {
    public static final String PATH_SYSTEM_CACHE_ANDROID = "Android/data/";
    public static final String PATH_SYSTEM_CACHE_DATA = "data/data/";
    private static final String TAG = "HwCustAPPTrashFilter";

    public static boolean isProtectPathValid(String pkg, String path) {
        return true;
    }

    public static boolean isTrashPathValid(String pkg, String path) {
        return (isTrashContainOthersPkg(pkg, path) || isTrashContainSystemPath(path)) ? false : true;
    }

    private static boolean isTrashContainOthersPkg(String pkg, String path) {
        boolean result = false;
        if (TextUtils.isEmpty(pkg) || TextUtils.isEmpty(path)) {
            HwLog.e(TAG, "pkg or path is null");
        } else if (path.contains(PATH_SYSTEM_CACHE_DATA)) {
            result = !path.contains(pkg);
            if (!result) {
                HwLog.e(TAG, pkg + " contains other app's data");
            }
        }
        return result;
    }

    private static boolean isTrashContainSystemPath(String path) {
        return false;
    }

    public static boolean isHwApp(PackageInfo pi) {
        boolean hwSign = BaseSignatures.getInstance().contains(HsmPkgInfo.getSignaturesCode(pi));
        HwLog.i(TAG, "this new installed app has huawei signatures?" + hwSign);
        boolean sysApp = (pi.applicationInfo.flags & 1) != 0;
        HwLog.i(TAG, "this new installed app is system app?" + sysApp);
        return !hwSign ? sysApp : true;
    }
}
