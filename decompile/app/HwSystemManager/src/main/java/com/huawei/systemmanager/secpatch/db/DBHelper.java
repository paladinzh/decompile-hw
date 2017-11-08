package com.huawei.systemmanager.secpatch.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.huawei.systemmanager.util.HwLog;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "secpatch.db";
    private static final int DB_VERSION = 1;
    private static final String TAG = "SecPatchDBHelper";

    protected DBHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    public void onCreate(SQLiteDatabase db) {
        if (db == null) {
            HwLog.e(TAG, "onCreate: Invalid db ,Fail to init DB tables and data");
            return;
        }
        createDBTables(db);
        createDBViews(db);
        HwLog.i(TAG, "onCreate: DB is created");
    }

    public static int getDBVersion() {
        return 1;
    }

    private void createDBTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS secpatch ( _id INTEGER PRIMARY KEY AUTOINCREMENT, pver TEXT,sid TEXT, ocid TEXT,src TEXT, digest TEXT,digest_en TEXT, fix_version TEXT,updated TEXT,etime TEXT  )");
        db.execSQL("CREATE TABLE IF NOT EXISTS systemVersion ( _id INTEGER PRIMARY KEY AUTOINCREMENT, pver TEXT,need_update TEXT  )");
    }

    private void createDBViews(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS searchView");
        db.execSQL("CREATE VIEW IF NOT EXISTS searchView AS SELECT * FROM secpatch WHERE pver = fix_version ORDER BY sid DESC; ");
    }

    private void dropDBTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS searchView");
        db.execSQL("DROP TABLE IF EXISTS secpatch");
        db.execSQL("DROP TABLE IF EXISTS systemVersion");
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        HwLog.i(TAG, "onDowngrade: oldVersion = " + oldVersion + ", newVersion = " + newVersion);
        dropDBTables(db);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        HwLog.i(TAG, "onUpgrade: oldVersion = " + oldVersion + ", newVersion = " + newVersion);
    }
}
