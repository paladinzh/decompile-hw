package com.huawei.keyguard.monitor;

import android.content.ContentResolver;
import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.text.TextUtils;
import com.huawei.bd.Reporter;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.database.ClientHelper;
import com.huawei.keyguard.support.magazine.BigPictureInfo;
import com.huawei.keyguard.support.magazine.BigPictureInfo.DescriptionInfo;
import com.huawei.keyguard.support.magazine.MagazineUtils;
import com.huawei.keyguard.support.magazine.MagazineWallpaper;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.OsUtils;
import com.huawei.keyguard.util.SingleHandUtils;
import com.huawei.openalliance.ad.inter.HiAdMagLock.Builder;
import com.huawei.openalliance.ad.inter.constant.EventType;
import com.huawei.theme.v1.HiStat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HwLockScreenReporter {
    private static Runnable mUpdater = new Runnable() {
        public void run() {
            Context context = GlobalContext.getContext();
            if (context == null) {
                HwLog.w("HwLockScreenReporter", "report for succ verify fail as no context");
                return;
            }
            ContentResolver resolver = context.getContentResolver();
            int currentUser = OsUtils.getCurrentUser();
            String successTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Long.valueOf(new Date().getTime()));
            System.putStringForUser(resolver, "verify_unlock_success_time", successTime, currentUser);
            if (System.getStringForUser(resolver, "verify_unlock_failed_time", currentUser) != null) {
                String failTime = System.getStringForUser(resolver, "verify_unlock_failed_time", currentUser);
                String firstLockTime = System.getStringForUser(resolver, "first_lock_time", currentUser);
                HwLockScreenReporter.report(context, 174, "{SuccessTime:" + successTime + ",FailTime:" + failTime + ",FirstLockTime:" + firstLockTime + ",LockSettingTime:" + System.getStringForUser(resolver, "lock_settings_time", currentUser) + ",CurrentUser:" + currentUser + "}");
                System.putStringForUser(resolver, "verify_unlock_failed_time", null, currentUser);
            }
        }
    };
    private static String[] mValues = new String[2];

    public static boolean report(Context context, int eventID, String eventMsg) {
        if (eventMsg == null) {
            try {
                HwLog.w("HwLockScreenReporter", "report msg is null");
                return false;
            } catch (Exception ex) {
                HwLog.w("HwLockScreenReporter", "report type:" + eventID + " msg:" + eventMsg + "Report fail and error is :", ex);
                return false;
            }
        }
        if (eventID < 2 && eventID >= 0) {
            if (eventMsg.equalsIgnoreCase(mValues[eventID])) {
                return false;
            }
            mValues[eventID] = eventMsg;
        }
        boolean reporter = Reporter.e(context, eventID, eventMsg);
        HwLog.w("HwLockScreenReporter", "report result: " + reporter + " type:" + eventID + " msg:" + eventMsg);
        return reporter;
    }

    public static void statReport(Context context, int eventID, String eventMsg) {
        if (!MagazineUtils.isMagazineEnable(context) || !isChinaVersion()) {
            return;
        }
        if (eventMsg == null) {
            try {
                HwLog.w("HwLockScreenReporter", "report msg is null");
                return;
            } catch (Exception ex) {
                HwLog.w("HwLockScreenReporter", "report type:" + eventID + " msg:" + eventMsg + "Report fail and error is :", ex);
                return;
            }
        }
        HiStat.e(context, String.valueOf(eventID), eventMsg);
    }

    public static void reportMagazinePictureInfo(Context context, int eventId, int switchType) {
        try {
            BigPictureInfo bigPictureInfo = MagazineWallpaper.getInst(context).getPictureInfo(switchType);
            if (bigPictureInfo != null && !bigPictureInfo.getIsCustom()) {
                HwLog.i("HwLockScreenReporter", "report msg is :{picture: " + bigPictureInfo.getPicUniqueName() + "}");
                report(context, eventId, "{picture: " + bigPictureInfo.getPicUniqueName() + ", channelId: " + bigPictureInfo.getChannelId() + "}");
            }
        } catch (Exception ex) {
            HwLog.e("HwLockScreenReporter", "reportMagazinePictureInfo fail and error is :" + ex);
        }
    }

    public static void reportVerifyTimeOut(Context context, long timeToWait, int tstCnt) {
        final int currentUser = OsUtils.getCurrentUser();
        final Context context2 = context;
        final long j = timeToWait;
        final int i = tstCnt;
        GlobalContext.getBackgroundHandler().post(new Runnable() {
            public void run() {
                String failTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Long.valueOf(new Date().getTime()));
                OsUtils.putSystemString(context2, "verify_unlock_failed_time", failTime);
                String successTime = System.getStringForUser(context2.getContentResolver(), "verify_unlock_success_time", currentUser);
                String firstLockTime = System.getStringForUser(context2.getContentResolver(), "first_lock_time", currentUser);
                HwLockScreenReporter.report(context2, 175, "{SuccessTime:" + successTime + ",FailTime:" + failTime + ",FirstLockTime:" + firstLockTime + ",LockSettingTime:" + System.getStringForUser(context2.getContentResolver(), "lock_settings_time", currentUser) + ",WaitTime:" + j + ",tryCount:" + i + ",CurrentUser:" + currentUser + "}");
            }
        });
    }

    public static void reportUnlockInfo(Context context, int eventId, boolean verifyResult, long startUnlockInputTime, int singleHandStatus) {
        HwLog.w("HwLockScreenReporter", "reportUnlockInfo " + eventId + " -- " + startUnlockInputTime + " " + verifyResult);
        if (eventId > 0 && startUnlockInputTime > 0) {
            long unlockUsedTime = System.currentTimeMillis() - startUnlockInputTime;
            if (verifyResult && (eventId == 152 || eventId == 150)) {
                GlobalContext.getBackgroundHandler().post(mUpdater);
            }
            report(context, eventId, "{VerifySucess:" + verifyResult + ",CostTime:" + unlockUsedTime + ",UseHand:" + SingleHandUtils.getSingleHandleName(singleHandStatus) + "}");
        }
    }

    public static void reportAdEvent(Context context, BigPictureInfo info, EventType event) {
        if (context != null && info != null && MagazineUtils.isMagazineEnable(context) && isChinaVersion()) {
            DescriptionInfo des = info.getDescriptionInfo();
            if (!(des == null || TextUtils.isEmpty(des.getAdcontentid()))) {
                new Builder().build().reportEvent(context, des.getAdcontentid(), event);
                if (EventType.REMOVE == event) {
                    ClientHelper.getInstance().insertDeletedHiAdIds(context, des.getAdcontentid());
                }
            }
        }
    }

    public static void reportPicInfoAdEvent(Context context, EventType event, int statType, int direct) {
        if (context != null && MagazineUtils.isMagazineEnable(context) && isChinaVersion()) {
            BigPictureInfo bigPic = MagazineWallpaper.getInst(context).getPictureInfo(direct);
            if (bigPic != null) {
                reportAdEvent(context, bigPic, event);
                statReport(context, statType, "{picture:" + bigPic.getPicName() + "}");
            }
        }
    }

    private static boolean isChinaVersion() {
        return "zh".equals(SystemProperties.get("ro.product.locale.language")) ? "CN".equals(SystemProperties.get("ro.product.locale.region")) : false;
    }
}
