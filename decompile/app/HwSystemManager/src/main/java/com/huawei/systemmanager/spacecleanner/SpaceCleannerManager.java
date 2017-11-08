package com.huawei.systemmanager.spacecleanner;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.utils.NotificationUtil;
import com.huawei.systemmanager.util.HwLog;

public class SpaceCleannerManager {
    private static final String TAG = "SpaceCleannerManager";
    private static boolean isSupportHwFileAnalysis = false;
    private static final Object sMutexSpaceCleannerManager = new Object();
    private static SpaceCleannerManager sSingleton;

    public static boolean isSupportHwFileAnalysis() {
        return isSupportHwFileAnalysis;
    }

    public static SpaceCleannerManager getInstance() {
        SpaceCleannerManager spaceCleannerManager;
        synchronized (sMutexSpaceCleannerManager) {
            if (sSingleton == null) {
                sSingleton = new SpaceCleannerManager();
                init();
            }
            spaceCleannerManager = sSingleton;
        }
        return spaceCleannerManager;
    }

    public static void init() {
        isSupportHwFileAnalysis = ifSupportHwFileAnalysis();
    }

    public static boolean ifSupportHwFileAnalysis() {
        boolean isSupport = true;
        if (!SystemProperties.getBoolean("ro.config.hw_file_analysis_on", true)) {
            isSupport = false;
        }
        HwLog.i(TAG, "isSupportHwFileAnalysis:" + isSupport);
        return isSupport;
    }

    public PendingIntent createFileAnalysisIntent(Context context, int shouldAnalysisTrashType) {
        Intent intent = new Intent();
        intent.setPackage(context.getPackageName());
        intent.setClass(context, SpaceManagerActivity.class);
        intent.putExtra(SpaceManagerActivity.KEY_CREATE_NEW_HANDLER_ID, true);
        intent.putExtra(SpaceManagerActivity.KEY_ONLY_SCAN_INTERNAL, true);
        intent.putExtra(SpaceManagerActivity.FROM_OP, 1);
        intent.putExtra(SpaceManagerActivity.ANALYSIS_TRASH_TYPE, shouldAnalysisTrashType);
        intent.setFlags(268468224);
        return PendingIntent.getActivity(context, 0, intent, ShareCfg.PERMISSION_MODIFY_CALENDAR);
    }

    public void cancelFileAnalysisNotify() {
        ((NotificationManager) GlobalContext.getContext().getSystemService("notification")).cancel(NotificationUtil.SPACE_ANALYSIS_REPORT_NOTIFY_ID);
        HwLog.i(TAG, "cancelFileAnalysisNotify");
    }
}
