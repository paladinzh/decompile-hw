package com.huawei.systemmanager.util.app;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import java.util.List;

public class PackageManagerWrapper {
    private static final int HSM_MATCH_DIRECT_BOOT = 786432;

    public static ResolveInfo resolveActivity(PackageManager pm, Intent intent, int flags) {
        return pm.resolveActivity(intent, HSM_MATCH_DIRECT_BOOT | flags);
    }

    public static List<ResolveInfo> queryIntentActivities(PackageManager pm, Intent intent, int flags) {
        return pm.queryIntentActivities(intent, HSM_MATCH_DIRECT_BOOT | flags);
    }

    public static List<ResolveInfo> queryBroadcastReceivers(PackageManager pm, Intent intent, int flags) {
        return pm.queryBroadcastReceivers(intent, HSM_MATCH_DIRECT_BOOT | flags);
    }

    public static List<PackageInfo> getInstalledPackages(PackageManager pm, int flags) {
        return pm.getInstalledPackages(HSM_MATCH_DIRECT_BOOT | flags);
    }

    public static PackageInfo getPackageInfo(PackageManager pm, String packageName, int flags) throws NameNotFoundException {
        return pm.getPackageInfo(packageName, HSM_MATCH_DIRECT_BOOT | flags);
    }
}
