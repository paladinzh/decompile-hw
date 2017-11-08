package com.android.server.jankshield;

import android.content.ContentValues;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.JankEventData;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TableJankEvent {
    protected static final boolean HWDBG;
    protected static final boolean HWFLOW;
    protected static final boolean HWLOGW_E = true;
    private static final String TAG = "JankShield";
    private static final String[] field_Names = new String[]{"CaseName", "TimeStamp", "Arg1", "Arg2", "CpuLoad", "FreeMem", "FreeStorage", "Limit_Freq", "CpuLoadTop_proc1", "CpuLoadTop_proc2", "CpuLoadTop_proc3", "CpuLoad_proc1", "CpuLoad_proc2", "CpuLoad_proc3", "IoWaitLoad", "Reserve1", "Reserve2"};
    private static final String[] field_Types = new String[]{"varchar", "varchar", "varchar", "integer", "integer", "integer", "integer", "integer", "varchar", "varchar", "varchar", "integer", "integer", "integer", "integer", "integer", "integer"};
    public static final long recDELTACOUNT = 200;
    public static final long recMAXCOUNT = 2000;
    private static long recordCount = 0;
    public static final String tbName = "JankEvent";

    static {
        boolean z = true;
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
        if (!Log.HWLog) {
            if (Log.HWModuleLog) {
                z = Log.isLoggable(TAG, 3);
            } else {
                z = false;
            }
        }
        HWDBG = z;
    }

    public static boolean insert(SQLiteDatabase db, JankEventData jankevent) {
        boolean ret = false;
        ContentValues values = jankevent.getContentValues(field_Names);
        if (values == null) {
            return false;
        }
        if (recordCount >= 2200) {
            delete(db, (recordCount - 2000) + 1);
        }
        if (-1 != db.insert(tbName, null, values)) {
            ret = true;
            recordCount++;
        } else if (HWFLOW) {
            Log.i(TAG, "insert(" + values.toString() + ") failed");
        }
        return ret;
    }

    private static void delete(SQLiteDatabase db, long num) {
        String lastTime = getOutdate(60);
        db.beginTransaction();
        try {
            db.execSQL("delete from JankEvent where id in(select id from JankEvent order by TimeStamp asc limit " + num + " )");
            db.execSQL("delete from JankEvent where TimeStamp <= " + lastTime + " ");
            db.setTransactionSuccessful();
            getCount(db);
        } finally {
            db.endTransaction();
        }
    }

    public static long getCount(SQLiteDatabase db) {
        recordCount = DatabaseUtils.longForQuery(db, "select count(*) from JankEvent", null);
        return recordCount;
    }

    public static void dropTb(SQLiteDatabase db) {
        db.execSQL("drop table if exists JankEvent" + ";");
    }

    public static String getOutdate(int days) {
        return new SimpleDateFormat("yyyyMMdd").format(new Date(new Date().getTime() - (((long) days) * 86400000)));
    }

    public static String getCreateSql() {
        return getAddSql("create table if not exists JankEvent (id integer primary key autoincrement ", field_Names, field_Types);
    }

    public static void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > 3 && oldVersion <= 3) {
            db.beginTransaction();
            try {
                db.execSQL("alter table JankEvent rename to _temp_JankEvent");
                db.execSQL(getCreateSql());
                db.execSQL("insert into JankEvent select id, CaseName, TimeStamp, Arg1, Arg2, CpuLoad, FreeMem, FreeStorage, Limit_Freq, CpuLoadTop_proc1, CpuLoadTop_proc2, CpuLoadTop_proc3, '0', '0', '0', '0', Reserve1, Reserve2 from _temp_JankEvent");
                db.execSQL("drop table _temp_JankEvent");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

    public static void downgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private static String getAddSql(String str, String[] names, String[] types) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < names.length; i++) {
            buf.append(", ");
            buf.append(names[i]);
            buf.append(" ");
            buf.append(types[i]);
        }
        buf.append(" )");
        return str + buf.toString();
    }
}
