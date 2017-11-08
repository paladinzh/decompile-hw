package com.huawei.systemmanager.power.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteException;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.power.comm.ActionConst;
import com.huawei.systemmanager.power.comm.SharedPrefKeyConst;
import com.huawei.systemmanager.power.data.xml.PowerWarningParam;
import com.huawei.systemmanager.power.util.SavingSettingUtil;
import com.huawei.systemmanager.util.HwLog;

public class PowerNotificationMgr {
    private static final String TAG = PowerNotificationMgr.class.getSimpleName();

    public static boolean isAppNotified(Context ctx, String pkgName) {
        SharedPreferences notifiedAppPreferences = ctx.getSharedPreferences(SharedPrefKeyConst.NOTIFIED_PACKAGES_SHAREDPREF_NAME, 4);
        Editor editor = notifiedAppPreferences.edit();
        if (notifiedAppPreferences.getBoolean(pkgName, false)) {
            return true;
        }
        editor.putBoolean(pkgName, true);
        editor.commit();
        return false;
    }

    public static void clearNotifiedPkgPreference(Context ctx) {
        Editor editor = ctx.getSharedPreferences(SharedPrefKeyConst.NOTIFIED_PACKAGES_SHAREDPREF_NAME, 4).edit();
        editor.clear();
        editor.commit();
    }

    public static boolean isAppIgnore(Context ctx, String pkgName) {
        boolean isIgnore = false;
        try {
            Integer ignore = (Integer) SavingSettingUtil.getRogue(ctx.getContentResolver(), pkgName, 1);
            if (ignore != null && ignore.intValue() == 1) {
                isIgnore = true;
                HwLog.d(TAG, pkgName + " PACKAGE_FIELD_IGNORE= 1");
                Integer isrogue = (Integer) SavingSettingUtil.getRogue(ctx.getContentResolver(), pkgName, 0);
                if (isrogue != null && isrogue.intValue() == 0) {
                    SavingSettingUtil.setRogue(ctx.getContentResolver(), pkgName, 0, Integer.valueOf(1));
                }
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        return isIgnore;
    }

    public static void setPowerNotificationAlarmTime(Context context, long timeInMillis) {
        AlarmManager am = (AlarmManager) context.getSystemService("alarm");
        Intent intent = new Intent(ActionConst.INTENT_POWER_STATISTIC);
        intent.setPackage(context.getPackageName());
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, ShareCfg.PERMISSION_MODIFY_CALENDAR);
        long interval = ((long) PowerWarningParam.getCount_frequency(context)) * 60000;
        am.cancel(sender);
        am.setRepeating(1, timeInMillis + interval, interval, sender);
    }
}
