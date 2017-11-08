package com.huawei.powergenie.core.policy;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;
import com.huawei.powergenie.integration.adapter.NativeAdapter;

public final class IntelligentProvider extends ContentProvider {
    public static final Uri APPSINFO_URI = Uri.parse("content://com.huawei.powergenie.stats/appinfo");
    public static final Uri APP_SCROFF_URI = Uri.parse("content://com.huawei.powergenie.stats/appscroff");
    public static final Uri SCROFF_URI = Uri.parse("content://com.huawei.powergenie.stats/scroff");
    public static final Uri USER_ACTIVITY_URI = Uri.parse("content://com.huawei.powergenie.stats/useractivity");
    private static DatabaseHelper mOpenHelper;
    private static UriMatcher sURIMatcher = new UriMatcher(-1);
    private int mDeleteCount = 0;
    private int mInsertCount = 0;
    private int mUpdateCount = 0;

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, "pgstats.db", null, 1);
        }

        public void onCreate(SQLiteDatabase db) {
            Log.i("IntelligentProvider", "create stats tables.");
            db.execSQL("CREATE TABLE scroff (_id INTEGER PRIMARY KEY, startTime TEXT  NOT NULL, totalTime LONG, wkTime LONG, powerUsage INTEGER, avgPower INTEGER, wakeups LONG, mobileRx LONG, mobileTx LONG, wifiRx LONG, wifiTx LONG, wifiScan LONG, gpsTime LONG, reason TEXT);");
            db.execSQL("CREATE TABLE useractivity ( userStateId INTEGER PRIMARY KEY NOT NULL, userState TEXT, stateType INTEGER ,totalTime LONG );");
            db.execSQL("CREATE TABLE appinfo ( appName TEXT PRIMARY KEY NOT NULL, sysApp INTEGER, signature INTEGER, useHardware INTEGER, appType INTEGER, ownerCom INTEGER, hasIcon INTEGER);");
            db.execSQL("CREATE TABLE appscroff (appName TEXT PRIMARY KEY NOT NULL, wakeups LONG, bgCpuTime LONG, bgUseTime LONG, wkTime LONG, wifiScan LONG, gpsTime LONG, mobileRx LONG, mobileTx LONG, wifiRx LONG, wifiTx LONG, consumptionRank INTEGER, updateTime TEXT);");
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (newVersion > oldVersion) {
                Log.i("IntelligentProvider", "Version: " + oldVersion + " -> " + newVersion);
            }
        }
    }

    static {
        sURIMatcher.addURI("com.huawei.powergenie.stats", "scroff", 1);
        sURIMatcher.addURI("com.huawei.powergenie.stats", "useractivity", 2);
        sURIMatcher.addURI("com.huawei.powergenie.stats", "appinfo", 3);
        sURIMatcher.addURI("com.huawei.powergenie.stats", "appscroff", 4);
    }

    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    public String getType(Uri uri) {
        return "vnd.android.cursor.item/stats";
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        if (db != null) {
            switch (sURIMatcher.match(uri)) {
                case NativeAdapter.PLATFORM_MTK /*1*/:
                    return db.query("scroff", projection, selection, selectionArgs, null, null, sortOrder);
                case NativeAdapter.PLATFORM_HI /*2*/:
                    return db.query("useractivity", projection, selection, selectionArgs, null, null, sortOrder);
                case NativeAdapter.PLATFORM_K3V3 /*3*/:
                    return db.query("appinfo", projection, selection, selectionArgs, null, null, sortOrder);
                case 4:
                    return db.query("appscroff", projection, selection, selectionArgs, null, null, sortOrder);
                default:
                    try {
                        throw new IllegalArgumentException("Unknown URI " + uri);
                    } catch (SQLiteException e) {
                        Log.e("IntelligentProvider", "query datebase error", e);
                        mOpenHelper.close();
                        break;
                    }
            }
        }
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long newRecordId = 0;
        if (db != null) {
            int matchCode = sURIMatcher.match(uri);
            switch (matchCode) {
                case NativeAdapter.PLATFORM_MTK /*1*/:
                    newRecordId = db.insert("scroff", null, values);
                    break;
                case NativeAdapter.PLATFORM_HI /*2*/:
                    newRecordId = db.insert("useractivity", null, values);
                    break;
                case NativeAdapter.PLATFORM_K3V3 /*3*/:
                    newRecordId = db.insert("appinfo", null, values);
                    break;
                case 4:
                    newRecordId = db.insert("appscroff", null, values);
                    break;
                default:
                    try {
                        throw new IllegalArgumentException("Unknown URI " + uri);
                    } catch (SQLiteException e) {
                        Log.e("IntelligentProvider", "insert datebase error", e);
                        mOpenHelper.close();
                        break;
                    }
            }
            if (newRecordId != -1 && matchCode == 3) {
                getContext().getContentResolver().notifyChange(uri, null);
            } else if (newRecordId == -1) {
                Log.e("IntelligentProvider", "insert failed! uri=" + uri);
            }
        } else {
            Log.e("IntelligentProvider", "insert can not get the database!");
        }
        return uri;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if (db == null) {
            Log.e("IntelligentProvider", "update can not get the database!");
            return 0;
        }
        int count = 0;
        try {
            switch (sURIMatcher.match(uri)) {
                case NativeAdapter.PLATFORM_HI /*2*/:
                    count = db.update("useractivity", values, where, whereArgs);
                    break;
                case NativeAdapter.PLATFORM_K3V3 /*3*/:
                    count = db.update("appinfo", values, where, whereArgs);
                    break;
                case 4:
                    count = db.update("appscroff", values, where, whereArgs);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
        } catch (SQLiteException e) {
            Log.e("IntelligentProvider", "update datebase error", e);
            mOpenHelper.close();
        }
        if (count > 0 && sURIMatcher.match(uri) == 3) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if (db == null) {
            Log.e("IntelligentProvider", "delete can not get the database!");
            return 0;
        }
        int count = -1;
        try {
            switch (sURIMatcher.match(uri)) {
                case NativeAdapter.PLATFORM_MTK /*1*/:
                    count = db.delete("scroff", where, whereArgs);
                    break;
                case NativeAdapter.PLATFORM_K3V3 /*3*/:
                    count = db.delete("appinfo", where, whereArgs);
                    break;
                case 4:
                    count = db.delete("appscroff", where, whereArgs);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
        } catch (SQLiteException e) {
            Log.e("IntelligentProvider", "delete datebase error", e);
            mOpenHelper.close();
        }
        return count > 0 ? count : count;
    }

    public static void beginTransaction() {
        if (mOpenHelper != null) {
            mOpenHelper.getWritableDatabase().beginTransaction();
        }
    }

    public static void setTransactionSuccessful() {
        if (mOpenHelper != null) {
            mOpenHelper.getWritableDatabase().setTransactionSuccessful();
        }
    }

    public static void endTransaction() {
        if (mOpenHelper != null) {
            mOpenHelper.getWritableDatabase().endTransaction();
        }
    }
}
