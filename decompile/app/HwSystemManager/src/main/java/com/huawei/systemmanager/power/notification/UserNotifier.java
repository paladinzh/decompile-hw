package com.huawei.systemmanager.power.notification;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.v4.internal.view.SupportMenu;
import com.huawei.notificationmanager.rule.NotificationRuleMgr;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.wrapper.SharePrefWrapper;
import com.huawei.systemmanager.power.comm.SharedPrefKeyConst;
import com.huawei.systemmanager.power.receiver.BootBroadcastReceiver;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.NotificationID;
import java.util.ArrayList;

public class UserNotifier {
    public static final String ACTION_NOTIFICATION_CLOSE_APP = "huawei.intent.action.NOTIFICATION_CLOSE_APP";
    public static final String ACTION_NOTIFICATION_DELETED = "huawei.intent.action.NOTIFICATION_DELETED";
    public static final String ACTION_NOTIFICATION_NOT_REMIND = "huawei.intent.action.NOTIFICATION_NOT_REMIND";
    private static final String TAG = "UserNotifier";

    public static void sendNotification(Context context, ArrayList<String> data, Intent intent) {
        notificationMgr(context).notify(NotificationID.POWER, createNotification(context, data, intent));
    }

    public static void sendNotificationForSuperHighPower(Context context, ArrayList<String> data, Intent intent) {
        Notification notification = createNotificationForSuperHighPower(context, data, intent);
        if (SharePrefWrapper.getPrefValue(context, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.SUPER_HIGH_POWER_SWITCH_KEY, false)) {
            notificationMgr(context).notify(NotificationID.SUPER_HIGH_POWER, notification);
        } else {
            notificationMgr(context).notify(NotificationID.POWER, notification);
        }
    }

    public static void sendNotificationForSuperMode(Context context) {
        Builder builder = new Builder(context);
        builder.setSmallIcon(R.drawable.stat_notify_phonemanager_remindpower).setContentTitle(context.getString(R.string.super_power_saving_title)).setTicker(context.getString(R.string.super_power_saving_title)).setContentText(context.getString(R.string.super_power_saving_notification)).setOngoing(true);
        Notification notification = builder.build();
        notification.extras = NotificationRuleMgr.getNotificationThemeData(R.drawable.stat_notify_phonemanager_remindpower, -1, 1, 15);
        notificationMgr(context).notify(NotificationID.POWER_ATT, notification);
    }

    public static void destroyNotification(Context context) {
        notificationMgr(context).cancel(NotificationID.POWER);
        notificationMgr(context).cancel(NotificationID.SUPER_HIGH_POWER);
        HwLog.i(TAG, "destroyNotification   clear notification!");
    }

    private static Notification createNotification(Context context, ArrayList<String> data, Intent intent) {
        String left = context.getString(R.string.power_notification_left_button);
        CharSequence leftTitle = left.subSequence(0, left.length());
        String right = context.getString(R.string.notification_stop_app);
        CharSequence rightTitle = right.subSequence(0, right.length());
        int ndSize = data.size();
        CharSequence message = null;
        HwLog.d(TAG, "notificaitonData.size() = " + ndSize);
        if (ndSize > 0) {
            message = getApplicationLabel(context, (String) data.get(0));
            if (ndSize > 1) {
                message = context.getResources().getString(R.string.notification_power_msg_etc);
            } else {
                message = String.format(context.getResources().getString(R.string.notification_power_msg), new Object[]{message});
            }
            HwLog.d(TAG, "Notification message = " + message);
        }
        if (ndSize == 1) {
            return excessivePowerPropertyBuilder(context, message, intent).setDefaults(0).addAction(0, leftTitle, getLeftActionIntent(context, (String) data.get(0))).addAction(0, rightTitle, getRightActionIntent(context, (String) data.get(0))).build();
        }
        return excessivePowerPropertyBuilder(context, message, intent).setDefaults(4).build();
    }

    private static CharSequence getApplicationLabel(Context context, String pkgName) {
        CharSequence message = null;
        try {
            PackageManager pm = context.getPackageManager();
            return pm.getApplicationLabel(pm.getApplicationInfo(pkgName, 128));
        } catch (NameNotFoundException e) {
            HwLog.e(TAG, "Handle wasting power apps NameNotFoundException");
            return pkgName.subSequence(0, pkgName.length());
        } catch (Exception e2) {
            e2.printStackTrace();
            return message;
        }
    }

    private static Notification createNotificationForSuperHighPower(Context context, ArrayList<String> data, Intent intent) {
        boolean autoClearSuperHighPowerApp = SharePrefWrapper.getPrefValue(context, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.SUPER_HIGH_POWER_SWITCH_KEY, false);
        String right = context.getString(R.string.notification_stop_app);
        CharSequence rightTitle = right.subSequence(0, right.length());
        int ndSize = data.size();
        CharSequence charSequence = null;
        HwLog.d(TAG, "createNotificationForSuperHighPower notificaitonData.size() = " + ndSize);
        if (ndSize > 0) {
            charSequence = getApplicationLabel(context, (String) data.get(0));
            if (ndSize > 1) {
                if (autoClearSuperHighPowerApp) {
                    charSequence = context.getResources().getQuantityString(R.plurals.super_high_power_notify_content2, ndSize, new Object[]{Integer.valueOf(ndSize)});
                } else {
                    charSequence = context.getResources().getString(R.string.notification_power_msg_etc);
                }
            } else if (autoClearSuperHighPowerApp) {
                charSequence = String.format(context.getResources().getString(R.string.super_high_power_notify_content1), new Object[]{charSequence});
            } else {
                charSequence = String.format(context.getResources().getString(R.string.notification_power_msg), new Object[]{charSequence});
            }
            HwLog.d(TAG, "Notification message = " + charSequence);
        }
        if (ndSize == 1) {
            if (autoClearSuperHighPowerApp) {
                return samePropertyBuilderForSuperHighPower(context, charSequence, intent).setDefaults(0).build();
            }
            return samePropertyBuilder(context, charSequence, intent).setDefaults(0).addAction(0, rightTitle, getRightActionIntent(context, (String) data.get(0))).build();
        } else if (autoClearSuperHighPowerApp) {
            return samePropertyBuilderForSuperHighPower(context, charSequence, intent).setDefaults(4).build();
        } else {
            return samePropertyBuilder(context, charSequence, intent).setDefaults(4).build();
        }
    }

    private static PendingIntent getContentIntent(Context context, Intent activityIntent) {
        return PendingIntent.getActivity(context, 0, activityIntent, 134217728);
    }

    private static PendingIntent getDeleteIntent(Context context) {
        Intent intent = new Intent(context, BootBroadcastReceiver.class);
        intent.setAction(ACTION_NOTIFICATION_DELETED);
        return PendingIntent.getBroadcast(context, 1, intent, ShareCfg.PERMISSION_MODIFY_CALENDAR);
    }

    private static PendingIntent getLeftActionIntent(Context context, String pkgName) {
        Intent intent = new Intent(context, BootBroadcastReceiver.class);
        intent.setAction(ACTION_NOTIFICATION_NOT_REMIND);
        intent.putExtra("pkgName", pkgName);
        return PendingIntent.getBroadcast(context, 2, intent, ShareCfg.PERMISSION_MODIFY_CALENDAR);
    }

    private static PendingIntent getRightActionIntent(Context context, String pkgName) {
        Intent intent = new Intent(context, BootBroadcastReceiver.class);
        intent.setAction(ACTION_NOTIFICATION_CLOSE_APP);
        intent.putExtra("pkgName", pkgName);
        return PendingIntent.getBroadcast(context, 3, intent, ShareCfg.PERMISSION_MODIFY_CALENDAR);
    }

    private static NotificationManager notificationMgr(Context context) {
        return (NotificationManager) context.getSystemService("notification");
    }

    private static Builder excessivePowerPropertyBuilder(Context context, CharSequence message, Intent intent) {
        String hint = context.getString(R.string.super_high_power_notify_title3);
        CharSequence title = hint.subSequence(0, hint.length());
        return new Builder(context).setSmallIcon(R.drawable.ic_battery_hight_notification).setTicker(title).setContentTitle(title).setContentText(message).setPriority(1).setAutoCancel(true).setSound(null).setColor(SupportMenu.CATEGORY_MASK).setContentIntent(getContentIntent(context, intent)).setDeleteIntent(getDeleteIntent(context));
    }

    private static Builder samePropertyBuilder(Context context, CharSequence message, Intent intent) {
        String hint = context.getString(R.string.super_high_power_notify_title1);
        CharSequence title = hint.subSequence(0, hint.length());
        return new Builder(context).setSmallIcon(R.drawable.ic_battery_super_notification).setTicker(title).setContentTitle(title).setContentText(message).setPriority(1).setAutoCancel(true).setSound(null).setColor(SupportMenu.CATEGORY_MASK).setContentIntent(getContentIntent(context, intent)).setDeleteIntent(getDeleteIntent(context));
    }

    private static Builder samePropertyBuilderForSuperHighPower(Context context, CharSequence message, Intent intent) {
        String hint = context.getString(R.string.super_high_power_notify_title2);
        CharSequence title = hint.subSequence(0, hint.length());
        return new Builder(context).setSmallIcon(R.drawable.ic_battery_super_notification).setTicker(title).setContentTitle(title).setContentText(message).setPriority(1).setAutoCancel(true).setSound(null).setColor(SupportMenu.CATEGORY_MASK).setContentIntent(getContentIntent(context, intent)).setDeleteIntent(getDeleteIntent(context));
    }
}
