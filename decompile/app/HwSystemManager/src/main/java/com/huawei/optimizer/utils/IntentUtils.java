package com.huawei.optimizer.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.util.List;

public class IntentUtils {
    public static boolean isActivityAvailable(Context cxt, Intent intent) {
        List<ResolveInfo> list = PackageManagerWrapper.queryIntentActivities(cxt.getPackageManager(), intent, 0);
        if (list == null || list.size() <= 0) {
            return false;
        }
        return true;
    }

    public static boolean hasLauncherEntry(Context cxt, String pkgName) {
        return cxt.getPackageManager().getLaunchIntentForPackage(pkgName) != null;
    }

    public static List<ResolveInfo> getActivityInfo(Context cxt, Intent intent) {
        return PackageManagerWrapper.queryIntentActivities(cxt.getPackageManager(), intent, 0);
    }
}
