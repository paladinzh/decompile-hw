package com.huawei.systemmanager.power.model;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemProperties;
import com.google.common.collect.Lists;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.power.comm.ApplicationConstant;
import com.huawei.systemmanager.power.data.battery.BatteryInfo;
import com.huawei.systemmanager.power.provider.PowerXmlHelper;
import com.huawei.systemmanager.power.provider.SmartProvider;
import com.huawei.systemmanager.power.receiver.ScheduleRecordRemainTimeSceneReceiver;
import com.huawei.systemmanager.power.util.SysCoreUtils;
import com.huawei.systemmanager.util.HwLog;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RemainingTimeSceneHelper {
    private static final String AVERAGE_CURRENT_VALUE = "averagevalue";
    public static final String DB_CURRENT_VALUE = "electric_current_value";
    public static final String DB_NUMBER = "number";
    public static final String DB_RECORD_DATE = "record_type";
    public static final String DB_RECORD_DATE_DEFAULT = "default";
    public static final String DB_RECORD_HOLIDAY_DATA_ONE = "one_holiday";
    public static final String DB_RECORD_HOLIDAY_DATA_TWO = "two_holiday";
    public static final String DB_RECORD_WORKDAY_DATA_FIVE = "five_workday";
    public static final String DB_RECORD_WORKDAY_DATA_FOUR = "four_workday";
    public static final String DB_RECORD_WORKDAY_DATA_ONE = "one_workday";
    public static final String DB_RECORD_WORKDAY_DATA_THREE = "three_workday";
    public static final String DB_RECORD_WORKDAY_DATA_TWO = "two_workday";
    public static final String DB_TYPE = "type";
    private static final long DURING_TIME_THRESHOLD = 43200000;
    public static final double MIN_CURRENT_VALUE = 1.0d;
    public static final int ONE_HOUR = 60;
    public static final int SCENE_DURATION = 10;
    public static final double SLEEP_CURRENT_VALUE = 20.0d;
    public static final String TAG = RemainingTimeSceneHelper.class.getSimpleName();
    public static final String TAG_INIT_TABLE = "RemainingTimeSceneHelper_init_table";
    public static final int TIME_SCENE_NUM_ONE_DAY = 144;
    public static final int TIME_SCENE_NUM_ONE_HOUR = 6;
    public static final int WEEKEND = 1;
    public static final int WORK_DAY = 0;
    private static double rmCapacity_temp = 0.0d;
    private static long timestamp_temp = 0;

    public static String createTimeSceneTableSQL() {
        StringBuffer sql = new StringBuffer();
        try {
            sql.append("CREATE TABLE IF NOT EXISTS timescene ( ");
            sql.append("number INTEGER , ");
            sql.append("record_type TEXT , ");
            sql.append("electric_current_value DOUBLE, ");
            sql.append("type INTEGER ) ");
            HwLog.i(TAG_INIT_TABLE, "DatabaseHelper.creatTable TimeSceneTable: " + sql);
            return sql.toString();
        } catch (Exception e) {
            HwLog.e(TAG_INIT_TABLE, "DatabaseHelper.creatTable TimeSceneTable: catch exception " + e.toString());
            return null;
        }
    }

    public static void initTimeSceneTable(SQLiteDatabase db) {
        HwLog.i(TAG, "initTimeSceneTable...");
        long time1 = System.currentTimeMillis();
        List<TimeSceneXmlBean> mTimeSceneList = PowerXmlHelper.parseTimeSceneDefaultValue(GlobalContext.getContext());
        List<ContentValues> values = new ArrayList();
        for (int i = 0; i < mTimeSceneList.size(); i++) {
            ContentValues value0 = new ContentValues();
            value0.put("number", Integer.valueOf(((TimeSceneXmlBean) mTimeSceneList.get(i)).getNumber()));
            value0.put("record_type", DB_RECORD_DATE_DEFAULT);
            value0.put(DB_CURRENT_VALUE, Double.valueOf(((TimeSceneXmlBean) mTimeSceneList.get(i)).getCurrentValue()));
            value0.put("type", Integer.valueOf(0));
            values.add(value0);
            ContentValues value1 = new ContentValues();
            value1.put("number", Integer.valueOf(((TimeSceneXmlBean) mTimeSceneList.get(i)).getNumber()));
            value1.put("record_type", DB_RECORD_WORKDAY_DATA_ONE);
            value1.put(DB_CURRENT_VALUE, Double.valueOf(((TimeSceneXmlBean) mTimeSceneList.get(i)).getCurrentValue()));
            value1.put("type", Integer.valueOf(0));
            values.add(value1);
            ContentValues value2 = new ContentValues();
            value2.put("number", Integer.valueOf(((TimeSceneXmlBean) mTimeSceneList.get(i)).getNumber()));
            value2.put("record_type", DB_RECORD_WORKDAY_DATA_TWO);
            value2.put(DB_CURRENT_VALUE, Double.valueOf(((TimeSceneXmlBean) mTimeSceneList.get(i)).getCurrentValue()));
            value2.put("type", Integer.valueOf(0));
            values.add(value2);
            ContentValues value3 = new ContentValues();
            value3.put("number", Integer.valueOf(((TimeSceneXmlBean) mTimeSceneList.get(i)).getNumber()));
            value3.put("record_type", DB_RECORD_WORKDAY_DATA_THREE);
            value3.put(DB_CURRENT_VALUE, Double.valueOf(((TimeSceneXmlBean) mTimeSceneList.get(i)).getCurrentValue()));
            value3.put("type", Integer.valueOf(0));
            values.add(value3);
            ContentValues value4 = new ContentValues();
            value4.put("number", Integer.valueOf(((TimeSceneXmlBean) mTimeSceneList.get(i)).getNumber()));
            value4.put("record_type", DB_RECORD_WORKDAY_DATA_FOUR);
            value4.put(DB_CURRENT_VALUE, Double.valueOf(((TimeSceneXmlBean) mTimeSceneList.get(i)).getCurrentValue()));
            value4.put("type", Integer.valueOf(0));
            values.add(value4);
            ContentValues value5 = new ContentValues();
            value5.put("number", Integer.valueOf(((TimeSceneXmlBean) mTimeSceneList.get(i)).getNumber()));
            value5.put("record_type", DB_RECORD_WORKDAY_DATA_FIVE);
            value5.put(DB_CURRENT_VALUE, Double.valueOf(((TimeSceneXmlBean) mTimeSceneList.get(i)).getCurrentValue()));
            value5.put("type", Integer.valueOf(0));
            values.add(value5);
            ContentValues value6 = new ContentValues();
            value6.put("number", Integer.valueOf(((TimeSceneXmlBean) mTimeSceneList.get(i)).getNumber()));
            value6.put("record_type", DB_RECORD_DATE_DEFAULT);
            value6.put(DB_CURRENT_VALUE, Double.valueOf(((TimeSceneXmlBean) mTimeSceneList.get(i)).getCurrentValue()));
            value6.put("type", Integer.valueOf(1));
            values.add(value6);
            ContentValues value7 = new ContentValues();
            value7.put("number", Integer.valueOf(((TimeSceneXmlBean) mTimeSceneList.get(i)).getNumber()));
            value7.put("record_type", DB_RECORD_HOLIDAY_DATA_ONE);
            value7.put(DB_CURRENT_VALUE, Double.valueOf(((TimeSceneXmlBean) mTimeSceneList.get(i)).getCurrentValue()));
            value7.put("type", Integer.valueOf(1));
            values.add(value7);
            ContentValues value8 = new ContentValues();
            value8.put("number", Integer.valueOf(((TimeSceneXmlBean) mTimeSceneList.get(i)).getNumber()));
            value8.put("record_type", DB_RECORD_HOLIDAY_DATA_TWO);
            value8.put(DB_CURRENT_VALUE, Double.valueOf(((TimeSceneXmlBean) mTimeSceneList.get(i)).getCurrentValue()));
            value8.put("type", Integer.valueOf(1));
            values.add(value8);
        }
        db.beginTransaction();
        try {
            int count = values.size();
            for (int j = 0; j < count; j++) {
                db.insert(SmartProvider.REMAINING_TIME_SCENE_TABLE, null, (ContentValues) values.get(j));
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            HwLog.e(TAG, "initTimeSceneTable faild! " + e.toString());
        } finally {
            db.endTransaction();
        }
        HwLog.i(TAG, "initTimeSceneTable, cost time(ms) = " + (System.currentTimeMillis() - time1));
    }

    public static void recordTimeScene(int number, String date, double currentValue, int type) {
        ContentValues updatevalues = new ContentValues();
        updatevalues.put("number", Integer.valueOf(number));
        updatevalues.put("record_type", date);
        updatevalues.put(DB_CURRENT_VALUE, Double.valueOf(currentValue));
        updatevalues.put("type", Integer.valueOf(type));
        GlobalContext.getContext().getContentResolver().update(SmartProvider.REMAINING_TIME_SCENE_TABLE_URI, updatevalues, "number= ? and record_type= ? and type= ?", new String[]{String.valueOf(number), date, String.valueOf(type)});
    }

    public static String dropTimeSceneTableSQL() {
        String dropSQL = "DROP TABLE IF EXISTS timescene";
        HwLog.i(TAG_INIT_TABLE, "DatabaseHelper.dropTable TimeSceneTable: " + dropSQL);
        return dropSQL;
    }

    public static List<TimeSceneItem> getTimeSceneList(int type) {
        List<TimeSceneItem> mTimesceneList = Lists.newArrayList();
        boolean isDataSaveFull = DateMapToTimeSceneHelper.isDataSaveFull(type);
        Cursor cursor = null;
        HwLog.i(TAG, "isDataSaveFull = " + isDataSaveFull + " type= " + type);
        TimeSceneItem item;
        if (isDataSaveFull) {
            try {
                cursor = GlobalContext.getContext().getContentResolver().query(SmartProvider.REMAINING_TIME_SCENE_TABLE_URI, new String[]{"number", "avg(electric_current_value) averagevalue", "type"}, "type =? and record_type != 'default' group by number", new String[]{String.valueOf(type)}, "number asc");
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        item = new TimeSceneItem();
                        item.setNumber(cursor.getInt(0));
                        item.setCurrentValue(SysCoreUtils.format2decimal(cursor.getDouble(1)));
                        item.setType(cursor.getInt(2));
                        mTimesceneList.add(item);
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
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
        } else {
            try {
                cursor = GlobalContext.getContext().getContentResolver().query(SmartProvider.REMAINING_TIME_SCENE_TABLE_URI, new String[]{"number", "record_type", DB_CURRENT_VALUE, "type"}, "record_type=? and type=?", new String[]{DB_RECORD_DATE_DEFAULT, String.valueOf(type)}, "number asc");
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        item = new TimeSceneItem();
                        item.setNumber(cursor.getInt(0));
                        item.setRecordType(cursor.getString(1));
                        item.setCurrentValue(SysCoreUtils.format2decimal(cursor.getDouble(2)));
                        item.setType(cursor.getInt(3));
                        mTimesceneList.add(item);
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                return mTimesceneList;
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return mTimesceneList;
    }

    public static boolean isWeekend(long timestamp) {
        Calendar c = Calendar.getInstance(Locale.getDefault());
        c.setTimeInMillis(timestamp);
        switch (c.get(7)) {
            case 1:
            case 7:
                return true;
            default:
                return false;
        }
    }

    public static int getNumberSceneByTimeStamp(long timestamp) {
        int tempHour = 0;
        Calendar c = Calendar.getInstance(Locale.getDefault());
        c.setTimeInMillis(timestamp);
        int hour = c.get(11);
        int minute = c.get(12);
        if (hour > 0) {
            tempHour = hour;
        }
        return (tempHour * 6) + (minute / 10);
    }

    private static long getTodayBeginTimeStamp(long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return format.parse(format.format(Long.valueOf(timestamp))).getTime() / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static int getCurrentSaveMode() {
        if (SystemProperties.getBoolean("sys.super_power_save", false)) {
            return 3;
        }
        return ChangeMode.getInstance(GlobalContext.getContext()).readSaveMode();
    }

    public static void updateTimeSceneData() {
        if (getCurrentSaveMode() != 1) {
            timestamp_temp = 0;
            rmCapacity_temp = 0.0d;
            HwLog.i(TAG, "the current power mode is not smart mode, do not update data.");
            return;
        }
        long begin = System.currentTimeMillis();
        double currentRmCapacity = (double) BatteryInfo.getBatteryCapacityRmValue();
        long currentTimeStamp = System.currentTimeMillis();
        double deltaRmCapacity = rmCapacity_temp - currentRmCapacity <= 0.0d ? 0.0d : rmCapacity_temp - currentRmCapacity;
        long deltaTimeStamp = currentTimeStamp - timestamp_temp <= 0 ? 0 : currentTimeStamp - timestamp_temp;
        HwLog.i(TAG, "updateTimeSceneData ,deltaRmCapacity= " + deltaRmCapacity + " deltaTimeStamp=" + deltaTimeStamp + " timestamp_temp= " + timestamp_temp);
        if (0 == timestamp_temp || deltaRmCapacity <= 0.0d || deltaTimeStamp <= 0 || deltaTimeStamp > 43200000) {
            HwLog.i(TAG, "invalid condition, do not update time scene.");
        } else {
            int type;
            long temp_date;
            int temp_number;
            String temp_recordtype;
            double tempCurrentValue = SysCoreUtils.format2decimal((3600000.0d * deltaRmCapacity) / ((double) deltaTimeStamp));
            if (tempCurrentValue < 1.0d) {
                tempCurrentValue = 1.0d;
            }
            long beforeTimeStamp = timestamp_temp;
            HwLog.i(TAG, "updateTimeSceneData, beforeTimeStamp= " + beforeTimeStamp + ", currentTimeStamp= " + currentTimeStamp + ", tempCurrentValue= " + tempCurrentValue);
            while (beforeTimeStamp < currentTimeStamp) {
                type = isWeekend(beforeTimeStamp) ? 1 : 0;
                temp_date = getTodayBeginTimeStamp(beforeTimeStamp);
                temp_number = getNumberSceneByTimeStamp(beforeTimeStamp);
                temp_recordtype = DateMapToTimeSceneHelper.calculateRecordTypeOfTimeSceneTable(temp_date, type);
                HwLog.i(TAG, "updateTimeSceneData,temp_date= " + temp_date + " temp_number= " + temp_number + " temp_recordtype= " + temp_recordtype);
                if (temp_recordtype != null) {
                    recordTimeScene(temp_number, temp_recordtype, tempCurrentValue, type);
                }
                beforeTimeStamp += 600000;
            }
            type = isWeekend(currentTimeStamp) ? 1 : 0;
            temp_date = getTodayBeginTimeStamp(currentTimeStamp);
            temp_number = getNumberSceneByTimeStamp(currentTimeStamp);
            temp_recordtype = DateMapToTimeSceneHelper.calculateRecordTypeOfTimeSceneTable(temp_date, type);
            HwLog.i(TAG, "updateTimeSceneData,temp_date= " + temp_date + " temp_number= " + temp_number + " temp_recordtype= " + temp_recordtype);
            if (temp_recordtype != null) {
                recordTimeScene(temp_number, temp_recordtype, tempCurrentValue, type);
            }
        }
        rmCapacity_temp = currentRmCapacity;
        timestamp_temp = currentTimeStamp;
        HwLog.i(TAG, "updateTimeSceneData during time(ms)= " + (System.currentTimeMillis() - begin));
    }

    public static void scheduleRecordTimeScene() {
        Context context = GlobalContext.getContext();
        Intent newIntent = new Intent(ApplicationConstant.ACTION_ALARM_SCHEDULE_RECORD_REMAINING_TIME_SCENE);
        newIntent.setClass(context, ScheduleRecordRemainTimeSceneReceiver.class);
        ((AlarmManager) context.getSystemService("alarm")).setRepeating(3, 0, 600000, PendingIntent.getBroadcast(context, 0, newIntent, ShareCfg.PERMISSION_MODIFY_CALENDAR));
        HwLog.i(TAG, "scheduleRecordTimeScene,start alarms.");
    }
}
