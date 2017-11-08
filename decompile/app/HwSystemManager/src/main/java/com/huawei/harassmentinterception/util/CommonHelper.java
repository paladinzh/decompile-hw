package com.huawei.harassmentinterception.util;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.UserHandle;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.text.format.DateUtils;
import com.huawei.harassmentinterception.common.CommonObject.InterceptionRuleInfo;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.harassmentinterception.ui.InterceptionActivity;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.HarassmentFilter;
import com.huawei.systemmanager.util.HwLog;

public class CommonHelper {
    public static final String TAG = "CommonHelper";
    private static final String[] sCountryCode = new String[]{ConstValues.PHONE_COUNTRY_CODE_CHINA};
    private static final String[] sRCSCountryCode = new String[]{"+49"};

    public static String[] getRCSCountryCode() {
        return sRCSCountryCode;
    }

    @Deprecated
    public static boolean isInterceptionSettingOn(Context context) {
        int blockSettingState = DBAdapter.getRuleState(context, ConstValues.RULE_INTERCEPTION);
        HwLog.d(TAG, "isInterceptionSettingOn : rule = " + blockSettingState);
        if (Utility.isWifiOnlyMode() || Utility.isDataOnlyMode()) {
            blockSettingState = 0;
        }
        if (blockSettingState == 0) {
            return false;
        }
        if (blockSettingState == 1) {
            return true;
        }
        HwLog.i(TAG, "isInterceptionSettingOn : Invalid status = " + blockSettingState);
        return false;
    }

    public static boolean setInterceptionSettingOn(Context context, boolean bOn, boolean showToast) {
        InterceptionRuleInfo settingRule;
        if (bOn) {
            settingRule = new InterceptionRuleInfo(ConstValues.RULE_INTERCEPTION, 1);
        } else {
            settingRule = new InterceptionRuleInfo(ConstValues.RULE_INTERCEPTION, 0);
        }
        if (DBAdapter.updateInterceptionRule(context, settingRule) <= 0) {
            HwLog.w(TAG, "setInterceptionSettingOn : Failed to updateInterceptionRules");
            return false;
        }
        notifyInterceptionSettingChange(context);
        HwLog.i(TAG, "setInterceptionSettingOn : Interception switch is set to be " + bOn);
        return true;
    }

    public static boolean setNotifyRule(Context context, int notifyRule) {
        try {
            if (DBAdapter.updateInterceptionRule(context, new InterceptionRuleInfo(ConstValues.RULE_NOTIFICATION, notifyRule)) <= 0) {
                HwLog.w(TAG, "setNotifyRule : Failed to update notification Rules");
                return false;
            }
            HwLog.i(TAG, "setNotifyRule : notifyRule = " + notifyRule);
            return true;
        } catch (Exception e) {
            HwLog.e(TAG, "setNotifyRule : Exception = " + e.getMessage());
            return false;
        }
    }

    public static int getNotifyRule(Context context) {
        int notifyRule = 1;
        try {
            notifyRule = DBAdapter.getRuleState(context, ConstValues.RULE_NOTIFICATION);
            HwLog.i(TAG, "getNotifyRule: notifyRule = " + notifyRule);
            return notifyRule;
        } catch (Exception e) {
            HwLog.e(TAG, "getNotifyRule : Exception = " + e.getMessage());
            return notifyRule;
        }
    }

    public static void sendNotificationForAll(Context context) {
        sendNotificationForAll(context, 1);
    }

    public static void sendNotificationForAll(Context context, int blockReason) {
        int notifyRule = getNotifyRule(context);
        HwLog.i(TAG, "sendNotificationForAll: blockReason = " + blockReason);
        if (notifyRule == 0 || (2 == notifyRule && 1 == blockReason)) {
            HwLog.i(TAG, "sendNotificationForAll: Skip, notifyRule = " + notifyRule);
            return;
        }
        Builder nBuilder = new Builder(context);
        int countBlockCall = DBAdapter.getUnreadCallCount(context);
        int countBlockMessage = DBAdapter.getUnreadMsgCount(context);
        String notifyTicker = context.getString(R.string.harassmentInterceptionNewBlock);
        String notifyTitle = notifyTicker;
        String notifyContent = context.getString(R.string.harassmentInterceptionNotificationForAll, new Object[]{Integer.valueOf(countBlockCall), Integer.valueOf(countBlockMessage)});
        Intent appIntent = new Intent();
        appIntent.setClass(context, InterceptionActivity.class);
        appIntent.setFlags(603979776);
        appIntent.putExtra(HsmStatConst.KEY_NOTFICATION_EVENT, HarassmentFilter.ACTION_CLICK_HARASSMENT_NOTIFICATION);
        int showTabsNumber = getTheDefaultTabIndex(countBlockCall, countBlockMessage);
        appIntent.putExtra("showTabsNumber", showTabsNumber);
        nBuilder.setSmallIcon(R.drawable.stat_sys_phonemanager_intercept).setTicker(notifyTicker).setContentTitle(notifyTicker).setContentText(notifyContent).setContentIntent(PendingIntent.getActivity(context, showTabsNumber, appIntent, 134217728)).setDefaults(0).setAutoCancel(false);
        Notification notification = nBuilder.build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
        notificationManager.cancel(R.string.harassmentInterceptionNotificationManagerTitle);
        notificationManager.notify(R.string.harassmentInterceptionNotificationManagerTitle, notification);
    }

    private static int getTheDefaultTabIndex(int call, int message) {
        if (call > 0) {
            return 1;
        }
        if (message > 0) {
            return 0;
        }
        return 1;
    }

    public static void notifyInterceptionSettingChange(Context context) {
        context.sendBroadcastAsUser(new Intent(ConstValues.ACTION_INTERCEPT_RULE_CHANGE), UserHandle.OWNER, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
    }

    public static boolean isUIThread() {
        return Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId();
    }

    public static String getSystemDateStyle(Context context, long time) {
        return DateUtils.formatDateTime(context, time, 65553);
    }

    public static String getSystemDateTimeStyle(Context context, long time) {
        int flags;
        if (DateUtils.isToday(time)) {
            flags = 1;
        } else {
            flags = 65552;
        }
        return DateUtils.formatDateTime(context, time, flags);
    }

    public static boolean isInvalidPhoneNumber(String phone) {
        if (TextUtils.isEmpty(phone) || ConstValues.INVALID_PHONENUMBER_1.equals(phone) || ConstValues.INVALID_PHONENUMBER_2.equals(phone)) {
            return true;
        }
        return ConstValues.INVALID_PHONENUMBER_3.equals(phone);
    }

    public static boolean isValidDigtalPhoneNumber(String phone) {
        if (isInvalidPhoneNumber(phone)) {
            HwLog.i(TAG, "InvalidPhoneNumber");
            return false;
        } else if (!TextUtils.isEmpty(PhoneNumberUtils.stripSeparators(phone))) {
            return true;
        } else {
            HwLog.i(TAG, "not digital number");
            return false;
        }
    }

    public static boolean isInvalidPhoneNumber(String number, int presentation) {
        if (presentation != 1) {
            HwLog.w(TAG, " presentation is " + presentation);
            return true;
        } else if (isInvalidPhoneNumber(number)) {
            HwLog.w(TAG, " is invalid phone number, and it is ");
            return true;
        } else if (checkIsAllBlank(number)) {
            HwLog.w(TAG, "isInvalidPhoneNumber, checkIsAllBlank is true");
            return true;
        } else if (isCnapSpecialCases(number)) {
            return true;
        } else {
            HwLog.i(TAG, " isInvalidPhoneNumber is false. presentation: " + presentation);
            return false;
        }
    }

    public static boolean checkIsAllBlank(String number) {
        if (number == null || TextUtils.isEmpty(number.replaceAll("\\s*", ""))) {
            return true;
        }
        return false;
    }

    private static boolean isCnapSpecialCases(String n) {
        if (n == null) {
            HwLog.e(TAG, " n is null and not normal or cnap special str");
            return false;
        } else if (n.equals("PRIVATE") || n.equals("P") || n.equals("RES")) {
            HwLog.i(TAG, "isCnapSpecialCases, PRIVATE string: " + n);
            return true;
        } else if (n.equals("UNAVAILABLE") || n.equals("UNKNOWN") || n.equals("UNA") || n.equals("U")) {
            HwLog.i(TAG, "isCnapSpecialCases, UNKNOWN string: " + n);
            return true;
        } else {
            HwLog.i(TAG, "it is not cnap special string");
            return false;
        }
    }

    public static void addToNewContact(Context context, String name, String phone) {
        Intent intent = new Intent("android.intent.action.INSERT");
        intent.setType("vnd.android.cursor.dir/contact");
        intent.setType("vnd.android.cursor.dir/person");
        intent.setType("vnd.android.cursor.dir/raw_contact");
        intent.putExtra("name", name);
        intent.putExtra("phone", phone);
        context.startActivity(intent);
    }

    public static void addToExistContact(Context context, String phone) {
        Intent intent = new Intent("android.intent.action.INSERT_OR_EDIT");
        intent.setType("vnd.android.cursor.item/contact");
        intent.setType("vnd.android.cursor.item/person");
        intent.setType("vnd.android.cursor.item/raw_contact");
        intent.putExtra("phone", phone);
        context.startActivity(intent);
    }

    public static int calibrateBlacklistOption(int nOption) {
        int result = nOption & 3;
        if (result != 0) {
            return result;
        }
        HwLog.w(TAG, "calibrateBlacklistOption: Invalid option, " + nOption + ", correct to " + 3);
        return 3;
    }

    public static String trimPhoneCountryCode(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return "";
        }
        if (phoneNumber.startsWith(ConstValues.PHONE_COUNTRY_CODE_CHINA)) {
            return phoneNumber.substring(ConstValues.PHONE_COUNTRY_CODE_CHINA.length());
        }
        return phoneNumber;
    }

    public static String trimCountryCode(String phoneNumber) {
        int i = 0;
        if (TextUtils.isEmpty(phoneNumber)) {
            return "";
        }
        String prefix;
        for (String prefix2 : sCountryCode) {
            if (phoneNumber.startsWith(prefix2)) {
                return phoneNumber.substring(prefix2.length());
            }
        }
        String[] strArr = sRCSCountryCode;
        int length = strArr.length;
        while (i < length) {
            prefix2 = strArr[i];
            if (phoneNumber.startsWith(prefix2)) {
                return phoneNumber.substring(prefix2.length());
            }
            i++;
        }
        return phoneNumber;
    }
}
