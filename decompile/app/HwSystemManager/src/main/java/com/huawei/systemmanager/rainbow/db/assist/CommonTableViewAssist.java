package com.huawei.systemmanager.rainbow.db.assist;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.huawei.systemmanager.rainbow.db.CloudDBHelper;

public class CommonTableViewAssist {
    public static int delete(CloudDBHelper dbHelper, Uri uri, String whereClause, String[] whereArgs) {
        return dbHelper.deleteComm(tableOrViewName(uri), whereClause, whereArgs);
    }

    public static long insert(CloudDBHelper dbHelper, Uri uri, ContentValues values) {
        return dbHelper.insertComm(tableOrViewName(uri), values);
    }

    public static Cursor query(CloudDBHelper dbHelper, Uri uri, String[] projection, String selection, String[] selectionArgs) {
        return dbHelper.queryComm(tableOrViewName(uri), projection, selection, selectionArgs);
    }

    public static int bulkInsert(CloudDBHelper dbHelper, Uri uri, ContentValues[] values) {
        return dbHelper.bulkInsertComm(tableOrViewName(uri), values);
    }

    private static String tableOrViewName(Uri uri) {
        return uri.getLastPathSegment();
    }
}
