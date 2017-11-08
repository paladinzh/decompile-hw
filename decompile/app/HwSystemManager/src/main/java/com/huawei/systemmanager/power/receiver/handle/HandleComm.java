package com.huawei.systemmanager.power.receiver.handle;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import com.huawei.systemmanager.comm.wrapper.SharePrefWrapper;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.PowerSavingMgr;
import com.huawei.systemmanager.power.comm.ActionConst;
import com.huawei.systemmanager.power.comm.ApplicationConstant;
import com.huawei.systemmanager.power.comm.SharedPrefKeyConst;
import com.huawei.systemmanager.power.notification.UserNotifier;
import com.huawei.systemmanager.power.service.BgPowerManagerService;
import com.huawei.systemmanager.power.util.SysCoreUtils;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;

public class HandleComm {
    private static final String POWER_CONSUME_ACTIVITY = "com.huawei.systemmanager.power.ui.BackgroundConsumeActivity";
    private static final String SUPER_HIGHPOWER_ACTIVITY = "com.huawei.systemmanager.power.ui.SuperHighPowerActivity";
    private static final String TAG = HandleComm.class.getSimpleName();

    public static void notifyConsume(Context ctx, ArrayList<String> pkgList, ArrayList<Integer> uidList) {
        boolean bTotalSwitchOn = SharePrefWrapper.getPrefValue(ctx, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.POWER_INTENSIVE_PRMOPT_SWITCH_KEY, true);
        if (SysCoreUtils.IS_ATT) {
            bTotalSwitchOn = SharePrefWrapper.getPrefValue(ctx, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.POWER_INTENSIVE_PRMOPT_SWITCH_KEY, false);
        }
        if (bTotalSwitchOn) {
            Intent saIntent = Intent.makeRestartActivityTask(new ComponentName(ctx.getPackageName(), POWER_CONSUME_ACTIVITY));
            saIntent.putExtra(HsmStatConst.KEY_NOTFICATION_EVENT, PowerSavingMgr.ACTION_CLICK_POWER_COST_NOTIFICATION);
            saIntent.putStringArrayListExtra(ApplicationConstant.USERNOTIFY_BUNDLE_NOTIFY_PKGNAME_LIST, pkgList);
            UserNotifier.sendNotification(ctx, pkgList, saIntent);
            startBgService(ctx, uidList);
        }
    }

    static void notifyConsumeForSuperHighPower(Context ctx, ArrayList<String> pkgList, ArrayList<Integer> uidList) {
        Intent saIntent;
        HwLog.i(TAG, "notifyConsumeForSuperHighPower");
        if (SharePrefWrapper.getPrefValue(ctx, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.SUPER_HIGH_POWER_SWITCH_KEY, false)) {
            saIntent = Intent.makeRestartActivityTask(new ComponentName(ctx.getPackageName(), SUPER_HIGHPOWER_ACTIVITY));
        } else {
            saIntent = Intent.makeRestartActivityTask(new ComponentName(ctx.getPackageName(), POWER_CONSUME_ACTIVITY));
        }
        saIntent.putExtra(HsmStatConst.KEY_NOTFICATION_EVENT, PowerSavingMgr.ACTION_CLICK_POWER_COST_NOTIFICATION);
        saIntent.putStringArrayListExtra(ApplicationConstant.USERNOTIFY_BUNDLE_NOTIFY_PKGNAME_LIST, pkgList);
        UserNotifier.sendNotificationForSuperHighPower(ctx, pkgList, saIntent);
        startBgService(ctx, uidList);
    }

    private static void startBgService(Context ctx, ArrayList<Integer> uidList) {
        Intent serviceIntent = new Intent(ActionConst.INNER_SERVICE_ACTION_NOTIFY_LIST);
        serviceIntent.setClass(ctx, BgPowerManagerService.class);
        Bundle bundle = new Bundle();
        bundle.putIntegerArrayList(ApplicationConstant.BGSERVICE_BUNDLE_NOTIFY_UID_LIST, uidList);
        serviceIntent.putExtras(bundle);
        ctx.startServiceAsUser(serviceIntent, UserHandle.CURRENT);
    }
}
