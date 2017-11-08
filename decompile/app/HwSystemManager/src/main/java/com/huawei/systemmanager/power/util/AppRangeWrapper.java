package com.huawei.systemmanager.power.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.SystemProperties;
import android.support.v4.view.PointerIconCompat;
import com.google.android.collect.Sets;
import com.huawei.systemmanager.optimize.process.ProtectAppControl;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.util.List;
import java.util.Set;

public class AppRangeWrapper {
    private static final String TAG = AppRangeWrapper.class.getSimpleName();

    public static Set<Integer> getAppThirdUidSet(Context ctx) {
        boolean showSysApp;
        Set<Integer> thirdUids = Sets.newHashSet();
        if (SystemProperties.get("persist.sys.systemapp.enable", "0").equals("1")) {
            showSysApp = true;
        } else {
            showSysApp = false;
        }
        List<PackageInfo> packageInfos = PackageManagerWrapper.getInstalledPackages(ctx.getPackageManager(), 8192);
        List<String> protectedAppList = ProtectAppControl.getInstance(ctx).getAllControlledAppFromDb();
        HwLog.d(TAG, "protectedAppList = " + protectedAppList);
        int googlePlayUid = SysCoreUtils.getGoogleSharedUid(ctx);
        for (PackageInfo rapp : packageInfos) {
            addThirdUid(ctx, rapp.packageName, thirdUids, showSysApp, googlePlayUid, protectedAppList);
        }
        return thirdUids;
    }

    public static Set<Integer> getRunningThirdUidSet(Context ctx) {
        boolean showSysApp;
        Set<Integer> thirdUids = Sets.newHashSet();
        ActivityManager am = (ActivityManager) ctx.getSystemService("activity");
        if (SystemProperties.get("persist.sys.systemapp.enable", "0").equals("1")) {
            showSysApp = true;
        } else {
            showSysApp = false;
        }
        List<RunningAppProcessInfo> listAppcations = am.getRunningAppProcesses();
        List<String> protectedAppList = ProtectAppControl.getInstance(ctx).getAllControlledAppFromDb();
        HwLog.d(TAG, "protectedAppList = " + protectedAppList);
        int googlePlayUid = SysCoreUtils.getGoogleSharedUid(ctx);
        int currentUserId = ActivityManager.getCurrentUser();
        for (RunningAppProcessInfo rapp : listAppcations) {
            if (!SpecialAppUid.isOtherUserUid(SpecialAppUid.collapseUidsTogether(rapp.uid, currentUserId))) {
                String[] pkgList = rapp.pkgList;
                for (String pkgName : pkgList) {
                    addThirdUid(ctx, pkgName, thirdUids, showSysApp, googlePlayUid, protectedAppList);
                }
            }
        }
        return thirdUids;
    }

    protected static void addThirdUid(Context ctx, String pkgName, Set<Integer> thirdUids, boolean showSysApp, int googlePlayUid, List<String> protectedAppList) {
        if ("com.huawei.systemmanager".equals(pkgName)) {
            HwLog.d(TAG, "This app is systemmanager!");
        } else if (thirdUids != null && protectedAppList != null) {
            try {
                ApplicationInfo app = SysCoreUtils.getAppInfoByPackageName(ctx, pkgName);
                if (app == null || thirdUids.contains(Integer.valueOf(app.uid)) || (-1 != googlePlayUid && app.uid == googlePlayUid)) {
                    HwLog.d(TAG, "it is googleUid apk!");
                    return;
                }
                if (showSysApp) {
                    if (app.uid != 0 && app.uid != 1000 && app.uid != PointerIconCompat.TYPE_ALL_SCROLL) {
                        thirdUids.add(Integer.valueOf(app.uid));
                    }
                } else if (protectedAppList.contains(pkgName) && app.uid >= 10000) {
                    thirdUids.add(Integer.valueOf(app.uid));
                }
            } catch (RuntimeException e) {
                HwLog.e(TAG, "addThirdUid" + e.toString());
            }
        }
    }
}
