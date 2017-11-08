package com.huawei.systemmanager.optimize;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.os.SystemClock;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.reflect.ActivityManagerReflect;
import com.huawei.systemmanager.optimize.process.ProcessFilterPolicy;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class ProcessManager {
    public static final int MAX_TASKS_NUM = 21;
    public static final String TAG = "ProcessManager";

    public static void clearPackages(List<String> packageList) {
        if (HsmCollections.isEmpty(packageList)) {
            HwLog.w(TAG, "clearPackages called, but packageList is null");
            return;
        }
        HwLog.i(TAG, "clearPackages called," + packageList);
        Context ctx = GlobalContext.getContext();
        forcestopApp(ctx, packageList);
        removePackagesFromRecentList(ctx, packageList);
    }

    private static void removePackagesFromRecentList(Context ctx, List<String> pacakgeNeedRemove) {
        ActivityManager am = (ActivityManager) ctx.getSystemService("activity");
        List<RecentTaskInfo> recentTasks = am.getRecentTasks(21, 2);
        if (recentTasks == null || recentTasks.size() <= 0) {
            HwLog.w(TAG, "no recent task, could not remove packages from recent list.");
            return;
        }
        for (RecentTaskInfo task : recentTasks) {
            ComponentName cpn = task.baseIntent.getComponent();
            if (cpn != null && pacakgeNeedRemove.contains(cpn.getPackageName())) {
                ActivityManagerReflect.setRemoveTask(am, task.persistentId, 1);
            }
        }
    }

    public static int trimApps(Context ctx, List<String> pkgList, List<Integer> useridList) {
        if (pkgList == null || pkgList.isEmpty() || useridList == null || useridList.isEmpty()) {
            HwLog.w(TAG, "trimApps, pkgList or uidList is empty");
            return 0;
        }
        HwLog.i(TAG, "trimApps, before filter pkgList:" + pkgList.toString());
        List<String> apps = ProcessFilterPolicy.conver2ListPkg(ProcessFilterPolicy.getpkgsAfterfilter(ctx, pkgList));
        HwLog.i(TAG, "trimApps, after filter pkgList:" + apps.toString());
        for (int i = 0; i < pkgList.size(); i++) {
            if (!apps.contains(pkgList.get(i))) {
                pkgList.remove(i);
                useridList.remove(i);
            }
        }
        return forcestopApp(ctx, pkgList, useridList);
    }

    private static int forcestopApp(Context context, List<String> pkgs) {
        if (pkgs == null || pkgs.isEmpty()) {
            HwLog.e(TAG, "forcestopApp pkgs is empty!!");
            return 0;
        }
        HwLog.i(TAG, "forcestopApp begin!! pkgs = " + pkgs);
        long start = SystemClock.elapsedRealtime();
        ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
        for (int i = 0; i < pkgs.size(); i++) {
            HwLog.i(TAG, "forcestopApp pkgName= " + ((String) pkgs.get(i)));
            activityManager.forceStopPackage((String) pkgs.get(i));
        }
        long end = SystemClock.elapsedRealtime();
        int size = pkgs.size();
        HwLog.i(TAG, "forcestopApp end, cost time:" + (end - start) + " ,size = " + size);
        return size;
    }

    private static int forcestopApp(Context context, List<String> pkgs, List<Integer> userids) {
        if (pkgs == null || pkgs.isEmpty()) {
            HwLog.e(TAG, "forcestopApp pkgs is empty!!");
            return 0;
        } else if (userids == null || userids.isEmpty() || userids.size() != pkgs.size()) {
            HwLog.e(TAG, "forcestopApp uids is invalid!!");
            return 0;
        } else {
            HwLog.i(TAG, "forcestopApp begin!! pkgs = " + pkgs);
            long start = SystemClock.elapsedRealtime();
            ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
            for (int i = 0; i < pkgs.size(); i++) {
                HwLog.i(TAG, "forcestopApp pkgName= " + ((String) pkgs.get(i)) + " userid = " + userids.get(i));
                activityManager.forceStopPackageAsUser((String) pkgs.get(i), ((Integer) userids.get(i)).intValue());
            }
            long end = SystemClock.elapsedRealtime();
            int size = pkgs.size();
            HwLog.i(TAG, "forcestopApp end, cost time:" + (end - start) + " ,size = " + size);
            return size;
        }
    }
}
