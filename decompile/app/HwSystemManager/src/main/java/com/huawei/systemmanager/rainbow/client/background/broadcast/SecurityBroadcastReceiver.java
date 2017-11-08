package com.huawei.systemmanager.rainbow.client.background.broadcast;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.push.CustomTaskHandler;
import com.huawei.systemmanager.rainbow.CloudSwitchHelper;
import com.huawei.systemmanager.rainbow.client.background.service.RainbowCommonService;
import com.huawei.systemmanager.rainbow.client.base.ClientConstant.CloudActions;
import com.huawei.systemmanager.rainbow.client.base.CloudSpfKeys;
import com.huawei.systemmanager.rainbow.client.helper.LocalSharedPrefrenceHelper;
import com.huawei.systemmanager.rainbow.client.tipsmanager.NotificationUtilConst;
import com.huawei.systemmanager.useragreement.UserAgreementHelper;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.content.HsmBroadcastReceiver;

public class SecurityBroadcastReceiver extends HsmBroadcastReceiver {
    private static final String TAG = "SecurityBroadcastReceiver";

    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null || TextUtils.isEmpty(intent.getAction())) {
            HwLog.e(TAG, "SecurityBroadcastReceiver get null intent error!");
        } else {
            sendToBackground(context, intent);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void doInBackground(Context context, Intent intent) {
        if (CloudSwitchHelper.isCloudEnabled()) {
            String action = intent.getAction();
            HwLog.d(TAG, "action = " + action);
            if (!Utility.isTokenRegistered(context) && UserAgreementHelper.getUserAgreementState(context) && "android.intent.action.BOOT_COMPLETED".equals(action)) {
                CustomTaskHandler.getInstance(context).removeMessages(1);
                CustomTaskHandler.getInstance(context).sendEmptyMessageDelayed(1, 60000);
            }
            if (NotificationUtilConst.ACTION_MSG_NOTIFICATION_NOT_NEED_REMIND.equals(action)) {
                Editor editor = context.getSharedPreferences(CloudSpfKeys.FILE_NAME, 0).edit();
                editor.putBoolean(CloudSpfKeys.NEED_CLOUD_SYNC_COMPLETE_NOTIFIED, false);
                editor.commit();
                ((NotificationManager) context.getSystemService("notification")).cancel(ShareCfg.INTELLIGENT_NOTIFICATION_ID);
            }
            if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                Intent serviceIntent = new Intent(CloudActions.INTENT_INIT_CLOUDDB);
                serviceIntent.setClass(context, RainbowCommonService.class);
                context.startService(serviceIntent);
            }
            if (!(NotificationUtilConst.CLOUD_NOTIFICATION_REFUSE.equals(action) || NotificationUtilConst.CLOUD_NOTIFICATION_ALLOW.equals(action) || isReplaceOperation(context, intent) || intent.getData() == null || !"android.intent.action.PACKAGE_ADDED".equals(action))) {
                Intent recIntent = new Intent(CloudActions.INTENT_CLOUD_RECOMMEND_SINGLE_APK);
                recIntent.setClass(context, RainbowCommonService.class);
                recIntent.putExtra("packageName", intent.getData().getSchemeSpecificPart());
                context.startService(recIntent);
            }
            return;
        }
        HwLog.e(TAG, "SecurityBroadcastReceiver the rainbow is not enabled!");
    }

    private boolean isReplaceOperation(Context context, Intent intent) {
        boolean repalce = intent.getBooleanExtra("android.intent.extra.REPLACING", false);
        new LocalSharedPrefrenceHelper(context).putBoolean(CloudSpfKeys.MUTIL_APPS_CHANGE, true);
        return repalce;
    }
}
