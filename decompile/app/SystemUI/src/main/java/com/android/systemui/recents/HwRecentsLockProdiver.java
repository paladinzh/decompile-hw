package com.android.systemui.recents;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import com.android.systemui.database.RecentDBHelper;
import com.android.systemui.utils.HwLog;

public class HwRecentsLockProdiver extends ContentProvider {
    public static final Uri AUTHORITY_URI = Uri.parse("content://com.android.systemui.recent.HwRecentsLockProdiver");
    public static final Uri RECENTS_LOCK_BACKUP_RESTORE_URI = Uri.withAppendedPath(AUTHORITY_URI, "recents_lock_bkp");
    private static final UriMatcher uriMatcher = new UriMatcher(-1);
    private RecentDBHelper mOpenHelper;

    static {
        uriMatcher.addURI("com.android.systemui.recent.HwRecentsLockProdiver", "recents_lock_bkp", 1);
    }

    public boolean onCreate() {
        this.mOpenHelper = RecentDBHelper.getInstance(getContext());
        return true;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (uri.equals(AUTHORITY_URI)) {
            return this.mOpenHelper.queryInner("recent_lock_List", projection, selection, selectionArgs);
        }
        switch (uriMatcher.match(uri)) {
            case 1:
                return this.mOpenHelper.queryInner("recent_lock_List", projection, selection, selectionArgs);
            default:
                HwLog.e("HwRecentsLockProdiver", "un-support query uri: " + uri.toString());
                return null;
        }
    }

    public Uri insert(Uri uri, ContentValues values) {
        long rowId = -1;
        if (!uri.equals(AUTHORITY_URI)) {
            switch (uriMatcher.match(uri)) {
                case 1:
                    rowId = this.mOpenHelper.insertInner("recent_lock_List", values);
                    break;
                default:
                    HwLog.e("HwRecentsLockProdiver", "un-support insert uri: " + uri.toString());
                    break;
            }
        }
        rowId = this.mOpenHelper.insertInner("recent_lock_List", values);
        if (-1 != rowId) {
            return ContentUris.withAppendedId(uri, rowId);
        }
        HwLog.e("HwRecentsLockProdiver", "insert failed");
        return null;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (uri.equals(AUTHORITY_URI)) {
            return this.mOpenHelper.deleteInner("recent_lock_List", selection, selectionArgs);
        }
        switch (uriMatcher.match(uri)) {
            case 1:
                return this.mOpenHelper.deleteInner("recent_lock_List", selection, selectionArgs);
            default:
                HwLog.e("HwRecentsLockProdiver", "un-support delete uri: " + uri.toString());
                return 0;
        }
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (uri.equals(AUTHORITY_URI)) {
            return this.mOpenHelper.updateInner("recent_lock_List", values, selection, selectionArgs);
        }
        switch (uriMatcher.match(uri)) {
            case 1:
                return this.mOpenHelper.updateInner("recent_lock_List", values, selection, selectionArgs);
            default:
                HwLog.e("HwRecentsLockProdiver", "un-support delete uri: " + uri.toString());
                return 0;
        }
    }
}
