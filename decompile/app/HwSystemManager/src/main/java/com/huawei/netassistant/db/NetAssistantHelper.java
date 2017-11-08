package com.huawei.netassistant.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.huawei.systemmanager.util.HwLog;
import java.io.File;

public class NetAssistantHelper extends SQLiteOpenHelper {
    static final /* synthetic */ boolean -assertionsDisabled;
    public static final int DATABASE_VERSION = 5;
    public static final String DB_NAME = "netassistant.db";
    private static final String TAG = "NetAssistantHelper";
    private static NetAssistantHelper sInstance = null;
    private static Object syncObj = new Object();
    private Context mContext;
    private SQLiteDatabase mDatabase;

    private static class SqlStatement {
        static final String SQL_CREATE_ADJUST_TABLE = "create table if not exists trafficadjustinfo ( id integer primary key autoincrement, imsi text, adjust_value long, adjust_type int, adjust_date long, adjust_province text, adjust_city text, adjust_provider text, adjust_brand text);";
        static final String SQL_CREATE_NETACCESS_TABLE = "create table if not exists netaccessinfo ( id integer primary key autoincrement, package_name text, package_uid int, package_trust int, net_access_type int);";
        static final String SQL_CREATE_SETTING_TABLE = "create table if not exists settinginfo ( id integer primary key autoincrement, imsi text, package_total long, begin_date int, regular_adjust_type int, regular_adjust_begin_time long, excess_monty_type long  DEFAULT (2), is_overmark_month long, is_overmark_day long, is_after_locked int, is_notification int, is_speed_notification int, month_limit_byte long, month_limit_snooze long, month_warn_byte long  DEFAULT (80), month_warn_snooze long, daily_warn_byte long  DEFAULT (10), daily_limit_snooze long);";

        private SqlStatement() {
        }
    }

    static {
        boolean z;
        if (NetAssistantHelper.class.desiredAssertionStatus()) {
            z = false;
        } else {
            z = true;
        }
        -assertionsDisabled = z;
    }

    public static NetAssistantHelper getInstance(Context context) {
        NetAssistantHelper netAssistantHelper;
        synchronized (syncObj) {
            if (sInstance == null) {
                sInstance = new NetAssistantHelper(context);
            }
            netAssistantHelper = sInstance;
        }
        return netAssistantHelper;
    }

    public static void releaseResource() {
        if (-assertionsDisabled) {
            synchronized (syncObj) {
                if (sInstance != null) {
                    sInstance.closeDB();
                    sInstance.close();
                    sInstance = null;
                }
            }
            return;
        }
        throw new AssertionError();
    }

    public NetAssistantHelper(Context context) {
        super(context, DB_NAME, null, 5);
        this.mContext = context;
    }

    private void closeDB() {
        if (this.mDatabase != null) {
            HwLog.e(TAG, " Close Database! - : " + this.mDatabase);
            this.mDatabase.close();
            this.mDatabase = null;
        }
    }

    public static int getDBVersion() {
        return 5;
    }

    private SQLiteDatabase openDatabase() {
        if (!(this.mDatabase == null || new File(this.mDatabase.getPath()).exists())) {
            HwLog.w(TAG, " db file is not exist, close db ");
            closeDB();
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

    public void onCreate(SQLiteDatabase db) {
        createDBTables(db);
    }

    public void createDBTables(SQLiteDatabase db) {
        db.execSQL("create table if not exists settinginfo ( id integer primary key autoincrement, imsi text, package_total long, begin_date int, regular_adjust_type int, regular_adjust_begin_time long, excess_monty_type long  DEFAULT (2), is_overmark_month long, is_overmark_day long, is_after_locked int, is_notification int, is_speed_notification int, month_limit_byte long, month_limit_snooze long, month_warn_byte long  DEFAULT (80), month_warn_snooze long, daily_warn_byte long  DEFAULT (10), daily_limit_snooze long);");
        db.execSQL("create table if not exists trafficadjustinfo ( id integer primary key autoincrement, imsi text, adjust_value long, adjust_type int, adjust_date long, adjust_province text, adjust_city text, adjust_provider text, adjust_brand text);");
        db.execSQL("create table if not exists netaccessinfo ( id integer primary key autoincrement, package_name text, package_uid int, package_trust int, net_access_type int);");
    }

    public void onUpgrade(SQLiteDatabase db, int fromVersion, int toVersion) {
        updateDatabase(this.mContext, db, fromVersion, toVersion);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS settinginfo");
        db.execSQL("DROP TABLE IF EXISTS trafficadjustinfo");
        db.execSQL("DROP TABLE IF EXISTS netaccessinfo");
        onCreate(db);
    }

    private void updateDatabase(Context context, SQLiteDatabase db, int fromVersion, int toVersion) {
        if (toVersion != 5) {
            HwLog.w(TAG, "/updateDatabase: Illegal update request. Got " + toVersion + ", expected " + 5);
            throw new IllegalArgumentException();
        } else if (fromVersion > toVersion) {
            HwLog.w(TAG, "/updateDatabase: Illegal update request: can't downgrade from " + fromVersion + " to " + toVersion + ". Did you forget to wipe data?");
            throw new IllegalArgumentException();
        } else {
            upgradeDB(fromVersion, toVersion, db);
        }
    }

    private void upgradeDB(int fromVersion, int toVersion, SQLiteDatabase db) {
        if (fromVersion < toVersion && toVersion <= 5) {
            switch (fromVersion) {
                case 1:
                    upgradeFrom1To2(db);
                    break;
                case 2:
                    upgradeFrom2To3(db);
                    break;
                case 3:
                    upgradeFrom3To4(db);
                    break;
                case 4:
                    upgradeFrom4To5(db);
                    break;
                default:
                    HwLog.w(TAG, "onUpgrade: Invalid oldVersion: " + fromVersion);
                    return;
            }
            upgradeDB(fromVersion + 1, toVersion, db);
        }
    }

    private void upgradeFrom2To3(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS settinginfo");
        db.execSQL("DROP TABLE IF EXISTS trafficadjustinfo");
        db.execSQL("DROP TABLE IF EXISTS netaccessinfo");
        onCreate(db);
    }

    private void upgradeFrom4To5(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS settinginfo");
        db.execSQL("DROP TABLE IF EXISTS trafficadjustinfo");
        db.execSQL("DROP TABLE IF EXISTS netaccessinfo");
        onCreate(db);
    }

    private void upgradeFrom1To2(SQLiteDatabase db) {
        try {
            db.beginTransaction();
            String tableBak = "settinginfo_bak";
            String tableNew = NetAssistantStore.TABLE_NAME_SETTING_INFO;
            db.execSQL("ALTER TABLE " + tableNew + " RENAME TO " + tableBak);
            db.execSQL("create table if not exists settinginfo ( id integer primary key autoincrement, imsi text, package_total long, begin_date int, regular_adjust_type int, regular_adjust_begin_time long, excess_monty_type long  DEFAULT (2), is_overmark_month long, is_overmark_day long, is_after_locked int, is_notification int, is_speed_notification int, month_limit_byte long, month_limit_snooze long, month_warn_byte long  DEFAULT (80), month_warn_snooze long, daily_warn_byte long  DEFAULT (10), daily_limit_snooze long);");
            db.execSQL("INSERT INTO " + tableNew + "(id, imsi, package_total, begin_date, regular_adjust_type, regular_adjust_begin_time, is_overmark_month, is_overmark_day, is_after_locked, is_notification, is_speed_notification, month_limit_byte)" + " SELECT " + "id, imsi, package_total, begin_date, regular_adjust_type, regular_adjust_begin_time, is_overmark_month, is_overmark_day, is_after_locked, is_notification, is_speed_notification, package_total" + " FROM " + tableBak);
            db.execSQL("DROP TABLE " + tableBak);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            HwLog.e(TAG, "upgradeFrom1To2: Exception", e);
        } finally {
            db.endTransaction();
        }
    }

    private void upgradeFrom3To4(SQLiteDatabase db) {
        try {
            db.beginTransaction();
            String tableBak = "settinginfo_bak";
            String tableNew = NetAssistantStore.TABLE_NAME_SETTING_INFO;
            db.execSQL("ALTER TABLE " + tableNew + " RENAME TO " + tableBak);
            db.execSQL("create table if not exists settinginfo ( id integer primary key autoincrement, imsi text, package_total long, begin_date int, regular_adjust_type int, regular_adjust_begin_time long, excess_monty_type long  DEFAULT (2), is_overmark_month long, is_overmark_day long, is_after_locked int, is_notification int, is_speed_notification int, month_limit_byte long, month_limit_snooze long, month_warn_byte long  DEFAULT (80), month_warn_snooze long, daily_warn_byte long  DEFAULT (10), daily_limit_snooze long);");
            db.execSQL("INSERT INTO " + tableNew + "(id, imsi, package_total, begin_date, regular_adjust_type, regular_adjust_begin_time, is_overmark_month, is_overmark_day, is_after_locked, is_notification, excess_monty_type, is_speed_notification, month_limit_byte, month_warn_byte, daily_warn_byte)" + " SELECT " + "id, imsi, package_total, begin_date, regular_adjust_type, regular_adjust_begin_time, is_overmark_month, is_overmark_day, is_after_locked, is_notification, excess_monty_type, is_speed_notification, month_limit_byte, month_warn_byte, daily_warn_byte" + " FROM " + tableBak);
            db.execSQL("DROP TABLE " + tableBak);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            HwLog.e(TAG, "upgradeFrom2To3: Exception", e);
        } finally {
            db.endTransaction();
        }
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
