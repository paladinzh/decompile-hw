package com.huawei.systemmanager.startupmgr.service;

import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import com.google.common.collect.Sets;
import com.huawei.permissionmanager.db.DBAdapter;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.spacecleanner.engine.base.SpaceConst;
import com.huawei.systemmanager.startupmgr.comm.AwakedRecordInfo;
import com.huawei.systemmanager.startupmgr.comm.AwakedStartupInfo;
import com.huawei.systemmanager.startupmgr.comm.FwkAwakedStartInfo;
import com.huawei.systemmanager.startupmgr.comm.SysCallUtils;
import com.huawei.systemmanager.startupmgr.confdata.StartupConfData;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import java.util.Set;

class AwakedFirstConfirm {
    private static final int AWAKED_NOTIFICATION_ID = 1074141824;
    private static final String TAG = "AwakedFirstConfirm";
    private Set<String> mConfirmPkgs = Sets.newHashSet();
    private final Handler mHandler;

    private static class ForcestopTask implements Runnable {
        private final Context mContext;
        private final String mPkg;

        public ForcestopTask(Context ctx, String pkg) {
            this.mContext = ctx;
            this.mPkg = pkg;
        }

        public void run() {
            if (this.mContext == null || TextUtils.isEmpty(this.mPkg)) {
                HwLog.w(AwakedFirstConfirm.TAG, "forceStopForbidStartupPackage failed!");
                return;
            }
            HwLog.i(AwakedFirstConfirm.TAG, "forceStopForbidStartupPackage while handlePkgFirstAwaked: " + this.mPkg);
            SysCallUtils.forceStopForbidStartupPackage(this.mContext, this.mPkg);
        }
    }

    public AwakedFirstConfirm(Handler handler) {
        this.mHandler = handler;
    }

    public void handlePkgFirstAwaked(Context ctx, FwkAwakedStartInfo info) {
        HwLog.d(TAG, "handlePkgFirstAwaked in " + info.toString());
        String pkgName = info.mPkgName;
        if (this.mConfirmPkgs.contains(pkgName)) {
            HwLog.w(TAG, "handlePkgFirstAwaked already confirmed");
            return;
        }
        boolean bAllow = StartupConfData.getDftProviderServiceCfg(ctx, pkgName);
        String callerPkgName = SysCallUtils.getPackageByPidUid(ctx, info.mCallerPid, info.mCallerUid);
        if (TextUtils.isEmpty(callerPkgName)) {
            HwLog.w(TAG, "handlePkgFirstAwaked callerPkgName is empty!");
        } else {
            AwakedStartupInfo awakedInfo = new AwakedStartupInfo(info.mPkgName, bAllow);
            awakedInfo.setLastCaller(callerPkgName);
            awakedInfo.persistFullData(ctx, true);
            if (!(bAllow || info.mCallerPid == SpaceConst.SCANNER_TYPE_ALL)) {
                this.mHandler.postDelayed(new ForcestopTask(ctx, pkgName), 500);
                if (DBAdapter.getAwakedAppNotifySwitchOpenStatus(ctx)) {
                    sendNotificationForNewArrivedPkg(ctx, pkgName, callerPkgName);
                }
            }
            AwakedRecordInfo recordInfo = AwakedRecordInfo.createWhenFirstConfirm(pkgName, callerPkgName, info.mType, bAllow);
            HwLog.d(TAG, "handlePkgFirstAwaked record it: " + recordInfo);
            recordInfo.insertOrUpdateRecordCountToDB(ctx);
            this.mConfirmPkgs.add(pkgName);
        }
    }

    public void removeSingleConfirmedPackage(String pkgName) {
        this.mConfirmPkgs.remove(pkgName);
    }

    public void cleanConfirmedCached() {
        this.mConfirmPkgs.clear();
    }

    public void cancelAwakedNotification(Context ctx) {
        ((NotificationManager) ctx.getSystemService("notification")).cancel(AWAKED_NOTIFICATION_ID);
    }

    private void sendNotificationForNewArrivedPkg(Context ctx, String pkgName, String callerPkgName) {
        if (SysCallUtils.checkUser()) {
            HwLog.i(TAG, "sendNotificationForNewArrivedPkg, pkgName:" + pkgName + ", callerPkgName:" + callerPkgName);
            CharSequence csTitle = getTitleCharSequence(ctx, pkgName, callerPkgName);
            ((NotificationManager) ctx.getSystemService("notification")).notify(AWAKED_NOTIFICATION_ID, new Builder(ctx).setSmallIcon(R.drawable.stat_notify_startup).setTicker(csTitle).setContentTitle(csTitle).setContentText(strResToCharSequence(ctx.getString(R.string.startupmgr_awaked_forbid_notification_content))).setSound(null).setAutoCancel(true).setContentIntent(getContentPendingIntent(ctx)).setDeleteIntent(getDeletePendingIntent(ctx)).build());
            return;
        }
        HwLog.i(TAG, "current is not owner, didnot send notfication");
    }

    private CharSequence getTitleCharSequence(Context ctx, String pkgName, String callerPkgName) {
        if (!this.mConfirmPkgs.isEmpty()) {
            return strResToCharSequence(ctx.getString(R.string.startupmgr_awaked_forbid_notification_multi_title));
        }
        return strResToCharSequence(ctx.getString(R.string.startupmgr_awaked_forbid_notification_single_title, new Object[]{HsmPackageManager.getInstance().getLabel(callerPkgName), HsmPackageManager.getInstance().getLabel(pkgName)}));
    }

    private CharSequence strResToCharSequence(String value) {
        return value.subSequence(0, value.length());
    }

    private PendingIntent getContentPendingIntent(Context ctx) {
        Intent intent = new Intent("com.huawei.android.hsm.AWAKE_NOTIFICATION_CLICK");
        intent.setClass(ctx, StartupResidentService.class);
        return PendingIntent.getService(ctx, 0, intent, 134217728);
    }

    private PendingIntent getDeletePendingIntent(Context ctx) {
        Intent intent = new Intent("com.huawei.android.hsm.AWAKE_NOTIFICATION_DELETE");
        intent.setClass(ctx, StartupResidentService.class);
        return PendingIntent.getService(ctx, 0, intent, 134217728);
    }
}
