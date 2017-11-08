package com.huawei.systemmanager.spacecleanner.utils;

import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build.VERSION;
import android.os.SystemProperties;
import android.text.format.Time;
import com.google.android.collect.Maps;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.Storage.StorageHelper;
import com.huawei.systemmanager.comparator.SizeComparator;
import com.huawei.systemmanager.spacecleanner.Const;
import com.huawei.systemmanager.spacecleanner.engine.base.SpaceConst;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class AppCleanUpAndStorageNotifyUtils {
    public static final String ACTION_LACK_MEMORY_NOTIFY = "com.huawei.android.LACK_MEMORY_NOTIFY";
    public static final String ACTION_NOTIFICATION_BUTTON_CLICK_LEFT = "com.huawei.android.NOTIFICATION_BUTTON_CLICK_LEFT";
    public static final String ACTION_NOTIFICATION_BUTTON_CLICK_RIGHT = "com.huawei.android.NOTIFICATION_BUTTON_CLICK_RIGHT";
    public static final String AUTH = "com.huawei.systemmanager.RarelyUsedAppProvider";
    public static final int DAILY_CHECK_STORAGE_TIME_HOUR = 20;
    public static final int DAILY_CHECK_STORAGE_TIME_MIN = 0;
    public static final int DANGROUS_SPACE_USAGE_LEVEL = 85;
    public static final int EXTRA_TIME_FOR_DEALY = 60000;
    public static final String IS_LACK_SPACE_SHOW = "is_lack_space_show";
    public static final int LACK_SPACE_NOTIFICATION_ID = 2131231025;
    public static final long LOW_STORAGE_ABSOLUTE = 524288000;
    public static final int LOW_STORAGE_PERCENT = 10;
    public static final int MSG_BOOT_COMPLETE = 2;
    public static final int MSG_INSTALL_PACKAGE = 0;
    public static final int MSG_LACK_MEMORY_NOTIFY = 3;
    public static final int MSG_NOTIFICATION_BUTTON_CLICK_LEFT = 4;
    public static final int MSG_NOTIFICATION_BUTTON_CLICK_RIGHT = 5;
    public static final int MSG_UNINSTALL_PACKAGE = 1;
    public static final int NOTIFICATION_GAP_SPACE_LEVEL = 500;
    public static final int NOTIFICATION_TYPE_EXTERNAL_ONE_BUTTON = 2;
    public static final int NOTIFICATION_TYPE_EXTERNAL_TWO_BUTTON = 4;
    public static final int NOTIFICATION_TYPE_INNER_ONE_BUTTON = 1;
    public static final int NOTIFICATION_TYPE_INNER_TWO_BUTTON = 3;
    public static final int NOTIFICATION_TYPE_NO = 0;
    public static final String NOT_COMMONLY_USED_NOTIFY = "not_commonly_used_notify";
    public static final int ONE_DAY_TO_MILLISECOND = 86400000;
    public static final String PACKAGE_NAME_ARGS = "package_name_args";
    public static final String SERVICE_INTENT_ARGS = "service_intent_args";
    public static final String TAG = "AppCleanUpAndStorageNotifyUtils";
    public static final SizeComparator<RarelyUsedAppBean> UNUSEDAPP_COMPARATOR = new SizeComparator<RarelyUsedAppBean>() {
        public long getKey(RarelyUsedAppBean t) {
            if (t != null) {
                return (long) t.getDayNotUsed();
            }
            return 0;
        }
    };
    private static final int UNUSEED_APP_DAY_LIMIT = 15;
    private static final int VALIDITY_PERIOD_YEARS = 5;

    public static HashMap<String, RarelyUsedAppBean> getUnusedApp(Context ctx) {
        SharedPreferences unusedTimePerfer = ctx.getSharedPreferences(Const.SPACE_CLEAN_SHARED_PERFERENCE, 0);
        long dayMill = unusedTimePerfer.getLong(Const.ONE_DAY_TO_MILLISECOND_KEY, 86400000);
        int dayLimit = unusedTimePerfer.getInt(Const.UNUSEED_DAY_LIMIT_KEY, 15);
        HashMap<String, RarelyUsedAppBean> map = Maps.newHashMap();
        List<RarelyUsedAppBean> showList = new ArrayList();
        HashMap<String, RarelyUsedAppBean> unusedMap = Maps.newHashMap();
        try {
            RarelyUsedAppBean bean;
            String pkgName;
            int dayNotUsed;
            long curTime = System.currentTimeMillis();
            for (PackageInfo pkg : getAllThirdPackages(ctx)) {
                bean = new RarelyUsedAppBean();
                pkgName = pkg.packageName;
                if (!map.containsKey(pkgName)) {
                    long lastUpdateTime = pkg.lastUpdateTime;
                    dayNotUsed = (int) ((curTime - lastUpdateTime) / dayMill);
                    if (dayNotUsed >= dayLimit && ((long) dayNotUsed) < SpaceConst.FILE_ANALYSIS_VALIDITY_PERIOD_DAYS) {
                        HwLog.i(TAG, " can remove AllThirdPackages getPackageName()=" + pkgName + " dayNotUsed=" + dayNotUsed + " lastUpdateTime = " + (lastUpdateTime / dayMill) + " now" + (curTime / dayMill));
                        bean.setAppName(pkgName);
                        bean.setPackageName(pkgName);
                        bean.setTimestamp(lastUpdateTime);
                        bean.setDayNotUsed(dayNotUsed);
                        map.put(pkgName, bean);
                    }
                }
            }
            Calendar cal = Calendar.getInstance();
            cal.add(1, -5);
            for (UsageStats unUsedApp : ((UsageStatsManager) ctx.getSystemService("usagestats")).queryUsageStats(4, cal.getTimeInMillis(), curTime)) {
                bean = new RarelyUsedAppBean();
                pkgName = unUsedApp.getPackageName();
                if (HsmPackageManager.getInstance().isRemovable(pkgName)) {
                    bean.setAppName(pkgName);
                    bean.setPackageName(pkgName);
                    long timeStamp = unUsedApp.getLastTimeUsed();
                    bean.setTimestamp(timeStamp);
                    dayNotUsed = (int) ((curTime - timeStamp) / dayMill);
                    HwLog.i(TAG, " can remove unUsedApp.getPackageName()=" + pkgName + " dayNotUsed=" + dayNotUsed + " timeStamp = " + (timeStamp / dayMill) + " now" + (curTime / dayMill));
                    bean.setDayNotUsed(dayNotUsed);
                    if (map.containsKey(pkgName)) {
                        map.remove(pkgName);
                    }
                    if (dayNotUsed >= dayLimit && ((long) dayNotUsed) < SpaceConst.FILE_ANALYSIS_VALIDITY_PERIOD_DAYS) {
                        map.put(pkgName, bean);
                    }
                }
            }
            if (!map.isEmpty()) {
                showList.addAll(map.values());
                Collections.sort(showList, UNUSEDAPP_COMPARATOR);
                for (RarelyUsedAppBean unUsedBean : showList) {
                    HwLog.i(TAG, " RarelyUsedAppBean=" + unUsedBean.getAppName() + " dayNotUsed=" + unUsedBean.getDayNotUsed());
                    unusedMap.put(unUsedBean.getAppName(), unUsedBean);
                }
            }
        } catch (Exception e) {
            HwLog.e(TAG, "getUnusedApp error!");
            e.printStackTrace();
        }
        return unusedMap;
    }

    private static List<PackageInfo> getAllThirdPackages(Context context) {
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> canRemovePkgs = new ArrayList();
        for (PackageInfo pkg : PackageManagerWrapper.getInstalledPackages(pm, 8192)) {
            if (HsmPackageManager.getInstance().isRemovable(pkg.packageName)) {
                canRemovePkgs.add(pkg);
            }
        }
        return canRemovePkgs;
    }

    private static PendingIntent getPendingIntent(Context context, String action) {
        Intent intent = new Intent(action);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, ShareCfg.PERMISSION_MODIFY_CALENDAR);
        intent.setPackage(context.getPackageName());
        return sender;
    }

    private static long getTriggerTime(int hour, int minute) {
        Time time = new Time();
        long nowTime = System.currentTimeMillis();
        time.set(nowTime);
        time.hour = hour;
        time.minute = minute;
        time.second = 0;
        long PM_8Time = time.toMillis(false);
        return nowTime < PM_8Time ? PM_8Time : PM_8Time + 86400000;
    }

    private static int getStorageNotificationType(Context context) {
        boolean isDefaultStorageSDCard = isDefaultSDCard();
        boolean isExternalStorageDangerous = checkExternalStorageDangerous(context);
        StorageHelper helper = StorageHelper.getStorage();
        long InternalAvailableSize = helper.getAvalibaleSize(0);
        long ExternalAvailableSzie = helper.getAvalibaleSize(1);
        HwLog.d(TAG, "External Storage Dangerous is " + isExternalStorageDangerous);
        HwLog.d(TAG, "InternalAvailableSize is " + InternalAvailableSize);
        HwLog.d(TAG, "ExternalAvailableSzie is " + ExternalAvailableSzie);
        HwLog.d(TAG, "Default Storage is SDCard " + isDefaultStorageSDCard);
        if (!isExternalStorageDangerous || isOverCheckTime(System.currentTimeMillis())) {
            return 0;
        }
        if (isSwitchVolumeAvailable(isDefaultStorageSDCard, InternalAvailableSize, ExternalAvailableSzie, false)) {
            return 4;
        }
        return 2;
    }

    private static boolean isSwitchVolumeAvailable(boolean isDefaultStorageSDCard, long InternalAvailableSize, long ExternalAvailableSzie, boolean isInternalStorageDangerous) {
        boolean z = true;
        boolean z2 = false;
        if (isInternalStorageDangerous) {
            if (isDefaultStorageSDCard || 524288000 + InternalAvailableSize >= ExternalAvailableSzie) {
                z = false;
            }
            return z;
        }
        if (isDefaultStorageSDCard && 524288000 + ExternalAvailableSzie < InternalAvailableSize) {
            z2 = true;
        }
        return z2;
    }

    private static boolean isDefaultSDCard() {
        return SystemProperties.get("persist.sys.primarysd", "0").equals("1");
    }

    public static void sendLackOfMemoryNotification(Context context) {
        int notificationShowType = getStorageNotificationType(context);
        if (notificationShowType == 0) {
            HwLog.d(TAG, "hi buddy , we don't need show !");
            return;
        }
        NotificationManager nm = (NotificationManager) context.getSystemService("notification");
        PendingIntent contentIntent = getPendingIntent(context, ACTION_NOTIFICATION_BUTTON_CLICK_LEFT);
        Builder builder = new Builder(context);
        builder.setSmallIcon(R.drawable.ic_storage_notification).setAutoCancel(true).setTicker(context.getString(R.string.space_clear_sd_lack_space_notify_title)).setContentText(context.getString(R.string.lack_space_notify_content)).setContentIntent(contentIntent);
        if (isSDcardNotificationType(notificationShowType)) {
            builder.setContentTitle(context.getString(R.string.space_clean_notification_external_title));
        } else {
            builder.setContentTitle(context.getString(R.string.space_clean_notification_inner_title));
        }
        HwLog.d(TAG, "hi buddy , we need show !");
        if (isTwoButtonNotificationType(notificationShowType)) {
            HwLog.d(TAG, "congratulation! , we need show two button !");
            builder.addAction(0, context.getString(R.string.lack_space_notify_right), getPendingIntent(context, ACTION_NOTIFICATION_BUTTON_CLICK_RIGHT)).addAction(0, context.getString(R.string.lack_space_notify_left), contentIntent);
        }
        nm.notify(NotificationUtil.SPACE_LOWER_SPACE_NOTIFY_ID, builder.build());
    }

    private static boolean isSDcardNotificationType(int type) {
        if (type == 2 || type == 4) {
            return true;
        }
        return false;
    }

    private static boolean isTwoButtonNotificationType(int type) {
        if (type == 3 || type == 4) {
            return true;
        }
        return false;
    }

    private static boolean checkExternalStorageDangerous(Context context) {
        StorageHelper helper = StorageHelper.getStorage();
        if (helper.isSdcardaviliable()) {
            long total = helper.getTotalSize(1);
            long free = helper.getAvalibaleSize(1);
            long checkLimit = Math.min((10 * total) / 100, 524288000);
            if (checkLimit <= 0) {
                HwLog.e(TAG, "checkExternalStorageDangerous, checkLimit invalidate: " + checkLimit);
                return false;
            }
            HwLog.d(TAG, "checkExternalStorageDangerous total:" + total + ", free:" + free + ",checkLimit:" + checkLimit);
            boolean isDangerous = free < checkLimit;
            if (!isDangerous || free != 0) {
                return isDangerous;
            }
            HwLog.e(TAG, "external storage free is 0");
            return false;
        }
        HwLog.d(TAG, "checkExternalStorageDangerous, no external storage");
        return false;
    }

    private static boolean isOverCheckTime(long nowTime) {
        long triggerTime = getTriggerTime(20, 0) + 60000;
        HwLog.d(TAG, "nowTime is  " + nowTime);
        HwLog.d(TAG, "triggerTime is  " + triggerTime);
        if (nowTime > triggerTime) {
            return true;
        }
        return false;
    }

    public static void collapseStatusBar(Context context) {
        int currentApiVersion = VERSION.SDK_INT;
        try {
            Object service = context.getSystemService("statusbar");
            Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
            if (service != null) {
                Method collapse;
                if (currentApiVersion <= 16) {
                    collapse = statusbarManager.getMethod("collapse", new Class[0]);
                } else {
                    collapse = statusbarManager.getMethod("collapsePanels", new Class[0]);
                }
                collapse.setAccessible(true);
                collapse.invoke(service, new Object[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
