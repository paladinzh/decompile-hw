package com.huawei.systemmanager.power.model;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.power.provider.SmartProvider;
import com.huawei.systemmanager.util.HwLog;

public class DateMapToTimeSceneHelper {
    public static final String DB_DATE = "date";
    public static final String DB_RECORD_TYPE = "record_type";
    public static final String DB_WORK_TYPE = "work_type";
    public static final int HOLIDAY = 1;
    public static final int NUM_HOLIDAY_RECORD = 2;
    public static final int NUM_WORKDAY_RECORD = 5;
    public static final String TAG = DateMapToTimeSceneHelper.class.getSimpleName();
    public static final int WORKDAY = 0;

    public static String createDateMapToTimeSceneTableSQL() {
        StringBuffer sql = new StringBuffer();
        try {
            sql.append("CREATE TABLE IF NOT EXISTS datemaptotimescene ( ");
            sql.append("date LONG NOT NULL PRIMARY KEY , ");
            sql.append("record_type TEXT , ");
            sql.append("work_type INTEGER ) ");
            HwLog.i(TAG, "DatabaseHelper.creatTable DateMaptoTimeSceneTable: " + sql);
            return sql.toString();
        } catch (Exception e) {
            HwLog.e(TAG, "DatabaseHelper.creatTable DateMaptoTimeSceneTable: catch exception " + e.toString());
            return null;
        }
    }

    public static String dropDateMapToTimeSceneTableSQL() {
        String dropSQL = "DROP TABLE IF EXISTS datemaptotimescene";
        HwLog.i(TAG, "DatabaseHelper.dropTable DateMaptoTimeSceneTable: " + dropSQL);
        return dropSQL;
    }

    public static boolean isDataSaveFull(int type) {
        ContentResolver cr = GlobalContext.getContext().getContentResolver();
        Cursor cursor = null;
        try {
            cursor = cr.query(SmartProvider.DATE_MAPTO_TIME_SCENE_TABLE_URI, new String[]{"date"}, "work_type = " + type, null, null);
            if (cursor != null) {
                if (cursor.getCount() >= 5 && type == 0) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    return true;
                } else if (cursor.getCount() >= 2 && type == 1) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    return true;
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
        return false;
    }

    private static int getCountByWorkType(int type) {
        ContentResolver cr = GlobalContext.getContext().getContentResolver();
        Cursor cursor = null;
        try {
            cursor = cr.query(SmartProvider.DATE_MAPTO_TIME_SCENE_TABLE_URI, new String[]{"date"}, "work_type = " + type, null, null);
            if (cursor != null) {
                int count = cursor.getCount();
                if (cursor != null) {
                    cursor.close();
                }
                return count;
            }
            if (cursor != null) {
                cursor.close();
            }
            return 0;
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

    private static String getRecordType(long date, int type) {
        String str = null;
        ContentResolver cr = GlobalContext.getContext().getContentResolver();
        Cursor cursor = null;
        try {
            cursor = cr.query(SmartProvider.DATE_MAPTO_TIME_SCENE_TABLE_URI, new String[]{"record_type"}, "date = " + date + " and " + DB_WORK_TYPE + " = " + type, null, null);
            if (cursor != null && cursor.moveToNext()) {
                str = cursor.getString(0);
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
        return str;
    }

    private static String getReplaceRecordDate(long datebegin, int type) {
        String str = null;
        long oldestDate = 0;
        Cursor cursor = null;
        try {
            cursor = GlobalContext.getContext().getContentResolver().query(SmartProvider.DATE_MAPTO_TIME_SCENE_TABLE_URI, new String[]{"date", "record_type"}, "work_type=?", new String[]{String.valueOf(type)}, "date asc");
            if (cursor != null && cursor.moveToNext()) {
                oldestDate = cursor.getLong(0);
                str = cursor.getString(1);
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
        if (!(oldestDate == 0 || str == null)) {
            ContentValues updatevalues = new ContentValues();
            updatevalues.put("date", Long.valueOf(datebegin));
            updatevalues.put("record_type", str);
            updatevalues.put(DB_WORK_TYPE, Integer.valueOf(type));
            GlobalContext.getContext().getContentResolver().update(SmartProvider.DATE_MAPTO_TIME_SCENE_TABLE_URI, updatevalues, "record_type= ?", new String[]{str});
        }
        return str;
    }

    public static String calculateRecordTypeOfTimeSceneTable(long datebegin, int type) {
        String recordType = getRecordType(datebegin, type);
        if (recordType != null) {
            return recordType;
        }
        int num = getCountByWorkType(type);
        boolean isFull = false;
        if (type == 0) {
            switch (num) {
                case 0:
                    recordType = RemainingTimeSceneHelper.DB_RECORD_WORKDAY_DATA_ONE;
                    break;
                case 1:
                    recordType = RemainingTimeSceneHelper.DB_RECORD_WORKDAY_DATA_TWO;
                    break;
                case 2:
                    recordType = RemainingTimeSceneHelper.DB_RECORD_WORKDAY_DATA_THREE;
                    break;
                case 3:
                    recordType = RemainingTimeSceneHelper.DB_RECORD_WORKDAY_DATA_FOUR;
                    break;
                case 4:
                    recordType = RemainingTimeSceneHelper.DB_RECORD_WORKDAY_DATA_FIVE;
                    break;
                default:
                    isFull = true;
                    break;
            }
        }
        if (type == 1) {
            switch (num) {
                case 0:
                    recordType = RemainingTimeSceneHelper.DB_RECORD_HOLIDAY_DATA_ONE;
                    break;
                case 1:
                    recordType = RemainingTimeSceneHelper.DB_RECORD_HOLIDAY_DATA_TWO;
                    break;
                default:
                    isFull = true;
                    break;
            }
        }
        HwLog.i(TAG, "calculateRecordTypeOfTimeSceneTable isFull= " + isFull + " ,num= " + num + " ,type= " + type);
        ContentResolver cr = GlobalContext.getContext().getContentResolver();
        if (isFull) {
            recordType = getReplaceRecordDate(datebegin, type);
        } else {
            ContentValues values = new ContentValues();
            values.put("date", Long.valueOf(datebegin));
            values.put("record_type", recordType);
            values.put(DB_WORK_TYPE, Integer.valueOf(type));
            cr.insert(SmartProvider.DATE_MAPTO_TIME_SCENE_TABLE_URI, values);
        }
        return recordType;
    }
}
