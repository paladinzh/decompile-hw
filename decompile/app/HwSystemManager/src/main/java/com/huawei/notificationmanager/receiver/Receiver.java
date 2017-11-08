package com.huawei.notificationmanager.receiver;

import android.app.Notification;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import com.huawei.notificationmanager.cloud.CloudNotificationControlChange;
import com.huawei.notificationmanager.common.ConstValues;
import com.huawei.notificationmanager.db.DBAdapter;
import com.huawei.notificationmanager.ui.NotificationManagmentActivity;
import com.huawei.notificationmanager.util.Helper;
import com.huawei.notificationmanager.util.NmCenterDefValueXmlHelper;
import com.huawei.notificationmanager.util.NotificationParser;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.grule.GRuleManager;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.mainscreen.MainScreenActivity;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.ControlRangeWhiteList;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.content.HsmBroadcastReceiver;

public class Receiver extends HsmBroadcastReceiver {
    private static final String ACTION_NOTIFICATION_USER_INITIALIZE = "android.intent.action.USER_INITIALIZE";
    private static final String KEY_USERID = "userid";
    private static final String NOTIFICATION_APP_PKG_NAME = "packageName";
    private static final String NOTIFICATION_BUNDLE = "notifyBundle";
    private static final String NOTIFICATION_SEND = "sendNotify";
    private static final String TAG = "NotificationManagerReceiver";

    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            HwLog.w(TAG, "onReceive : Invalid context or intent");
            return;
        }
        String intentAction = intent.getAction();
        if (intentAction == null) {
            HwLog.w(TAG, "onReceive : Invalid intent action");
            return;
        }
        HwLog.i(TAG, "onReceive action = " + intentAction);
        if (ConstValues.NOTIFICATION_ACTIVITY.equals(intentAction)) {
            handleNotificationActivityAction(context, intent);
        } else if (ConstValues.ACTION_NOTIFICATION_DETECT.equals(intentAction)) {
            HwLog.d(TAG, "onReceive: Skip recording notification block event. Package name = " + intent.getStringExtra("packageName"));
        } else {
            HwLog.d(TAG, "onReceive, send action to background");
            intent.putExtra("userid", context.getUserId());
            sendToBackground(context, intent);
        }
    }

    public void doInBackground(Context context, Intent intent) {
        if (ConstValues.ACTION_NOTIFICATION_DETECT.equals(intent.getAction())) {
            interceptNotification(context, intent);
        } else if (ConstValues.ACTION_NOTIFICATION_ALLOW_FORAPK.equals(intent.getAction())) {
            HsmStat.statClickNotificationFilter(true);
            handleNotificationAllowAction(context, intent);
        } else if (ConstValues.ACTION_NOTIFICATION_REFUSE_FORAPK.equals(intent.getAction())) {
            HsmStat.statClickNotificationFilter(false);
            handleNotificationRefuseAction(context, intent);
        } else if ("android.intent.action.PACKAGE_ADDED".equals(intent.getAction())) {
            handlePackageAddedAction(context, intent);
        } else if ("android.intent.action.PACKAGE_REMOVED".equals(intent.getAction())) {
            handlePackageRemovedAction(context, intent);
        } else if (ControlRangeWhiteList.WHITE_LIST_CHANGE_ACTION.equals(intent.getAction())) {
            new CloudNotificationControlChange(context).handleWhiteListChange(intent.getStringArrayListExtra(ControlRangeWhiteList.CHANGED_ADD_LIST_KEY), intent.getStringArrayListExtra(ControlRangeWhiteList.CHANGED_MINUS_LIST_KEY));
        } else if ("com.rainbow.blacklist.change".equals(intent.getAction())) {
            new CloudNotificationControlChange(context).handleBlackListChange(intent.getStringArrayListExtra(ControlRangeWhiteList.CHANGED_ADD_LIST_KEY), intent.getStringArrayListExtra(ControlRangeWhiteList.CHANGED_MINUS_LIST_KEY));
        } else if (ACTION_NOTIFICATION_USER_INITIALIZE.equals(intent.getAction())) {
            disableMainScreen(context, intent.getIntExtra("userid", -1));
        }
    }

    private void handleNotificationActivityAction(Context context, Intent intent) {
        Intent appIntent = new Intent();
        appIntent.setClass(context, NotificationManagmentActivity.class);
        appIntent.putExtra("showTabsNumber", 0);
        appIntent.addFlags(ShareCfg.PERMISSION_MODIFY_CALENDAR);
        context.startActivity(appIntent);
    }

    private void interceptNotification(Context context, Intent intent) {
        String pkgName = intent.getStringExtra("packageName");
        if (GRuleManager.getInstance().shouldMonitor(context, "notification", pkgName)) {
            Bundle bundle = intent.getBundleExtra(NOTIFICATION_BUNDLE);
            if (bundle == null) {
                HwLog.w(TAG, "interceptNotification: Fail to getBundleExtra. pkgName = " + pkgName);
                return;
            }
            HwLog.i(TAG, "interceptNotification : Blocked a notification from " + pkgName);
            NotificationParser notificationParser = new NotificationParser((Notification) bundle.get(NOTIFICATION_SEND));
            String notifyText = notificationParser.getNotificationContent();
            if (notifyText.isEmpty()) {
                notifyText = context.getString(R.string.empty_notify_tip);
            }
            String notifyTitle = notificationParser.getTickerText();
            ContentValues values = new ContentValues();
            values.put("packageName", pkgName);
            values.put("logDatetime", Long.valueOf(System.currentTimeMillis()));
            values.put("logTitle", notifyTitle);
            values.put("logText", notifyText);
            try {
                new DBAdapter(context).insertLog(values);
                Helper.setLogChangeFlag(context, true);
            } catch (Exception e) {
                HwLog.e(TAG, "interceptNotification exception:" + e.getMessage());
            }
            return;
        }
        HwLog.w(TAG, "interceptNotification: log from an app not being monitored, " + pkgName);
    }

    private void handleNotificationAllowAction(Context context, Intent intent) {
        HwLog.d(TAG, "handleNotificationAllowAction starts");
        String pkgName = intent.getStringExtra("pkgName");
        int uid = intent.getIntExtra("uid", 0);
        if (new DBAdapter(context).updateCfg(pkgName, true)) {
            Helper.setCfgChangeFlag(context, true);
        }
        Helper.setNotificationsEnabledForPackage(pkgName, uid, Boolean.valueOf(true));
        Intent intentForFramework = new Intent(ConstValues.ACTION_NOTIFICATION_ALLOW);
        intentForFramework.putExtra("uid", uid);
        intentForFramework.putExtra("pkgName", pkgName);
        context.sendBroadcastAsUser(intentForFramework, UserHandle.CURRENT, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
        HwLog.i(TAG, "Notification cfg is updated in APK, notify framework to update status");
    }

    private void handleNotificationRefuseAction(Context context, Intent intent) {
        HwLog.d(TAG, "handleNotificationRefuseAction starts");
        String pkgName = intent.getStringExtra("pkgName");
        int uid = intent.getIntExtra("uid", 0);
        if (new DBAdapter(context).updateCfg(pkgName, true)) {
            Helper.setCfgChangeFlag(context, true);
        }
        Helper.setNotificationsEnabledForPackage(pkgName, uid, Boolean.valueOf(false));
        Intent intentForFramework = new Intent(ConstValues.ACTION_NOTIFICATION_REFUSE);
        intentForFramework.putExtra("uid", uid);
        intentForFramework.putExtra("pkgName", pkgName);
        context.sendBroadcastAsUser(intentForFramework, UserHandle.CURRENT, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
        HwLog.i(TAG, "Notification cfg is updated in APK, notify framework to update status");
    }

    private void handlePackageAddedAction(Context context, Intent intent) {
        if (!shouldSkipAction(context, intent)) {
            updateDBWhenInstall(context, intent.getData().getSchemeSpecificPart());
            Helper.setCfgChangeFlag(context, true);
        }
    }

    private void updateDBWhenInstall(Context context, String pkgName) {
        new DBAdapter(context).initNewApp(pkgName, Helper.getDefaultValue(new NmCenterDefValueXmlHelper(), context, pkgName, false));
    }

    private void handlePackageRemovedAction(Context context, Intent intent) {
        if (!shouldSkipAction(context, intent)) {
            String pkgName = intent.getData().getSchemeSpecificPart();
            DBAdapter dbAdapter = new DBAdapter(context);
            dbAdapter.deleteCfg(pkgName);
            dbAdapter.deleteLog(pkgName);
            Helper.setCfgChangeFlag(context, true);
            Helper.setLogChangeFlag(context, true);
        }
    }

    private boolean shouldSkipAction(Context context, Intent intent) {
        if (isPackageReplaceAction(intent)) {
            HwLog.d(TAG, "shouldSkipAction: replace action ,skip");
            return true;
        }
        String pkgName = intent.getData().getSchemeSpecificPart();
        if (GRuleManager.getInstance().shouldMonitor(context, "notification", pkgName)) {
            return false;
        }
        HwLog.d(TAG, "shouldSkipAction: Not being monitored ,skip. package name = " + pkgName);
        return true;
    }

    private boolean isPackageReplaceAction(Intent intent) {
        return intent.getBooleanExtra("android.intent.extra.REPLACING", false);
    }

    private void disableMainScreen(Context ctx, int userId) {
        if (userId >= 0) {
            Context context;
            try {
                context = ctx.createPackageContextAsUser(ctx.getPackageName(), 0, new UserHandle(userId));
            } catch (Exception e) {
                context = null;
            }
            if (context != null) {
                UserInfo currentUser = ((UserManager) context.getSystemService("user")).getUserInfo(context.getUserId());
                if (currentUser != null ? currentUser.isManagedProfile() : false) {
                    context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, MainScreenActivity.class), 2, 1);
                }
            }
        }
    }
}
