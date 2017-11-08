package com.huawei.systemmanager.optimize.smcs;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.database.IDatabaseConst.ColType;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.optimize.base.Const;
import com.huawei.systemmanager.optimize.smcs.SMCSDatabaseConstant.AdBlockColumns;
import com.huawei.systemmanager.optimize.smcs.SMCSDatabaseConstant.DlBlockColumns;
import com.huawei.systemmanager.optimize.smcs.SMCSDatabaseConstant.VirusTableConst;
import com.huawei.systemmanager.util.HwLog;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String CREATE_PREVENT_MODE_WHITELIST_TABLE = "create table if not exists whitelist(_id INTEGER PRIMARY KEY AUTOINCREMENT , Phone_number_id INTEGER,Phone_number TEXT, short_number TEXT, Phone_photo TEXT, location TEXT, mobile_operator TEXT, Phone_name TEXT);";
    private static final boolean mDebugEnabled = (SMCSPropConstant.localLOGV ? SMCSPropConstant.localDBLOGV : false);
    private static Object syncObj = new Object();
    private Context mContext;

    public DatabaseHelper(Context context) {
        super(context, "stusagestat.db", null, 17);
        this.mContext = context;
    }

    public void onCreate(SQLiteDatabase db) {
        if (mDebugEnabled) {
            HwLog.v("SmartMemoryCleanService", "DatabaseHelper.onCreate: ");
        }
        createAllTables(db);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
        if (mDebugEnabled) {
            HwLog.v("SmartMemoryCleanService", "DatabaseHelper.onUpgrade: oldVersion " + oldVersion + " currentVersion " + currentVersion);
        }
        if (oldVersion < 8) {
            removeAllTables(db);
            createAllTables(db);
            return;
        }
        upgrade8To9(db, oldVersion, currentVersion);
        upgrade9To10(db, oldVersion, currentVersion);
        upgrade10To11(db, oldVersion, currentVersion);
        upgrade11To12(db, oldVersion, currentVersion);
        upgrade12To13(db, oldVersion, currentVersion);
        upgrade13To14(db, oldVersion, currentVersion);
        upgrade14To15(db, oldVersion, currentVersion);
        upgrade15To16(db, oldVersion, currentVersion);
        upgrade16To17(db, oldVersion, currentVersion);
    }

    private void upgrade8To9(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 8 && newVersion > 8) {
            createDefaultValueTable(db);
            initialDefaultValueTable(db);
        }
    }

    private void upgrade9To10(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 9 && newVersion > 9) {
            updateFrom9to10(db);
        }
    }

    private void upgrade10To11(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 10 && newVersion > 10) {
            updateFrom10to11(db);
        }
    }

    private void upgrade11To12(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 11 && newVersion > 11) {
            updateFrom11to12(db);
        }
    }

    private void upgrade12To13(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 12 && newVersion > 12) {
            createAdBlockTable(db);
        }
    }

    private void upgrade13To14(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 13 && newVersion > 13) {
            updateFrom13to14(db);
        }
    }

    private void upgrade14To15(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 14 && newVersion > 14) {
            updateFrom14to15(db);
        }
    }

    private void upgrade15To16(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 15 && newVersion > 15) {
            createDlBlockTable(db);
            try {
                db.execSQL("ALTER TABLE ad_block_table ADD column use_tencent INTEGER DEFAULT (0);");
            } catch (Exception e) {
                HwLog.e("SmartMemoryCleanService", "COLUMN_USER_TENCENT has been in tables");
            }
        }
    }

    private void upgrade16To17(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 16 && newVersion > 16) {
            updateFrom16to17(db);
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (mDebugEnabled) {
            HwLog.v("SmartMemoryCleanService", "DatabaseHelper.onDowngrade: oldVersion " + oldVersion + " currentVersion " + newVersion);
        }
        removeAllTables(db);
        createAllTables(db);
    }

    public void execSQLs(String arg, String token) {
        Exception e;
        if (arg != null && arg.length() != 0) {
            if (token == null) {
                token = "";
            }
            if (mDebugEnabled) {
                HwLog.v("SmartMemoryCleanService", "DatabaseHelper.execSQLs: " + arg);
            }
            try {
                SQLiteDatabase db = getWritableDatabase();
                if (db != null) {
                    StringTokenizer stzer = new StringTokenizer(arg, token);
                    try {
                        if (stzer.countTokens() != 0) {
                            db.execSQL("BEGIN TRANSACTION;");
                            while (stzer.hasMoreTokens()) {
                                String sql = stzer.nextToken();
                                if (sql.length() > 0) {
                                    if (mDebugEnabled) {
                                        HwLog.v("SmartMemoryCleanService", "DatabaseHelper.execSQLs: sql " + sql);
                                    }
                                    db.execSQL(sql + SqlMarker.SQL_END);
                                }
                            }
                            db.execSQL("COMMIT TRANSACTION;");
                        }
                    } catch (Exception e2) {
                        e = e2;
                        StringTokenizer stringTokenizer = stzer;
                        HwLog.e("SmartMemoryCleanService", "DatabaseHelper.execSQLs: catch exception " + e.toString());
                    }
                }
            } catch (Exception e3) {
                e = e3;
                HwLog.e("SmartMemoryCleanService", "DatabaseHelper.execSQLs: catch exception " + e.toString());
            }
        }
    }

    public boolean createAllTables(SQLiteDatabase db) {
        if (db == null) {
            return false;
        }
        try {
            db.execSQL(CREATE_PREVENT_MODE_WHITELIST_TABLE);
            db.execSQL("BEGIN TRANSACTION;");
            db.execSQL(VirusTableConst.CREATE_VIRUS_APPS_TABLE);
            db.execSQL(VirusTableConst.CREATE_VIRUS_APPS_TABLE_INDEX_PACKAGENAME);
            createDefaultValueTable(db);
            createAdBlockTable(db);
            createDlBlockTable(db);
            String sql = createTimeTables();
            if (sql == null) {
                throw new Exception("create time table failed");
            }
            logv("DatabaseHelper.createAllTables: create time table " + sql);
            sql = createSTUsageTables();
            if (sql == null) {
                throw new Exception("create usage stat table failed");
            }
            logv("DatabaseHelper.createAllTables: create usage table " + sql);
            sql = createProcessRelationTable();
            if (sql == null) {
                throw new Exception("create process relation table failed");
            }
            logv("DatabaseHelper.createAllTables: create process relation table " + sql);
            sql = createBlacklistTable();
            if (sql == null) {
                throw new Exception("create blacklist table failed");
            }
            logv("DatabaseHelper.createAllTables: create blacklist table " + sql);
            sql = createGlobalEnableTable();
            if (sql == null) {
                throw new Exception("create global enable table failed");
            }
            logv("DatabaseHelper.createAllTables: create global enable table " + sql);
            sql = createOBUsedStatTable();
            if (sql == null) {
                throw new Exception("create ob period stat table failed");
            }
            logv("DatabaseHelper.createAllTables: create ob period stat table " + sql);
            sql = createStatUsedStatTable();
            if (sql == null) {
                throw new Exception("create stat period stat table failed");
            }
            logv("DatabaseHelper.createAllTables: create stat period stat table " + sql);
            sql = createProtectedPkgsTable();
            if (sql == null) {
                throw new Exception("create protected processes table failed");
            }
            logv("DatabaseHelper.createAllTables: create protected processes table " + sql);
            db.execSQL(sql + SqlMarker.SQL_END);
            sql = createMemoryThresholdTable();
            if (sql == null) {
                throw new Exception("create memory threshold table failed");
            }
            logv("DatabaseHelper.createAllTables: create memory threshold table " + sql);
            sql = createBasicParaTable();
            if (sql == null) {
                throw new Exception("create basic para table failed");
            }
            logv("DatabaseHelper.createAllTables: create basic para table " + sql);
            sql = createKeyProcessTable();
            if (sql == null) {
                throw new Exception("create key process table failed");
            }
            logv("DatabaseHelper.createAllTables: create key process table " + sql);
            db.execSQL("COMMIT TRANSACTION;");
            initialDefaultValueTable(db);
            return true;
        } catch (Exception e) {
            HwLog.e("SmartMemoryCleanService", "DatabaseHelper.createAllTables: catch exception " + e.toString());
            return false;
        }
    }

    private String createTimeTables() {
        try {
            return creatTable("st_time_table", new String[]{"time_name", "time_value"}, new String[]{"TEXT NOT NULL PRIMARY KEY", ColType.COL_TYPE_INT}, null);
        } catch (Exception e) {
            HwLog.v("SmartMemoryCleanService", "DatabaseHelper.readPowerOnTotalTime: catch exception " + e.toString());
            return null;
        }
    }

    private String createSTUsageTables() {
        try {
            return creatTable("st_usage_table", new String[]{"pkg_name", "used_latest_time", "used_ob_index"}, new String[]{"TEXT NOT NULL", ColType.COL_TYPE_INT, ColType.COL_TYPE_INT}, null);
        } catch (Exception e) {
            HwLog.e("SmartMemoryCleanService", "DatabaseHelper.createSTUsageTables: catch exception " + e.toString());
            return null;
        }
    }

    private final String createProcessRelationTable() {
        try {
            return creatTable("st_process_relation_table", new String[]{"st_process_relation_client", "st_process_relation_server"}, new String[]{"TEXT NOT NULL", "TEXT NOT NULL"}, null);
        } catch (Exception e) {
            HwLog.v("SmartMemoryCleanService", "DatabaseHelper.createProcessRelationTable: catch exception " + e.toString());
            return null;
        }
    }

    private final String createBlacklistTable() {
        try {
            return creatTable("st_process_blacklist_table", new String[]{"st_process_blacklist_processname"}, new String[]{"TEXT NOT NULL"}, null);
        } catch (Exception e) {
            HwLog.v("SmartMemoryCleanService", "DatabaseHelper.createBlacklistTable: catch exception " + e.toString());
            return null;
        }
    }

    private final String createProtectedPkgsTable() {
        try {
            return creatTable(SMCSDatabaseConstant.ST_PROTECTED_PKGS_TABLE, new String[]{"pkg_name", SMCSDatabaseConstant.ST_PROTECTED_PKG_CHECK, "userchanged"}, new String[]{"TEXT NOT NULL PRIMARY KEY", ColType.COL_TYPE_INT, "INTEGER DEFAULT 0"}, null);
        } catch (Exception e) {
            HwLog.v("SmartMemoryCleanService", "DatabaseHelper.createProtectedPkgsTable: catch exception " + e.toString());
            return null;
        }
    }

    private String createMemoryThresholdTable() {
        try {
            return creatTable("st_memory_threshold", new String[]{"protected_limit", "upper_limit"}, new String[]{"Long", "Long"}, null);
        } catch (Exception e) {
            HwLog.v("SmartMemoryCleanService", "DatabaseHelper.createMemoryThresholdTable: catch exception " + e.toString());
            return null;
        }
    }

    private String createBasicParaTable() {
        try {
            return creatTable("st_basic_para_table", new String[]{"st_basic_para_self_auto_trimer"}, new String[]{"TEXT NOT NULL"}, null);
        } catch (Exception e) {
            HwLog.v("SmartMemoryCleanService", "DatabaseHelper.createBasicParaTable: catch exception " + e.toString());
            return null;
        }
    }

    private String createKeyProcessTable() {
        try {
            return creatTable("st_key_procs_table", new String[]{"st_key_process"}, new String[]{"TEXT NOT NULL"}, null);
        } catch (Exception e) {
            HwLog.v("SmartMemoryCleanService", "DatabaseHelper.createKeyProcessTable: catch exception " + e.toString());
            return null;
        }
    }

    private final String createOBUsedStatTable() {
        try {
            return creatTable("st_pkg_used_ob_period", new String[]{"pkg_name", "ob_period_index", "used_times"}, new String[]{"TEXT NOT NULL", ColType.COL_TYPE_INT, ColType.COL_TYPE_INT}, null);
        } catch (Exception e) {
            HwLog.v("SmartMemoryCleanService", "DatabaseHelper.createOBUsedStatTable: catch exception " + e.toString());
            return null;
        }
    }

    private final String createStatUsedStatTable() {
        try {
            return creatTable("st_pkg_used_stat_period", new String[]{"pkg_name", "stat_period_index", "used_time"}, new String[]{"TEXT NOT NULL", ColType.COL_TYPE_INT, ColType.COL_TYPE_INT}, null);
        } catch (Exception e) {
            HwLog.v("SmartMemoryCleanService", "DatabaseHelper.createStatUsedStatTable: catch exception " + e.toString());
            return null;
        }
    }

    private final String createGlobalEnableTable() {
        try {
            return creatTable("st_global_enable_table", new String[]{"st_global_enable_value"}, new String[]{ColType.COL_TYPE_INT}, null);
        } catch (Exception e) {
            HwLog.v("SmartMemoryCleanService", "DatabaseHelper.createGlobalEnableTable: catch exception " + e.toString());
            return null;
        }
    }

    private String creatTable(String tableName, String[] cols, String[] colArgs, String constrain) {
        StringBuffer sql = new StringBuffer();
        if (tableName == null || tableName.length() == 0) {
            return null;
        }
        try {
            sql.append("CREATE TABLE IF NOT EXISTS " + tableName + " ( ");
            int len = cols.length;
            int i = 0;
            while (i < len - 1) {
                sql.append(cols[i] + " " + colArgs[i] + SqlMarker.COMMA_SEPARATE);
                i++;
            }
            sql.append(cols[i] + " " + colArgs[i]);
            if (constrain != null && constrain.length() > 0) {
                sql.append(SqlMarker.COMMA_SEPARATE + constrain);
            }
            sql.append(")");
            if (mDebugEnabled) {
                HwLog.v("SmartMemoryCleanService", "DatabaseHelper.creatTable: " + sql);
            }
            return sql.toString();
        } catch (Exception e) {
            HwLog.e("SmartMemoryCleanService", "DatabaseHelper.creatTable: catch exception " + e.toString());
            return null;
        }
    }

    private void updateFrom9to10(SQLiteDatabase db) {
        int i = 0;
        HwLog.i("SmartMemoryCleanService", "upgrade from 9 to 10");
        if (db != null) {
            String[] tables = new String[]{"st_usage_table", "st_process_relation_table", "st_process_relation_table", "st_process_blacklist_table", "st_global_enable_table", "st_pkg_used_ob_period", "st_pkg_used_stat_period", "st_time_table", "st_memory_threshold", "st_basic_para_table", "st_key_procs_table"};
            try {
                db.execSQL("BEGIN TRANSACTION;");
                int length = tables.length;
                while (i < length) {
                    String table = tables[i];
                    String sql = dropTable(table);
                    if (sql == null) {
                        throw new Exception("drop table failed, table:" + table);
                    }
                    db.execSQL(sql + SqlMarker.SQL_END);
                    i++;
                }
                db.execSQL("COMMIT TRANSACTION;");
            } catch (Exception e) {
                HwLog.e("SmartMemoryCleanService", "DatabaseHelper.createAllTables: catch exception " + e.toString());
            }
        }
    }

    private void updateFrom10to11(SQLiteDatabase db) {
        HwLog.i("SmartMemoryCleanService", "upgrade from 10 to 11");
        if (db != null) {
            try {
                db.beginTransaction();
                db.execSQL("ALTER TABLE st_protected_pkgs_table ADD userchanged INTEGER DEFAULT 1;");
                db.setTransactionSuccessful();
            } catch (Exception e) {
                HwLog.e("SmartMemoryCleanService", "updateFrom10to11: Exception", e);
            } finally {
                db.endTransaction();
            }
        }
    }

    private void updateFrom11to12(SQLiteDatabase db) {
        HwLog.i("SmartMemoryCleanService", " upgrade from 11 to 12, do nothing.");
    }

    private void updateFrom13to14(SQLiteDatabase db) {
        HwLog.i("SmartMemoryCleanService", " upgrade from 13 to 14");
        db.execSQL(CREATE_PREVENT_MODE_WHITELIST_TABLE);
        attachOldDB(db);
    }

    private void updateFrom14to15(SQLiteDatabase db) {
        HwLog.i("SmartMemoryCleanService", " upgrade from 14 to 15");
        db.execSQL(VirusTableConst.CREATE_VIRUS_APPS_TABLE);
        db.execSQL(VirusTableConst.CREATE_VIRUS_APPS_TABLE_INDEX_PACKAGENAME);
    }

    private void updateFrom16to17(SQLiteDatabase db) {
        HwLog.i("SmartMemoryCleanService", " upgrade from 16 to 17, do nothing");
    }

    private void attachOldDB(SQLiteDatabase db) {
        if (db != null) {
            String path = db.getPath().substring(0, db.getPath().lastIndexOf("/") + 1) + "Optimize.db";
            SQLiteDatabase temp = null;
            try {
                temp = SQLiteDatabase.openOrCreateDatabase(path, null);
            } catch (SQLiteCantOpenDatabaseException e) {
                HwLog.e("SmartMemoryCleanService", "the database is not exist ");
                e.printStackTrace();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            if (temp != null) {
                copyOldTable(temp, db);
                temp.close();
                this.mContext.deleteDatabase(path);
            }
        }
    }

    private void copyOldTable(SQLiteDatabase oldDB, SQLiteDatabase newDB) {
        copyTable(oldDB, newDB, "whitelist", new String[]{Const.PREVENT_WHITE_LIST_ID, Const.PREVENT_WHITE_LIST_NUMBER, Const.PREVENT_WHITE_LIST_SHORTNUMBER, Const.PREVENT_WHITE_LIST_PHONE_PHOTO, "location", Const.PREVENT_WHITE_LIST_MOBILE, Const.PREVENT_WHITE_LIST_NAME});
    }

    private void copyTable(SQLiteDatabase oldDB, SQLiteDatabase newDB, String tableName, String[] columns) {
        bulkInsertOldTable(newDB, tableName, getContentValues(oldDB, tableName, columns));
    }

    private ContentValues[] getContentValues(SQLiteDatabase db, String tableName, String[] columns) {
        Cursor cursor = null;
        ContentValues[] contentValues = null;
        try {
            cursor = db.query(tableName, columns, null, null, null, null, null);
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            contentValues = new ContentValues[cursor.getColumnCount()];
            int index = 0;
            int columnsLength = columns.length;
            while (cursor.moveToNext()) {
                contentValues[index] = new ContentValues(columnsLength);
                for (int i = 0; i < columnsLength; i++) {
                    contentValues[index].put(columns[i], cursor.getString(i));
                }
                index++;
            }
            if (cursor != null) {
                cursor.close();
            }
            return contentValues;
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

    private void bulkInsertOldTable(SQLiteDatabase db, String tableName, ContentValues[] values) {
        if (values != null) {
            synchronized (syncObj) {
                try {
                    db.beginTransaction();
                    for (ContentValues insert : values) {
                        db.insert(tableName, null, insert);
                    }
                    db.setTransactionSuccessful();
                    db.endTransaction();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    HwLog.e("SmartMemoryCleanService", "bulkInsertOldDB " + tableName + " catch SQLException:" + ex.getMessage());
                    db.endTransaction();
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                    HwLog.e("SmartMemoryCleanService", "bulkInsertOldDB catch exception");
                    db.endTransaction();
                } catch (Throwable th) {
                    db.endTransaction();
                }
            }
        }
    }

    public boolean removeAllTables(SQLiteDatabase db) {
        if (db == null) {
            return false;
        }
        String[] tables = new String[]{"whitelist", "st_usage_table", "st_time_table", "st_process_relation_table", "st_process_blacklist_table", "st_global_enable_table", "st_pkg_used_ob_period", "st_pkg_used_stat_period", SMCSDatabaseConstant.ST_PROTECTED_PKGS_TABLE, "st_memory_threshold", "st_basic_para_table", "st_key_procs_table", SMCSDatabaseConstant.TABLE_DEFAULT_VALUE, SMCSDatabaseConstant.AD_BLOCK_TABLE, VirusTableConst.VIRUS_TABLE, SMCSDatabaseConstant.DL_BLOCK_TABLE};
        try {
            db.execSQL("BEGIN TRANSACTION;");
            for (String table : tables) {
                String sql = dropTable(table);
                if (sql == null) {
                    throw new Exception("drop table failed, table:" + table);
                }
                db.execSQL(sql + SqlMarker.SQL_END);
                if (table.equals(VirusTableConst.VIRUS_TABLE)) {
                    db.execSQL("DROP INDEX IF EXISTS  CREATE INDEX packagenameIndex ON virus(package_name)");
                }
            }
            db.execSQL("COMMIT TRANSACTION;");
            return true;
        } catch (Exception e) {
            HwLog.e("SmartMemoryCleanService", "DatabaseHelper.createAllTables: catch exception " + e.toString());
            return false;
        }
    }

    private String dropTable(String tableName) {
        StringBuffer sql = new StringBuffer();
        if (tableName == null || tableName.length() == 0) {
            return null;
        }
        try {
            sql.append("DROP TABLE IF EXISTS ");
            sql.append(tableName);
            if (mDebugEnabled) {
                HwLog.v("SmartMemoryCleanService", "DatabaseHelper.dropTable: " + sql);
            }
            return sql.toString();
        } catch (SQLException e) {
            HwLog.e("SmartMemoryCleanService", "DatabaseHelper.dropTable: catch sql exception " + e.toString());
            return null;
        } catch (Exception e2) {
            HwLog.e("SmartMemoryCleanService", "DatabaseHelper.dropTable: catch exception " + e2.toString());
            return null;
        }
    }

    private void logv(String msg) {
        if (mDebugEnabled) {
            HwLog.v("SmartMemoryCleanService", msg);
        }
    }

    private void createDefaultValueTable(SQLiteDatabase db) {
        cols = new String[4];
        String[] colArgs = new String[]{"pkg_name", "TEXT NOT NULL PRIMARY KEY", SMCSDatabaseConstant.COLUMN_CONTROLL, ColType.COL_TYPE_INT};
        cols[2] = "protect";
        colArgs[2] = ColType.COL_TYPE_INT;
        cols[3] = "keytask";
        colArgs[3] = ColType.COL_TYPE_INT;
        String sql = creatTable(SMCSDatabaseConstant.TABLE_DEFAULT_VALUE, cols, colArgs, null);
        if (TextUtils.isEmpty(sql)) {
            HwLog.e("SmartMemoryCleanService", "DatabaseHelper.createDefaultValueTable, sql is null!");
            return;
        }
        try {
            db.execSQL(sql + SqlMarker.SQL_END);
        } catch (Exception e) {
            HwLog.e("SmartMemoryCleanService", "DatabaseHelper.createDefaultValueTable, error " + e.toString());
        }
    }

    private void initialDefaultValueTable(SQLiteDatabase db) {
        try {
            for (Entry<String, Map<String, String>> entry : SMCSXMLHelper.parseProtectTableDefaultValue(this.mContext).entrySet()) {
                ContentValues values = new ContentValues(4);
                Map<String, String> attrs = (Map) entry.getValue();
                values.put("pkg_name", (String) entry.getKey());
                values.put(SMCSDatabaseConstant.COLUMN_CONTROLL, Integer.valueOf(SMCSDatabaseConstant.changeStringToInt((String) attrs.get(SMCSXMLHelper.ATTR_CONTROLLED))));
                values.put("protect", Integer.valueOf(SMCSDatabaseConstant.changeStringToInt((String) attrs.get("check"))));
                values.put("keytask", Integer.valueOf(SMCSDatabaseConstant.changeStringToInt((String) attrs.get("keytask"))));
                db.insert(SMCSDatabaseConstant.TABLE_DEFAULT_VALUE, null, values);
            }
        } catch (Exception e) {
            HwLog.e("SmartMemoryCleanService", "initialDefaultValueTable faild! " + e.toString());
        }
    }

    void refreshDefaultValueTable() {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            db.execSQL("DELETE FROM default_value_table;");
            initialDefaultValueTable(db);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            HwLog.e("SmartMemoryCleanService", "refreshDefaultValueTable failed!");
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    private void createAdBlockTable(SQLiteDatabase db) {
        cols = new String[11];
        String[] colArgs = new String[]{"pkg_name", "TEXT NOT NULL PRIMARY KEY", AdBlockColumns.COLUMN_VERSION_CODE, ColType.COL_TYPE_INT, AdBlockColumns.COLUMN_VERSION_NAME, ColType.COL_TYPE_TXT, AdBlockColumns.COLUMN_ENABLE, ColType.COL_TYPE_INT, AdBlockColumns.COLUMN_DIRTY, ColType.COL_TYPE_INT, "views"};
        colArgs[5] = ColType.COL_TYPE_TXT;
        cols[6] = AdBlockColumns.COLUMN_VIEW_IDS;
        colArgs[6] = ColType.COL_TYPE_TXT;
        cols[7] = AdBlockColumns.COLUMN_DL_CHECK;
        colArgs[7] = ColType.COL_TYPE_INT;
        cols[8] = AdBlockColumns.COLUMN_URLS;
        colArgs[8] = ColType.COL_TYPE_TXT;
        cols[9] = AdBlockColumns.COLUMN_TX_URLS;
        colArgs[9] = ColType.COL_TYPE_TXT;
        cols[10] = AdBlockColumns.COLUMN_USER_TENCENT;
        colArgs[10] = ColType.COL_TYPE_INT;
        String sql = creatTable(SMCSDatabaseConstant.AD_BLOCK_TABLE, cols, colArgs, null);
        if (TextUtils.isEmpty(sql)) {
            HwLog.e("SmartMemoryCleanService", "DatabaseHelper.createAdBlockTable, sql is empty!");
            return;
        }
        try {
            db.execSQL(sql + SqlMarker.SQL_END);
        } catch (Exception e) {
            HwLog.e("SmartMemoryCleanService", "DatabaseHelper.createAdBlockTable, Exception", e);
        }
    }

    private void createDlBlockTable(SQLiteDatabase db) {
        cols = new String[6];
        String[] colArgs = new String[]{DlBlockColumns.COLUMN_UID_PKGNAME, "TEXT NOT NULL PRIMARY KEY", DlBlockColumns.COLUMN_DOWNLOADER_PKGNAME, ColType.COL_TYPE_TXT, DlBlockColumns.COLUMN_DOWNLOAD_APK_PKG_NAME, ColType.COL_TYPE_TXT};
        cols[3] = DlBlockColumns.COLUMN_DOWNLOAD_APK_APPNAME;
        colArgs[3] = ColType.COL_TYPE_TXT;
        cols[4] = DlBlockColumns.COLUMN_OPT_POLICY;
        colArgs[4] = ColType.COL_TYPE_INT;
        cols[5] = DlBlockColumns.COLUMN_TIMESTAMP;
        colArgs[5] = "Long";
        String sql = creatTable(SMCSDatabaseConstant.DL_BLOCK_TABLE, cols, colArgs, null);
        if (TextUtils.isEmpty(sql)) {
            HwLog.e("SmartMemoryCleanService", "DatabaseHelper.createDlBlockTable, sql is empty!");
            return;
        }
        try {
            db.execSQL(sql + SqlMarker.SQL_END);
        } catch (Exception e) {
            HwLog.e("SmartMemoryCleanService", "DatabaseHelper.createDlBlockTable, Exception", e);
        }
    }
}
