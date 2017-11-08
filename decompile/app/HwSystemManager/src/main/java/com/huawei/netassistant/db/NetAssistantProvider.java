package com.huawei.netassistant.db;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.backup.HsmContentProvider;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;

public class NetAssistantProvider extends HsmContentProvider {
    private static final String DISTINCT_PARAMETE = "distinct";
    private static final String LIMIT_PARAMETER = "limit";
    private static int NUMBER_ZERO = 0;
    private static String TAG = "NetAssistantProvider";
    private static final UriMatcher mUriMatcher = new UriMatcher(-1);
    NetAssistantHelper mDBHelper;

    static {
        mUriMatcher.addURI(NetAssistantStore.AUTHORITY, NetAssistantStore.TABLE_NAME_SETTING_INFO, 1);
        mUriMatcher.addURI(NetAssistantStore.AUTHORITY, "settinginfo/#", 2);
        mUriMatcher.addURI(NetAssistantStore.AUTHORITY, NetAssistantStore.TABLE_NAME_TRAFFIC_ADJUST_INFO, 3);
        mUriMatcher.addURI(NetAssistantStore.AUTHORITY, "trafficadjustinfo/#", 4);
        mUriMatcher.addURI(NetAssistantStore.AUTHORITY, NetAssistantStore.TABLE_NAME_NET_ACCESS_INFO, 5);
        mUriMatcher.addURI(NetAssistantStore.AUTHORITY, "netaccessinfo/#", 6);
        mUriMatcher.addURI(NetAssistantStore.AUTHORITY, NetAssistantStore.TABLE_NAME_SETTING_INFO_BACKUP, 7);
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = NUMBER_ZERO;
        StringBuilder strWhere = new StringBuilder();
        String table = null;
        String whereClause = selection;
        switch (mUriMatcher.match(uri)) {
            case 1:
                table = NetAssistantStore.TABLE_NAME_SETTING_INFO;
                break;
            case 2:
                strWhere.append("id=").append((String) uri.getPathSegments().get(1));
                if (!TextUtils.isEmpty(selection)) {
                    strWhere.append(" and (").append(selection).append(")");
                }
                table = NetAssistantStore.TABLE_NAME_SETTING_INFO;
                whereClause = strWhere.toString();
                break;
            case 3:
                table = NetAssistantStore.TABLE_NAME_TRAFFIC_ADJUST_INFO;
                break;
            case 4:
                strWhere.append("id=").append((String) uri.getPathSegments().get(1));
                if (!TextUtils.isEmpty(selection)) {
                    strWhere.append(" and (").append(selection).append(")");
                }
                table = NetAssistantStore.TABLE_NAME_TRAFFIC_ADJUST_INFO;
                whereClause = strWhere.toString();
                break;
            case 5:
                table = NetAssistantStore.TABLE_NAME_NET_ACCESS_INFO;
                break;
            case 6:
                strWhere.append("id=").append((String) uri.getPathSegments().get(1));
                if (!TextUtils.isEmpty(selection)) {
                    strWhere.append(" and (").append(selection).append(")");
                }
                table = NetAssistantStore.TABLE_NAME_NET_ACCESS_INFO;
                whereClause = strWhere.toString();
                break;
        }
        if (table == null) {
            HwLog.e(TAG, "/delete: unknow Uri");
        } else {
            count = this.mDBHelper.delete(table, whereClause, selectionArgs);
        }
        if (NUMBER_ZERO < count) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        Uri returnUri = null;
        if (values == null) {
            values = new ContentValues();
        }
        long rowId = (long) NUMBER_ZERO;
        int match = mUriMatcher.match(uri);
        switch (match) {
            case 1:
            case 2:
            case 7:
                rowId = this.mDBHelper.insert(NetAssistantStore.TABLE_NAME_SETTING_INFO, "id", values);
                break;
            case 3:
            case 4:
                rowId = this.mDBHelper.insert(NetAssistantStore.TABLE_NAME_TRAFFIC_ADJUST_INFO, "id", values);
                break;
            case 5:
            case 6:
                rowId = this.mDBHelper.insert(NetAssistantStore.TABLE_NAME_NET_ACCESS_INFO, "id", values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (rowId > ((long) NUMBER_ZERO)) {
            returnUri = ContentUris.withAppendedId(uri, rowId);
        }
        if (returnUri != null) {
            getContext().getContentResolver().notifyChange(uri, null);
            if (match == 7) {
                increaseRecoverSucceedCount();
            }
        } else if (match == 7) {
            increaseRecoverFailedCount();
        }
        return returnUri;
    }

    public boolean onCreate() {
        this.mDBHelper = NetAssistantHelper.getInstance(getContext());
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String limit = uri.getQueryParameter(LIMIT_PARAMETER);
        String table = null;
        boolean distinct = false;
        String whereClause = selection;
        switch (mUriMatcher.match(uri)) {
            case 1:
            case 7:
                table = NetAssistantStore.TABLE_NAME_SETTING_INFO;
                if (TextUtils.isEmpty(uri.getQueryParameter(DISTINCT_PARAMETE))) {
                    distinct = true;
                    break;
                }
                break;
            case 3:
                table = NetAssistantStore.TABLE_NAME_TRAFFIC_ADJUST_INFO;
                if (TextUtils.isEmpty(uri.getQueryParameter(DISTINCT_PARAMETE))) {
                    distinct = true;
                    break;
                }
                break;
            case 5:
                table = NetAssistantStore.TABLE_NAME_NET_ACCESS_INFO;
                if (TextUtils.isEmpty(uri.getQueryParameter(DISTINCT_PARAMETE))) {
                    distinct = true;
                    break;
                }
                break;
        }
        Cursor cursor = null;
        if (table != null) {
            cursor = this.mDBHelper.query(Boolean.valueOf(distinct), table, projection, selection, selectionArgs, null, null, sortOrder, limit);
            if (cursor != null) {
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
            }
        }
        return cursor;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = NUMBER_ZERO;
        StringBuilder strBuilder = new StringBuilder();
        if (values == null) {
            values = new ContentValues();
        }
        String table = null;
        String whereClause = selection;
        switch (mUriMatcher.match(uri)) {
            case 1:
                table = NetAssistantStore.TABLE_NAME_SETTING_INFO;
                whereClause = selection;
                break;
            case 2:
                strBuilder.append("id = ").append((String) uri.getPathSegments().get(1));
                if (!TextUtils.isEmpty(selection)) {
                    strBuilder.append(" and (").append(selection).append(")");
                }
                table = NetAssistantStore.TABLE_NAME_SETTING_INFO;
                whereClause = strBuilder.toString();
                break;
            case 3:
                table = NetAssistantStore.TABLE_NAME_TRAFFIC_ADJUST_INFO;
                whereClause = selection;
                break;
            case 4:
                strBuilder.append("id = ").append((String) uri.getPathSegments().get(1));
                if (!TextUtils.isEmpty(selection)) {
                    strBuilder.append(" and (").append(selection).append(")");
                }
                table = NetAssistantStore.TABLE_NAME_TRAFFIC_ADJUST_INFO;
                whereClause = strBuilder.toString();
                break;
            case 5:
                table = NetAssistantStore.TABLE_NAME_NET_ACCESS_INFO;
                whereClause = selection;
                break;
            case 6:
                strBuilder.append("id = ").append((String) uri.getPathSegments().get(1));
                if (!TextUtils.isEmpty(selection)) {
                    strBuilder.append(" and (").append(selection).append(")");
                }
                table = NetAssistantStore.TABLE_NAME_NET_ACCESS_INFO;
                whereClause = strBuilder.toString();
                break;
        }
        if (table == null) {
            HwLog.e(TAG, "/update: unknow Uri");
        } else {
            count = this.mDBHelper.update(table, values, whereClause, selectionArgs);
        }
        if (NUMBER_ZERO < count) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    protected int getDBVersion() {
        return 5;
    }

    protected boolean canRecoverDB(int nRecoverVersion) {
        if (nRecoverVersion < 5) {
            HwLog.i(TAG, "canRecoverDB: Recover from DB older than 5 is not supported");
            return false;
        }
        HwLog.i(TAG, "canRecoverDB: Try to recover from version : " + nRecoverVersion + ", Current version : " + getDBVersion());
        return true;
    }

    protected ArrayList<String> getBackupSupportedUriList() {
        ArrayList<String> list = Lists.newArrayList();
        list.add("content://com.huawei.systemmanager.NetAssistantProvider/settinginfo/bak");
        list.add("content://com.huawei.systemmanager.NetAssistantProvider/trafficadjustinfo");
        return list;
    }

    protected boolean onRecoverStart(int nRecoverVersion) {
        HwLog.d(TAG, "onRecoverStart: nRecoverVersion = " + nRecoverVersion);
        this.mDBHelper.delete(NetAssistantStore.TABLE_NAME_SETTING_INFO, null, null);
        this.mDBHelper.delete(NetAssistantStore.TABLE_NAME_TRAFFIC_ADJUST_INFO, null, null);
        return true;
    }

    protected boolean onRecoverComplete(int nRecoverVersion) {
        HwLog.i(TAG, "onRecoverComplete: Success = " + getRecoverSucceedCount() + ", Failure = " + getRecoverFailedCount());
        return true;
    }
}
