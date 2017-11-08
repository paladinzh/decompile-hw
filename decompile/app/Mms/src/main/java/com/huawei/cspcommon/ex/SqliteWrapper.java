package com.huawei.cspcommon.ex;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.IContentProvider;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Calendars;
import com.huawei.cspcommon.MLog;

public class SqliteWrapper {
    private SqliteWrapper() {
    }

    public static void checkSQLiteException(Context context, SQLiteException e) {
        android.database.sqlite.SqliteWrapper.checkSQLiteException(context, e);
    }

    public static Cursor query(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return query(context, context.getContentResolver(), uri, projection, selection, selectionArgs, sortOrder);
    }

    public static Cursor query(Context context, ContentResolver resolver, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        try {
            return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (SQLiteException e) {
            error(1, uri, e, null);
            return null;
        } catch (Exception e2) {
            ErrorMonitor.reportErrorInfo(8, "SqliteWrapper Catch a Exception when query: ", e2);
            return null;
        }
    }

    public static int update(Context context, Uri uri, ContentValues values, String where, String[] selectionArgs) {
        return update(context, context.getContentResolver(), uri, values, where, selectionArgs);
    }

    public static int update(Context context, ContentResolver resolver, Uri uri, ContentValues values, String where, String[] selectionArgs) {
        try {
            return resolver.update(uri, values, where, selectionArgs);
        } catch (SQLiteException e) {
            error(3, uri, e, null);
            return -1;
        } catch (Exception e2) {
            ErrorMonitor.reportErrorInfo(8, "SqliteWrapper Catch a Exception when update: ", e2);
            return -1;
        }
    }

    public static int delete(Context context, Uri uri, String where, String[] selectionArgs) {
        return delete(context, context.getContentResolver(), uri, where, selectionArgs);
    }

    public static int delete(Context context, ContentResolver resolver, Uri uri, String where, String[] selectionArgs) {
        try {
            return resolver.delete(uri, where, selectionArgs);
        } catch (SQLiteException e) {
            error(4, uri, e, null);
            return -1;
        } catch (Exception e2) {
            ErrorMonitor.reportErrorInfo(8, "SqliteWrapper Catch a Exception when delete: ", e2);
            return -1;
        }
    }

    public static Uri insert(Context context, Uri uri, ContentValues values) {
        return insert(context, context.getContentResolver(), uri, values);
    }

    public static Uri insert(Context context, ContentResolver resolver, Uri uri, ContentValues values) {
        try {
            return resolver.insert(uri, values);
        } catch (SQLiteException e) {
            error(2, uri, e, null);
            return null;
        } catch (Exception e2) {
            ErrorMonitor.reportErrorInfo(8, "SqliteWrapper Catch a Exception when insert: ", e2);
            return null;
        }
    }

    public static Bundle call(Context context, Uri uri, String callingPkg, String method, String arg, Bundle extras) {
        try {
            IContentProvider icp = context.getContentResolver().acquireProvider(uri);
            if (icp == null) {
                return null;
            }
            return icp.call(callingPkg, method, arg, extras);
        } catch (Exception e) {
            error(2, uri, e, "SqliteWrapper call METHOD " + method + "@" + callingPkg);
            return null;
        }
    }

    public static boolean isSkippedUri(Uri uri) {
        if (Calendars.CONTENT_URI.equals(uri)) {
            return true;
        }
        return false;
    }

    private static void error(int operation, Uri uri, Exception e, String msg) {
        String operationType;
        StringBuilder info = new StringBuilder();
        switch (operation) {
            case 1:
                info.append("Exception in Provider QUERY");
                operationType = "Query";
                break;
            case 2:
                info.append("Exception in Provider INSERT");
                operationType = "Insert";
                break;
            case 3:
                info.append("Exception in Provider UPDATE");
                operationType = "Update";
                break;
            case 4:
                info.append("Exception in Provider DELETE");
                operationType = "Delete";
                break;
            case 5:
                info.append("Exception in Provider CALL");
                operationType = "Call";
                break;
            default:
                info.append("Exception in Provider OP_?");
                operationType = "OP_?";
                break;
        }
        if (msg != null) {
            info.append(" ").append(msg);
        }
        if (isSkippedUri(uri)) {
            MLog.w("CSP_SQL", msg, e);
        } else {
            ErrorMonitor.reportErrorInfo(8, "SqliteWrapper Catch a SQLiteException when insert: ", new SqliteExceptionEx(operation, uri, e));
        }
        if (e instanceof SQLiteException) {
            ErrorMonitor.reportRadar(907000015, e.getMessage(), operationType, (Throwable) e);
        }
    }
}
