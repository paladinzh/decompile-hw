package com.huawei.systemmanager.applock.utils.sp;

import android.content.Context;
import com.huawei.systemmanager.applock.utils.DatabaseSharePrefUtil;

public class LockingPackageUtils {
    private static final String UNLOCK_PACKAGE_NAME_KEY = "unlock_package_name";

    public static String getLockingPackageName(Context context, String dftPackage) {
        return DatabaseSharePrefUtil.getPref(context, UNLOCK_PACKAGE_NAME_KEY, dftPackage, false);
    }

    public static void setLockingPackageName(Context context, String packageName) {
        DatabaseSharePrefUtil.setPref(context, UNLOCK_PACKAGE_NAME_KEY, packageName, false);
    }
}
