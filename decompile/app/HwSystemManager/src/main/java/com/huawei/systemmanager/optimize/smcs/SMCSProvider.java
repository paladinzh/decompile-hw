package com.huawei.systemmanager.optimize.smcs;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.systemmanager.backup.HsmContentProvider;
import com.huawei.systemmanager.comm.misc.Closeables;
import com.huawei.systemmanager.optimize.base.Const;
import com.huawei.systemmanager.optimize.process.SmcsDbHelper;
import com.huawei.systemmanager.optimize.smcs.SMCSDatabaseConstant.VirusTableConst;
import com.huawei.systemmanager.util.HwLog;
import java.io.Closeable;
import java.util.ArrayList;

public class SMCSProvider extends HsmContentProvider {
    private static final int PREVENT_MODE_WHITE_LIST = 13;
    private static final int PREVENT_MODE_WHITE_LIST_ALL_BACKUP = 30;
    private static final int PREVENT_MODE_WHITE_LITE_ONE = 14;
    private static final int SMCS_AD_BLOCK = 29;
    private static final int SMCS_BACKUP_PROTECTED_PKG = 28;
    private static final int SMCS_BASIC_PARA = 10;
    private static final int SMCS_BLACKLIST = 4;
    private static final int SMCS_DL_BLOCK = 31;
    private static final int SMCS_GLOBAL_ENABLE = 5;
    private static final int SMCS_KEY_DEFAULT_VALUE = 12;
    private static final int SMCS_KEY_PROCESS = 11;
    private static final int SMCS_MEMORY_THRESHOLD = 9;
    private static final int SMCS_PROTECTED_PKG = 8;
    private static final String SMCS_PROVIDER_FUNC_EXECSQLS = "execSQLS";
    private static final String SMCS_PROVIDER_FUNC_EXECSQLS_TOKEN = ";";
    private static final int SMCS_RELATION = 3;
    private static final int SMCS_TIME = 1;
    private static final String SMCS_URI_AUTH = "smcs";
    private static final int SMCS_USAGE = 2;
    private static final int SMCS_USED_OB_PERIOD = 6;
    private static final int SMCS_USED_STAT_PERIOD = 7;
    private static final String TAG = "SMCSProvider";
    private static final int VIRUS_APPS = 15;
    private static final boolean mDebugEnabled;
    private static final UriMatcher mUriMatcher = new UriMatcher(-1);
    protected DatabaseHelper mOpenHelper = null;

    static {
        boolean z;
        if (SMCSPropConstant.localLOGV) {
            z = SMCSPropConstant.localDBLOGV;
        } else {
            z = false;
        }
        mDebugEnabled = z;
        mUriMatcher.addURI("smcs", "st_time_table", 1);
        mUriMatcher.addURI("smcs", "st_usage_table", 2);
        mUriMatcher.addURI("smcs", "st_process_relation_table", 3);
        mUriMatcher.addURI("smcs", "st_process_blacklist_table", 4);
        mUriMatcher.addURI("smcs", "st_global_enable_table", 5);
        mUriMatcher.addURI("smcs", "st_pkg_used_ob_period", 6);
        mUriMatcher.addURI("smcs", "st_pkg_used_stat_period", 7);
        mUriMatcher.addURI("smcs", SMCSDatabaseConstant.ST_PROTECTED_PKGS_TABLE, 8);
        mUriMatcher.addURI("smcs", "st_memory_threshold", 9);
        mUriMatcher.addURI("smcs", "st_basic_para_table", 10);
        mUriMatcher.addURI("smcs", "st_key_procs_table", 11);
        mUriMatcher.addURI("smcs", SMCSDatabaseConstant.TABLE_DEFAULT_VALUE, 12);
        mUriMatcher.addURI("smcs", "backup_protected_pkgs_table", 28);
        mUriMatcher.addURI("smcs", SMCSDatabaseConstant.AD_BLOCK_TABLE, 29);
        mUriMatcher.addURI("smcs", "whitelist", 13);
        mUriMatcher.addURI("smcs", "whitelist/#", 14);
        mUriMatcher.addURI("smcs", "whitelist_backup", 30);
        mUriMatcher.addURI("smcs", VirusTableConst.VIRUS_TABLE, 15);
        mUriMatcher.addURI("smcs", SMCSDatabaseConstant.DL_BLOCK_TABLE, 31);
    }

    public boolean onCreate() {
        if (mDebugEnabled) {
            HwLog.v(TAG, "SMCSProvider.onCreate");
        }
        this.mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    public Cursor query(Uri uri, String[] select, String where, String[] whereArgs, String sort) {
        SQLiteDatabase db = this.mOpenHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String sTable = parseTable(uri, "query");
        if (TextUtils.isEmpty(sTable)) {
            return handlerBackupQuery(uri, select, where, whereArgs, sort, db);
        }
        qb.setTables(sTable);
        Cursor ret = qb.query(db, select, where, whereArgs, null, null, sort);
        if (ret != null) {
            ret.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return ret;
    }

    public String getType(Uri uri) {
        String sType = null;
        if (uri == null) {
            return null;
        }
        HwLog.v(TAG, "SMCSProvider.getType: uri " + uri.toString());
        switch (mUriMatcher.match(uri)) {
            case 1:
                sType = "vnd.android.cursor.item/st_time_table";
                break;
            case 2:
                sType = "vnd.android.cursor.dir/st_usage_table";
                break;
            case 3:
                sType = "vnd.android.cursor.dir/st_process_relation_table";
                break;
            case 4:
                sType = "vnd.android.cursor.dir/st_process_blacklist_table";
                break;
            case 5:
                sType = "vnd.android.cursor.item/st_global_enable_table";
                break;
            case 6:
                sType = "vnd.android.cursor.dir/st_pkg_used_ob_period";
                break;
            case 7:
                sType = "vnd.android.cursor.dir/st_pkg_used_stat_period";
                break;
            case 8:
                sType = "vnd.android.cursor.dir/st_protected_pkgs_table";
                break;
            case 9:
                sType = "vnd.android.cursor.item/st_memory_threshold";
                break;
            case 10:
                sType = "vnd.android.cursor.item/st_basic_para_table";
                break;
            case 11:
                sType = "vnd.android.cursor.dir/st_key_procs_table";
                break;
            case 13:
                sType = "vnd.android.cursor.dir/whitelist";
                break;
            case 14:
                sType = "vnd.android.cursor.item/whitelist/#";
                break;
            default:
                HwLog.v(TAG, "SMCSProvider.getType: unkown URI");
                break;
        }
        HwLog.v(TAG, "SMCSProvider.getType: " + sType);
        return sType;
    }

    public Uri insert(Uri uri, ContentValues initialValues) {
        Uri retUri = uri;
        if (uri == null) {
            HwLog.e(TAG, "SMCSProvider.insert: invalid uri");
            return null;
        }
        SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
        if (db == null) {
            HwLog.e(TAG, "SMCSProvider.insert: invalid database");
            return null;
        }
        String table = parseTable(uri, "insert");
        if (TextUtils.isEmpty(table)) {
            return handlerBackupInsert(uri, initialValues, db);
        }
        long rowId = db.insert(table, null, initialValues);
        if (rowId != -1) {
            retUri = Uri.withAppendedPath(uri, String.valueOf(rowId));
            notifiChanged(uri);
        } else {
            HwLog.i(TAG, "SMCSProvider.insert fail! uri=" + uri);
        }
        return retUri;
    }

    public int delete(Uri uri, String where, String[] whereArgs) {
        if (uri == null) {
            HwLog.e(TAG, "SMCSProvider.delete: invalid uri");
            return -1;
        }
        SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
        if (db == null) {
            HwLog.e(TAG, "SMCSProvider.delete: invalid database");
            return -1;
        }
        String table = parseTable(uri, "delete");
        if (table == null || table.length() == 0) {
            return -1;
        }
        try {
            int count = db.delete(table, where, whereArgs);
            notifiChanged(uri);
            return count;
        } catch (Exception e) {
            HwLog.e(TAG, "SMCSProvider delete exception", e);
            return -1;
        }
    }

    public int update(Uri uri, ContentValues initialValues, String where, String[] whereArgs) {
        if (uri == null) {
            HwLog.e(TAG, "SMCSProvider.update: invalid uri");
            return -1;
        }
        SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
        if (db == null) {
            HwLog.e(TAG, "SMCSProvider.update: invalid database");
            return -1;
        }
        String table = parseTable(uri, "update");
        if (table == null || table.length() == 0) {
            return -1;
        }
        int count = db.update(table, initialValues, where, whereArgs);
        notifiChanged(uri);
        return count;
    }

    public Bundle call(String method, String arg, Bundle extras) {
        if (TextUtils.isEmpty(method)) {
            return null;
        }
        HwLog.i("SmartMemoryCleanService", "Smcs provider called method:" + method);
        if (method.equals(SMCS_PROVIDER_FUNC_EXECSQLS)) {
            this.mOpenHelper.execSQLs(arg, ";");
        }
        if (SMCSDatabaseConstant.METHOD_INIT_DEFAULT_VALUE_TABLE.equals(method)) {
            this.mOpenHelper.refreshDefaultValueTable();
        }
        return super.call(method, arg, extras);
    }

    private final String parseTable(Uri uri, String sLog) {
        String sTable = null;
        if (uri == null) {
            HwLog.e(TAG, "SMCSProvider.parseTable: invalid uri");
            return null;
        }
        HwLog.v(TAG, "SMCSProvider.parseTable: uri " + uri.toString());
        switch (mUriMatcher.match(uri)) {
            case 1:
                sTable = "st_time_table";
                break;
            case 2:
                sTable = "st_usage_table";
                break;
            case 3:
                sTable = "st_process_relation_table";
                break;
            case 4:
                sTable = "st_process_blacklist_table";
                break;
            case 5:
                sTable = "st_global_enable_table";
                break;
            case 6:
                sTable = "st_pkg_used_ob_period";
                break;
            case 7:
                sTable = "st_pkg_used_stat_period";
                break;
            case 8:
                sTable = SMCSDatabaseConstant.ST_PROTECTED_PKGS_TABLE;
                break;
            case 9:
                sTable = "st_memory_threshold";
                break;
            case 10:
                sTable = "st_basic_para_table";
                break;
            case 11:
                sTable = "st_key_procs_table";
                break;
            case 12:
                sTable = SMCSDatabaseConstant.TABLE_DEFAULT_VALUE;
                break;
            case 13:
            case 14:
                sTable = "whitelist";
                break;
            case 15:
                sTable = VirusTableConst.VIRUS_TABLE;
                break;
            case 29:
                sTable = SMCSDatabaseConstant.AD_BLOCK_TABLE;
                break;
            case 31:
                sTable = SMCSDatabaseConstant.DL_BLOCK_TABLE;
                break;
            default:
                HwLog.e(TAG, "SMCSProvider.parseTable: unkown URI.");
                break;
        }
        HwLog.v(TAG, "SMCSProvider.parseTable: " + sTable + " for " + sLog);
        return sTable;
    }

    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
        ContentProviderResult[] contentProviderResultArr = null;
        try {
            db.beginTransaction();
            HwLog.i(TAG, "bulk smcs db applyBatch, operations:" + operations.size());
            contentProviderResultArr = super.applyBatch(operations);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        return contentProviderResultArr;
    }

    private Cursor handlerBackupQuery(Uri uri, String[] select, String where, String[] whereArgs, String sort, SQLiteDatabase db) {
        HwLog.i(TAG, "handlerBackupQuery");
        switch (mUriMatcher.match(uri)) {
            case 28:
                return db.query(SMCSDatabaseConstant.ST_PROTECTED_PKGS_TABLE, select, where, whereArgs, null, null, sort);
            case 30:
                return db.query("whitelist", select, where, whereArgs, null, null, sort);
            default:
                return null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Uri handlerBackupInsert(Uri uri, ContentValues initialValues, SQLiteDatabase db) {
        String table;
        switch (mUriMatcher.match(uri)) {
            case 28:
                table = SMCSDatabaseConstant.ST_PROTECTED_PKGS_TABLE;
                String packageName = initialValues.getAsString("pkg_name");
                if (!TextUtils.isEmpty(packageName)) {
                    if (db.update(table, initialValues, "pkg_name=?", new String[]{packageName}) <= 0) {
                        increaseRecoverFailedCount();
                        HwLog.i(TAG, "bakup insert failed, packageName=" + packageName);
                        break;
                    }
                    increaseRecoverSucceedCount();
                    break;
                }
                HwLog.i(TAG, "handlerBackupInsert, packageName is empty");
                break;
            case 30:
                table = "whitelist";
                String sort = "Phone_number desc";
                Closeable closeable = null;
                try {
                    closeable = db.query(table, null, "Phone_number=" + initialValues.getAsString(Const.PREVENT_WHITE_LIST_NUMBER), null, null, null, sort);
                    if (closeable != null && closeable.getCount() != 0) {
                        increaseRecoverSucceedCount();
                    } else if (db.insert(table, null, initialValues) > 0) {
                        increaseRecoverSucceedCount();
                    } else {
                        increaseRecoverFailedCount();
                    }
                    Closeables.close(closeable);
                    break;
                } catch (RuntimeException e) {
                    HwLog.w(TAG, "PREVENT_MODE_WHITE_LIST_ALL_BACKUP exception", e);
                    break;
                } catch (Throwable th) {
                    Closeables.close(closeable);
                }
                break;
        }
        return uri;
    }

    protected int getDBVersion() {
        return 17;
    }

    protected ArrayList<String> getBackupSupportedUriList() {
        ArrayList<String> list = new ArrayList(1);
        list.add("content://smcs/backup_protected_pkgs_table");
        list.add("content://smcs/whitelist_backup");
        return list;
    }

    protected boolean canRecoverDB(int nRecoverVersion) {
        HwLog.i(TAG, "canRecoverDB: Try to recover from version : " + nRecoverVersion + ", Current version : " + getDBVersion());
        return true;
    }

    protected boolean onRecoverStart(int nRecoverVersion) {
        HwLog.d(TAG, "onRecoverStart: nRecoverVersion = " + nRecoverVersion);
        return true;
    }

    protected boolean onRecoverComplete(int nRecoverVersion) {
        notifiChanged(SMCSDatabaseConstant.URI_BACKUP_END);
        notifiChanged(SmcsDbHelper.SMCS_PROTECT_TABLE_URI);
        HwLog.i(TAG, "onRecoverComplete: Success = " + getRecoverSucceedCount() + ", Failure = " + getRecoverFailedCount());
        return true;
    }

    public int bulkInsert(Uri uri, ContentValues[] values) {
        if (values == null) {
            HwLog.w(TAG, "bulkInsert: Invalid params");
            return 0;
        }
        SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
        int insertCount = 0;
        if (db != null) {
            switch (mUriMatcher.match(uri)) {
                case 15:
                    HwLog.i(TAG, "bulk inster to virus, size:" + values.length);
                    insertCount = bulkInserts(db, VirusTableConst.VIRUS_TABLE, values);
                    break;
            }
        }
        HwLog.w(TAG, "can not get the database!");
        return insertCount;
    }

    private int bulkInserts(SQLiteDatabase database, String tableName, ContentValues[] values) {
        int iInsertCount = 0;
        try {
            database.beginTransaction();
            database.execSQL("DELETE FROM virus;");
            for (ContentValues value : values) {
                if (-1 != database.insert(tableName, null, value)) {
                    iInsertCount++;
                }
            }
            database.setTransactionSuccessful();
        } catch (SQLException e) {
            HwLog.e(TAG, "bulkInserts cat exception");
        } finally {
            database.endTransaction();
        }
        return iInsertCount;
    }
}
