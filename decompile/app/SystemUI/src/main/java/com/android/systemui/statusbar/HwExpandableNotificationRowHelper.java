package com.android.systemui.statusbar;

import android.app.Notification;
import android.app.PendingIntent;
import android.service.notification.StatusBarNotification;
import com.android.systemui.utils.HwLog;
import java.util.ArrayList;

public class HwExpandableNotificationRowHelper {
    private static final String[] UNCLEAR_NOTIFICATION_PKGS = new String[]{"android", "com.huawei.systemmanager", "com.android.settings", "com.android.incallui", "com.android.phone", "com.android.systemui", "com.huawei.parentcontrol", "com.android.soundrecorder", "com.android.mediacenter"};

    public static boolean isClearable(StatusBarNotification statusBarNotification) {
        if (statusBarNotification == null || statusBarNotification.isClearable()) {
            return true;
        }
        String notificationPkgName = statusBarNotification.getPackageName();
        for (String pkgName : UNCLEAR_NOTIFICATION_PKGS) {
            if (pkgName.equalsIgnoreCase(notificationPkgName)) {
                HwLog.i("HwExpandableNotificationRowHelper", "unclear notification ,notificationPkgName:" + notificationPkgName + " key:" + statusBarNotification.getKey());
                return false;
            }
        }
        return true;
    }

    public static boolean isClearableWhenDeleteAllNotification(ExpandableNotificationRow row) {
        if (row != null && row.mStatusBarNotification != null) {
            return row.mStatusBarNotification.isClearable();
        }
        HwLog.e("HwExpandableNotificationRowHelper", "isClearableWhenDeleteAllNotification row == null or row.mStatusBarNotification == null");
        return false;
    }

    public static boolean showHeadsup(ExpandableNotificationRow row) {
        if (row != null && row.mStatusBarNotification != null) {
            return ("call".equals(row.mStatusBarNotification.getNotification().category) && "com.android.incallui".equals(row.mStatusBarNotification.getPackageName())) || "com.android.deskclock".equals(row.mStatusBarNotification.getPackageName());
        } else {
            HwLog.e("HwExpandableNotificationRowHelper", "isIncall row == null or row.mStatusBarNotification == null");
            return false;
        }
    }

    public static boolean canSnooze(ExpandableNotificationRow row) {
        if (row == null || row.mStatusBarNotification == null) {
            HwLog.e("HwExpandableNotificationRowHelper", "canSnooze row == null or row.mStatusBarNotification == null");
            return false;
        } else if ("call".equals(row.mStatusBarNotification.getNotification().category) && "com.android.incallui".equals(row.mStatusBarNotification.getPackageName())) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean needDimissKeyguard(ExpandableNotificationRow row) {
        if (row == null || row.mStatusBarNotification == null) {
            HwLog.e("HwExpandableNotificationRowHelper", "needDimissKeyguard row == null or row.mStatusBarNotification == null");
            return false;
        }
        Notification notification = row.mStatusBarNotification.getNotification();
        PendingIntent intent;
        if (notification.contentIntent != null) {
            intent = notification.contentIntent;
        } else {
            intent = notification.fullScreenIntent;
        }
        if (!"com.android.deskclock".equals(row.mStatusBarNotification.getPackageName()) || intent == null || intent.isActivity()) {
            return true;
        }
        return false;
    }

    public static boolean isVip(StatusBarNotification sbn) {
        if (sbn.getPackageName().equals("com.android.email")) {
            return sbn.getNotification().extras.getBoolean("hw_email_vip_notification", false);
        }
        return false;
    }

    public static boolean isVipLock(StatusBarNotification sbn) {
        if (!sbn.getPackageName().equals("com.android.email")) {
            return false;
        }
        boolean isVIP = sbn.getNotification().extras.getBoolean("hw_email_vip_notification", false);
        boolean isVIPLock = sbn.getNotification().extras.getBoolean("hw_email_vip_lockscreen", false);
        if (!isVIP) {
            isVIPLock = false;
        }
        return isVIPLock;
    }

    public static boolean isVipStatusBar(StatusBarNotification sbn) {
        if (!sbn.getPackageName().equals("com.android.email")) {
            return false;
        }
        boolean isVIP = sbn.getNotification().extras.getBoolean("hw_email_vip_notification", false);
        boolean isVIPStatusBar = sbn.getNotification().extras.getBoolean("hw_email_vip_statusbar", true);
        if (!isVIP) {
            isVIPStatusBar = false;
        }
        return isVIPStatusBar;
    }

    public static boolean exists(ArrayList<StatusBarIconView> icons, StatusBarIconView icon) {
        if (icons == null) {
            return false;
        }
        for (StatusBarIconView item : icons) {
            if (item.isSameAs(icon)) {
                return true;
            }
        }
        return false;
    }

    public static boolean alwaysShowPeek(StatusBarNotification sbn) {
        if (sbn == null || !sbn.getPackageName().equals("com.tencent.mm") || sbn.isOngoing() || !sbn.isClearable() || sbn.getNotification().isGroupSummary()) {
            return false;
        }
        return true;
    }
}
