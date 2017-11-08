package com.huawei.optimizer.utils;

import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import com.huawei.systemmanager.util.HwLog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PackageCompat {
    private static final String TAG = "PackageCompat";
    private static Method sGetPackageSizeInfoMethod;

    static {
        try {
            sGetPackageSizeInfoMethod = PackageManager.class.getMethod("getPackageSizeInfo", new Class[]{String.class, IPackageStatsObserver.class});
            HwLog.d(TAG, "==== good, it works");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static boolean packageManager_getPackageSizeInfo(PackageManager obj, String packageName, IPackageStatsObserver observer) {
        if (sGetPackageSizeInfoMethod != null) {
            try {
                sGetPackageSizeInfoMethod.invoke(obj, new Object[]{packageName, observer});
                return true;
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e2) {
            }
        }
        HwLog.e(TAG, "packageManager_getPackageSizeInfo failure");
        return false;
    }
}
