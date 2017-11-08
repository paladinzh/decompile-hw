package com.huawei.systemmanager.rainbow.client.background.handle.serv;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.notificationmanager.common.ConstValues;
import com.huawei.notificationmanager.db.DBProvider;
import com.huawei.notificationmanager.util.Helper;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.comm.grule.GRuleManager;
import com.huawei.systemmanager.rainbow.client.background.handle.IIntentHandler;
import com.huawei.systemmanager.rainbow.client.base.CloudSpfKeys;
import com.huawei.systemmanager.rainbow.client.helper.LocalSharedPrefrenceHelper;
import com.huawei.systemmanager.rainbow.client.tipsmanager.NotificationItem;
import com.huawei.systemmanager.rainbow.client.tipsmanager.NotificationUtil;
import com.huawei.systemmanager.rainbow.client.tipsmanager.NotificationUtilConst;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.NotificationTip;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPkgUtils;
import java.util.List;

public class CloudNotificationServHandle implements IIntentHandler {
    private static final String TAG = "CloudNotificationServHandle";

    private void changeAppWithCloudPermission(android.content.Context r1, java.lang.String r2, int r3, int r4) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.systemmanager.rainbow.client.background.handle.serv.CloudNotificationServHandle.changeAppWithCloudPermission(android.content.Context, java.lang.String, int, int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.systemmanager.rainbow.client.background.handle.serv.CloudNotificationServHandle.changeAppWithCloudPermission(android.content.Context, java.lang.String, int, int):void");
    }

    public void handleIntent(Context ctx, Intent intent) {
        if (NotificationUtilConst.CLOUD_NOTIFICATION_REFUSE.equals(intent.getAction())) {
            handleRefuseCloud(ctx, intent.getExtras());
        } else if (NotificationUtilConst.CLOUD_NOTIFICATION_ALLOW.equals(intent.getAction())) {
            handleAllowCloud(ctx, intent.getExtras());
        }
    }

    private void handleRefuseCloud(Context ctx, Bundle bundle) {
        ((NotificationManager) ctx.getSystemService("notification")).cancel(NotificationUtilConst.CLOUD_NOTIFICATION_TIPS_ID);
        if (bundle == null) {
            HwLog.e(TAG, "handleRefuseCloud bundle is null!");
            return;
        }
        List<NotificationItem> notificationItemList = (List) bundle.getSerializable(NotificationUtilConst.BUNDLE_CLOUD_KEY);
        if (notificationItemList == null || notificationItemList.isEmpty()) {
            HwLog.e(TAG, "handleRefuseCloud The notificationItemList is invalid!");
            return;
        }
        if (1 == notificationItemList.size()) {
            dealWithSingleAppCancel(ctx, ((NotificationItem) notificationItemList.get(0)).pkgName);
        } else {
            dealWithMutilAppsCancel(ctx);
        }
        for (NotificationItem item : notificationItemList) {
            HwLog.d(TAG, "The current item is: " + item.toString());
        }
    }

    private void dealWithSingleAppCancel(Context ctx, String pkgName) {
        changeAppRecommendStatus(ctx, pkgName);
    }

    private void dealWithMutilAppsCancel(Context ctx) {
        new LocalSharedPrefrenceHelper(ctx).putBoolean(CloudSpfKeys.MUTIL_APPS_CHANGE, false);
    }

    private void handleAllowCloud(Context ctx, Bundle bundle) {
        ((NotificationManager) ctx.getSystemService("notification")).cancel(NotificationUtilConst.CLOUD_NOTIFICATION_TIPS_ID);
        if (bundle == null) {
            HwLog.e(TAG, "handleAllowCloud bundle is null!");
            return;
        }
        List<NotificationItem> notificationItemList = (List) bundle.getSerializable(NotificationUtilConst.BUNDLE_CLOUD_KEY);
        if (notificationItemList == null || notificationItemList.isEmpty()) {
            HwLog.e(TAG, "handleAllowCloud The notificationItemList is invalid!");
            return;
        }
        for (NotificationItem item : notificationItemList) {
            HwLog.d(TAG, "The current item is: " + item.toString());
            changeAppRecommendStatus(ctx, item.pkgName);
            int uid = HsmPkgUtils.getPackageUid(item.pkgName);
            if (-1 != uid) {
                changeAppWithCloudNotification(ctx, item.pkgName, item.notification, uid);
                changeAppWithCloudPermission(ctx, item.pkgName, item.permissionCode, item.permissionCfg);
            }
        }
    }

    private void changeAppRecommendStatus(Context ctx, String pkgName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("packageName", pkgName);
        contentValues.put(NotificationTip.NOTIFICATION_TIP_STATUS, Integer.valueOf(1));
        ctx.getContentResolver().update(NotificationTip.CONTENT_OUTERTABLE_URI, contentValues, "packageName = \"" + pkgName + SqlMarker.QUOTATION, null);
    }

    private void changeAppWithCloudNotification(Context ctx, String pkgName, int cloudValue, int uid) {
        if (!GRuleManager.getInstance().shouldMonitor(ctx, "notification", pkgName)) {
            HwLog.e(TAG, "changeAppUseCloudNotification current app should not monitor!");
        } else if (2 == cloudValue) {
            HwLog.e(TAG, "changeAppUseCloudNotification cloudValue is invalid!");
        } else if (2 != NotificationUtil.getNotificationStatus(ctx, pkgName)) {
            HwLog.e(TAG, "changeAppUseCloudNotification userSelection works!");
        } else {
            if (cloudValue == 0) {
                Helper.setNotificationsEnabledForPackage(pkgName, uid, Boolean.valueOf(true));
            }
            if (1 == cloudValue) {
                Helper.setNotificationsEnabledForPackage(pkgName, uid, Boolean.valueOf(false));
            }
            ContentValues value = new ContentValues();
            value.put("packageName", pkgName);
            value.put(ConstValues.NOTIFICATION_CFG, Integer.valueOf(1));
            ctx.getContentResolver().insert(DBProvider.URI_NOTIFICATION_CFG, value);
        }
    }
}
