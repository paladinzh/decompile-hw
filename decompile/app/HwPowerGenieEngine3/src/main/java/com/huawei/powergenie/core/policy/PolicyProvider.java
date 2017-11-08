package com.huawei.powergenie.core.policy;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import java.util.HashMap;

public final class PolicyProvider extends ContentProvider {
    public static final Uri BACKLIGHT_URI = Uri.parse("content://com.huawei.powergenie/backlight");
    public static final Uri CPU_POLICY_URI = Uri.parse("content://com.huawei.powergenie/cpu_policy");
    public static final Uri PWLEVEL_URI = Uri.parse("content://com.huawei.powergenie/powerlevel");
    public static final Uri SWITCHER_URI = Uri.parse("content://com.huawei.powergenie/switchers");
    public static final Uri SYS_LOAD_URI = Uri.parse("content://com.huawei.powergenie/sys_load_policy");
    public static final HashMap<Integer, Integer> mMaxProfile = new HashMap<Integer, Integer>() {
        {
            put(Integer.valueOf(0), Integer.valueOf(6));
            put(Integer.valueOf(1), Integer.valueOf(27));
            put(Integer.valueOf(2), Integer.valueOf(29));
            put(Integer.valueOf(3), Integer.valueOf(31));
            put(Integer.valueOf(4), Integer.valueOf(19));
            put(Integer.valueOf(5), Integer.valueOf(33));
            put(Integer.valueOf(6), Integer.valueOf(35));
            put(Integer.valueOf(7), Integer.valueOf(37));
        }
    };
    public static final HashMap<Integer, Integer> mMinProfile = new HashMap<Integer, Integer>() {
        {
            put(Integer.valueOf(0), Integer.valueOf(7));
            put(Integer.valueOf(1), Integer.valueOf(28));
            put(Integer.valueOf(2), Integer.valueOf(30));
            put(Integer.valueOf(3), Integer.valueOf(32));
            put(Integer.valueOf(4), Integer.valueOf(20));
            put(Integer.valueOf(5), Integer.valueOf(34));
            put(Integer.valueOf(6), Integer.valueOf(36));
            put(Integer.valueOf(7), Integer.valueOf(38));
        }
    };
    private static DatabaseHelper mOpenHelper;
    public static final HashMap<Integer, Integer> mTagToCpuCore = new HashMap<Integer, Integer>() {
        {
            put(Integer.valueOf(6), Integer.valueOf(0));
            put(Integer.valueOf(7), Integer.valueOf(0));
            put(Integer.valueOf(27), Integer.valueOf(1));
            put(Integer.valueOf(28), Integer.valueOf(1));
            put(Integer.valueOf(29), Integer.valueOf(2));
            put(Integer.valueOf(30), Integer.valueOf(2));
            put(Integer.valueOf(31), Integer.valueOf(3));
            put(Integer.valueOf(32), Integer.valueOf(3));
            put(Integer.valueOf(19), Integer.valueOf(4));
            put(Integer.valueOf(20), Integer.valueOf(4));
            put(Integer.valueOf(33), Integer.valueOf(5));
            put(Integer.valueOf(34), Integer.valueOf(5));
            put(Integer.valueOf(35), Integer.valueOf(6));
            put(Integer.valueOf(36), Integer.valueOf(6));
            put(Integer.valueOf(37), Integer.valueOf(7));
            put(Integer.valueOf(38), Integer.valueOf(7));
        }
    };
    private static UriMatcher sURIMatcher = new UriMatcher(-1);

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, "powergenie.db", null, 1);
        }

        public void onCreate(SQLiteDatabase db) {
            Log.i("PolicyProvider", "create tables.");
            db.execSQL("CREATE TABLE powerlevel (_id INTEGER PRIMARY KEY, power_level INTEGER  NOT NULL, context INTEGER, state INTEGER, mode INTEGER);");
            db.execSQL("CREATE TABLE switchers (_id INTEGER PRIMARY KEY, power_level INTEGER NOT NULL, switcher_id INTEGER NOT NULL, switcher_value INTEGER NOT NULL, switcher_flag INTEGER NOT NULL);");
            db.execSQL("CREATE TABLE cpu_policy (_id INTEGER PRIMARY KEY, action_id INTEGER NOT NULL, power_mode INTEGER NOT NULL, policy_type INTEGER NOT NULL, policy_value INTEGER NOT NULL, pkg_name TEXT, extend TEXT);");
            db.execSQL("CREATE TABLE sys_load_policy (_id INTEGER PRIMARY KEY, check_mode INTEGER NOT NULL, up_load INTEGER NOT NULL, up_check_times INTEGER NOT NULL, up_check_space INTEGER NOT NULL, up_offset INTEGER NOT NULL, max_check_times INTEGER NOT NULL, extend TEXT);");
            db.execSQL("CREATE TABLE backlight (_id INTEGER PRIMARY KEY, action_id INTEGER NOT NULL, power_mode INTEGER NOT NULL, policy_type INTEGER NOT NULL, policy_value INTEGER NOT NULL, pkg_name TEXT);");
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (newVersion > oldVersion) {
                Log.i("PolicyProvider", "Version: " + oldVersion + " -> " + newVersion);
                db.execSQL("DROP TABLE IF EXISTS CREATE TABLE powerlevel (_id INTEGER PRIMARY KEY, power_level INTEGER  NOT NULL, context INTEGER, state INTEGER, mode INTEGER);");
                db.execSQL("DROP TABLE IF EXISTS CREATE TABLE switchers (_id INTEGER PRIMARY KEY, power_level INTEGER NOT NULL, switcher_id INTEGER NOT NULL, switcher_value INTEGER NOT NULL, switcher_flag INTEGER NOT NULL);");
                db.execSQL("DROP TABLE IF EXISTS CREATE TABLE cpu_policy (_id INTEGER PRIMARY KEY, action_id INTEGER NOT NULL, power_mode INTEGER NOT NULL, policy_type INTEGER NOT NULL, policy_value INTEGER NOT NULL, pkg_name TEXT, extend TEXT);");
                db.execSQL("DROP TABLE IF EXISTS CREATE TABLE sys_load_policy (_id INTEGER PRIMARY KEY, check_mode INTEGER NOT NULL, up_load INTEGER NOT NULL, up_check_times INTEGER NOT NULL, up_check_space INTEGER NOT NULL, up_offset INTEGER NOT NULL, max_check_times INTEGER NOT NULL, extend TEXT);");
                db.execSQL("DROP TABLE IF EXISTS CREATE TABLE backlight (_id INTEGER PRIMARY KEY, action_id INTEGER NOT NULL, power_mode INTEGER NOT NULL, policy_type INTEGER NOT NULL, policy_value INTEGER NOT NULL, pkg_name TEXT);");
                onCreate(db);
            }
        }
    }

    static {
        sURIMatcher.addURI("com.huawei.powergenie", "powerlevel", 1);
        sURIMatcher.addURI("com.huawei.powergenie", "switchers", 3);
        sURIMatcher.addURI("com.huawei.powergenie", "cpu_policy", 4);
        sURIMatcher.addURI("com.huawei.powergenie", "sys_load_policy", 5);
        sURIMatcher.addURI("com.huawei.powergenie", "backlight", 6);
    }

    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    public String getType(Uri uri) {
        return "vnd.android.cursor.item/powergenie";
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        if (db == null) {
            return null;
        }
        switch (sURIMatcher.match(uri)) {
            case NativeAdapter.PLATFORM_MTK /*1*/:
                return db.query("powerlevel", projection, selection, selectionArgs, null, null, sortOrder);
            case NativeAdapter.PLATFORM_K3V3 /*3*/:
                return db.query("switchers", projection, selection, selectionArgs, null, null, sortOrder);
            case 4:
                return db.query("cpu_policy", projection, selection, selectionArgs, null, null, sortOrder);
            case 5:
                return db.query("sys_load_policy", projection, selection, selectionArgs, null, null, sortOrder);
            case 6:
                return db.query("backlight", projection, selection, selectionArgs, null, null, sortOrder);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if (db != null) {
            long newRecordId;
            switch (sURIMatcher.match(uri)) {
                case NativeAdapter.PLATFORM_MTK /*1*/:
                    newRecordId = db.insert("powerlevel", null, values);
                    break;
                case NativeAdapter.PLATFORM_K3V3 /*3*/:
                    newRecordId = db.insert("switchers", null, values);
                    break;
                case 4:
                    newRecordId = db.insert("cpu_policy", null, values);
                    break;
                case 5:
                    newRecordId = db.insert("sys_load_policy", null, values);
                    break;
                case 6:
                    newRecordId = db.insert("backlight", null, values);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
            if (newRecordId != -1) {
                getContext().getContentResolver().notifyChange(uri, null);
            } else {
                Log.e("PolicyProvider", "insert failed! uri=" + uri);
            }
        } else {
            Log.e("PolicyProvider", "can not get the database!");
        }
        return uri;
    }

    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        int count;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sURIMatcher.match(uri)) {
            case NativeAdapter.PLATFORM_MTK /*1*/:
                count = db.update("powerlevel", values, where, whereArgs);
                break;
            case NativeAdapter.PLATFORM_K3V3 /*3*/:
                count = db.update("switchers", values, where, whereArgs);
                break;
            case 4:
                count = db.update("cpu_policy", values, where, whereArgs);
                break;
            case 5:
                count = db.update("sys_load_policy", values, where, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    public int delete(Uri uri, String where, String[] whereArgs) {
        int count;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sURIMatcher.match(uri)) {
            case NativeAdapter.PLATFORM_MTK /*1*/:
                count = db.delete("powerlevel", where, whereArgs);
                break;
            case NativeAdapter.PLATFORM_K3V3 /*3*/:
                count = db.delete("switchers", where, whereArgs);
                break;
            case 4:
                count = db.delete("cpu_policy", where, whereArgs);
                break;
            case 5:
                count = db.delete("sys_load_policy", where, whereArgs);
                break;
            case 6:
                count = db.delete("backlight", where, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
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
