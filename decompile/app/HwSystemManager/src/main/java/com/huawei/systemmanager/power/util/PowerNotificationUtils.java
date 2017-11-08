package com.huawei.systemmanager.power.util;

import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v4.internal.view.SupportMenu;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.wrapper.SharePrefWrapper;
import com.huawei.systemmanager.competitorintercept.Const;
import com.huawei.systemmanager.power.comm.ActionConst;
import com.huawei.systemmanager.power.comm.ApplicationConstant;
import com.huawei.systemmanager.power.comm.SharedPrefKeyConst;
import com.huawei.systemmanager.power.receiver.BootBroadcastReceiver;
import com.huawei.systemmanager.power.ui.HwPowerManagerActivity;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.numberlocation.NumberLocationPercent;

public class PowerNotificationUtils {
    private static final String POWER_MANAGER_ACTIVITY = "com.huawei.systemmanager.power.ui.HwPowerManagerActivity";
    public static final String POWER_NOTIFICATION_CONTENT_KEY = "comefrom";
    public static final String POWER_NOTIFICATION_SAVEMODE_VALUE = "powerSaveModeNotification";
    public static final String POWER_NOTIFICATION_SUPERMODE_VALUE = "powerSuperModeNotification";
    private static final String TAG = PowerNotificationUtils.class.getSimpleName();

    public static void showPowerModeQuitNotification(Context mContext) {
        String contentTitle = mContext.getString(R.string.power_save_mode_quit_notification_title);
        String contentText = mContext.getString(R.string.power_save_mode_open_notification_des);
        Builder builder = new Builder(mContext).setSmallIcon(R.drawable.ic_power_saving_notification).setTicker(contentTitle).setContentTitle(contentTitle).setContentText(contentText).setContentIntent(getContentIntent(mContext)).setDefaults(0).setPriority(1).setAutoCancel(true).setShowWhen(false).addAction(0, mContext.getString(R.string.power_save_mode_quit_notification_button), getQuitActionIntent(mContext));
        cancleLowBatterySaveModeNotification(mContext);
        notificationMgr(mContext).notify(Const.NOTIFICATION_ID_POWER_SAVE_MODE_QUIT, builder.build());
    }

    public static void showPowerModeEnterNotification(Context mContext, int noticationType, int level) {
        if (!SysCoreUtils.isOtherUserProcess()) {
            String enterTitle = null;
            String contentTitle = null;
            String deleteAction = null;
            int id = 0;
            int drawableId = R.drawable.ic_low10_power_notification;
            if (noticationType == 1) {
                contentTitle = mContext.getString(R.string.power_save_mode_notification_contentTitle);
                enterTitle = mContext.getString(R.string.power_save_mode_notification_button);
                deleteAction = ActionConst.INTENT_SAVE_MODE_DELETE_NOTIFY;
                id = Const.NOTIFICATION_ID_POWER_SAVE_MODE_REMINDER;
                drawableId = R.drawable.ic_battery_hight_notification;
            } else if (noticationType == 2) {
                contentTitle = mContext.getString(R.string.power_super_save_mode_notification_contentTitle);
                enterTitle = mContext.getString(R.string.super_power_save_mode_notification_button);
                deleteAction = ActionConst.INTENT_SUPER_SAVE_MODE_DELETE_NOTIFY;
                id = Const.NOTIFICATION_ID_POWER_SUPERSAVE_MODE_REMINDER;
                drawableId = R.drawable.ic_battery_hight_notification;
            }
            Builder builder = new Builder(mContext).setSmallIcon(drawableId).setTicker(contentTitle).setContentTitle(contentTitle).setContentText(String.format(mContext.getResources().getString(R.string.power_save_mode_notification_contentText), new Object[]{NumberLocationPercent.getPercent((double) level)})).setDeleteIntent(getDeleteIntent(mContext, deleteAction)).setContentIntent(getContentIntent(mContext, noticationType)).setDefaults(2).setPriority(2).setAutoCancel(true).setShowWhen(false).setOnlyAlertOnce(true).setColor(SupportMenu.CATEGORY_MASK).addAction(0, enterTitle, getEnterActionIntent(mContext, noticationType, level));
            if (noticationType == 2) {
                cancleLowBatterySaveModeNotification(mContext);
            }
            HwLog.i(TAG, "showPowerModeEnterNotification, noticationType= " + noticationType);
            notificationMgr(mContext).notify(id, builder.build());
        }
    }

    private static PendingIntent getEnterActionIntent(Context context, int noticationType, int mRawLevel) {
        Intent intent = new Intent(context, BootBroadcastReceiver.class);
        intent.putExtra(ApplicationConstant.POWER_NOTIFICATION_RAWLEVEL, mRawLevel);
        intent.putExtra(ApplicationConstant.POWER_NOTIFICATION_TYPE, noticationType);
        intent.setAction(ActionConst.INTENT_SVAE_MODE_NOTIFY);
        return PendingIntent.getBroadcast(context, 1, intent, ShareCfg.PERMISSION_MODIFY_CALENDAR);
    }

    private static PendingIntent getDeleteIntent(Context ctx, String action) {
        Intent intent = new Intent(ctx, BootBroadcastReceiver.class);
        intent.setAction(action);
        return PendingIntent.getBroadcastAsUser(ctx, 0, intent, ShareCfg.PERMISSION_MODIFY_CALENDAR, UserHandle.CURRENT);
    }

    private static PendingIntent getContentIntent(Context ctx) {
        Intent intent = new Intent(ctx, HwPowerManagerActivity.class);
        intent.setFlags(603979776);
        return PendingIntent.getActivity(ctx, 0, intent, 134217728);
    }

    private static PendingIntent getContentIntent(Context ctx, int noticationType) {
        Intent intent = new Intent(ctx, HwPowerManagerActivity.class);
        Bundle bundle = new Bundle();
        if (noticationType == 1) {
            bundle.putString("comefrom", POWER_NOTIFICATION_SAVEMODE_VALUE);
        } else if (noticationType == 2) {
            bundle.putString("comefrom", POWER_NOTIFICATION_SUPERMODE_VALUE);
        }
        intent.putExtras(bundle);
        intent.setFlags(603979776);
        return PendingIntent.getActivity(ctx, 0, intent, 134217728);
    }

    private static PendingIntent getQuitActionIntent(Context context) {
        Intent intent = new Intent(context, BootBroadcastReceiver.class);
        intent.setAction(ActionConst.INTENT_SAVE_MODE_CLOSE_NOTIFY);
        return PendingIntent.getBroadcast(context, 2, intent, ShareCfg.PERMISSION_MODIFY_CALENDAR);
    }

    private static NotificationManager notificationMgr(Context context) {
        return (NotificationManager) context.getSystemService("notification");
    }

    public static void cancleLowBatterySaveModeNotification(Context context) {
        notificationMgr(context).cancel(Const.NOTIFICATION_ID_POWER_SAVE_MODE_REMINDER);
        SharePrefWrapper.setPrefValue(context, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.POWER_SAVE_MODE_LOW_BATTERY_NOTIFICATION, false);
        HwLog.i(TAG, "cancleLowBatterySaveModeNotification");
    }

    public static void cancleLowBatterySuperModeNotification(Context context) {
        notificationMgr(context).cancel(Const.NOTIFICATION_ID_POWER_SUPERSAVE_MODE_REMINDER);
        SharePrefWrapper.setPrefValue(context, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.POWER_SUPER_MODE_LOW_BATTERY_NOTIFICATION, false);
        HwLog.i(TAG, "cancleLowBatterySuperModeNotification");
    }

    public static void canclePowerModeOpenNotification(Context context) {
        notificationMgr(context).cancel(Const.NOTIFICATION_ID_POWER_SAVE_MODE_QUIT);
        HwLog.i(TAG, "canclePowerModeOpenNotification");
    }

    public static void canclePowerModeNotification(Context context) {
        cancleLowBatterySaveModeNotification(context);
        cancleLowBatterySuperModeNotification(context);
        HwLog.d(TAG, " canclePowerModeNotification");
    }
}
