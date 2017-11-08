package com.huawei.systemmanager.antimal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.huawei.systemmanager.antimal.ui.AntimalwareNotification;
import com.huawei.systemmanager.comm.wrapper.SharePrefWrapper;
import com.huawei.systemmanager.customize.AbroadUtils;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.NotificationID;

public class AntiMalBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "AntiMalBroadcastReceiver";
    private AntimalwareNotification notification;

    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            HwLog.e(TAG, "Invalid context or intent!");
        } else if (!AbroadUtils.isAbroad() && !AntiMalUtils.isDone(context)) {
            handleAntiMalEvent(context, intent);
        }
    }

    private void handleAntiMalEvent(Context ctx, Intent intent) {
        String action = intent.getAction();
        if (!TextUtils.isEmpty(action)) {
            HwLog.i(TAG, "action = " + action);
            if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                SharePrefWrapper.setPrefValue(ctx, MalwareConst.ANTIMAL_ALERT_FILE, MalwareConst.ANTIMAL_START_TIMES, System.currentTimeMillis());
            } else if (action.equals(MalwareConst.ANTIMAL_RESTORE_LAUNCHER)) {
                AntiMalUtils.setDefaultLauncher(ctx);
                this.notification = AntiMalUtils.getAntimalNotify();
                this.notification.destroyNotification(ctx, NotificationID.ANTI_MALW_RESTORE);
                String statParm = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_RESET, "1");
                HsmStat.statE((int) Events.E_AMTIMAL_RESTORE_LAUNCHER, statParm);
                AntiMalUtils.setDone(ctx);
            } else if (action.equals(MalwareConst.ANTIMAL_CANCLE_NOTIFICATION)) {
                int notificationType = intent.getIntExtra(MalwareConst.ANTIMAL_NOTIFICATION_TYPE, 0);
                if (notificationType != 0) {
                    this.notification = AntiMalUtils.getAntimalNotify();
                    this.notification.destroyNotification(ctx, notificationType);
                }
            }
        }
    }
}
