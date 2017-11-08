package com.huawei.permissionmanager.ui;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import com.huawei.permissionmanager.db.AppInitializer;
import com.huawei.permissionmanager.db.DBAdapter;
import com.huawei.permissionmanager.db.PermissionDbVisitor;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.comm.grule.GRuleManager;
import com.huawei.systemmanager.comm.grule.scene.monitor.MonitorScenario;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.content.HsmBroadcastReceiver;

public class AppInstallationReceiver extends HsmBroadcastReceiver {
    private static final String LOG_TAG = "AppInstallationReceiver";

    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            HwLog.w(LOG_TAG, "onReceive : Invalid context or intent");
            return;
        }
        String action = intent.getAction();
        if (action == null) {
            HwLog.w(LOG_TAG, "onReceive : Invalid intent action");
            return;
        }
        HwLog.i(LOG_TAG, "AppInstallationReceiver onReceive action = " + action);
        sendToBackground(context.getApplicationContext(), intent);
    }

    public void doInBackground(Context context, Intent intent) {
        super.doInBackground(context, intent);
        if (intent.getData() != null) {
            if ("android.intent.action.PACKAGE_ADDED".equals(intent.getAction())) {
                if (!shouldIgnoreIntent(context, intent)) {
                    handlePackageAdd(context, intent);
                } else {
                    return;
                }
            } else if ("android.intent.action.PACKAGE_REMOVED".equals(intent.getAction())) {
                if (!isReplaceOperation(intent)) {
                    handlePackageRemove(context, intent);
                    PermissionDbVisitor.removeHistoryRecord(context, intent.getData().getSchemeSpecificPart());
                } else {
                    return;
                }
            } else if ("android.intent.action.PACKAGE_REPLACED".equals(intent.getAction()) || "android.intent.action.PACKAGE_CHANGED".equals(intent.getAction())) {
                handlePackageReplaced(context, intent);
            } else {
                return;
            }
            afterHandleAction(context, intent);
        }
    }

    boolean isReplaceOperation(Intent intent) {
        return intent.getBooleanExtra("android.intent.extra.REPLACING", false);
    }

    boolean shouldIgnoreIntent(Context context, Intent intent) {
        return !GRuleManager.getInstance().shouldMonitor(context, MonitorScenario.SCENARIO_PERMISSION, intent.getData().getSchemeSpecificPart());
    }

    private void handlePackageAdd(Context context, Intent intent) {
        String pkgName = intent.getData().getSchemeSpecificPart();
        if (DBAdapter.permissionConfigExistInDb(context, pkgName)) {
            HwLog.w(LOG_TAG, "The installed app has already configured. ignore " + pkgName);
        } else {
            AppInitializer.initilizeNewAppAndSyncToSys(context, pkgName, intent.getIntExtra("android.intent.extra.UID", -1), "new installed");
        }
    }

    private void handlePackageRemove(Context context, Intent intent) {
        DBAdapter.getInstance(context).deleteRecord(context, intent.getData().getSchemeSpecificPart());
    }

    private void handlePackageReplaced(Context context, Intent intent) {
        AppInitializer.updateReplaceApp(context, intent.getData().getSchemeSpecificPart(), intent.getIntExtra("android.intent.extra.UID", -1));
    }

    private void afterHandleAction(Context context, Intent intent) {
        Intent packageChangeIntent = new Intent(ShareCfg.PACKAGE_INSTALLATION_UNINSTALLATION);
        packageChangeIntent.setPackage(context.getPackageName());
        packageChangeIntent.putExtra("install_type", intent.getAction());
        packageChangeIntent.putExtra("pkgName", intent.getData().getSchemeSpecificPart());
        context.sendBroadcastAsUser(packageChangeIntent, UserHandle.CURRENT, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
        HsmPackageManager.getInstance().onReceive(context, intent);
    }
}
