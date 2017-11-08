package com.huawei.harassmentinterception.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import com.huawei.harassmentinterception.common.Tables.TbInterceptionRules;
import com.huawei.systemmanager.comm.misc.Closeables;
import com.huawei.systemmanager.util.HwLog;
import java.io.Closeable;
import java.util.List;

public abstract class TableBase {
    static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS ";
    public static final String TMP_TABLE_SUFFIX = "_tmpbak";

    public abstract void combineRecoverData(SQLiteDatabase sQLiteDatabase, int i);

    public abstract void create(SQLiteDatabase sQLiteDatabase);

    public abstract String createTempTable(SQLiteDatabase sQLiteDatabase);

    public abstract String getTableName();

    public String getTempTablename() {
        return getTableName() + "_tmpbak";
    }

    public void drop(SQLiteDatabase db) {
        db.execSQL(SQL_DROP_TABLE + getTableName());
    }

    protected static void putKeyStatusIntoContent(List<ContentValues> contentList, String key, int status) {
        ContentValues values = new ContentValues();
        values.put("key", key);
        values.put("status", Integer.valueOf(status));
        contentList.add(values);
    }

    protected static void putKeyStatusIntoContent(List<ContentValues> contentList, String key, int status, int value) {
        ContentValues values = new ContentValues();
        values.put("key", key);
        values.put("status", Integer.valueOf(status));
        values.put(TbInterceptionRules.VALUE_1, Integer.valueOf(value));
        values.put(TbInterceptionRules.VALUE_2, Integer.valueOf(value));
        contentList.add(values);
    }

    protected static boolean checkIfValueExsist(SQLiteDatabase db, String table, String column, String value) {
        Closeable closeable = null;
        boolean z;
        try {
            closeable = db.query(table, null, column + "=?", new String[]{value}, null, null, null);
            if (closeable == null) {
                z = false;
                return z;
            }
            int count = closeable.getCount();
            if (count <= 0) {
                Closeables.close(closeable);
                return false;
            }
            if (count > 1) {
                HwLog.e("TableBase", "checkIfValueExsist column is multi, table:" + table + ", column:" + column);
            }
            Closeables.close(closeable);
            return true;
        } catch (Exception e) {
            z = "TableBase";
            HwLog.e(z, "checkIfValueExsist error", e);
            return false;
        } finally {
            Closeables.close(closeable);
        }
    }
}
