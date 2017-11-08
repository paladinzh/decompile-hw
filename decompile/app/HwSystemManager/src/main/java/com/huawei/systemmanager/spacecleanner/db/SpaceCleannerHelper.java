package com.huawei.systemmanager.spacecleanner.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.huawei.systemmanager.util.HwLog;

public class SpaceCleannerHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 2;
    public static final String DB_NAME = "spacecleanner.db";
    private static final String TAG = "SpaceCleannerHelper";
    private static Object syncObj = new Object();
    private Context mContext;
    private SQLiteDatabase mDatabase;

    private static class SqlStatement {
        static final String SQL_CREATE_PROTECT_TABLE = "create table if not exists protectfolder ( id integer primary key autoincrement, pkgname text, package_path text);";
        static final String SQL_CREATE_TRASH_TABLE = "create table if not exists hwtrashinfo ( id integer primary key autoincrement, pkgname text, trash_path text, trash_type int, trash_recommended int, trash_rule text, trash_keep_time int, trash_keep_latest int);";

        private SqlStatement() {
        }
    }

    public SpaceCleannerHelper(Context context) {
        super(context, DB_NAME, null, 2);
        this.mContext = context;
    }

    public static int getDBVersion() {
        return 2;
    }

    public void onCreate(SQLiteDatabase db) {
        createDBTables(db);
    }

    public void createDBTables(SQLiteDatabase db) {
        db.execSQL("create table if not exists protectfolder ( id integer primary key autoincrement, pkgname text, package_path text);");
        db.execSQL("create table if not exists hwtrashinfo ( id integer primary key autoincrement, pkgname text, trash_path text, trash_type int, trash_recommended int, trash_rule text, trash_keep_time int, trash_keep_latest int);");
    }

    public void onUpgrade(SQLiteDatabase db, int fromVersion, int toVersion) {
        if (toVersion < 2) {
            updateDatabase(this.mContext, db, fromVersion, toVersion);
        } else {
            upgrade1To2(db, fromVersion, toVersion);
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS create table if not exists protectfolder ( id integer primary key autoincrement, pkgname text, package_path text);");
        db.execSQL("DROP TABLE IF EXISTS create table if not exists hwtrashinfo ( id integer primary key autoincrement, pkgname text, trash_path text, trash_type int, trash_recommended int, trash_rule text, trash_keep_time int, trash_keep_latest int);");
        db.execSQL("DROP TABLE IF EXISTS rarelyusedapp");
        onCreate(db);
    }

    private void upgrade1To2(SQLiteDatabase db, int fromVersion, int toVersion) {
        if (fromVersion <= 1 && toVersion > 1) {
            db.execSQL("DROP TABLE IF EXISTS rarelyusedapp");
        }
    }

    private void updateDatabase(Context context, SQLiteDatabase db, int fromVersion, int toVersion) {
        if (toVersion != 2) {
            HwLog.w(TAG, "/updateDatabase: Illegal update request. Got " + toVersion + ", expected " + 2);
            throw new IllegalArgumentException();
        } else if (fromVersion > toVersion) {
            HwLog.w(TAG, "/updateDatabase: Illegal update request: can't downgrade from " + fromVersion + " to " + toVersion + ". Did you forget to wipe data?");
            throw new IllegalArgumentException();
        }
    }

    public int delete(String table, String selection, String[] selectionArgs) {
        int delete;
        synchronized (syncObj) {
            this.mDatabase = getWritableDatabase();
            try {
                delete = this.mDatabase.delete(table, selection, selectionArgs);
            } catch (Exception e) {
                HwLog.w(TAG, "/delete :  operate DB faild");
                return 0;
            }
        }
        return delete;
    }

    public long insert(String table, String columns, ContentValues values) {
        long insert;
        synchronized (syncObj) {
            this.mDatabase = getWritableDatabase();
            try {
                insert = this.mDatabase.insert(table, columns, values);
            } catch (Exception e) {
                HwLog.w(TAG, "/insert :  operate DB faild");
                return 0;
            }
        }
        return insert;
    }

    public Cursor query(Boolean distinct, String table, String[] projection, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        Cursor cursor;
        synchronized (syncObj) {
            this.mDatabase = getWritableDatabase();
            cursor = null;
            try {
                cursor = this.mDatabase.query(table, projection, selection, selectionArgs, groupBy, having, orderBy);
            } catch (Exception e) {
                HwLog.w(TAG, "/query :  operate DB faild ! " + e.getMessage());
            }
        }
        return cursor;
    }

    public int update(String table, ContentValues values, String selection, String[] selectionArgs) {
        int update;
        synchronized (syncObj) {
            this.mDatabase = getWritableDatabase();
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
