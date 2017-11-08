package com.trustlook.sdk.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.trustlook.sdk.Constants;

public class DBHelper extends SQLiteOpenHelper {
    public static final String COLUMN_APKPATH = "apk_path";
    public static final String COLUMN_CATEGORY = "risk_category";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_MD5 = "md5";
    public static final String COLUMN_PACKAGE_NAME = "package_name";
    public static final String COLUMN_SCORE = "risk_score";
    public static final String COLUMN_SIZE = "apk_size";
    public static final String COLUMN_VIRUS_NAME = "virus_name";
    public static final String DATABASE_NAME = "trustlook.db";
    public static final String TABLE_APP_INFO = "table_appinfo";
    public static final String TABLE_RISK_HISTORY = "table_risk_history";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS table_appinfo (_id INTEGER PRIMARY KEY AUTOINCREMENT, md5 CHAR(50) UNIQUE, package_name CHAR(100), apk_path TEXT, apk_size INT, risk_score INT, risk_category TEXT, virus_name CHAR(200) )");
    }

    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        Log.e(Constants.TAG, "old: " + i + ", new: " + i2);
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS table_appinfo (_id INTEGER PRIMARY KEY AUTOINCREMENT, md5 CHAR(50) UNIQUE, package_name CHAR(100), apk_path TEXT, apk_size INT, risk_score INT, risk_category TEXT, virus_name CHAR(200) )");
    }
}
