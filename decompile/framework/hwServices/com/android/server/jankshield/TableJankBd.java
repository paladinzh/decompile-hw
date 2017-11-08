package com.android.server.jankshield;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.JankBdData;
import android.os.JankBdItem;
import android.util.Log;
import com.android.server.devicepolicy.HwDevicePolicyManagerServiceUtil;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TableJankBd {
    protected static final boolean HWDBG;
    protected static final boolean HWFLOW;
    protected static final boolean HWLOGW_E = true;
    private static final String TAG = "JankShield";
    private static final String[] field_Names = new String[]{"CaseName", "TimeStamp", "AppName", "TotalTime", "Marks", "Section1_cnt", "Section2_cnt", "Section3_cnt", "Section4_cnt", "Section5_cnt", "Section6_cnt", "Section7_cnt", "Section8_cnt", "Section9_cnt", "Section10_cnt"};
    private static final String[] field_Types = new String[]{"varchar", "varchar", "varchar", "integer", "varchar", "integer", "integer", "integer", "integer", "integer", "integer", "integer", "integer", "integer", "integer"};
    public static final long recDELTACOUNT = 2000;
    private static long recordCount = 0;
    public static final long recordMAXCOUNT = 20000;
    public static final String tbName = "JankBd";

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

    public static boolean insert(SQLiteDatabase db, JankBdData jankbd) {
        List<JankBdItem> itemslist = jankbd.getItems();
        for (int i = 0; i < itemslist.size(); i++) {
            JankBdItem item = (JankBdItem) itemslist.get(i);
            if (!item.isEmpty()) {
                insertItem(db, item);
            }
        }
        if (recordCount >= 22000) {
            deleteOutdate(db, (recordCount - recordMAXCOUNT) + 1);
        }
        return true;
    }

    private static void deleteOutdate(SQLiteDatabase db, long num) {
        String lastTime = getOutdate(30);
        db.beginTransaction();
        try {
            db.execSQL("delete from JankBd where TimeStamp <= " + lastTime + " ");
            db.execSQL("delete from JankBd where id in(select id from JankBd order by TimeStamp asc limit " + num + " )");
            db.setTransactionSuccessful();
            getCount(db);
        } finally {
            db.endTransaction();
        }
    }

    public static JankBdItem extractItem(Cursor cur) {
        JankBdItem item = new JankBdItem();
        item.id = cur.getInt(cur.getColumnIndex(HwDevicePolicyManagerServiceUtil.EXCHANGE_ID));
        item.casename = cur.getString(cur.getColumnIndex("CaseName"));
        item.timestamp = cur.getString(cur.getColumnIndex("TimeStamp"));
        item.appname = cur.getString(cur.getColumnIndex("AppName"));
        item.marks = cur.getString(cur.getColumnIndex("Marks"));
        for (int i = 1; i <= 10; i++) {
            int index = cur.getColumnIndex("Section" + i + "_cnt");
            if (index < 0 || cur.isNull(index)) {
                break;
            }
            int value = cur.getInt(index);
            if (value < 0) {
                break;
            }
            item.sectionCnts.add(Integer.valueOf(value));
        }
        return item;
    }

    public static JankBdItem queryItem(SQLiteDatabase db, String casename, String appname, String marks) {
        Cursor cursor = null;
        JankBdItem jankBdItem = null;
        try {
            cursor = db.rawQuery("select * from JankBd where CaseName = '" + casename + "' AND AppName = '" + appname + "' AND Marks = '" + marks + "' AND TimeStamp >= '" + getOutdate(40) + "'" + " order by TimeStamp desc limit 1 ", null);
            if (cursor != null && cursor.moveToFirst()) {
                jankBdItem = extractItem(cursor);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return jankBdItem;
    }

    public static boolean insertItem(SQLiteDatabase db, JankBdItem item) {
        boolean ret = false;
        ContentValues values = item.getContentValues(field_Names);
        if (values == null) {
            return false;
        }
        if (-1 != db.insert(tbName, null, values)) {
            ret = true;
            recordCount++;
        } else if (HWFLOW) {
            Log.i(TAG, "insert(" + values.toString() + ") failed");
        }
        return ret;
    }

    public static boolean updateItem(SQLiteDatabase db, int id, JankBdItem item) {
        boolean ret = true;
        ContentValues values = item.getContentValues(field_Names);
        if (values == null) {
            return false;
        }
        try {
            db.update(tbName, values, "id = ?", new String[]{id + AppHibernateCst.INVALID_PKG});
        } catch (Exception e) {
            ret = false;
            if (HWFLOW) {
                Log.i(TAG, "update() there catched Exception " + e.toString());
            }
        }
        return ret;
    }

    public static long getCount(SQLiteDatabase db) {
        recordCount = DatabaseUtils.longForQuery(db, "select count(*) from JankBd", null);
        return recordCount;
    }

    public static void dropTb(SQLiteDatabase db) {
        db.execSQL("drop table if exists JankBd" + ";");
    }

    public static String getOutdate(int days) {
        return new SimpleDateFormat("yyyyMMdd").format(new Date(new Date().getTime() - (((long) days) * 86400000)));
    }

    public static String getCreateSql() {
        return getAddSql("create table if not exists JankBd (id integer primary key autoincrement ", field_Names, field_Types);
    }

    public static void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion >= 3 && (oldVersion == 1 || oldVersion == 2)) {
            db.execSQL("alter table JankBd rename to _temp_JankBd");
            db.execSQL(getCreateSql());
            db.execSQL("insert into JankBd select id, CaseName, TimeStamp, AppName, '0', Marks, Section1_cnt, Section2_cnt, Section3_cnt, Section4_cnt, Section5_cnt, Section6_cnt, Section7_cnt, Section8_cnt, Section9_cnt, Section10_cnt from _temp_JankBd");
            db.execSQL("drop table _temp_JankBd");
            oldVersion = 3;
        }
        if (oldVersion != newVersion) {
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
