package com.android.systemui.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import com.android.systemui.recents.HwRecentsLockProdiver;
import com.android.systemui.utils.HwLog;
import java.util.ArrayList;

public class BackupProvider extends ContentProvider {
    private static final Uri AUTHORITY_URI = Uri.parse("content://com.android.systemui.backup.BackupRestore");
    private static final Uri QS_ORDER_BACKUP_RESTORE_URI = Uri.withAppendedPath(AUTHORITY_URI, "qs_order_bkp");
    private static final UriMatcher uriMatcher = new UriMatcher(-1);

    static {
        uriMatcher.addURI("com.android.systemui.backup.BackupRestore", "qs_order_bkp", 0);
    }

    public Bundle call(String method, String arg, Bundle extras) {
        HwLog.i("BackupProvider", "call " + method);
        if (method.equals("backup_query")) {
            return backup_query(arg, extras);
        }
        if (method.equals("backup_recover_start")) {
            return backup_recover_start(arg, extras);
        }
        if (method.equals("backup_recover_complete")) {
            return backup_recover_complete(arg, extras);
        }
        HwLog.w("BackupProvider", "call: Unknown call method = " + method);
        return super.call(method, arg, extras);
    }

    public boolean onCreate() {
        return false;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (uriMatcher.match(uri)) {
            case 0:
                return PreferenceBackup.matrixCursorOfQsOrder(getContext());
            default:
                HwLog.e("BackupProvider", "query un-support uri: " + uri);
                return null;
        }
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        switch (uriMatcher.match(uri)) {
            case 0:
                if (PreferenceBackup.restoreQsOrder(getContext(), values)) {
                    return uri;
                }
                break;
            default:
                HwLog.e("BackupProvider", "insert un-support uri: " + uri);
                break;
        }
        return null;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private Bundle backup_query(String arg, Bundle extras) {
        Bundle bundle = new Bundle();
        bundle.putInt("version", 1);
        ArrayList<String> uriList = new ArrayList();
        uriList.add(QS_ORDER_BACKUP_RESTORE_URI.toString());
        uriList.add(HwRecentsLockProdiver.RECENTS_LOCK_BACKUP_RESTORE_URI.toString());
        bundle.putStringArrayList("uri_list", uriList);
        bundle.putStringArrayList("uri_list_need_count", uriList);
        HwLog.i("BackupProvider", "backup_query: DB version = 1, uriList size = " + uriList.size());
        return bundle;
    }

    private Bundle backup_recover_start(String arg, Bundle extras) {
        RecentDBHelper.getInstance(getContext()).clearDataBeforeRestore();
        Bundle bundle = new Bundle();
        bundle.putBoolean("permit", true);
        return bundle;
    }

    private Bundle backup_recover_complete(String arg, Bundle extras) {
        return new Bundle();
    }
}
