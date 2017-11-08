package com.huawei.systemmanager.power.model;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.SystemClock;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.TimeUtil;
import com.huawei.systemmanager.comm.wrapper.SharePrefWrapper;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.power.comm.ApplicationConstant;
import com.huawei.systemmanager.power.comm.SharedPrefKeyConst;
import com.huawei.systemmanager.power.data.profile.HwPowerProfile;
import com.huawei.systemmanager.power.provider.SmartProvider;
import com.huawei.systemmanager.power.receiver.UsageStatusReceiver;
import com.huawei.systemmanager.util.HwLog;
import java.util.Calendar;
import java.util.Locale;

public class UsageStatusHelper {
    public static final String DB_SCREENOFFTIME = "screenofftime";
    public static final String DB_SCREENONTIME = "screenontime";
    public static final String DB_SCREENTIME = "screentime";
    public static final String DB_TYPE = "type";
    public static final String DB_USAGEDATE = "usagedate";
    public static final int MAX_COUNTS = 30;
    public static final String TAG = "UsageStatusHelper";
    public static final String TAG_INIT_TABLE = "UsageStatusHelper_init_table";
    public static final int WEEKEND = 1;
    public static final String WEEKENDAY = "1";
    public static final String WORKDAY = "0";
    public static final int WORK_DAY = 0;

    public static String createUsageStatusTalbeSQL() {
        StringBuffer sql = new StringBuffer();
        try {
            sql.append("CREATE TABLE IF NOT EXISTS usagestatus ( ");
            sql.append("usagedate TEXT NOT NULL PRIMARY KEY , ");
            sql.append("screenontime Long, ");
            sql.append("screenofftime Long, ");
            sql.append("screentime Long default 0, ");
            sql.append("type INTEGER ) ");
            HwLog.i(TAG_INIT_TABLE, "DatabaseHelper.creatTable UsageStatus: " + sql);
            return sql.toString();
        } catch (Exception e) {
            HwLog.e(TAG_INIT_TABLE, "DatabaseHelper.creatTable UsageStatus: catch exception " + e.toString());
            return null;
        }
    }

    public static String dropUsageStatusSQL() {
        String dropSQL = "DROP TABLE IF EXISTS usagestatus";
        HwLog.i(TAG_INIT_TABLE, "DatabaseHelper.dropTable UsageStatus: " + dropSQL);
        return dropSQL;
    }

    public static void recordScreenStatus(boolean screenOn) {
        long lastScreenTime;
        ContentValues cv = new ContentValues();
        boolean isWeekend = isWeekend();
        UsageStatusItem usi = getCurrentUsageInfo();
        long currentTime = SystemClock.elapsedRealtime();
        long screenOffTime = 0;
        long screenOnTime = 0;
        if (usi == null) {
            lastScreenTime = currentTime;
        } else {
            lastScreenTime = usi.getScreentime();
            screenOffTime = usi.getScreenofftime();
            screenOnTime = usi.getScreenontime();
        }
        long times = currentTime - lastScreenTime;
        boolean isValid = times >= 0;
        cv.put(DB_SCREENTIME, Long.valueOf(currentTime));
        cv.put(DB_USAGEDATE, TimeUtil.getToday());
        if (isWeekend) {
            cv.put("type", Integer.valueOf(1));
        } else {
            cv.put("type", Integer.valueOf(0));
        }
        if (screenOn) {
            if (isValid) {
                cv.put(DB_SCREENOFFTIME, Long.valueOf(screenOffTime + times));
            } else {
                cv.put(DB_SCREENOFFTIME, Long.valueOf(screenOffTime));
            }
            cv.put(DB_SCREENONTIME, Long.valueOf(screenOnTime));
        } else {
            if (isValid) {
                cv.put(DB_SCREENONTIME, Long.valueOf(screenOnTime + times));
            } else {
                cv.put(DB_SCREENONTIME, Long.valueOf(screenOnTime));
            }
            cv.put(DB_SCREENOFFTIME, Long.valueOf(screenOffTime));
        }
        insertScreenInfo(cv);
    }

    private static void insertScreenInfo(ContentValues cv) {
        GlobalContext.getContext().getContentResolver().insert(SmartProvider.SCREENSTATUS_TABLE_URI, cv);
    }

    public static UsageStatusItem getCurrentUsageInfo() {
        Throwable th;
        ContentResolver cr = GlobalContext.getContext().getContentResolver();
        Cursor cursor = null;
        UsageStatusItem usageStatusItem = null;
        String date = TimeUtil.getToday();
        try {
            cursor = cr.query(SmartProvider.SCREENSTATUS_TABLE_URI, new String[]{"type", DB_SCREENOFFTIME, DB_SCREENONTIME, DB_SCREENTIME, DB_USAGEDATE}, "usagedate = '" + date + "'", null, null);
            if (cursor != null && cursor.moveToNext()) {
                UsageStatusItem usi = new UsageStatusItem();
                try {
                    usi.setScreenofftime(cursor.getLong(cursor.getColumnIndex(DB_SCREENOFFTIME)));
                    usi.setScreenontime(cursor.getLong(cursor.getColumnIndex(DB_SCREENONTIME)));
                    usi.setDate(cursor.getString(cursor.getColumnIndex(DB_USAGEDATE)));
                    usi.setScreentime(cursor.getLong(cursor.getColumnIndex(DB_SCREENTIME)));
                    usi.setType(cursor.getInt(cursor.getColumnIndex("type")));
                    usageStatusItem = usi;
                } catch (SQLiteException e) {
                    usageStatusItem = usi;
                    try {
                        HwLog.e(TAG, "Database exception!");
                        if (cursor != null) {
                            cursor.close();
                        }
                        return usageStatusItem;
                    } catch (Throwable th2) {
                        th = th2;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException e2) {
            HwLog.e(TAG, "Database exception!");
            if (cursor != null) {
                cursor.close();
            }
            return usageStatusItem;
        }
        return usageStatusItem;
    }

    public static void deleteLongestUsageInfo() {
        ContentResolver cr = GlobalContext.getContext().getContentResolver();
        Cursor cursor = null;
        try {
            cursor = cr.query(SmartProvider.SCREENSTATUS_TABLE_URI, new String[]{DB_USAGEDATE}, null, null, null);
            if (cursor != null) {
                HwLog.i(TAG, " cursor =" + cursor + " cursor.getCount() =" + cursor.getCount());
                int count = cursor.getCount();
                if (count <= 30) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    return;
                }
                int delCounts = count - 30;
                StringBuffer sb = new StringBuffer();
                sb.append("(");
                for (int i = 0; i < delCounts; i++) {
                    cursor.moveToNext();
                    String usageDate = cursor.getString(cursor.getColumnIndex(DB_USAGEDATE));
                    sb.append("'" + usageDate + "',");
                    HwLog.i(TAG, "usageDate =" + usageDate);
                }
                String delKeys = sb.toString();
                delKeys = delKeys.substring(0, delKeys.length() - 1) + ")";
                HwLog.i(TAG, "delKeys =" + delKeys + " counts =" + count);
                cr.delete(SmartProvider.SCREENSTATUS_TABLE_URI, "usagedate in " + delKeys, null);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException e) {
            HwLog.e(TAG, "Database exception!");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static boolean isWeekend() {
        switch (Calendar.getInstance(Locale.getDefault()).get(7)) {
            case 1:
            case 7:
                return true;
            default:
                return false;
        }
    }

    public static void recordBaseScreenRatio() {
        double newRatio = caculateRatio();
        HwLog.i(TAG, " newRatio =" + newRatio);
        if (newRatio >= 1.0d || newRatio <= 0.0d) {
            HwLog.w(TAG, " new ratio is a invalid data! ");
            return;
        }
        boolean isWeekend = isWeekend();
        SharePrefWrapper.setPrefValue(GlobalContext.getContext(), SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, isWeekend ? SharedPrefKeyConst.BASE_SCREEN_STATUS_RATIO_WEEKEND_KEY : SharedPrefKeyConst.BASE_SCREEN_STATUS_RATIO_WORKDAY_KEY, String.valueOf(newRatio));
        HwPowerProfile.setScreenTimeScale(newRatio);
        String[] strArr = new String[4];
        strArr[0] = HsmStatConst.PARAM_KEY;
        strArr[1] = isWeekend ? "1" : "0";
        strArr[2] = HsmStatConst.PARAM_VAL;
        strArr[3] = String.valueOf(newRatio);
        HsmStat.statE((int) Events.E_POWER_SCREEN_ON_DURATION_RATIO, HsmStatConst.constructJsonParams(strArr));
    }

    private static double caculateRatio() {
        Cursor cursor = null;
        String aliasName = "sums";
        long screenontime = 0;
        long sums = 0;
        double newRatio = 0.0d;
        try {
            cursor = GlobalContext.getContext().getContentResolver().query(SmartProvider.SCREENSTATUS_TABLE_URI, new String[]{"type", DB_SCREENOFFTIME, DB_SCREENONTIME, DB_SCREENTIME, DB_USAGEDATE, "(screenofftime + screenontime) as " + aliasName}, "screenofftime !=0 and screenontime !=0 and " + aliasName + " > " + 43200000 + " and " + "type" + " = " + (isWeekend() ? 1 : 0), null, null);
            if (cursor != null) {
                HwLog.i(TAG, "valid data counts =" + cursor.getCount());
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        screenontime += cursor.getLong(cursor.getColumnIndex(DB_SCREENONTIME));
                        sums += cursor.getLong(cursor.getColumnIndex(aliasName));
                    }
                    newRatio = format4decimal(((double) screenontime) / ((double) sums));
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException e) {
            HwLog.e(TAG, "Database exception!");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return newRatio;
    }

    public static double format4decimal(double value) {
        return ((double) Math.round(value * 10000.0d)) / 10000.0d;
    }

    public static void updateRatio() {
        Context context = GlobalContext.getContext();
        Intent newIntent = new Intent(ApplicationConstant.ACTION_ALARM_UPDATE_USAGESTATUS_RECORD);
        newIntent.setClass(context, UsageStatusReceiver.class);
        ((AlarmManager) context.getSystemService("alarm")).setRepeating(0, 86400000, 86400000, PendingIntent.getBroadcast(context, 0, newIntent, ShareCfg.PERMISSION_MODIFY_CALENDAR));
        HwLog.i(TAG, "updateRatio,start alarms.");
    }
}
