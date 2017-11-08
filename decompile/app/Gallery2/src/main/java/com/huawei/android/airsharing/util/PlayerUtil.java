package com.huawei.android.airsharing.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import java.util.List;

public class PlayerUtil {
    public static boolean isSupportMultiscreen(Context context) {
        return hasAirSharingApp(context);
    }

    private static boolean hasAirSharingApp(Context mContext) {
        List<ApplicationInfo> packages = mContext.getPackageManager().getInstalledApplications(128);
        if (packages == null || packages.isEmpty()) {
            return false;
        }
        for (ApplicationInfo appInfo : packages) {
            if (appInfo != null && "com.huawei.android.airsharing".equals(appInfo.packageName)) {
                return true;
            }
        }
        return false;
    }
}
