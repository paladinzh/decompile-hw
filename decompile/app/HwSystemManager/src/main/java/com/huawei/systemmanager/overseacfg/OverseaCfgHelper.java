package com.huawei.systemmanager.overseacfg;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import com.huawei.systemmanager.customize.CustomizeManager;
import com.huawei.systemmanager.customize.CustomizeWrapper;
import com.huawei.systemmanager.customize.FearureConfigration;
import com.huawei.systemmanager.customize.OverseaCfgConst;
import com.huawei.systemmanager.startupmgr.comm.StartupBinderAccess;
import com.huawei.systemmanager.util.HwLog;

public class OverseaCfgHelper {
    private static final String LOG_TAG = "OverseaCfgHelper";

    public static void setOverseaSwitchChange(Context context, Intent intent) {
        if (intent.getBooleanExtra(OverseaCfgConst.OVERSEA_SWITCH_MODULE_NAME, false)) {
            int activateResult = setOverseaBootStart(context);
            if (1 == activateResult) {
                Intent overseaIntent = new Intent(OverseaCfgConst.OVERSEA_SWITCH_CHANGE_ACTION);
                overseaIntent.setPackage(context.getPackageName());
                context.sendBroadcastAsUser(overseaIntent, UserHandle.OWNER);
            }
            sendPermissionSettingBroadcast(context, activateResult);
            return;
        }
        sendPermissionSettingBroadcast(context, 0);
    }

    private static int setOverseaBootStart(Context context) {
        if (CustomizeManager.getInstance().isFeatureEnabled(3)) {
            HwLog.d(LOG_TAG, "Not oversea version, so no need open permissionManager! ");
            return 2;
        } else if (CustomizeWrapper.isOverseaBootstartEnabled(context)) {
            HwLog.d(LOG_TAG, "The oversea permissionManager is already open! ");
            return 3;
        } else {
            try {
                setInstalledAppsBootstartup();
                setSystemProviders(context);
                return 1;
            } catch (Exception e) {
                e.printStackTrace();
                HwLog.e(LOG_TAG, "setOverseaPermission update db error!");
                return 0;
            }
        }
    }

    private static void setSystemProviders(Context context) {
        System.putInt(context.getContentResolver(), FearureConfigration.BOOT_STARTUP_MANAGER, 1);
        Secure.putInt(context.getContentResolver(), OverseaCfgConst.SETTINGS_SECURITY_OVERSEA_STATUS, 1);
        Secure.putInt(context.getContentResolver(), OverseaCfgConst.SETTINGS_SECURITY_BOOTSTARTUP_STATUS, 1);
    }

    private static void sendPermissionSettingBroadcast(Context context, int openResult) {
        Intent intent = new Intent(OverseaCfgConst.OVERSEA_SWITCH_OPEN_RESULT_ACTION);
        intent.setPackage(OverseaCfgConst.OVERSEA_SWITCH_PACKAGE_NAME);
        intent.putExtra(OverseaCfgConst.OVERSEA_SWITCH_OPEN_RESULT_TYPE, openResult);
        context.sendBroadcastAsUser(intent, UserHandle.OWNER);
    }

    private static void setInstalledAppsBootstartup() {
        StartupBinderAccess.setAutoStartupPolicyEnabled(true);
    }
}
