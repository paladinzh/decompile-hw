package com.huawei.systemmanager.power.receiver.handle;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import com.huawei.systemmanager.comm.wrapper.SharePrefWrapper;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.power.comm.ActionConst;
import com.huawei.systemmanager.power.comm.ApplicationConstant;
import com.huawei.systemmanager.power.comm.SharedPrefKeyConst;
import com.huawei.systemmanager.power.model.PowerModeControl;
import com.huawei.systemmanager.power.service.SuperDialogShowService;
import com.huawei.systemmanager.power.util.PowerNotificationUtils;
import com.huawei.systemmanager.spacecleanner.utils.AppCleanUpAndStorageNotifyUtils;
import com.huawei.systemmanager.util.HwLog;

public class HandlePowerModeNotifier implements IBroadcastHandler {
    private static final String TAG = "HandlePowerModeNotifier";

    public void handleBroadcast(Context ctx, Intent intent) {
        String action = intent.getAction();
        if (ActionConst.INTENT_SVAE_MODE_NOTIFY.equals(action)) {
            AppCleanUpAndStorageNotifyUtils.collapseStatusBar(ctx);
            int level = intent.getIntExtra(ApplicationConstant.POWER_NOTIFICATION_RAWLEVEL, 20);
            int notificationType = intent.getIntExtra(ApplicationConstant.POWER_NOTIFICATION_TYPE, 1);
            Intent serviceIntent;
            Bundle bundle;
            if (notificationType == 1) {
                HsmStat.statE(Events.E_POWER_SVAEMODE_NOTIFICATION_BUTTON_ENTER);
                if (SharePrefWrapper.getPrefValue(ctx, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.POWER_SAVE_MODE_DIALOG_REMIND_KEY, String.valueOf(0)).equals("0")) {
                    HwLog.i(TAG, "Power Save Mode Remind Dialog need show.");
                    serviceIntent = new Intent(ctx, SuperDialogShowService.class);
                    bundle = new Bundle();
                    bundle.putString(ApplicationConstant.SUPER_DIALOG_LABEL, ApplicationConstant.POWER_SAVE_MODE_DIALOG);
                    serviceIntent.putExtras(bundle);
                    ctx.startServiceAsUser(serviceIntent, UserHandle.CURRENT);
                } else {
                    HwLog.i(TAG, "PowerModelStateChange, open save mode from saveMode notification.");
                    PowerModeControl.getInstance(ctx).changePowerMode(4);
                    PowerNotificationUtils.showPowerModeQuitNotification(ctx);
                    String statParam2 = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, "1");
                    HsmStat.statE((int) Events.E_POWER_POWERMODE_SELECT, statParam2);
                }
                PowerNotificationUtils.cancleLowBatterySaveModeNotification(ctx);
            } else if (notificationType == 2) {
                HsmStat.statE(Events.E_POWER_SUPERSVAEMODE_NOTIFICATION_BUTTON_ENTER);
                HwLog.i(TAG, "Super Power Save Mode Remind Dialog need show.");
                serviceIntent = new Intent(ctx, SuperDialogShowService.class);
                bundle = new Bundle();
                bundle.putInt(ApplicationConstant.SUPER_DIALOG_RAWLEVEL, level);
                bundle.putString(ApplicationConstant.SUPER_DIALOG_LABEL, ApplicationConstant.LOW_BATTERY_SUPER_DIALOG);
                serviceIntent.putExtras(bundle);
                ctx.startServiceAsUser(serviceIntent, UserHandle.CURRENT);
                PowerNotificationUtils.cancleLowBatterySuperModeNotification(ctx);
            }
        } else if (ActionConst.INTENT_SAVE_MODE_CLOSE_NOTIFY.equals(action)) {
            HsmStat.statE(Events.E_POWER_SVAEMODE_NOTIFICATION_BUTTON_QUIT);
            HwLog.i(TAG, "PowerModelStateChange, close save mode from saveMode quit notification.");
            PowerModeControl.getInstance(ctx).changePowerMode(1);
            PowerNotificationUtils.canclePowerModeOpenNotification(ctx);
            String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, "0");
            HsmStat.statE((int) Events.E_POWER_POWERMODE_SELECT, statParam);
        } else if (ActionConst.INTENT_SUPER_SAVE_MODE_DELETE_NOTIFY.equals(action)) {
            PowerNotificationUtils.cancleLowBatterySuperModeNotification(ctx);
        } else if (ActionConst.INTENT_SAVE_MODE_DELETE_NOTIFY.equals(action)) {
            PowerNotificationUtils.cancleLowBatterySaveModeNotification(ctx);
        }
    }
}
