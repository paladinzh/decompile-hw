package com.huawei.systemmanager.netassistant.db.comm;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.huawei.systemmanager.util.HwLog;
import java.io.File;

@TargetApi(19)
public abstract class IDataBaseHelper extends SQLiteOpenHelper {
    protected static final String TAG = IDataBaseHelper.class.getSimpleName();
    private static Object syncObj = new Object();
    private Context mContext;
    private SQLiteDatabase mDatabase;
    protected DBTable[] mTables;

    public IDataBaseHelper(Context context, String dbName, int version, DBTable... tables) {
        super(context, dbName, null, version);
        this.mTables = tables;
        this.mContext = context;
    }

    public void onCreate(SQLiteDatabase db) {
        HwLog.d(TAG, "on Create");
        for (DBTable tableInfo : this.mTables) {
            db.execSQL(tableInfo.getTableCreateCmd());
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (DBTable tableInfo : this.mTables) {
            db.execSQL(tableInfo.getTableDropCmd());
        }
    }

    public DBTable[] getTables() {
        return (DBTable[]) this.mTables.clone();
    }

    public void closeDB() {
        if (this.mDatabase != null) {
            HwLog.e(TAG, " Close Database! - : " + this.mDatabase);
            this.mDatabase.close();
            this.mDatabase = null;
        }
    }

    public SQLiteDatabase openDatabase() {
        if (!(this.mDatabase == null || new File(this.mDatabase.getPath()).exists())) {
            closeDB();
            HwLog.w(TAG, " db file is not exist, close db ");
        }
        if (this.mDatabase == null) {
            try {
                this.mDatabase = getWritableDatabase();
            } catch (Exception e) {
                e.printStackTrace();
            }
            HwLog.w(TAG, " DBHelper Create Database!  : " + this.mDatabase);
            if (this.mDatabase == null) {
                HwLog.e(TAG, " mDatabase is null ");
                return this.mDatabase;
            }
        }
        return this.mDatabase;
    }

    public long insert(String table, String columns, ContentValues values) {
        long insert;
        synchronized (syncObj) {
            openDatabase();
            try {
                insert = this.mDatabase.insert(table, columns, values);
            } catch (Exception e) {
                HwLog.w(TAG, "/insert :  operate DB faild");
                return 0;
            }
        }
        return insert;
    }

    public int delete(String table, String selection, String[] selectionArgs) {
        int delete;
        synchronized (syncObj) {
            openDatabase();
            try {
                delete = this.mDatabase.delete(table, selection, selectionArgs);
            } catch (Exception e) {
                HwLog.w(TAG, "/delete :  operate DB faild");
                return 0;
            }
        }
        return delete;
    }

    public Cursor query(Boolean distinct, String table, String[] projection, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        Cursor cursor;
        synchronized (syncObj) {
            openDatabase();
            cursor = null;
            try {
                cursor = this.mDatabase.query(distinct.booleanValue(), table, projection, selection, selectionArgs, groupBy, having, orderBy, limit);
            } catch (Exception e) {
                HwLog.w(TAG, "/query :  operate DB faild ! " + e.getMessage());
            }
        }
        return cursor;
    }

    public int update(String table, ContentValues values, String selection, String[] selectionArgs) {
        int update;
        synchronized (syncObj) {
            openDatabase();
            try {
                update = this.mDatabase.update(table, values, selection, selectionArgs);
            } catch (Exception e) {
                HwLog.w(TAG, "/update :  operate DB faild");
                return 0;
            }
        }
        return update;
    }
}
