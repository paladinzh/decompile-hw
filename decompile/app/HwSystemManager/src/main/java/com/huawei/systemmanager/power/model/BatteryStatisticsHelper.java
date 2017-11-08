package com.huawei.systemmanager.power.model;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import com.google.common.collect.Lists;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.power.comm.ApplicationConstant;
import com.huawei.systemmanager.power.data.stats.UidAndPower;
import com.huawei.systemmanager.power.provider.SmartProvider;
import com.huawei.systemmanager.power.receiver.ScheduleRecordPowerConsumeReceiver;
import com.huawei.systemmanager.util.HwLog;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatteryStatisticsHelper {
    private static Map<Integer, UidAndPower> BATTERY_CACHE = new HashMap();
    public static final String DB_DATE = "date";
    public static final String DB_POWER = "power";
    public static final String DB_ROWID = "rowid";
    public static final String DB_UID = "uid";
    public static final int MAX_COUNTS = 30;
    private static final String SUMPOWER_STR = "sumpower";
    public static final String TAG = "BatteryStatisticsHelper";
    public static final String TAG_INIT_TABLE = "BatteryStatisticsHelper_init_table";
    private static int TOP5 = 5;

    public static String createBatteryStatisticsTalbeSQL() {
        StringBuffer sql = new StringBuffer();
        try {
            sql.append("CREATE TABLE IF NOT EXISTS batterystatistics ( ");
            sql.append("rowid INTEGER PRIMARY KEY AUTOINCREMENT, ");
            sql.append("date Long, ");
            sql.append("uid INTEGER, ");
            sql.append("power DOUBLE ) ");
            HwLog.i(TAG_INIT_TABLE, "DatabaseHelper.creatTable UsageStatus: " + sql);
            return sql.toString();
        } catch (Exception e) {
            HwLog.e(TAG_INIT_TABLE, "DatabaseHelper.creatTable UsageStatus: catch exception " + e.toString());
            return null;
        }
    }

    public static String dropBatteryStatisticsSQL() {
        String dropSQL = "DROP TABLE IF EXISTS batterystatistics";
        HwLog.i(TAG_INIT_TABLE, "DatabaseHelper.dropTable BatteryStatistics: " + dropSQL);
        return dropSQL;
    }

    public static void insertBatteryStatistics(List<UidAndPower> apps) {
        List<UidAndPower> list = apps;
        Context context = GlobalContext.getContext();
        int counts = apps.size();
        HwLog.i(TAG, "counts =" + counts);
        if (counts >= TOP5) {
            list = apps.subList(0, TOP5);
        }
        if (counts > 0) {
            int size = list.size();
            long currentTime = System.currentTimeMillis();
            ContentResolver cr = context.getContentResolver();
            for (int i = 0; i < size; i++) {
                UidAndPower uap = (UidAndPower) list.get(i);
                HwLog.i(TAG, "uap.getUid() =" + uap.getUid() + " uap.getPower() =" + uap.getPower());
                double cpower = uap.getPower();
                if (cpower > 0.0d) {
                    UidAndPower tuap = (UidAndPower) BATTERY_CACHE.get(Integer.valueOf(uap.getUid()));
                    double tpower = 0.0d;
                    if (tuap != null) {
                        tpower = tuap.getPower();
                    }
                    double power = cpower - tpower;
                    if (tpower == 0.0d) {
                        power = 0.0d;
                    }
                    if (power > 0.0d) {
                        ContentValues cv = new ContentValues();
                        cv.put("date", Long.valueOf(currentTime));
                        cv.put("uid", Integer.valueOf(uap.getUid()));
                        cv.put(DB_POWER, Double.valueOf(format2decimal(power)));
                        cr.insert(SmartProvider.BATTERY_STATISTICS_TABLE_URI, cv);
                    }
                }
            }
            return;
        }
        HwLog.i(TAG, "no items!!!");
    }

    public static double format2decimal(double value) {
        return ((double) Math.round(value * 100.0d)) / 100.0d;
    }

    public static Map<Integer, UidAndPower> refreshBatteryCache(List<UidAndPower> list) {
        BATTERY_CACHE.clear();
        for (UidAndPower uap : list) {
            BATTERY_CACHE.put(Integer.valueOf(uap.getUid()), uap);
        }
        return BATTERY_CACHE;
    }

    public static void updateBatteryInfos(String pkgName) {
        try {
            ApplicationInfo ai = GlobalContext.getContext().getPackageManager().getApplicationInfo(pkgName, 8192);
            if (ai != null) {
                BATTERY_CACHE.remove(Integer.valueOf(ai.uid));
                deleteBatteryInfo(ai.uid);
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Map<Integer, UidAndPower> getBatteryCache() {
        return BATTERY_CACHE;
    }

    public void clearCache() {
        BATTERY_CACHE.clear();
    }

    public static void deleteBatteryInfo(int uid) {
        GlobalContext.getContext().getContentResolver().delete(SmartProvider.BATTERY_STATISTICS_TABLE_URI, "uid = " + uid, null);
    }

    public static void scheduleRecordPowerConsume() {
        Context context = GlobalContext.getContext();
        Intent newIntent = new Intent(ApplicationConstant.ACTION_ALARM_SCHEDULE_RECORD_POWER_CONSUME);
        newIntent.setClass(context, ScheduleRecordPowerConsumeReceiver.class);
        ((AlarmManager) context.getSystemService("alarm")).setRepeating(3, 0, 1800000, PendingIntent.getBroadcast(context, 0, newIntent, ShareCfg.PERMISSION_MODIFY_CALENDAR));
        HwLog.i(TAG, "scheduleRecordPowerConsume,start alarms.");
    }

    public static void deleteBatteryInfo2DaysAgo() {
        long whereTimes = System.currentTimeMillis() - 172800000;
        GlobalContext.getContext().getContentResolver().delete(SmartProvider.BATTERY_STATISTICS_TABLE_URI, "date < " + whereTimes, null);
        HwLog.i(TAG, "deleteBatteryInfo2DaysAgo  twoDaysMillseconds=" + 172800000 + " whereTimes =" + whereTimes);
    }

    public static void deleteBatteryInfoAllInfos() {
        ContentResolver cr = GlobalContext.getContext().getContentResolver();
        cr.delete(SmartProvider.BATTERY_STATISTICS_TABLE_URI, null, null);
        cr.delete(SmartProvider.SQLITE_SEQUENCE_TABLE_URI, "name = 'batterystatistics'", null);
    }

    public static List<UidAndPower> queryBatteryStatistics(Context context, long time) {
        List<UidAndPower> res = Lists.newArrayList();
        long endTime = time + 3600000;
        Cursor cursor = null;
        String selection = "date > " + (time - 1800000) + " and " + "date" + " < " + endTime + "  group by " + "uid";
        try {
            cursor = context.getContentResolver().query(SmartProvider.BATTERY_STATISTICS_TABLE_URI, new String[]{"uid", "sum(power) sumpower"}, selection, null, "sumpower desc");
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return res;
            }
            while (cursor.moveToNext()) {
                double power = cursor.getDouble(1);
                if (power > 0.0d) {
                    res.add(new UidAndPower(cursor.getInt(0), power, null));
                    if (res.size() >= TOP5) {
                        break;
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
