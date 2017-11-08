package com.huawei.permissionmanager.db;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import com.huawei.permissionmanager.model.HwAppPermissions;
import com.huawei.systemmanager.addviewmonitor.AddViewAppInfo;
import com.huawei.systemmanager.addviewmonitor.AddViewAppManager;
import com.huawei.systemmanager.backup.BackupConst;
import com.huawei.systemmanager.backup.BackupUtil;
import com.huawei.systemmanager.backup.HsmContentProvider;
import com.huawei.systemmanager.comm.misc.Closeables;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

public class PermissionDataProvider extends HsmContentProvider {
    private static final String ADDVIEW_PERMISSION_BACKUP = "addview_permission_backup";
    private static final int ADD_VIEW_PERMISSION_URI_INDICATOR_BACKUP = 8;
    public static final String AUTHORITY = "com.huawei.permissionmanager.provider.PermissionDataProvider";
    private static final String COMMON_TABLE = "common";
    private static final String COMMON_TABLE_BACKUP = "common_backup";
    private static final int COMMON_TABLE_URI_INDICATOR = 3;
    private static final int COMMON_TABLE_URI_INDICATOR_BACKUP = 6;
    private static final String LOG_TABLE = "log";
    private static final int LOG_TABLE_URI_INDICATOR = 2;
    private static final String LOG_TAG = "PermissionDataProvider";
    private static final int PERMISSION_HISTORY = 9;
    private static final String PERMISSION_REPLACE = "permission_replace";
    private static final int PERMISSION_REPLACE_INDICATOR = 10;
    private static final String PERMISSION_TABLE = "permission";
    private static final String PERMISSION_TABLE_BACKUP = "permission_backup";
    private static final int PERMISSION_TABLE_URI_INDICATOR = 1;
    private static final int PERMISSION_TABLE_URI_INDICATOR_BACKUP = 5;
    private static final int PREPERMISSION_TABLE_URI_INDICATOR = 7;
    private static final String RUNTIME_TABLE = "runtimePermissions";
    private static final int RUNTIME_TABLE_RUI_INDICATOR = 4;
    private static final UriMatcher uriMatchers = new UriMatcher(-1);
    private DBHelper mDatabaseHelper = null;

    static {
        uriMatchers.addURI(AUTHORITY, "permission", 1);
        uriMatchers.addURI(AUTHORITY, PERMISSION_REPLACE, 10);
        uriMatchers.addURI(AUTHORITY, LOG_TABLE, 2);
        uriMatchers.addURI(AUTHORITY, "common", 3);
        uriMatchers.addURI(AUTHORITY, "runtimePermissions", 4);
        uriMatchers.addURI(AUTHORITY, PERMISSION_TABLE_BACKUP, 5);
        uriMatchers.addURI(AUTHORITY, COMMON_TABLE_BACKUP, 6);
        uriMatchers.addURI(AUTHORITY, "prePermission", 7);
        uriMatchers.addURI(AUTHORITY, ADDVIEW_PERMISSION_BACKUP, 8);
        uriMatchers.addURI(AUTHORITY, DBHelper.TABLE_HISTORY, 9);
    }

    public boolean onCreate() {
        GlobalContext.setContext(getContext());
        this.mDatabaseHelper = new DBHelper(getContext());
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String tableName;
        switch (uriMatchers.match(uri)) {
            case 1:
                tableName = "permissionCfg";
                break;
            case 2:
                tableName = "logRecord";
                break;
            case 3:
            case 6:
                tableName = DBHelper.COMMON_TABLE_NAME;
                break;
            case 4:
                tableName = "runtimePermissions";
                break;
            case 5:
                return getPermissionCfgCursor(projection, selection, selectionArgs, sortOrder);
            case 7:
                tableName = "prePermissionCfg";
                break;
            case 8:
                return BackupUtil.getAddViewPermissionsCursor(getAddViewPermissions());
            case 9:
                return handlerQueryHistory(projection, selection, selectionArgs, sortOrder);
            default:
                HwLog.e(LOG_TAG, "HwPerMgr: Unkown URI " + uri);
                return null;
        }
        try {
            return this.mDatabaseHelper.query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Cursor getPermissionCfgCursor(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        HwLog.i(LOG_TAG, "backup_restore: getPermissionCfgCursor begin get cfg data.");
        Closeable closeable = null;
        Cursor cursor;
        try {
            closeable = this.mDatabaseHelper.query("permissionCfg", projection, selection, selectionArgs, null, null, sortOrder);
            if (closeable == null) {
                HwLog.e(LOG_TAG, "getPermissionCfgCursor - cursor is null ");
                cursor = null;
                return cursor;
            } else if (closeable.getCount() <= 0 || closeable.getColumnCount() <= 0) {
                HwLog.w(LOG_TAG, "getPermissionCfgCursor - no permission data.");
                Closeables.close(closeable);
                return null;
            } else {
                MatrixCursor matrixCursor = new MatrixCursor(closeable.getColumnNames());
                int idIndex = closeable.getColumnIndex("_id");
                int uidIndex = closeable.getColumnIndex("uid");
                int pkgNameIndex = closeable.getColumnIndex("packageName");
                int pCodeIndex = closeable.getColumnIndex("permissionCode");
                int trustIndex = closeable.getColumnIndex("trust");
                int pCfgIndex = closeable.getColumnIndex("permissionCfg");
                closeable.moveToFirst();
                while (!closeable.isAfterLast()) {
                    int rowId = closeable.getInt(idIndex);
                    int uid = closeable.getInt(uidIndex);
                    DBPermissionItem item = new DBPermissionItem(closeable.getString(pkgNameIndex));
                    item.mPermissionCode = closeable.getInt(pCodeIndex);
                    item.mTrustCode = closeable.getInt(trustIndex);
                    item.mPermissionCfg = closeable.getInt(pCfgIndex);
                    DBPermissionItem item_new = HwAppPermissions.create(getContext(), closeable.getString(pkgNameIndex)).getBackupPermissionData(item);
                    matrixCursor.addRow(new Object[]{Integer.valueOf(rowId), item_new.mPkgName, Integer.valueOf(uid), Integer.valueOf(item.mPermissionCode), Integer.valueOf(item.mTrustCode), Integer.valueOf(item.mPermissionCfg)});
                    closeable.moveToNext();
                }
                HwLog.i(LOG_TAG, "backup_restore: getPermissionCfgCursor matrix size:" + matrixCursor.getCount());
                Closeables.close(closeable);
                return matrixCursor;
            }
        } catch (NullPointerException e) {
            cursor = LOG_TAG;
            HwLog.w((String) cursor, "error:" + e);
            return null;
        } catch (Exception e2) {
            cursor = LOG_TAG;
            HwLog.w((String) cursor, "error:" + e2);
            return null;
        } finally {
            Closeables.close(closeable);
        }
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        Uri resultUri = null;
        long rowId;
        Uri logUri;
        switch (uriMatchers.match(uri)) {
            case 1:
                try {
                    rowId = this.mDatabaseHelper.insert(values, "permissionCfg");
                    if (rowId > 0) {
                        resultUri = ContentUris.withAppendedId(uri, rowId);
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
                break;
            case 3:
                try {
                    rowId = this.mDatabaseHelper.insert(values, DBHelper.COMMON_TABLE_NAME);
                    if (rowId > 0) {
                        resultUri = ContentUris.withAppendedId(uri, rowId);
                        break;
                    }
                } catch (Exception e2) {
                    HwLog.e(LOG_TAG, "Insert COMMON_TABLE_URI_INDICATOR  error!");
                    break;
                }
                break;
            case 4:
                try {
                    rowId = this.mDatabaseHelper.insert(values, "runtimePermissions");
                    if (rowId > 0) {
                        resultUri = ContentUris.withAppendedId(uri, rowId);
                        break;
                    }
                } catch (Exception e3) {
                    e3.printStackTrace();
                    break;
                }
                break;
            case 5:
                try {
                    if ("com.huawei.android.backup".equals(values.getAsString("packageName"))) {
                        HwLog.w(LOG_TAG, "Don't recover backup itself.");
                        return ContentUris.withAppendedId(uri, -2);
                    }
                    rowId = this.mDatabaseHelper.insertForRestorePermissionCfg(getContext(), values, this.mDatabaseHelper.getRecoverTmpTableMap("permissionCfg"));
                    logUri = null;
                    if (rowId >= 0) {
                        logUri = ContentUris.withAppendedId(uri, rowId);
                        increaseRecoverSucceedCount();
                    } else {
                        if (-2 == rowId) {
                            logUri = ContentUris.withAppendedId(uri, rowId);
                        }
                        increaseRecoverFailedCount();
                    }
                    return logUri;
                } catch (Exception e32) {
                    e32.printStackTrace();
                    break;
                }
            case 6:
                try {
                    rowId = this.mDatabaseHelper.insert(values, this.mDatabaseHelper.getRecoverTmpTableMap(DBHelper.COMMON_TABLE_NAME));
                    logUri = null;
                    if (rowId > 0) {
                        logUri = ContentUris.withAppendedId(uri, rowId);
                        increaseRecoverSucceedCount();
                    } else {
                        increaseRecoverFailedCount();
                    }
                    return logUri;
                } catch (Exception e322) {
                    e322.printStackTrace();
                    break;
                }
            case 7:
                try {
                    rowId = this.mDatabaseHelper.insert(values, "prePermissionCfg");
                    if (rowId > 0) {
                        resultUri = ContentUris.withAppendedId(uri, rowId);
                        break;
                    }
                } catch (Exception e3222) {
                    e3222.printStackTrace();
                    break;
                }
                break;
            case 8:
                try {
                    rowId = (long) setAddViewPermissions(values);
                    logUri = null;
                    if (rowId >= 0) {
                        logUri = ContentUris.withAppendedId(uri, rowId);
                        increaseRecoverSucceedCount();
                    } else {
                        if (-2 == rowId) {
                            logUri = ContentUris.withAppendedId(uri, rowId);
                        }
                        increaseRecoverFailedCount();
                    }
                    return logUri;
                } catch (Exception e32222) {
                    e32222.printStackTrace();
                    break;
                }
            case 10:
                try {
                    rowId = this.mDatabaseHelper.replace(values, "permissionCfg");
                    if (rowId > 0) {
                        resultUri = ContentUris.withAppendedId(uri, rowId);
                        break;
                    }
                } catch (Exception e322222) {
                    e322222.printStackTrace();
                    break;
                }
                break;
            default:
                HwLog.w(LOG_TAG, "HwPerMgr: Illegal URI " + uri);
                break;
        }
        if (resultUri != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return resultUri;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int deleteCount = 0;
        switch (uriMatchers.match(uri)) {
            case 1:
                deleteCount = this.mDatabaseHelper.delete("permissionCfg", selection, selectionArgs);
                break;
            case 4:
                deleteCount = this.mDatabaseHelper.delete("runtimePermissions", selection, selectionArgs);
                break;
            case 7:
                this.mDatabaseHelper.deleteAllData("prePermissionCfg");
                break;
            case 9:
                deleteCount = this.mDatabaseHelper.delete(DBHelper.TABLE_HISTORY, selection, selectionArgs);
                break;
        }
        if (deleteCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return deleteCount;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int rowId = 0;
        try {
            switch (uriMatchers.match(uri)) {
                case 1:
                    rowId = this.mDatabaseHelper.update("permissionCfg", values, selection, selectionArgs);
                    break;
                case 3:
                    rowId = this.mDatabaseHelper.update(DBHelper.COMMON_TABLE_NAME, values, selection, selectionArgs);
                    break;
                default:
                    HwLog.w(LOG_TAG, "HwPerMgr: Illegal URI " + uri);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (rowId > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowId;
    }

    public int bulkInsert(Uri uri, ContentValues[] values) {
        int count;
        switch (uriMatchers.match(uri)) {
            case 1:
                count = this.mDatabaseHelper.bulkInsert("permissionCfg", values);
                break;
            case 7:
                count = this.mDatabaseHelper.bulkInsert("prePermissionCfg", values);
                break;
            case 10:
                count = this.mDatabaseHelper.bulkReplace("permissionCfg", values);
                break;
            default:
                HwLog.w(LOG_TAG, "bulkInsert unknown URI " + uri);
                return 0;
        }
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    public Bundle call(String method, String arg, Bundle extras) {
        if (method == null) {
            HwLog.w(LOG_TAG, "Call method is null");
            return null;
        } else if (DBHelper.METHOD_RECORD_HISTORY.equals(method)) {
            return handlerRecordHistory(extras);
        } else {
            return super.call(method, arg, extras);
        }
    }

    protected int getDBVersion() {
        return DBHelper.getDBVersion();
    }

    protected ArrayList<String> getBackupSupportedUriList() {
        ArrayList<String> uriList = new ArrayList();
        uriList.add("content://com.huawei.permissionmanager.provider.PermissionDataProvider/permission_backup");
        uriList.add("content://com.huawei.permissionmanager.provider.PermissionDataProvider/common_backup");
        uriList.add("content://com.huawei.permissionmanager.provider.PermissionDataProvider/addview_permission_backup");
        return uriList;
    }

    protected boolean canRecoverDB(int nRecoverVersion) {
        if (nRecoverVersion >= 9) {
            return true;
        }
        HwLog.i(LOG_TAG, "canRecoverDB: Recover from DB older than 9 is not supported");
        return false;
    }

    protected boolean onRecoverStart(int nRecoverVersion) {
        HwLog.i(LOG_TAG, "debug onRecoverStart.");
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        if (db != null) {
            return this.mDatabaseHelper.onRecoverStart(db, nRecoverVersion);
        }
        HwLog.w(LOG_TAG, "onRecoverStart: Fail to get getWritableDatabase");
        return false;
    }

    protected boolean onRecoverComplete(int nRecoverVersion) {
        this.mDatabaseHelper.clearTrustAppsCache();
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        if (db == null) {
            HwLog.w(LOG_TAG, "onRecoverComplete: Fail to get getWritableDatabase");
            return false;
        }
        HwLog.i(LOG_TAG, "backup_restore: onRecoverComplete: Success = " + getRecoverSucceedCount() + ", Failure = " + getRecoverFailedCount());
        if (!this.mDatabaseHelper.onRecoverComplete(db, nRecoverVersion)) {
            return false;
        }
        getContext().getContentResolver().notifyChange(DBHelper.BLOCK_TABLE_NAME_URI, null);
        return true;
    }

    private ContentValues getAddViewPermissions() {
        List<AddViewAppInfo> backupApps = AddViewAppManager.getInstance(getContext()).initAddViewAppList();
        if (backupApps.isEmpty()) {
            HwLog.w(LOG_TAG, "getAddViewPermissions : No apps should backup!");
            return null;
        }
        ContentValues values = new ContentValues();
        for (AddViewAppInfo appInfo : backupApps) {
            values.put(appInfo.mPkgName, Boolean.valueOf(appInfo.mAddViewAllow));
        }
        return values;
    }

    private int setAddViewPermissions(ContentValues values) {
        if (values == null) {
            HwLog.w(LOG_TAG, "setAddViewPermissions : Invalid content values");
            return -1;
        }
        String keyString = values.getAsString(BackupConst.PREFERENCE_KEY);
        boolean bValue = values.getAsBoolean(BackupConst.PREFERENCE_VALUE).booleanValue();
        int uid = BackupUtil.getPackageUid(getContext(), keyString);
        if (-1 == uid) {
            HwLog.w(LOG_TAG, "setAddViewPermissions fails , uid = -1 and keyString is " + keyString);
            return -2;
        }
        if (this.mDatabaseHelper.isTrustendWhenRestore(keyString)) {
            HwLog.i(LOG_TAG, "backup_restore : ignore trusted app when restore add view value for:" + keyString);
        } else {
            AddViewAppManager.getInstance(getContext()).setOpsMode(uid, keyString, bValue);
            HwLog.d(LOG_TAG, String.format("setAddViewPermissions : %1$s = %2$s", new Object[]{keyString, Boolean.valueOf(bValue)}));
        }
        return 1;
    }

    private Bundle handlerRecordHistory(Bundle extras) {
        if (this.mDatabaseHelper.recordHistory(extras) > 0) {
            getContext().getContentResolver().notifyChange(DBHelper.HISTORY_URI, null);
        } else {
            HwLog.e(LOG_TAG, "handlerRecordHistory recordNum is empty");
        }
        return null;
    }

    private Cursor handlerQueryHistory(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        try {
            return this.mDatabaseHelper.queryWithLimit(DBHelper.TABLE_HISTORY, projection, selection, selectionArgs, null, null, sortOrder, String.valueOf(5000));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
