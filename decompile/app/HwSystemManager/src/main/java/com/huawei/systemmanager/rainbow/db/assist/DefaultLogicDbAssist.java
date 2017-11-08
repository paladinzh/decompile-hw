package com.huawei.systemmanager.rainbow.db.assist;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.text.TextUtils;
import com.huawei.systemmanager.rainbow.db.CloudDBHelper;
import com.huawei.systemmanager.rainbow.db.CloudDBTableMap;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.ControlRangeBlackList;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.ControlRangeWhiteList;
import com.huawei.systemmanager.util.HwLog;

public class DefaultLogicDbAssist {
    private static final String TAG = "DefaultLogicDbAssist";

    public static int defaultDelete(CloudDBHelper dbHelper, int indicatorCode, String selection, String[] selectionArgs) {
        String strTableName = getMatchedTable(indicatorCode);
        if (TextUtils.isEmpty(strTableName)) {
            HwLog.w(TAG, "deleting unknown/invalid code: " + indicatorCode);
            return -1;
        }
        int count = -1;
        try {
            count = dbHelper.getWritableDatabase().delete(strTableName, selection, selectionArgs);
            if (ControlRangeWhiteList.OUTERTABLE_NAME.equals(strTableName) || ControlRangeBlackList.OUTERTABLE_NAME.equals(strTableName)) {
                count = 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return count;
    }

    public static long defaultInsert(CloudDBHelper dbHelper, int indicatorCode, ContentValues values) {
        if (values == null) {
            HwLog.e(TAG, "insert : values");
            return -1;
        }
        String table = getMatchedTable(indicatorCode);
        if (TextUtils.isEmpty(table)) {
            HwLog.w(TAG, "calling insert on an unknown/invalid code: " + indicatorCode);
            return -1;
        }
        try {
            return dbHelper.insertComm(table, values);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        } catch (Exception e2) {
            e2.printStackTrace();
            return -1;
        }
    }

    public static Cursor defaultQuery(CloudDBHelper dbHelper, int indicatorCode, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String table = getMatchedTable(indicatorCode);
        if (TextUtils.isEmpty(table)) {
            HwLog.i(TAG, "query failed!,table is empty");
            return null;
        }
        try {
            return dbHelper.queryComm(table, projection, selection, selectionArgs);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public static int defaultUpdate(CloudDBHelper dbHelper, int indicatorCode, ContentValues values, String whereClause, String[] whereArgs) {
        if (values == null) {
            HwLog.e(TAG, "update : Invalid  values");
            return -1;
        }
        String strTableName = getMatchedTable(indicatorCode);
        if (TextUtils.isEmpty(strTableName)) {
            HwLog.w(TAG, "updating unknown/invalid code: " + indicatorCode);
            return -1;
        }
        try {
            return dbHelper.updateCommon(strTableName, values, whereClause, whereArgs);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        } catch (Exception e2) {
            e2.printStackTrace();
            return -1;
        }
    }

    public static int defaultBulkInsert(CloudDBHelper dbHelper, int indicatorCode, ContentValues[] values) {
        if (values == null || values.length == 0) {
            HwLog.e(TAG, "bulkInsert : Invalid DB helper or values");
            return -1;
        }
        String table = getMatchedTable(indicatorCode);
        if (TextUtils.isEmpty(table)) {
            HwLog.w(TAG, "calling insert on an unknown/invalid code: " + indicatorCode);
            return -1;
        } else if ("CloudPermission".equals(table) || "CloudVaguePermission".equals(table)) {
            return dbHelper.replaceFeatureRows(table, values);
        } else {
            return dbHelper.bulkInsertComm(table, values);
        }
    }

    public static int defaultBulkReplace(CloudDBHelper dbHelper, int indicatorCode, ContentValues[] values) {
        if (values == null || values.length == 0) {
            HwLog.e(TAG, "bulkInsert : Invalid DB helper or values");
            return -1;
        }
        String table = getMatchedTable(indicatorCode);
        if (!TextUtils.isEmpty(table)) {
            return dbHelper.bulkReplaceComm(table, values);
        }
        HwLog.w(TAG, "calling insert on an unknown/invalid code: " + indicatorCode);
        return -1;
    }

    private static String getMatchedTable(int indicatorCode) {
        return (String) CloudDBTableMap.getTablesMap().get(indicatorCode, "");
    }
}
