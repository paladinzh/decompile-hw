package com.huawei.systemmanager.antimal;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import com.huawei.systemmanager.antimal.ui.AntimalwareNotification;
import com.huawei.systemmanager.comm.wrapper.SharePrefWrapper;
import com.huawei.systemmanager.customize.AbroadUtils;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TreeMap;

public class AntiMalUtils {
    private static final int ONE_DAY_TO_MILLISECOND = 86400000;
    private static final String TAG = "AntiMalUtils";
    private static final int VALIDITY_PERIOD_YEARS = 5;
    private static AntimalwareNotification antimalwareNotification;

    public static boolean isNowLauncher(Context context, String pkgName) {
        if (context == null || TextUtils.isEmpty(pkgName)) {
            HwLog.e(TAG, "param is null.");
            return false;
        }
        ResolveInfo res = context.getPackageManager().resolveActivity(getMainIntent(), 0);
        if (res == null || res.activityInfo == null) {
            HwLog.e(TAG, "isDefaultLauncher param is null.");
            return false;
        } else if (pkgName.equals(res.activityInfo.packageName)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isAlerted(Context context) {
        return SharePrefWrapper.getPrefValue(context, MalwareConst.ANTIMAL_ALERT_RESULT, MalwareConst.ALERT_RESULT, false);
    }

    public static boolean isDone(Context context) {
        return SharePrefWrapper.getPrefValue(context, MalwareConst.ANTIMAL_ALERT_FILE, MalwareConst.ANTIMAL_DONE, false);
    }

    public static void setDone(Context context) {
        SharePrefWrapper.setPrefValue(context, MalwareConst.ANTIMAL_ALERT_FILE, MalwareConst.ANTIMAL_DONE, true);
    }

    public static void setDefaultLauncher(Context context) {
        if (context != null) {
            boolean isFromLauncher = isLauncherOnForeground(context);
            HwLog.i(TAG, "isFromLauncher = " + isFromLauncher);
            PackageManager mPackageManager = context.getPackageManager();
            if (mPackageManager != null) {
                List<ResolveInfo> resolveInfos = mPackageManager.queryIntentActivities(getMainIntent(), 0);
                if (resolveInfos != null) {
                    int i;
                    int sz = resolveInfos.size();
                    for (i = 0; i < sz; i++) {
                        ResolveInfo resolveInfo = (ResolveInfo) resolveInfos.get(i);
                        if (resolveInfo != null) {
                            mPackageManager.clearPackagePreferredActivities(resolveInfo.activityInfo.packageName);
                        }
                    }
                    int find = -1;
                    ComponentName[] set = new ComponentName[sz];
                    for (i = 0; i < sz; i++) {
                        ResolveInfo info = (ResolveInfo) resolveInfos.get(i);
                        set[i] = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
                        if (info.activityInfo.packageName.equals("com.huawei.android.launcher")) {
                            find = i;
                            break;
                        }
                    }
                    if (find != -1) {
                        IntentFilter inf = new IntentFilter("android.intent.action.MAIN");
                        inf.addCategory("android.intent.category.HOME");
                        inf.addCategory("android.intent.category.DEFAULT");
                        mPackageManager.addPreferredActivity(inf, 1048576, set, set[find]);
                    }
                    if (isFromLauncher) {
                        context.startActivity(getMainIntent());
                    }
                }
            }
        }
    }

    public static String getUnusedAppName(Context ctx, List<String> malAppList) {
        String unUsedAppName = "";
        if (ctx == null || malAppList == null) {
            return unUsedAppName;
        }
        unUsedAppName = (String) malAppList.get(0);
        TreeMap<Integer, String> appMap = new TreeMap();
        try {
            long curTime = System.currentTimeMillis();
            Calendar cal = Calendar.getInstance();
            cal.add(1, -5);
            for (UsageStats unUsedApp : ((UsageStatsManager) ctx.getSystemService("usagestats")).queryUsageStats(4, cal.getTimeInMillis(), curTime)) {
                String pkgName = unUsedApp.getPackageName();
                if (malAppList.contains(pkgName)) {
                    appMap.put(Integer.valueOf((int) ((curTime - unUsedApp.getLastTimeUsed()) / 86400000)), pkgName);
                }
            }
            if (!appMap.isEmpty()) {
                unUsedAppName = (String) appMap.lastEntry().getValue();
            }
        } catch (Exception e) {
            HwLog.e(TAG, "get unUsedapp error!");
        }
        HwLog.i(TAG, "unUsedApp = " + unUsedAppName);
        return unUsedAppName;
    }

    public static List<String> getLauncherApps(Context ctx, List<String> malAppList) {
        List<String> list = new ArrayList();
        if (ctx == null || malAppList == null) {
            return list;
        }
        List<ResolveInfo> resolveInfos = ctx.getPackageManager().queryIntentActivities(getMainIntent(), 0);
        if (resolveInfos == null) {
            return list;
        }
        int size = resolveInfos.size();
        for (int i = 0; i < size; i++) {
            ResolveInfo resolveInfo = (ResolveInfo) resolveInfos.get(i);
            if (resolveInfo != null) {
                list.add(resolveInfo.activityInfo.packageName);
            }
        }
        list.retainAll(malAppList);
        return list;
    }

    private static Intent getMainIntent() {
        Intent mainIntent = new Intent("android.intent.action.MAIN");
        mainIntent.addCategory("android.intent.category.HOME");
        mainIntent.addCategory("android.intent.category.DEFAULT");
        return mainIntent;
    }

    private static boolean shouldAlert(Context context) {
        if (context == null) {
            return false;
        }
        long nowTime = System.currentTimeMillis();
        if (nowTime - SharePrefWrapper.getPrefValue(context, MalwareConst.ANTIMAL_ALERT_FILE, MalwareConst.ANTIMAL_START_TIMES, 0) >= 604800000) {
            SharePrefWrapper.setPrefValue(context, MalwareConst.ANTIMAL_ALERT_FILE, MalwareConst.ANTIMAL_START_TIMES, nowTime);
            SharePrefWrapper.setPrefValue(context, MalwareConst.ANTIMAL_ALERT_FILE, MalwareConst.ANTIMAL_ALERT_TIMES, 0);
            HwLog.i(TAG, "is aready 7 days,recyle new time");
        }
        int alertTimesCount = SharePrefWrapper.getPrefValue(context, MalwareConst.ANTIMAL_ALERT_FILE, MalwareConst.ANTIMAL_ALERT_TIMES, 0);
        long lastAlertTime = SharePrefWrapper.getPrefValue(context, MalwareConst.ANTIMAL_ALERT_FILE, MalwareConst.ANTIMAL_LAST_ALERT_TIME, 0);
        HwLog.i(TAG, "alertTimesCount = " + alertTimesCount + ";lastAlertTime = " + lastAlertTime);
        if (alertTimesCount >= 3 || nowTime - lastAlertTime < 43200000) {
            return false;
        }
        HwLog.i(TAG, "go to sendNotification");
        SharePrefWrapper.setPrefValue(context, MalwareConst.ANTIMAL_ALERT_FILE, MalwareConst.ANTIMAL_ALERT_TIMES, alertTimesCount + 1);
        SharePrefWrapper.setPrefValue(context, MalwareConst.ANTIMAL_ALERT_FILE, MalwareConst.ANTIMAL_LAST_ALERT_TIME, nowTime);
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void sendNotification(Context context) {
        synchronized (AntiMalUtils.class) {
            if (context != null) {
                if (isLauncherAntimalEnabled(context)) {
                    if (shouldAlert(context)) {
                        antimalwareNotification = getAntimalNotify();
                        antimalwareNotification.showRestoreNotification(context);
                        if (SharePrefWrapper.getPrefValue(context, MalwareConst.ANTIMAL_ALERT_FILE, "isFirst", true)) {
                            String statParm = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_RESET, "0");
                            HsmStat.statE((int) Events.E_AMTIMAL_RESTORE_LAUNCHER, statParm);
                            SharePrefWrapper.setPrefValue(context, MalwareConst.ANTIMAL_ALERT_FILE, "isFirst", false);
                        }
                    }
                }
            }
        }
    }

    public static AntimalwareNotification getAntimalNotify() {
        if (antimalwareNotification == null) {
            antimalwareNotification = new AntimalwareNotification();
        }
        return antimalwareNotification;
    }

    public static boolean isLauncherAntimalEnabled(Context context) {
        if (AbroadUtils.isAbroad() || !isAlerted(context) || isDone(context) || isNowLauncher(context, "com.huawei.android.launcher")) {
            return false;
        }
        return true;
    }

    private static boolean isLauncherOnForeground(Context context) {
        ResolveInfo res = context.getPackageManager().resolveActivity(getMainIntent(), 0);
        if (res == null || res.activityInfo == null) {
            return false;
        }
        List<RunningTaskInfo> tasks = ((ActivityManager) context.getSystemService("activity")).getRunningTasks(1);
        return !tasks.isEmpty() && ((RunningTaskInfo) tasks.get(0)).topActivity.getPackageName().equals(res.activityInfo.packageName);
    }
}
