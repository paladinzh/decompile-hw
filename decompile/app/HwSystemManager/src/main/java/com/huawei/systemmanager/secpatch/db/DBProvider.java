package com.huawei.systemmanager.secpatch.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import com.huawei.systemmanager.secpatch.common.ConstValues;
import com.huawei.systemmanager.util.HwLog;

public class DBProvider extends ContentProvider {
    public static final String AUTH = "com.huawei.systemmanager.SecPatchDBProvider";
    private static final int INDICATOR_SEARCH_VIEW = 3;
    private static final int INDICATOR_SECPATCH = 1;
    private static final int INDICATOR_SYSTEM_VERSION_SECPATCH = 2;
    public static final String TAG = "SecPatchDBProvider";
    private static UriMatcher mUriMatcher = new UriMatcher(-1);
    private DBHelper mDatabaseHelper = null;

    static {
        mUriMatcher.addURI(AUTH, ConstValues.TB_SECPATCH, 1);
        mUriMatcher.addURI(AUTH, ConstValues.SYSTEM_VERSION_SECPATCH, 2);
        mUriMatcher.addURI(AUTH, ConstValues.SEARCH_VIEW_NAME_SECPATCH, 3);
    }

    public int bulkInsert(Uri uri, ContentValues[] values) {
        int i = 0;
        if (values == null || values.length <= 0) {
            HwLog.e(TAG, "bulkInsert: Invalid values");
            return 0;
        }
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        if (db == null) {
            HwLog.e(TAG, "bulkInsert: Failed to getWritableDatabase, uri = " + uri);
            return 0;
        }
        String tableName = "";
        switch (mUriMatcher.match(uri)) {
            case 1:
                tableName = ConstValues.TB_SECPATCH;
                break;
            case 2:
                tableName = ConstValues.SYSTEM_VERSION_SECPATCH;
                break;
            default:
                HwLog.w(TAG, "bulkInsert: Invalid uri = " + uri);
                return 0;
        }
        if (TextUtils.isEmpty(tableName)) {
            HwLog.w(TAG, "bulkInsert: Fail to get matched table");
            return 0;
        }
        int nInsertCount = 0;
        try {
            db.beginTransaction();
            int length = values.length;
            while (i < length) {
                if (db.insert(tableName, null, values[i]) > 0) {
                    nInsertCount++;
                }
                i++;
            }
            db.setTransactionSuccessful();
            if (nInsertCount > 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            HwLog.i(TAG, "bulkInsert: Insert count = " + nInsertCount);
        } catch (Exception e) {
            nInsertCount = 0;
            HwLog.e(TAG, "bulkInsert: Exception", e);
        } finally {
            db.endTransaction();
        }
        return nInsertCount;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        if (db == null) {
            HwLog.e(TAG, "delete: Failed to getWritableDatabase, uri = " + uri);
            return 0;
        }
        String tableName = "";
        switch (mUriMatcher.match(uri)) {
            case 1:
                tableName = ConstValues.TB_SECPATCH;
                break;
            case 2:
                tableName = ConstValues.SYSTEM_VERSION_SECPATCH;
                break;
            default:
                HwLog.w(TAG, "delete: Invalid uri = " + uri);
                return 0;
        }
        int nCount = db.delete(tableName, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return nCount;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        if (values == null || values.size() <= 0) {
            HwLog.e(TAG, "insert: Invalid values");
            return null;
        }
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        if (db == null) {
            HwLog.e(TAG, "insert: Failed to getWritableDatabase, uri = " + uri);
            return null;
        }
        String tableName = "";
        Uri retUri = uri;
        switch (mUriMatcher.match(uri)) {
            case 1:
                tableName = ConstValues.TB_SECPATCH;
                break;
            case 2:
                tableName = ConstValues.SYSTEM_VERSION_SECPATCH;
                break;
            default:
                HwLog.w(TAG, "insert: Invalid uri = " + uri);
                return null;
        }
        retUri = Uri.withAppendedPath(uri, String.valueOf(db.insert(tableName, null, values)));
        getContext().getContentResolver().notifyChange(uri, null);
        return retUri;
    }

    public boolean onCreate() {
        if (this.mDatabaseHelper == null) {
            this.mDatabaseHelper = new DBHelper(getContext());
            HwLog.d(TAG, "onCreate: DB helper is created");
        }
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = this.mDatabaseHelper.getReadableDatabase();
        if (db == null) {
            HwLog.e(TAG, "query : Fail to getReadableDatabase, uri = " + uri);
            return null;
        }
        String tableName = "";
        switch (mUriMatcher.match(uri)) {
            case 1:
                tableName = ConstValues.TB_SECPATCH;
                break;
            case 2:
                tableName = ConstValues.SYSTEM_VERSION_SECPATCH;
                break;
            case 3:
                tableName = ConstValues.SEARCH_VIEW_NAME_SECPATCH;
                break;
            default:
                HwLog.w(TAG, "query: Invalid uri = " + uri);
                return null;
        }
        return db.query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        if (db == null) {
            HwLog.e(TAG, "update : Fail to getReadableDatabase, uri = " + uri);
            return 0;
        }
        String tableName = "";
        switch (mUriMatcher.match(uri)) {
            case 1:
                tableName = ConstValues.TB_SECPATCH;
                break;
            case 2:
                tableName = ConstValues.SYSTEM_VERSION_SECPATCH;
                break;
            default:
                HwLog.w(TAG, "update: Invalid uri = " + uri);
                return 0;
        }
        int nCount = db.update(tableName, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return nCount;
    }
}
