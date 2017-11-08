package com.huawei.systemmanager.power.provider;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings.Global;
import com.huawei.android.provider.SettingsEx.Systemex;
import com.huawei.systemmanager.backup.BackupConst;
import com.huawei.systemmanager.backup.BackupUtil;
import com.huawei.systemmanager.backup.HsmContentProvider;
import com.huawei.systemmanager.backup.HsmSQLiteOpenHelper;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.wrapper.SharePrefWrapper;
import com.huawei.systemmanager.customize.AbroadUtils;
import com.huawei.systemmanager.power.comm.ActionConst;
import com.huawei.systemmanager.power.comm.SharedPrefKeyConst;
import com.huawei.systemmanager.power.model.BatteryStatisticsHelper;
import com.huawei.systemmanager.power.model.DateMapToTimeSceneHelper;
import com.huawei.systemmanager.power.model.PowerModeControl;
import com.huawei.systemmanager.power.model.RemainingTimeSceneHelper;
import com.huawei.systemmanager.power.model.UnifiedPowerAppControl;
import com.huawei.systemmanager.power.model.UsageStatusHelper;
import com.huawei.systemmanager.power.util.SysCoreUtils;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Map;

public class SmartProvider extends HsmContentProvider {
    public static final String AUTH = "com.huawei.android.smartpowerprovider";
    private static final int BATTERY_STATISTICS_APPS_TABLE_ITEM_CODE = 111;
    private static final int BATTERY_STATISTICS_APP_TABLE_ITEM_CODE = 110;
    public static final String BATTERY_STATISTICS_TABLE = "batterystatistics";
    public static final Uri BATTERY_STATISTICS_TABLE_URI = Uri.parse("content://com.huawei.android.smartpowerprovider/batterystatistics");
    public static final String CALL_METHOD_ARGS_ALL = "all";
    public static final String CALL_METHOD_ARGS_PROTECT = "protect";
    public static final String CALL_METHOD_ARGS_UNPROTECT = "unprotect";
    private static final String CALL_METHOD_GET_FREE_LIST_POWERGENIE = "hsm_get_freeze_list";
    private static final String CALL_METHOD_MODIFY_UNIFIEDPOWERAPPS = "hsm_modify_unifiedpowerapps";
    public static final String CALL_METHOD_PROTECT_KEY = "frz_protect";
    public static final String CALL_METHOD_UNPROTECT_KEY = "frz_unprotect";
    public static final String CREATE_FORBIDDENAPPS_TABLE = "CREATE TABLE IF NOT EXISTS forbiddenapps(_id INTEGER PRIMARY KEY AUTOINCREMENT , package_name TEXT, app_type INTEGER);";
    public static final String CREATE_PROTECTEDAPPS_TABLE = "CREATE TABLE IF NOT EXISTS protectedapps(_id INTEGER PRIMARY KEY AUTOINCREMENT , package_name TEXT, list_type INTEGER);";
    public static final String CREATE_ROGUE_APPS_TABLE = "CREATE TABLE IF NOT EXISTS rogueapps(pkgname TEXT PRIMARY KEY , isrogue INTEGER default 0, ignore INTEGER default 0, clear INTEGER default 0, presetblackapp INTEGER default 1,highwakefreq INTEGER default 0,ignorewakeup INTEGER default 0,time INTEGER default 0,roguetype INTEGER default 0);";
    public static final String CREATE_SUPERPOWER_APPS_TABLE = "CREATE TABLE IF NOT EXISTS superpowerapps(pkgname TEXT  , isrogue INTEGER default 0, ignore INTEGER default 0, clear INTEGER default 0, presetblackapp INTEGER default 1,highwakefreq INTEGER default 0,ignorewakeup INTEGER default 0,time Long default 0,roguetype INTEGER default 0);";
    public static final String CREATE_WAKEUP_APPS_TABLE = "CREATE TABLE IF NOT EXISTS wakeupapps(pkgname TEXT PRIMARY KEY , wakeupnum_all INTEGER default 0, wakeupnum_h INTEGER default 0 );";
    public static final String DATABASE_NAME = "smartpowerprovider.db";
    public static final int DATABASE_VERSION = 21;
    public static final int DATABASE_VERSION_FOR_UNIFIEDPOWERAPPS_BACKUP = 13;
    private static final int DATE_MAPTO_TIME_SCENE_APPS_TABLE_ITEM_CODE = 117;
    private static final int DATE_MAPTO_TIME_SCENE_APP_TABLE_ITEM_CODE = 116;
    public static final String DATE_MAPTO_TIME_SCENE_TABLE = "datemaptotimescene";
    public static final Uri DATE_MAPTO_TIME_SCENE_TABLE_URI = Uri.parse("content://com.huawei.android.smartpowerprovider/datemaptotimescene");
    public static final String FORBIDDEN_APPS = "forbiddenapps";
    private static final int FORBIDDEN_APPS_ITEMS_CODE = 1;
    private static final int FORBIDDEN_APPS_ITEM_CODE = 2;
    private static final int MAX_OPERATIONS_PER_PATCH = 500;
    public static final Uri NIFIED_POWER_APP_DEFAULT_VALUE_RUI = Uri.parse("content://com.huawei.android.smartpowerprovider/unifiedpowerappsdefaultvalue");
    private static final String PREFERENCE_BACKUP = "power_preference_backup";
    private static final int PREFERENCE_BAK = 16;
    public static final String PROTECTED_APPS = "protectedapps";
    private static final int PROTECTED_APPS_ITEMS_CODE = 3;
    private static final int PROTECTED_APPS_ITEM_CODE = 4;
    private static final int REMAINING_TIME_SCENE_APPS_TABLE_ITEM_CODE = 115;
    private static final int REMAINING_TIME_SCENE_APP_TABLE_ITEM_CODE = 114;
    public static final String REMAINING_TIME_SCENE_TABLE = "timescene";
    public static final Uri REMAINING_TIME_SCENE_TABLE_URI = Uri.parse("content://com.huawei.android.smartpowerprovider/timescene");
    public static final String ROGUE_APPS = "rogueapps";
    private static final String ROGUE_APPS_BACKUP = "rogueapps_backup";
    private static final int ROGUE_APPS_ITEMS_CODE = 5;
    private static final int ROGUE_APPS_ITEMS_CODE_BAK = 15;
    private static final int ROGUE_APPS_ITEM_CODE = 6;
    public static final Uri ROGUE_APP_RUI = Uri.parse("content://com.huawei.android.smartpowerprovider/rogueapps");
    public static final Uri SCREENSTATUS_TABLE_URI = Uri.parse("content://com.huawei.android.smartpowerprovider/usagestatus");
    private static final int SQLITE_SEQUENCE_APPS_TABLE_ITEM_CODE = 113;
    private static final int SQLITE_SEQUENCE_APP_TABLE_ITEM_CODE = 112;
    public static final String SQLITE_SEQUENCE_TABLE = "sqlite_sequence";
    public static final Uri SQLITE_SEQUENCE_TABLE_URI = Uri.parse("content://com.huawei.android.smartpowerprovider/sqlite_sequence");
    public static final String SUPERPOWER_APPS = "superpowerapps";
    private static final String SUPERPOWER_APPS_BACKUP = "superpowerapps_backup";
    private static final int SUPERPOWER_APPS_ITEMS_CODE_BAK = 14;
    private static final int SUPERPOWER_APPS_ITEM_CODE = 11;
    public static final Uri SUPERPOWER_APP_URI = Uri.parse("content://com.huawei.android.smartpowerprovider/superpowerapps");
    public static final String TAG = "SmartProvider";
    private static final int UNIFIED_POWER_APPS_DEFAULT_VALUE_ITEM_CODE = 102;
    private static final int UNIFIED_POWER_APPS_DEFAULT_VALUE_TABLE_BACKUP_ITEM_CODE = 106;
    private static final int UNIFIED_POWER_APPS_ITEM_CODE = 100;
    private static final int UNIFIED_POWER_APPS_TABLE_BACKUP_ITEM_CODE = 104;
    public static final String UNIFIED_POWER_APP_COLUMNS = "is_changed";
    private static final int UNIFIED_POWER_APP_DEFAULT_VALUE_ITEM_CODE = 103;
    public static final String UNIFIED_POWER_APP_DEFAULT_VALUE_TABLE = "unifiedpowerappsdefaultvalue";
    public static final String UNIFIED_POWER_APP_DEFAULT_VALUE_TABLE_BACKUP = "unifiedpowerappsdefaultvalue_backup";
    private static final int UNIFIED_POWER_APP_DEFAULT_VALUE_TABLE_BACKUP_ITEM_CODE = 107;
    private static final int UNIFIED_POWER_APP_ITEM_CODE = 101;
    public static final Uri UNIFIED_POWER_APP_RUI = Uri.parse("content://com.huawei.android.smartpowerprovider/unifiedpowerapps");
    public static final String UNIFIED_POWER_APP_TABLE = "unifiedpowerapps";
    public static final String UNIFIED_POWER_APP_TABLE_BACKUP = "unifiedpowerapps_backup";
    private static final int UNIFIED_POWER_APP_TABLE_BACKUP_ITEM_CODE = 105;
    public static final String USAGESTATUS_TABLE = "usagestatus";
    private static final int USAGE_STATUS_APPS_TABLE_ITEM_CODE = 109;
    private static final int USAGE_STATUS_APP_TABLE_ITEM_CODE = 108;
    public static final String WAKEUP_APPS = "wakeupapps";
    private static final int WAKEUP_APPS_ITEM_CODE = 7;
    public static final Uri WAKEUP_RUI = Uri.parse("content://com.huawei.android.smartpowerprovider/wakeupapps");
    private static UriMatcher uriMatcher = new UriMatcher(-1);
    private MySQLiteOpenHelper mDatabaseHelper = null;

    public static class Columns {
        public static final String FORBIDDEN_APP_TYPE = "app_type";
        public static final String PACKAGE_NAME = "package_name";
        public static final String PROTECT_LIST_TYPE = "list_type";
    }

    class MySQLiteOpenHelper extends HsmSQLiteOpenHelper {

        class MyThread extends Thread {
            private Context context;
            private SQLiteDatabase db;

            public MyThread(Context context, SQLiteDatabase db) {
                this.context = context;
                this.db = db;
            }

            public void run() {
                UnifiedPowerAppControl.getInstance(this.context).initUnifiedpowerappsTable(this.db);
            }
        }

        class UpdateThread extends Thread {
            private Context context;
            private SQLiteDatabase db;

            public UpdateThread(Context context, SQLiteDatabase db) {
                this.context = context;
                this.db = db;
            }

            public void run() {
                UnifiedPowerAppControl.getInstance(this.context).updateLocalUnifiedPowerAppTableByHOTA(this.db);
            }
        }

        public MySQLiteOpenHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        public void onCreate(SQLiteDatabase db) {
            GlobalContext.setContext(SmartProvider.this.getContext());
            createTables(db);
            new MyThread(SmartProvider.this.getContext(), db).start();
            RemainingTimeSceneHelper.initTimeSceneTable(db);
            HwLog.i(SmartProvider.TAG, "SmartProvider onCreate ...");
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            HwLog.v(SmartProvider.TAG, "SmartProvider onUpgrade oldVersion = " + oldVersion + " newVersion = " + newVersion + "  " + (newVersion > oldVersion));
            upgradeFromLE5To6(db, oldVersion, newVersion);
            upgradeFromLE6To7(db, oldVersion, newVersion);
            upgradeFromLE7To8(db, oldVersion, newVersion);
            upgradeFromLE9To10(db, oldVersion, newVersion);
            upgradeFromLE10To11(db, oldVersion, newVersion);
            upgradeFromLE11To12(db, oldVersion, newVersion);
            upgradeFromLE12To13(db, oldVersion, newVersion);
            upgradeFromLE13To14(db, oldVersion, newVersion);
            upgradeFromLE14To15(db, oldVersion, newVersion);
            upgradeFromLE15To16(db, oldVersion, newVersion);
            upgradeFromLE16To17(db, oldVersion, newVersion);
            upgradeFromLE17To18(db, oldVersion, newVersion);
            upgradeFromLE18To19(db, oldVersion, newVersion);
            upgradeFromLE19To20(db, oldVersion, newVersion);
            SmartProvider.this.upgradeFromLE20To21(db, oldVersion, newVersion);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            HwLog.i(SmartProvider.TAG, "SmartProvider onDowngrade: oldVersion = " + oldVersion + ", newVersion =" + newVersion);
            dropTables(db);
            createTables(db);
        }

        protected boolean onRecoverStart(SQLiteDatabase db, int oldVersion) {
            HwLog.i(SmartProvider.TAG, "onRecoverStart: oldVersion = " + oldVersion);
            return true;
        }

        protected boolean onRecoverComplete(SQLiteDatabase db, int oldVersion) {
            HwLog.i(SmartProvider.TAG, "onRecoverComplete: Start, oldVersion = " + oldVersion);
            return true;
        }

        private void createTables(SQLiteDatabase db) {
            db.execSQL(SmartProvider.CREATE_FORBIDDENAPPS_TABLE);
            db.execSQL(SmartProvider.CREATE_PROTECTEDAPPS_TABLE);
            db.execSQL(SmartProvider.CREATE_ROGUE_APPS_TABLE);
            db.execSQL(SmartProvider.CREATE_WAKEUP_APPS_TABLE);
            db.execSQL(SmartProvider.CREATE_SUPERPOWER_APPS_TABLE);
            SmartProvider.this.createUnifiedPowerAppTable(db);
            SmartProvider.this.createUnifiedPowerAppDefaultValueTable(db);
            SmartProvider.this.createUsageStatusTable(db);
            SmartProvider.this.createBatteryStatisticsTable(db);
            SmartProvider.this.createTimeSceneTable(db);
            SmartProvider.this.createDateMapToTimeScene(db);
        }

        private void dropTables(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS forbiddenapps");
            db.execSQL("DROP TABLE IF EXISTS protectedapps");
            db.execSQL("DROP TABLE IF EXISTS rogueapps");
            db.execSQL("DROP TABLE IF EXISTS wakeupapps");
            db.execSQL("DROP TABLE IF EXISTS superpowerapps");
            SmartProvider.this.dropUnifiedPowerAppTable(db);
            SmartProvider.this.dropUnifiedPowerAppDefaultValueTable(db);
            SmartProvider.this.dropUsageStatusTable(db);
            SmartProvider.this.dropBatteryStatisticsTable(db);
            SmartProvider.this.dropTimeSceneTable(db);
            SmartProvider.this.dropDateMapToTimeScene(db);
        }

        private void upgradeFromLE5To6(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion <= 5 && newVersion > 5) {
                db.execSQL("DROP TABLE forbiddenapps");
                db.execSQL("DROP TABLE protectedapps");
                db.execSQL(SmartProvider.CREATE_FORBIDDENAPPS_TABLE);
                db.execSQL(SmartProvider.CREATE_PROTECTEDAPPS_TABLE);
                db.execSQL(SmartProvider.CREATE_WAKEUP_APPS_TABLE);
            }
        }

        private void upgradeFromLE6To7(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion <= 6 && newVersion > 6) {
                db.execSQL(SmartProvider.CREATE_ROGUE_APPS_TABLE);
                db.execSQL(SmartProvider.CREATE_WAKEUP_APPS_TABLE);
            }
        }

        private void upgradeFromLE7To8(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion <= 7 && newVersion > 7) {
                db.execSQL("ALTER TABLE rogueapps ADD highwakefreq INTEGER default 0;");
                db.execSQL("ALTER TABLE rogueapps ADD ignorewakeup INTEGER default 0;");
                db.execSQL(SmartProvider.CREATE_WAKEUP_APPS_TABLE);
            }
        }

        private void upgradeFromLE9To10(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion <= 9 && newVersion > 9) {
                try {
                    db.execSQL("ALTER TABLE rogueapps ADD time INTEGER default 0;");
                } catch (SQLException ex) {
                    HwLog.w(SmartProvider.TAG, "upgradeFrom8or9To10 may catch SQLException!", ex);
                }
            }
        }

        private void upgradeFromLE10To11(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion <= 10 && newVersion > 10) {
                db.execSQL("ALTER TABLE rogueapps ADD roguetype INTEGER default 0;");
                db.execSQL("UPDATE rogueapps SET roguetype=1 where roguetype=0 and highwakefreq= 1");
            }
        }

        private void upgradeFromLE11To12(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion <= 11 && newVersion > 11) {
                HwLog.d("TAG", "upgradeFromLE11To12");
                db.execSQL(SmartProvider.CREATE_SUPERPOWER_APPS_TABLE);
            }
        }

        private void upgradeFromLE12To13(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion <= 12 && newVersion > 12) {
                GlobalContext.setContext(SmartProvider.this.getContext());
                SmartProvider.this.createUnifiedPowerAppTable(db);
                SmartProvider.this.createUnifiedPowerAppDefaultValueTable(db);
                new MyThread(SmartProvider.this.getContext(), db).start();
                HwLog.i(SmartProvider.TAG, "upgradeFromLE12To13 ...");
            }
        }

        private void upgradeFromLE13To14(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion <= 13 && newVersion > 13) {
                GlobalContext.setContext(SmartProvider.this.getContext());
                SmartProvider.this.createUsageStatusTable(db);
                HwLog.i(SmartProvider.TAG, "upgradeFromLE13To114 ...");
            }
        }

        private void upgradeFromLE14To15(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion <= 14 && newVersion > 14) {
                GlobalContext.setContext(SmartProvider.this.getContext());
                SmartProvider.this.createBatteryStatisticsTable(db);
                HwLog.i(SmartProvider.TAG, "upgradeFromLE14To15 ...");
            }
        }

        private void upgradeFromLE15To16(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion <= 15 && newVersion > 15) {
                if (oldVersion <= 12) {
                    HwLog.i(SmartProvider.TAG, "is_changed column has added, upgradeFromLE15To16 ...");
                    return;
                }
                if (SmartProvider.this.isExistField(db, SmartProvider.UNIFIED_POWER_APP_TABLE, "is_changed")) {
                    HwLog.i(SmartProvider.TAG, "is_changed column has existed, do not alter table column");
                } else {
                    HwLog.i(SmartProvider.TAG, "is_changed column has not existed, alter table column");
                    GlobalContext.setContext(SmartProvider.this.getContext());
                    db.execSQL("ALTER TABLE unifiedpowerapps ADD is_changed INTEGER default 1;");
                }
                HwLog.i(SmartProvider.TAG, "upgradeFromLE15To16 ...");
            }
        }

        private void upgradeFromLE16To17(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion <= 16 && newVersion > 16) {
                GlobalContext.setContext(SmartProvider.this.getContext());
                SmartProvider.this.createTimeSceneTable(db);
                RemainingTimeSceneHelper.initTimeSceneTable(db);
                HwLog.i(SmartProvider.TAG, "upgradeFromLE16To17 ...");
            }
        }

        private void upgradeFromLE17To18(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion <= 17 && newVersion > 17) {
                GlobalContext.setContext(SmartProvider.this.getContext());
                SmartProvider.this.createDateMapToTimeScene(db);
                HwLog.i(SmartProvider.TAG, "upgradeFromLE17To18 ...");
            }
        }

        private void upgradeFromLE18To19(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion <= 18 && newVersion > 18) {
                if (PowerModeControl.getInstance(SmartProvider.this.getContext()).readSaveMode() != 1) {
                    PowerModeControl.getInstance(SmartProvider.this.getContext()).changePowerMode(1);
                    HwLog.i(SmartProvider.TAG, " the curr power Mode is not smartMode OTA ago, changePowerMode to SMART after OTA");
                }
                HwLog.i(SmartProvider.TAG, "upgradeFromLE18To19 ...");
            }
        }

        private void upgradeFromLE19To20(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion <= 19 && newVersion > 19) {
                if (oldVersion < 13) {
                    HwLog.i(SmartProvider.TAG, "upgradeFromLE19To20,unifiedPowerApp table is not existed.");
                    return;
                }
                GlobalContext.setContext(SmartProvider.this.getContext());
                if (AbroadUtils.isAbroad()) {
                    new UpdateThread(SmartProvider.this.getContext(), db).start();
                    HwLog.i(SmartProvider.TAG, "upgradeFromLE19To20,this is abroad version");
                } else {
                    HwLog.i(SmartProvider.TAG, "upgradeFromLE19To20, this is chinese version and do nothing.");
                }
            }
        }
    }

    public static class ROGUETYPE {
        public static final int GPSCONSUME = 3;
        public static final int HIGHWAKEUP = 1;
        public static final int NOROGUE = 0;
        public static final int PREVENTSLEEP = 2;
        public static final int SUPERHIGHPOWER = 4;
    }

    public static class ROGUE_Columns {
        public static final String CLEAR = "clear";
        public static final String HIGHWAKEUPFREQ = "highwakefreq";
        public static final String IGNORE = "ignore";
        public static final String IGNOREWAKEUPAPP = "ignorewakeup";
        public static final String ISROGUE = "isrogue";
        public static final String PKGNAME = "pkgname";
        public static final String PRESETBLACKAPP = "presetblackapp";
        public static final String ROGUETYPE = "roguetype";
        public static final String TIME = "time";
    }

    public static class WakeUp_Columns {
        public static final String PKGNAME = "pkgname";
        public static final String WAKEUPNUM_ALL = "wakeupnum_all";
        public static final String WAKEUPNUM_H = "wakeupnum_h";
    }

    static {
        uriMatcher.addURI(AUTH, "forbiddenapps", 1);
        uriMatcher.addURI(AUTH, "forbiddenapps/#", 2);
        uriMatcher.addURI(AUTH, "protectedapps", 3);
        uriMatcher.addURI(AUTH, "protectedapps/#", 4);
        uriMatcher.addURI(AUTH, SUPERPOWER_APPS, 11);
        uriMatcher.addURI(AUTH, ROGUE_APPS, 5);
        uriMatcher.addURI(AUTH, "rogueapps/#", 6);
        uriMatcher.addURI(AUTH, WAKEUP_APPS, 7);
        uriMatcher.addURI(AUTH, ROGUE_APPS_BACKUP, 15);
        uriMatcher.addURI(AUTH, PREFERENCE_BACKUP, 16);
        uriMatcher.addURI(AUTH, SUPERPOWER_APPS_BACKUP, 14);
        uriMatcher.addURI(AUTH, UNIFIED_POWER_APP_TABLE, 100);
        uriMatcher.addURI(AUTH, "unifiedpowerapps/#", 101);
        uriMatcher.addURI(AUTH, UNIFIED_POWER_APP_DEFAULT_VALUE_TABLE, 102);
        uriMatcher.addURI(AUTH, "unifiedpowerappsdefaultvalue/#", 103);
        uriMatcher.addURI(AUTH, UNIFIED_POWER_APP_TABLE_BACKUP, 104);
        uriMatcher.addURI(AUTH, "unifiedpowerapps_backup/#", 105);
        uriMatcher.addURI(AUTH, UNIFIED_POWER_APP_DEFAULT_VALUE_TABLE_BACKUP, 106);
        uriMatcher.addURI(AUTH, "unifiedpowerappsdefaultvalue_backup/#", UNIFIED_POWER_APP_DEFAULT_VALUE_TABLE_BACKUP_ITEM_CODE);
        uriMatcher.addURI(AUTH, USAGESTATUS_TABLE, 108);
        uriMatcher.addURI(AUTH, "usagestatus/#", 109);
        uriMatcher.addURI(AUTH, BATTERY_STATISTICS_TABLE, BATTERY_STATISTICS_APP_TABLE_ITEM_CODE);
        uriMatcher.addURI(AUTH, "batterystatistics/#", 111);
        uriMatcher.addURI(AUTH, SQLITE_SEQUENCE_TABLE, 112);
        uriMatcher.addURI(AUTH, "sqlite_sequence/#", SQLITE_SEQUENCE_APPS_TABLE_ITEM_CODE);
        uriMatcher.addURI(AUTH, REMAINING_TIME_SCENE_TABLE, REMAINING_TIME_SCENE_APP_TABLE_ITEM_CODE);
        uriMatcher.addURI(AUTH, "timescene/#", 115);
        uriMatcher.addURI(AUTH, DATE_MAPTO_TIME_SCENE_TABLE, 116);
        uriMatcher.addURI(AUTH, "datemaptotimescene/#", DATE_MAPTO_TIME_SCENE_APPS_TABLE_ITEM_CODE);
    }

    private void upgradeFromLE20To21(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 20 && newVersion > 20) {
            GlobalContext.setContext(getContext());
            SysCoreUtils.setSuperHighPowerSwitchState(getContext(), true);
            HwLog.i(TAG, "super power clean switch set to " + true + " after HOTA");
            HwLog.i(TAG, "upgradeFromLE20To21 ...");
        }
    }

    private boolean isExistField(SQLiteDatabase db, String tableName, String fieldName) {
        StringBuilder builder = new StringBuilder();
        builder.append("name = '").append(tableName).append("' AND sql LIKE '%").append(fieldName).append("%'");
        Cursor cursor = null;
        try {
            cursor = db.query("sqlite_master", null, builder.toString(), null, null, null, null);
            if (cursor != null) {
                boolean z = cursor.getCount() > 0;
                if (cursor != null) {
                    cursor.close();
                }
                return z;
            }
            if (cursor != null) {
                cursor.close();
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        int ret = 0;
        if (db != null) {
            String delId;
            switch (uriMatcher.match(uri)) {
                case 1:
                    ret = db.delete("forbiddenapps", selection, selectionArgs);
                    break;
                case 2:
                    delId = uri.getLastPathSegment();
                    try {
                        HwLog.v(TAG, "use the id replace the selection!");
                        ret = db.delete("forbiddenapps", "_id=" + delId, null);
                        break;
                    } catch (NumberFormatException e) {
                        ret = db.delete("forbiddenapps", selection, selectionArgs);
                        break;
                    }
                case 3:
                    ret = db.delete("protectedapps", selection, selectionArgs);
                    break;
                case 4:
                    delId = uri.getLastPathSegment();
                    try {
                        HwLog.v(TAG, "use the id replace the selection!");
                        ret = db.delete("protectedapps", "_id=" + delId, null);
                        break;
                    } catch (NumberFormatException e2) {
                        ret = db.delete("protectedapps", selection, selectionArgs);
                        break;
                    }
                case 5:
                    ret = db.delete(ROGUE_APPS, selection, selectionArgs);
                    break;
                case 7:
                    ret = db.delete(WAKEUP_APPS, selection, selectionArgs);
                    break;
                case 11:
                    ret = db.delete(SUPERPOWER_APPS, selection, selectionArgs);
                    break;
                case 100:
                case 101:
                    ret = db.delete(UNIFIED_POWER_APP_TABLE, selection, selectionArgs);
                    break;
                case 108:
                case 109:
                    ret = db.delete(USAGESTATUS_TABLE, selection, selectionArgs);
                    break;
                case BATTERY_STATISTICS_APP_TABLE_ITEM_CODE /*110*/:
                case 111:
                    ret = db.delete(BATTERY_STATISTICS_TABLE, selection, selectionArgs);
                    break;
                case 112:
                case SQLITE_SEQUENCE_APPS_TABLE_ITEM_CODE /*113*/:
                    ret = db.delete(SQLITE_SEQUENCE_TABLE, selection, selectionArgs);
                    break;
                case REMAINING_TIME_SCENE_APP_TABLE_ITEM_CODE /*114*/:
                case 115:
                    ret = db.delete(REMAINING_TIME_SCENE_TABLE, selection, selectionArgs);
                    break;
                case 116:
                case DATE_MAPTO_TIME_SCENE_APPS_TABLE_ITEM_CODE /*117*/:
                    ret = db.delete(DATE_MAPTO_TIME_SCENE_TABLE, selection, selectionArgs);
                    break;
                default:
                    throw new UnsupportedOperationException("Invalid URI " + uri);
            }
            if (ret > 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        } else {
            HwLog.d(TAG, "can not get the database!");
        }
        return ret;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        Uri retUri = uri;
        long newRecordId = 0;
        if (db != null) {
            switch (uriMatcher.match(uri)) {
                case 1:
                case 2:
                    newRecordId = db.insert("forbiddenapps", null, values);
                    break;
                case 3:
                case 4:
                    newRecordId = db.insert("protectedapps", null, values);
                    break;
                case 5:
                    newRecordId = db.insert(ROGUE_APPS, null, values);
                    break;
                case 7:
                    newRecordId = db.insert(WAKEUP_APPS, null, values);
                    break;
                case 11:
                    newRecordId = db.insert(SUPERPOWER_APPS, null, values);
                    break;
                case 14:
                case 15:
                    increaseRecoverSucceedCount();
                    break;
                case 16:
                    HwLog.v(TAG, "insert PREFERENCE_BAK");
                    newRecordId = (long) setPowerPreference(values);
                    if (newRecordId <= 0) {
                        increaseRecoverFailedCount();
                        break;
                    }
                    increaseRecoverSucceedCount();
                    break;
                case 100:
                case 101:
                    newRecordId = db.insert(UNIFIED_POWER_APP_TABLE, null, values);
                    break;
                case 102:
                case 103:
                    newRecordId = db.insert(UNIFIED_POWER_APP_DEFAULT_VALUE_TABLE, null, values);
                    break;
                case 104:
                case 105:
                    newRecordId = (long) db.update(UNIFIED_POWER_APP_TABLE, values, "pkg_name = ?", new String[]{values.getAsString("pkg_name")});
                    break;
                case 106:
                case UNIFIED_POWER_APP_DEFAULT_VALUE_TABLE_BACKUP_ITEM_CODE /*107*/:
                    newRecordId = db.replace(UNIFIED_POWER_APP_DEFAULT_VALUE_TABLE, null, values);
                    break;
                case 108:
                case 109:
                    newRecordId = db.replace(USAGESTATUS_TABLE, null, values);
                    break;
                case BATTERY_STATISTICS_APP_TABLE_ITEM_CODE /*110*/:
                case 111:
                    newRecordId = db.insert(BATTERY_STATISTICS_TABLE, null, values);
                    break;
                case REMAINING_TIME_SCENE_APP_TABLE_ITEM_CODE /*114*/:
                case 115:
                    newRecordId = db.insert(REMAINING_TIME_SCENE_TABLE, null, values);
                    break;
                case 116:
                case DATE_MAPTO_TIME_SCENE_APPS_TABLE_ITEM_CODE /*117*/:
                    newRecordId = db.insert(DATE_MAPTO_TIME_SCENE_TABLE, null, values);
                    break;
                default:
                    newRecordId = -1;
                    HwLog.i(TAG, "newRecordId =" + -1);
                    break;
            }
            if (newRecordId != -1) {
                retUri = Uri.withAppendedPath(uri, String.valueOf(newRecordId));
                getContext().getContentResolver().notifyChange(uri, null);
                return retUri;
            }
            HwLog.d(TAG, "insert failed! uri=" + uri);
            return retUri;
        }
        HwLog.d(TAG, "can not get the database!");
        return retUri;
    }

    public boolean onCreate() {
        this.mDatabaseHelper = new MySQLiteOpenHelper(getContext(), DATABASE_NAME, null, 21);
        try {
            return this.mDatabaseHelper.getWritableDatabase() != null;
        } catch (SQLiteException e) {
            return false;
        }
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = this.mDatabaseHelper.getReadableDatabase();
        if (db != null) {
            switch (uriMatcher.match(uri)) {
                case 1:
                case 2:
                    return db.query("forbiddenapps", projection, selection, selectionArgs, null, null, sortOrder);
                case 3:
                case 4:
                    return db.query("protectedapps", projection, selection, selectionArgs, null, null, sortOrder);
                case 5:
                case 6:
                    return db.query(ROGUE_APPS, projection, selection, selectionArgs, null, null, sortOrder);
                case 7:
                    return db.query(WAKEUP_APPS, projection, selection, selectionArgs, null, null, sortOrder);
                case 11:
                    return db.query(SUPERPOWER_APPS, projection, selection, selectionArgs, null, null, sortOrder);
                case 16:
                    HwLog.v(TAG, "query  PREFERENCE_BAK");
                    return BackupUtil.getPreferenceCursor(getPowerPreference());
                case 100:
                case 101:
                case 104:
                case 105:
                    HwLog.v(TAG, "query  UNIFIED_POWER_APP_TABLE");
                    return db.query(UNIFIED_POWER_APP_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                case 102:
                case 103:
                case 106:
                case UNIFIED_POWER_APP_DEFAULT_VALUE_TABLE_BACKUP_ITEM_CODE /*107*/:
                    HwLog.v(TAG, "query  UNIFIED_POWER_APP_DEFAULT_VALUE_TABLE");
                    return db.query(UNIFIED_POWER_APP_DEFAULT_VALUE_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                case 108:
                case 109:
                    return db.query(USAGESTATUS_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                case BATTERY_STATISTICS_APP_TABLE_ITEM_CODE /*110*/:
                case 111:
                    return db.query(BATTERY_STATISTICS_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                case REMAINING_TIME_SCENE_APP_TABLE_ITEM_CODE /*114*/:
                case 115:
                    return db.query(REMAINING_TIME_SCENE_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                case 116:
                case DATE_MAPTO_TIME_SCENE_APPS_TABLE_ITEM_CODE /*117*/:
                    return db.query(DATE_MAPTO_TIME_SCENE_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
            }
        }
        return null;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int ret = 0;
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        if (db != null) {
            switch (uriMatcher.match(uri)) {
                case 1:
                case 2:
                    ret = db.update("forbiddenapps", values, selection, selectionArgs);
                    break;
                case 3:
                case 4:
                    ret = db.update("protectedapps", values, selection, selectionArgs);
                    break;
                case 5:
                case 6:
                    ret = db.update(ROGUE_APPS, values, selection, selectionArgs);
                    break;
                case 7:
                    ret = db.update(WAKEUP_APPS, values, selection, selectionArgs);
                    break;
                case 11:
                    ret = db.update(SUPERPOWER_APPS, values, selection, selectionArgs);
                    break;
                case 100:
                case 101:
                    ret = db.update(UNIFIED_POWER_APP_TABLE, values, selection, selectionArgs);
                    break;
                case REMAINING_TIME_SCENE_APP_TABLE_ITEM_CODE /*114*/:
                case 115:
                    ret = db.update(REMAINING_TIME_SCENE_TABLE, values, selection, selectionArgs);
                    break;
                case 116:
                case DATE_MAPTO_TIME_SCENE_APPS_TABLE_ITEM_CODE /*117*/:
                    try {
                        ret = db.update(DATE_MAPTO_TIME_SCENE_TABLE, values, selection, selectionArgs);
                        break;
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                default:
                    throw new UnsupportedOperationException("Invalid URI " + uri);
            }
            if (ret > 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }
        return ret;
    }

    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        HwLog.i(TAG, "SmartProvider applyBatch ....");
        int opCount = 0;
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            int numOperations = operations.size();
            ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                opCount++;
                if (opCount > 500) {
                    throw new OperationApplicationException("Too many content provider operations between yield points. The maximum number of operations per yield point is 500", 0);
                }
                results[i] = ((ContentProviderOperation) operations.get(i)).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        int matchCode = uriMatcher.match(uri);
        HwLog.i(TAG, "bulkInsert matchCode = " + matchCode);
        String tableName = null;
        switch (matchCode) {
            case BATTERY_STATISTICS_APP_TABLE_ITEM_CODE /*110*/:
            case 111:
                tableName = BATTERY_STATISTICS_TABLE;
                break;
            case REMAINING_TIME_SCENE_APP_TABLE_ITEM_CODE /*114*/:
            case 115:
                tableName = REMAINING_TIME_SCENE_TABLE;
                break;
            default:
                HwLog.i(TAG, "Unknown bulkInsert uri = " + uri);
                break;
        }
        db.beginTransaction();
        try {
            for (ContentValues insert : values) {
                if (db.insert(tableName, null, insert) < 0) {
                    return 0;
                }
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            return values.length;
        } finally {
            db.endTransaction();
        }
    }

    protected int getDBVersion() {
        return 21;
    }

    protected ArrayList<String> getBackupSupportedUriList() {
        HwLog.v(TAG, "getRecoverUris");
        ArrayList<String> uris = new ArrayList();
        uris.add("content://com.huawei.android.smartpowerprovider/rogueapps_backup");
        uris.add("content://com.huawei.android.smartpowerprovider/power_preference_backup");
        uris.add("content://com.huawei.android.smartpowerprovider/superpowerapps_backup");
        uris.add("content://com.huawei.android.smartpowerprovider/unifiedpowerapps_backup");
        uris.add("content://com.huawei.android.smartpowerprovider/unifiedpowerappsdefaultvalue_backup");
        return uris;
    }

    protected boolean canRecoverDB(int nRecoverVersion) {
        HwLog.i(TAG, "canRecoverDB: Try to recover from version : " + nRecoverVersion + ", Current version : " + getDBVersion());
        if (getDBVersion() < 13) {
            return false;
        }
        return true;
    }

    protected boolean onRecoverStart(int nRecoverVersion) {
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        if (db != null) {
            return this.mDatabaseHelper.onRecoverStart(db, nRecoverVersion);
        }
        HwLog.w(TAG, "onRecoverStart: Fail to get getWritableDatabase");
        return false;
    }

    protected boolean onRecoverComplete(int nRecoverVersion) {
        if (this.mDatabaseHelper.getWritableDatabase() == null) {
            HwLog.w(TAG, "onRecoverComplete: Fail to get getWritableDatabase");
            return false;
        }
        notifiChanged(UNIFIED_POWER_APP_RUI);
        notifiChanged(NIFIED_POWER_APP_DEFAULT_VALUE_RUI);
        HwLog.i(TAG, "onRecoverComplete: Success = " + getRecoverSucceedCount() + ", Failure = " + getRecoverFailedCount());
        return true;
    }

    private ContentValues getPowerPreference() {
        Context context = getContext();
        ContentValues values = new ContentValues();
        values.put(SharedPrefKeyConst.CURRENT_POWER_MODE_KEY, Integer.valueOf(PowerModeControl.getInstance(context).readSaveMode()));
        boolean bSwitchOn = SharePrefWrapper.getPrefValue(context, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.POWER_INTENSIVE_PRMOPT_SWITCH_KEY, true);
        if (SysCoreUtils.IS_ATT) {
            bSwitchOn = SharePrefWrapper.getPrefValue(context, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.POWER_INTENSIVE_PRMOPT_SWITCH_KEY, false);
        }
        values.put(SharedPrefKeyConst.POWER_INTENSIVE_PRMOPT_SWITCH_KEY, Boolean.valueOf(bSwitchOn));
        String remindValue = SharePrefWrapper.getPrefValue(context, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.SUPER_POWER_SAVING_THRESHOLD_KEY, String.valueOf(8));
        if (SysCoreUtils.IS_ATT) {
            remindValue = SharePrefWrapper.getPrefValue(context, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.SUPER_POWER_SAVING_THRESHOLD_KEY, String.valueOf(15));
        }
        values.put(SharedPrefKeyConst.SUPER_POWER_SAVING_THRESHOLD_KEY, remindValue);
        values.put(SharedPrefKeyConst.SLEEP_WLAN_KEY, Integer.valueOf(Global.getInt(context.getContentResolver(), "wifi_sleep_policy", 2)));
        values.put(SharedPrefKeyConst.SLEEP_DATA_KEY, Integer.valueOf(Systemex.getInt(context.getContentResolver(), SharedPrefKeyConst.POWER_SAVING_ON, 0)));
        getSuperHighPowerSwitchPreference(values, context);
        return values;
    }

    private void getSuperHighPowerSwitchPreference(ContentValues values, Context context) {
        values.put(SharedPrefKeyConst.SUPER_HIGH_POWER_SWITCH_KEY, Boolean.valueOf(SysCoreUtils.getSuperHighPowerSwitchState(context)));
    }

    private int setPowerPreference(ContentValues values) {
        int nUpdateCount = -1;
        if (values == null) {
            HwLog.w(TAG, "setPowerPreference : Invalid content values");
            return -1;
        }
        Context context = getContext();
        String keyString = values.getAsString(BackupConst.PREFERENCE_KEY);
        String valueString = values.getAsString(BackupConst.PREFERENCE_VALUE);
        HwLog.v(TAG, String.format("setPowerPreference : %1$s = %2$s", new Object[]{keyString, valueString}));
        if (SharedPrefKeyConst.CURRENT_POWER_MODE_KEY.equalsIgnoreCase(keyString)) {
            PowerModeControl.getInstance(context).changePowerMode(Integer.parseInt(valueString));
            nUpdateCount = 1;
        } else if (SharedPrefKeyConst.POWER_INTENSIVE_PRMOPT_SWITCH_KEY.equalsIgnoreCase(keyString)) {
            SharePrefWrapper.setPrefValue(context, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.POWER_INTENSIVE_PRMOPT_SWITCH_KEY, Boolean.valueOf(Boolean.parseBoolean(valueString)).booleanValue());
            nUpdateCount = 1;
        } else if (SharedPrefKeyConst.SUPER_POWER_SAVING_THRESHOLD_KEY.equalsIgnoreCase(keyString)) {
            SharePrefWrapper.setPrefValue(context, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.SUPER_POWER_SAVING_THRESHOLD_KEY, valueString);
            context.sendBroadcast(new Intent(ActionConst.INTENT_REMIND_CHECKBOX_CHANGE), "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
            nUpdateCount = 1;
        } else if (SharedPrefKeyConst.SLEEP_WLAN_KEY.equalsIgnoreCase(keyString)) {
            Global.putInt(context.getContentResolver(), "wifi_sleep_policy", Integer.parseInt(valueString));
            nUpdateCount = 1;
        } else if (SharedPrefKeyConst.SLEEP_DATA_KEY.equalsIgnoreCase(keyString)) {
            Systemex.putInt(context.getContentResolver(), SharedPrefKeyConst.POWER_SAVING_ON, Integer.parseInt(valueString));
            nUpdateCount = 1;
        } else if (SharedPrefKeyConst.SUPER_HIGH_POWER_SWITCH_KEY.equalsIgnoreCase(keyString)) {
            setSuperHighPowerPreference(valueString, context);
            nUpdateCount = 1;
        }
        return nUpdateCount;
    }

    private void setSuperHighPowerPreference(String valueString, Context context) {
        SysCoreUtils.setSuperHighPowerSwitchState(context, Boolean.parseBoolean(valueString));
    }

    private void createUnifiedPowerAppTable(SQLiteDatabase db) {
        db.execSQL(UnifiedPowerAppControl.createUnifiedPowerAppSQL());
    }

    private void createUnifiedPowerAppDefaultValueTable(SQLiteDatabase db) {
        db.execSQL(UnifiedPowerAppControl.createUnifiedPowerAppDefaulValuetSQL());
    }

    private void createUsageStatusTable(SQLiteDatabase db) {
        db.execSQL(UsageStatusHelper.createUsageStatusTalbeSQL());
    }

    private void createTimeSceneTable(SQLiteDatabase db) {
        db.execSQL(RemainingTimeSceneHelper.createTimeSceneTableSQL());
    }

    private void createDateMapToTimeScene(SQLiteDatabase db) {
        db.execSQL(DateMapToTimeSceneHelper.createDateMapToTimeSceneTableSQL());
    }

    private void dropTimeSceneTable(SQLiteDatabase db) {
        db.execSQL(RemainingTimeSceneHelper.dropTimeSceneTableSQL());
    }

    private void dropDateMapToTimeScene(SQLiteDatabase db) {
        db.execSQL(DateMapToTimeSceneHelper.dropDateMapToTimeSceneTableSQL());
    }

    private void dropUnifiedPowerAppTable(SQLiteDatabase db) {
        db.execSQL(UnifiedPowerAppControl.dropUnifiedPowerAppSQL());
    }

    private void dropUnifiedPowerAppDefaultValueTable(SQLiteDatabase db) {
        db.execSQL(UnifiedPowerAppControl.dropUnifiedPowerAppDefaultValueSQL());
    }

    private void dropUsageStatusTable(SQLiteDatabase db) {
        db.execSQL(UsageStatusHelper.dropUsageStatusSQL());
    }

    private void createBatteryStatisticsTable(SQLiteDatabase db) {
        db.execSQL(BatteryStatisticsHelper.createBatteryStatisticsTalbeSQL());
    }

    private void dropBatteryStatisticsTable(SQLiteDatabase db) {
        db.execSQL(BatteryStatisticsHelper.dropBatteryStatisticsSQL());
    }

    public Bundle call(String method, String arg, Bundle extras) {
        if (method == null) {
            HwLog.i(TAG, "Call method is null");
            return null;
        } else if (method.equals(CALL_METHOD_GET_FREE_LIST_POWERGENIE)) {
            Map<String, ArrayList<String>> res = SmartProviderHelper.getProtectAppFromDbForPowerGenie(getContext(), arg, extras);
            Bundle bundle = new Bundle();
            bundle.putStringArrayList(CALL_METHOD_PROTECT_KEY, (ArrayList) res.get(CALL_METHOD_PROTECT_KEY));
            bundle.putStringArrayList(CALL_METHOD_UNPROTECT_KEY, (ArrayList) res.get(CALL_METHOD_UNPROTECT_KEY));
            return bundle;
        } else if (method.equals(CALL_METHOD_MODIFY_UNIFIEDPOWERAPPS)) {
            HwLog.i(TAG, "method:hsm_modify_unifiedpowerapps is used");
            return null;
        } else {
            HwLog.w(TAG, "call method");
            return super.call(method, arg, extras);
        }
    }
}
