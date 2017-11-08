package com.huawei.netassistant.util;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.Formatter;
import com.huawei.netassistant.cardmanager.SimCardManager;
import com.huawei.netassistant.common.ParcelableAppItem;
import com.huawei.netassistant.db.NetAssistantDBManager;
import com.huawei.netassistant.ui.NetAssistantMainActivity;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.NetWorkMgr;
import com.huawei.systemmanager.netassistant.netapp.ui.LockScreenAppListActivity;
import com.huawei.systemmanager.netassistant.traffic.setting.NatSettingManager;
import com.huawei.systemmanager.netassistant.traffic.setting.NotifyPreference;
import com.huawei.systemmanager.netassistant.traffic.statusspeed.NatSettingInfo;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;

public class NotificationUtil {
    public static final int CATEGORY_TYPE_BASE = 1074041823;
    public static final int CATEGORY_UNLIMITED = 1074041835;
    private static final int FLAG_NO_NEED = -1;
    public static final String KEY_IMSI = "key_imsi";
    public static final String KEY_MONTHLY_RESET_NOTIFY = "monthly_reset_notify";
    public static final String KEY_RESULT_LIST = "result_list";
    public static final String KEY_RESULT_LIST_TYPE = "result_list_type";
    public static final String KEY_TOTAL_MOBILE_BYTES = "total_mobile";
    public static final int MAIN_CARD_CATEGORY_ABNORMAL_TRAFFIC = 1074041827;
    public static final int MAIN_CARD_CATEGORY_EXCESS_DAILY_MARK = 1074041824;
    public static final int MAIN_CARD_CATEGORY_EXCESS_MONTH_LIMIT = 1074041823;
    public static final int MAIN_CARD_CATEGORY_EXCESS_MONTH_MARK = 1074041825;
    public static final int MAIN_CARD_CATEGORY_NORMAL_TRAFFIC = 1074041828;
    public static final int MAIN_CARD_CATEGORY_SCREEN_LOCK_TRAFFIC = 1074041826;
    public static final int NOTIFICATION_ID_LEISURE_NOTIFY = 1074041925;
    public static final int NOTIFICATION_ID_LIMIT_NOTIFY = 1074041923;
    public static final int NOTIFICATION_ID_LOCKSCREEN_NOTIFY = 1074041924;
    public static final int NOTIFICATION_ID_MONTYLY_RESET_NOTIFY = 1074041926;
    public static final int SECONDARY_CARD_CATEGORY_ABNORMAL_TRAFFIC = 1074041833;
    public static final int SECONDARY_CARD_CATEGORY_EXCESS_DAILY_MARK = 1074041830;
    public static final int SECONDARY_CARD_CATEGORY_EXCESS_MONTH_LIMIT = 1074041829;
    public static final int SECONDARY_CARD_CATEGORY_EXCESS_MONTH_MARK = 1074041831;
    public static final int SECONDARY_CARD_CATEGORY_NORMAL_TRAFFIC = 1074041834;
    public static final int SECONDARY_CARD_CATEGORY_SCREEN_LOCK_TRAFFIC = 1074041832;
    private static final String TAG = "NotificationUtil";
    public static final String TIPS_MAIN_CARD = "1";
    public static final String TIPS_SECONDARY_CARD = "2";

    public static void sendNotification(long trafficUsed, long trafficTotal, ArrayList<ParcelableAppItem> itemsList, int msgType) {
        NotificationManager nm = getNotificationManager(GlobalContext.getContext());
        Context context = GlobalContext.getContext();
        String textUsed = Formatter.formatFileSize(GlobalContext.getContext(), trafficUsed);
        switch (msgType) {
            case 1074041823:
            case SECONDARY_CARD_CATEGORY_EXCESS_MONTH_LIMIT /*1074041829*/:
                notifyMonthLimit(nm, context, textUsed, itemsList, msgType);
                return;
            case MAIN_CARD_CATEGORY_EXCESS_DAILY_MARK /*1074041824*/:
            case SECONDARY_CARD_CATEGORY_EXCESS_DAILY_MARK /*1074041830*/:
                notifyDailyWarn(nm, context, textUsed, itemsList, msgType);
                return;
            case MAIN_CARD_CATEGORY_EXCESS_MONTH_MARK /*1074041825*/:
            case SECONDARY_CARD_CATEGORY_EXCESS_MONTH_MARK /*1074041831*/:
                notifyMonthWarn(nm, context, textUsed, itemsList, msgType);
                return;
            case MAIN_CARD_CATEGORY_SCREEN_LOCK_TRAFFIC /*1074041826*/:
            case SECONDARY_CARD_CATEGORY_SCREEN_LOCK_TRAFFIC /*1074041832*/:
                nm.notify(NOTIFICATION_ID_LOCKSCREEN_NOTIFY, createNotificationForScreenLock(context, Integer.valueOf(R.drawable.ic_data_unlock_notification), itemsList, textUsed, msgType));
                return;
            default:
                return;
        }
    }

    private static void notifyMonthWarn(NotificationManager nm, Context context, String textUsed, ArrayList<ParcelableAppItem> itemsList, int msgType) {
        if (canSendLimitNotification(context)) {
            Notification notification = createNotificationForMonthMarkExcess(context, Integer.valueOf(R.drawable.ic_data_over_notification), itemsList, textUsed, msgType);
            if (notification == null || !NotifyPreference.getInstance().shouldMonthWarnNotify()) {
                HwLog.e(TAG, "Month Warning Notification not create");
            } else {
                NotifyPreference.getInstance().setMonthWarnPreference();
                nm.notify(NOTIFICATION_ID_LIMIT_NOTIFY, notification);
            }
        }
    }

    private static void notifyDailyWarn(NotificationManager nm, Context context, String textUsed, ArrayList<ParcelableAppItem> itemsList, int msgType) {
        if (canSendLimitNotification(context)) {
            Notification notification = createNotificationForDailyMarkExcess(context, Integer.valueOf(R.drawable.ic_data_over_notification), itemsList, textUsed, msgType);
            if (notification == null || !NotifyPreference.getInstance().shouldDailyWarnNotify()) {
                HwLog.e(TAG, "Daily Warning Notification not create");
            } else {
                NotifyPreference.getInstance().setDailyWarnPreference();
                nm.notify(NOTIFICATION_ID_LIMIT_NOTIFY, notification);
            }
        }
    }

    private static void notifyMonthLimit(NotificationManager nm, Context context, String textUsed, ArrayList<ParcelableAppItem> itemsList, int msgType) {
        if (canSendLimitNotification(context)) {
            Notification notification = createNotificationForMonthLimitExcess(context, Integer.valueOf(R.drawable.ic_data_over_notification), itemsList, textUsed, msgType);
            if (notification != null) {
                nm.notify(NOTIFICATION_ID_LIMIT_NOTIFY, notification);
            }
        }
    }

    private static Notification createCommonNotification(int smallIcon, String tickerText, String title, String content, PendingIntent intent, int flags) {
        Builder builder = new Builder(GlobalContext.getContext());
        builder.setSmallIcon(smallIcon);
        builder.setTicker(tickerText);
        builder.setContentIntent(intent);
        builder.setContentTitle(title);
        builder.setContentText(content);
        Notification notification = builder.build();
        if (-1 != flags) {
            notification.flags = flags;
        }
        return notification;
    }

    private static Notification createNotificationForMonthMarkExcess(Context context, Integer iconID, ArrayList<ParcelableAppItem> arrayList, String totalUsed, int msgType) {
        if (((int) NetAssistantDBManager.getInstance().getMonthWarnByte(SimCardManager.getInstance().getPreferredDataSubscriberId())) < 0) {
            return null;
        }
        return createCommonNotification(iconID.intValue(), (String) context.getText(R.string.netassistant_excess_monthmark_notofication_title), (String) context.getText(R.string.netassistant_excess_monthmark_notofication_title), String.format(context.getString(R.string.net_assistant_month_notify_message), new Object[]{CommonMethodUtil.formatPercentString(monthPercent)}), createNormalIntent(context, msgType, NetWorkMgr.ACTION_CLICK_NOTIFICATION_MONTH_MARK), -1);
    }

    private static Notification createNotificationForDailyMarkExcess(Context context, Integer iconID, ArrayList<ParcelableAppItem> arrayList, String totalUsed, int msgType) {
        if (NatSettingManager.getDailyWarnNotifyByte(SimCardManager.getInstance().getPreferredDataSubscriberId()) < 0) {
            return null;
        }
        return createCommonNotification(iconID.intValue(), (String) context.getText(R.string.netassistant_excess_daymark_notofication_title), (String) context.getText(R.string.netassistant_excess_daymark_notofication_title), String.format(context.getString(R.string.net_assistant_daily_notify_message), new Object[]{CommonMethodUtil.formatBytes(context, monthWarn)}), createNormalIntent(context, msgType, NetWorkMgr.ACTION_CLICK_NOTIFICATION_DAILY_MARK), -1);
    }

    private static PendingIntent createNormalIntent(Context context, int msgType, String dcKey) {
        Intent intent = new Intent();
        intent.setFlags(67108864);
        intent.setClass(context, NetAssistantMainActivity.class);
        if (!TextUtils.isEmpty(dcKey)) {
            intent.putExtra(HsmStatConst.KEY_NOTFICATION_EVENT, dcKey);
        }
        return PendingIntent.getActivity(context, msgType, intent, 134217728);
    }

    private static Notification createNotificationForScreenLock(Context context, Integer iconID, ArrayList<ParcelableAppItem> itemsList, String totalUsed, int msgType) {
        return createCommonNotification(iconID.intValue(), (String) context.getText(R.string.netassistant_lockscreen_notofication_title), (String) context.getText(R.string.netassistant_lockscreen_notofication_title), String.format((String) context.getText(R.string.netassistant_lockscreen_notofication_explain), new Object[]{totalUsed}), createCustomIntent(context, 335544320, itemsList, totalUsed, msgType, NetWorkMgr.ACTION_CLICK_NOTIFICATION_SCREEN_LOCK), 16);
    }

    private static Notification createNotificationForMonthLimitExcess(Context context, Integer iconID, ArrayList<ParcelableAppItem> arrayList, String totalUsed, int msgType) {
        if (NatSettingManager.getLimitNotifyByte(SimCardManager.getInstance().getPreferredDataSubscriberId()) < 0) {
            return null;
        }
        return createCommonNotification(iconID.intValue(), (String) context.getText(R.string.netassistant_excess_monthlimit_notofication_title), (String) context.getText(R.string.netassistant_excess_monthlimit_notofication_title), context.getString(R.string.net_assistant_month_limit_message, new Object[]{CommonMethodUtil.formatBytes(context, NatSettingManager.getLimitNotifyByte(SimCardManager.getInstance().getPreferredDataSubscriberId()))}), createNormalIntent(context, msgType, NetWorkMgr.ACTION_CLICK_NOTIFICATION_MONTH_LIMIT), -1);
    }

    private static PendingIntent createCustomIntent(Context context, int flags, ArrayList<ParcelableAppItem> itemsList, String totalUsed, int msgType, String dcKey) {
        Intent intent = new Intent();
        intent.setFlags(flags);
        if (!(itemsList == null || itemsList.isEmpty())) {
            Bundle newBundle = new Bundle();
            newBundle.putParcelableArrayList(KEY_RESULT_LIST, itemsList);
            intent.putExtras(newBundle);
            intent.putExtra(KEY_TOTAL_MOBILE_BYTES, totalUsed);
            intent.putExtra(KEY_RESULT_LIST_TYPE, msgType);
            intent.setClass(context, LockScreenAppListActivity.class);
        }
        if (!TextUtils.isEmpty(dcKey)) {
            intent.putExtra(HsmStatConst.KEY_NOTFICATION_EVENT, dcKey);
        }
        return PendingIntent.getActivity(context, msgType, intent, 134217728);
    }

    public static String getCardIndex(int msgType) {
        String index = "1";
        switch (msgType) {
            case 1074041823:
            case MAIN_CARD_CATEGORY_EXCESS_DAILY_MARK /*1074041824*/:
            case MAIN_CARD_CATEGORY_EXCESS_MONTH_MARK /*1074041825*/:
            case MAIN_CARD_CATEGORY_SCREEN_LOCK_TRAFFIC /*1074041826*/:
            case MAIN_CARD_CATEGORY_ABNORMAL_TRAFFIC /*1074041827*/:
            case MAIN_CARD_CATEGORY_NORMAL_TRAFFIC /*1074041828*/:
                return "1";
            case SECONDARY_CARD_CATEGORY_EXCESS_MONTH_LIMIT /*1074041829*/:
            case SECONDARY_CARD_CATEGORY_EXCESS_DAILY_MARK /*1074041830*/:
            case SECONDARY_CARD_CATEGORY_EXCESS_MONTH_MARK /*1074041831*/:
            case SECONDARY_CARD_CATEGORY_SCREEN_LOCK_TRAFFIC /*1074041832*/:
            case SECONDARY_CARD_CATEGORY_ABNORMAL_TRAFFIC /*1074041833*/:
            case SECONDARY_CARD_CATEGORY_NORMAL_TRAFFIC /*1074041834*/:
                return "2";
            default:
                return index;
        }
    }

    public static void cancelNotification(int msgType) {
        getNotificationManager(GlobalContext.getContext()).cancel(msgType);
    }

    private static NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService("notification");
    }

    public static void notifyLeisureTrafficWarn(Context context, String imsi, String textUsed) {
        getNotificationManager(GlobalContext.getContext()).notify(NOTIFICATION_ID_LIMIT_NOTIFY, createNotificationForTrafficExcess(context, Integer.valueOf(R.drawable.ic_flow_notifi), getResCode(imsi), textUsed));
    }

    public static void notifyRoamingTrafficWarn(Context context, String imsi, String textUsed) {
        if (canSendLimitNotification(context)) {
            getNotificationManager(GlobalContext.getContext()).notify(NOTIFICATION_ID_LIMIT_NOTIFY, createNotificationForRoamingTraffic(context, Integer.valueOf(R.drawable.ic_flow_notifi), getResCode(imsi), textUsed));
        }
    }

    private static Integer getResCode(String imsi) {
        if (SimCardManager.getInstance().isPhoneSupportDualCard()) {
            if (TextUtils.equals(SimCardManager.getInstance().getSimcardByIndex(0), imsi)) {
                return Integer.valueOf(R.drawable.ic_card1);
            }
            if (TextUtils.equals(SimCardManager.getInstance().getSimcardByIndex(1), imsi)) {
                return Integer.valueOf(R.drawable.ic_card2);
            }
        }
        return null;
    }

    private static Notification createNotificationForTrafficExcess(Context context, Integer iconID, Integer cardIconID, String msg) {
        return createCommonNotification(iconID.intValue(), (String) context.getText(R.string.netassistant_excess_leisure_notofication_title), (String) context.getText(R.string.netassistant_excess_leisure_notofication_title), context.getString(R.string.net_assistant_leisure_notify_message, new Object[]{msg}), createTrafficIntent(context), -1);
    }

    private static Notification createNotificationForRoamingTraffic(Context context, Integer iconID, Integer cardIconID, String msg) {
        return createCommonNotification(iconID.intValue(), (String) context.getText(R.string.roaming_traffic_notification_title), (String) context.getText(R.string.roaming_traffic_notification_title), context.getString(R.string.roaming_traffic_notification_summary, new Object[]{msg}), createTrafficIntent(context), -1);
    }

    private static PendingIntent createTrafficIntent(Context context) {
        Intent intent = new Intent();
        intent.setFlags(67108864);
        intent.setClass(context, NetAssistantMainActivity.class);
        return PendingIntent.getActivity(context, 1, intent, 134217728);
    }

    public static void notifyMonthlyResetWarning(String imsi) {
        NotificationManager nm = getNotificationManager(GlobalContext.getContext());
        Context context = GlobalContext.getContext();
        String contentText = context.getString(R.string.month_total_notification_summary);
        String contentTitle = (String) context.getText(R.string.month_total_notification_title);
        String tickerText = (String) context.getText(R.string.month_total_notification_title);
        Intent intent = new Intent();
        intent.setFlags(67108864);
        intent.putExtra(KEY_IMSI, imsi);
        intent.putExtra(KEY_MONTHLY_RESET_NOTIFY, true);
        intent.setClass(context, NetAssistantMainActivity.class);
        nm.notify(NOTIFICATION_ID_MONTYLY_RESET_NOTIFY, createCommonNotification(R.drawable.ic_data_over_notification, tickerText, contentTitle, contentText, PendingIntent.getActivity(context, 1, intent, 134217728), -1));
    }

    private static boolean canSendLimitNotification(Context context) {
        return NatSettingInfo.getTrafficDisplaySet(context) == 0;
    }
}
