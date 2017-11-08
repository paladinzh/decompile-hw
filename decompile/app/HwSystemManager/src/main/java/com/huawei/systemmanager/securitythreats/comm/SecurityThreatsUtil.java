package com.huawei.systemmanager.securitythreats.comm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.systemmanager.securitythreats.ui.VirusNotifyService;
import com.huawei.systemmanager.util.HwLog;

public class SecurityThreatsUtil {
    private static final String TAG = "SecurityThreatsUtil";

    public static void notifyVirusByPushToUI(Context context, String pkg) {
        notifyVirusToUI(context, pkg, false, true, 2);
    }

    public static void notifyVirusToUI(Context context, String pkg, boolean needCallback, boolean needStat, int level) {
        HwLog.i(TAG, "notifyVirusToUI pkg=" + pkg + ", needCallback=" + needCallback + ", level=" + level);
        Intent service = new Intent(SecurityThreatsConst.ACTION_VIRUS_NOTIFY);
        service.setClass(context, VirusNotifyService.class);
        Bundle bundle = new Bundle();
        bundle.putString("package_name", pkg);
        bundle.putBoolean(SecurityThreatsConst.BUNDLE_KEY_NEED_CALLBACK, needCallback);
        bundle.putBoolean(SecurityThreatsConst.BUNDLE_KEY_NEED_STAT, needStat);
        bundle.putInt(SecurityThreatsConst.BUNDLE_KEY_VIRUS_LEVEL, level);
        service.putExtras(bundle);
        context.startService(service);
    }

    public static void notifyNewInstallVirusToService(Context context, String pkg, int level) {
        HwLog.i(TAG, "notifyNewInstallVirusToService pkg=" + pkg + ", level=" + level);
        Intent intent = new Intent(SecurityThreatsConst.ACTION_VIRUS_NEW_INSTALL);
        Bundle bundle = new Bundle();
        bundle.putString("package_name", pkg);
        bundle.putInt(SecurityThreatsConst.BUNDLE_KEY_VIRUS_LEVEL, level);
        intent.putExtras(bundle);
        context.sendBroadcast(intent, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
    }

    public static void notifyFinishToService(Context context, String pkg) {
        HwLog.i(TAG, "notifyFinishToService pkg=" + pkg);
        Intent intent = new Intent(SecurityThreatsConst.ACTION_VIRUS_NOTIFY_FINISH);
        Bundle bundle = new Bundle();
        bundle.putString("package_name", pkg);
        intent.putExtras(bundle);
        context.sendBroadcast(intent, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
    }
}
