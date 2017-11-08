package com.huawei.permission;

import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import com.huawei.permissionmanager.ui.history.PermissionHistoryActivity;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import java.util.concurrent.TimeUnit;

public class HoldReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "HoldReceiver";
    private Context mContext;
    private NotificationManager mNotificationManager;

    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            HwLog.w(LOG_TAG, "onReceive : Invalid context or intent");
            return;
        }
        String action = intent.getAction();
        if (action == null) {
            HwLog.w(LOG_TAG, "onReceive : Invalid intent action");
            return;
        }
        this.mContext = context;
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        HwLog.d(LOG_TAG, "AppInstallationReceiver onReceive action = " + action);
        int resId;
        if (HoldServiceConst.ADD_VIEW_TOAST_ACTION.equals(action)) {
            String toastContent = intent.getStringExtra(HoldServiceConst.ADD_VIEW_TOAST_CONTENT);
            if (TextUtils.isEmpty(toastContent)) {
                resId = intent.getIntExtra("toastResId", -1);
                if (-1 == resId) {
                    HwLog.w(LOG_TAG, "no content to toast.");
                    return;
                }
                String pkg = intent.getStringExtra("packageName");
                toastContent = context.getString(resId, new Object[]{HsmPackageManager.getInstance().getLabel(pkg)});
            }
            Toast.makeText(context.getApplicationContext(), toastContent, 1).show();
        } else if (HoldServiceConst.PERMISSION_BLOCKED_ACTION.equals(action)) {
            Bundle bundle = intent.getBundleExtra("bundle");
            if (bundle != null) {
                sendPermissionBlockedNotification(bundle);
            }
        } else if (HoldServiceConst.PERMISSION_BLOCKED_TOAST_ACTION.equals(action)) {
            resId = intent.getIntExtra("toastResId", -1);
            if (-1 == resId) {
                HwLog.w(LOG_TAG, "preBlockToast, no content to toast.");
                return;
            }
            Toast.makeText(context.getApplicationContext(), context.getString(resId), 1).show();
        }
    }

    private void sendPermissionBlockedNotification(Bundle bundle) {
        Intent jumpIntent;
        int uid = bundle.getInt(HoldServiceConst.APP_UID);
        String pkgName = bundle.getString("packageName");
        String label = HsmPackageManager.getInstance().getLabel(pkgName);
        String notificationTitle = bundle.getString(HoldServiceConst.PERMISSION_BLOCED_TITLE);
        String notificationContent = bundle.getString(HoldServiceConst.PERMISSION_BLOCKED_CONTENT);
        String ticker = bundle.getString(HoldServiceConst.NOTIFICATION_TICKER);
        boolean userGroupBehavior = bundle.getBoolean(HoldServiceConst.GROUP_BEHAVIOR, false);
        if (ticker.length() > 44) {
            if ("true".equals(this.mContext.getString(R.string.is_ar))) {
                ticker = HoldServiceConst.SUSPENSION_POINTS + ticker.substring(0, 44);
            } else {
                ticker = ticker.substring(0, 44) + HoldServiceConst.SUSPENSION_POINTS;
            }
        }
        HwLog.d(LOG_TAG, "sendPermissionBlockedNotification uid:" + uid + " label:" + label + " pkgName:" + pkgName + " ticker:" + ticker);
        if (userGroupBehavior) {
            jumpIntent = new Intent("android.intent.action.MANAGE_APP_PERMISSIONS");
            jumpIntent.putExtra("android.intent.extra.PACKAGE_NAME", pkgName);
            jumpIntent.putExtra(ShareCfg.EXTRA_HIDE_INFO_BUTTON, true);
            jumpIntent.setPackage("com.android.packageinstaller");
        } else {
            jumpIntent = new Intent(this.mContext, PermissionHistoryActivity.class);
        }
        jumpIntent.setFlags(335544320);
        PendingIntent pendingIntent = PendingIntent.getActivity(this.mContext, 0, jumpIntent, 134217728);
        Builder builder = new Builder(this.mContext);
        builder.setContentIntent(pendingIntent).setAutoCancel(true).setSmallIcon(R.drawable.ic_permission_notification).setTicker(ticker).setShowWhen(true).setContentTitle(notificationTitle).setContentText(notificationContent);
        try {
            this.mNotificationManager.notify(ShareCfg.PERMISSION_BLOCK_NOTIFICATION_ID, builder.build());
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (Exception e) {
            HwLog.e(LOG_TAG, "TimeUnit.MILLISECONDS.sleep exception", e);
        }
    }
}
