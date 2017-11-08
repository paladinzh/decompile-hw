package com.huawei.systemmanager.startupmgr.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import com.huawei.systemmanager.backup.BackupConst;
import com.huawei.systemmanager.startupmgr.comm.StartupDBConst;
import com.huawei.systemmanager.startupmgr.localize.LocalizePackageNameTable;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;

public class StartupProvider extends ContentProvider {
    public static final String AUTHORITY = "com.huawei.systemmanager.startupprovider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://com.huawei.systemmanager.startupprovider");
    private static final int STARTUP_AWAKED_BACKUP_INDICATOR = 12;
    private static final int STARTUP_AWAKED_CALLER_INDICATOR = 2;
    private static final int STARTUP_AWAKED_SWITCH_BACKUP_INDICATOR = 13;
    private static final int STARTUP_GFEATURE_INDICATOR = 0;
    private static final int STARTUP_LOCALIZE_NAME_INDICATOR = 4;
    private static final int STARTUP_NORMAL_BACKUP_INDICATOR = 11;
    private static final int STARTUP_QUERY_VIEW_INDICATOR = 1;
    private static final int STARTUP_RECORD_INDICATOR = 3;
    private static final String TAG = "StartupProvider";
    private static final UriMatcher uriMatchers = new UriMatcher(-1);

    public interface AwakedCallerProvider {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(StartupProvider.AUTHORITY_URI, AwakedCallerTable.TABLE_NAME);
    }

    public interface StartupGFeatureProvider {
        public static final String CALL_DELETE_FUNCTION = "startup_gfeature_call_delete";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(StartupProvider.AUTHORITY_URI, "gfeature");
    }

    public interface StartupInfoQueryProvider {
        public static final Uri CONTENT_URI_BASE = Uri.withAppendedPath(StartupProvider.AUTHORITY_URI, "queryview");
    }

    public interface StartupLocalizeNameProvider {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(StartupProvider.AUTHORITY_URI, "localizename");
    }

    public interface StartupRecordProvider {
        public static final String CALL_CHECK_TABLE_FUNCTION = "startup_record_call_check_table";
        public static final Uri CONTENT_URI_BASE = Uri.withAppendedPath(StartupProvider.AUTHORITY_URI, "startuprecord");
    }

    public interface SwitchStatusBackupProvider {
        public static final Uri AWAKED_BACKUP_STATUS_URI = Uri.withAppendedPath(StartupProvider.AUTHORITY_URI, "backup/awaked");
        public static final Uri AWAKED_BACKUP_SWITCH_STATUS_URI = Uri.withAppendedPath(StartupProvider.AUTHORITY_URI, "backup/awaked_switch");
        public static final Uri NORMAL_BACKUP_STATUS_URI = Uri.withAppendedPath(StartupProvider.AUTHORITY_URI, "backup/normal");
    }

    static {
        uriMatchers.addURI(AUTHORITY, "gfeature", 0);
        uriMatchers.addURI(AUTHORITY, "queryview/*", 1);
        uriMatchers.addURI(AUTHORITY, AwakedCallerTable.TABLE_NAME, 2);
        uriMatchers.addURI(AUTHORITY, "startuprecord/*", 3);
        uriMatchers.addURI(AUTHORITY, "localizename", 4);
        uriMatchers.addURI(AUTHORITY, "backup/normal", 11);
        uriMatchers.addURI(AUTHORITY, "backup/awaked", 12);
        uriMatchers.addURI(AUTHORITY, "backup/awaked_switch", 13);
    }

    public boolean onCreate() {
        return true;
    }

    public Bundle call(String method, String arg, Bundle extras) {
        if (method == null) {
            HwLog.e(TAG, "call method is null, ignore it");
            return null;
        } else if (StartupGFeatureProvider.CALL_DELETE_FUNCTION.equals(method)) {
            StartupDBOpenHelper.getInstance(getContext().getApplicationContext()).deleteFeatureRows(StartupDBConst.SHARED_REAL_TABLE, arg);
            return null;
        } else if (StartupRecordProvider.CALL_CHECK_TABLE_FUNCTION.equals(method)) {
            StartupDBOpenHelper.getInstance(getContext().getApplicationContext()).checkRecordTable(arg);
            return null;
        } else if (method.equals(BackupConst.METHOND_BACKUP_QUERY)) {
            return backup_query(arg, extras);
        } else {
            if (method.equals(BackupConst.METHOND_BACKUP_RECOVER_START)) {
                return backup_recover_start(arg, extras);
            }
            if (method.equals(BackupConst.METHOND_BACKUP_RECOVER_COMPLETE)) {
                return backup_recover_complete(arg, extras);
            }
            return super.call(method, arg, extras);
        }
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Context context = getContext().getApplicationContext();
        StartupDBOpenHelper helper = StartupDBOpenHelper.getInstance(context);
        switch (uriMatchers.match(uri)) {
            case 1:
                return helper.queryComm(uri.getLastPathSegment(), projection, selection, selectionArgs);
            case 3:
                return helper.queryComm(uri.getLastPathSegment(), projection, selection, selectionArgs);
            case 4:
                return helper.queryComm(LocalizePackageNameTable.TABLE_NAME, projection, selection, selectionArgs);
            case 11:
                return helper.queryNormalBackup();
            case 12:
                return helper.queryAwakedBackup();
            case 13:
                return helper.queryAwakedSwitchBackup(context);
            default:
                HwLog.e(TAG, "query un-supported uri: " + uri);
                return null;
        }
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        Context context = getContext().getApplicationContext();
        StartupDBOpenHelper helper = StartupDBOpenHelper.getInstance(context);
        switch (uriMatchers.match(uri)) {
            case 0:
                helper.replaceFeatureRow(StartupDBConst.SHARED_REAL_TABLE, values);
                return null;
            case 3:
                helper.insertComm(uri.getLastPathSegment(), values);
                return uri;
            case 4:
                helper.insertComm(LocalizePackageNameTable.TABLE_NAME, values);
                return uri;
            case 11:
                helper.insertNormalBackup(values);
                return uri;
            case 12:
                helper.insertAwakedBackup(values);
                return uri;
            case 13:
                helper.insertAwakedSwitchBackup(context, values);
                return uri;
            default:
                return null;
        }
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        StartupDBOpenHelper helper = StartupDBOpenHelper.getInstance(getContext().getApplicationContext());
        switch (uriMatchers.match(uri)) {
            case 2:
                return helper.deleteComm(AwakedCallerTable.TABLE_NAME, selection, selectionArgs);
            case 3:
                return helper.deleteComm(uri.getLastPathSegment(), selection, selectionArgs);
            default:
                return 0;
        }
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        StartupDBOpenHelper helper = StartupDBOpenHelper.getInstance(getContext().getApplicationContext());
        switch (uriMatchers.match(uri)) {
            case 3:
                return helper.updateCommon(uri.getLastPathSegment(), values, selection, selectionArgs);
            default:
                return 0;
        }
    }

    public int bulkInsert(Uri uri, ContentValues[] values) {
        StartupDBOpenHelper helper = StartupDBOpenHelper.getInstance(getContext().getApplicationContext());
        switch (uriMatchers.match(uri)) {
            case 0:
                return helper.replaceFeatureRows(StartupDBConst.SHARED_REAL_TABLE, values);
            case 2:
                return helper.bulkReplaceComm(AwakedCallerTable.TABLE_NAME, values);
            case 4:
                return helper.bulkReplaceComm(LocalizePackageNameTable.TABLE_NAME, values);
            default:
                return 0;
        }
    }

    private Bundle backup_query(String arg, Bundle extras) {
        Bundle bundle = new Bundle();
        bundle.putInt("version", StartupDBOpenHelper.getDatabaseVersion());
        ArrayList<String> uriList = new ArrayList();
        uriList.add(SwitchStatusBackupProvider.NORMAL_BACKUP_STATUS_URI.toString());
        uriList.add(SwitchStatusBackupProvider.AWAKED_BACKUP_STATUS_URI.toString());
        uriList.add(SwitchStatusBackupProvider.AWAKED_BACKUP_SWITCH_STATUS_URI.toString());
        bundle.putStringArrayList(BackupConst.BUNDLE_KEY_URI_LIST, uriList);
        bundle.putStringArrayList(BackupConst.BUNDLE_KEY_URI_LIST_COUNT, uriList);
        HwLog.v(TAG, "backup_query out.");
        return bundle;
    }

    private Bundle backup_recover_start(String arg, Bundle extras) {
        if (extras == null) {
            return null;
        }
        Bundle bundle = new Bundle();
        bundle.putBoolean(BackupConst.BUNDLE_KEY_PERMIT, canRecoverDB(extras.getInt("version"), StartupDBOpenHelper.getDatabaseVersion()));
        HwLog.v(TAG, "backup_recover_start out.");
        return bundle;
    }

    private Bundle backup_recover_complete(String arg, Bundle extras) {
        Bundle bundle = new Bundle();
        bundle.putInt(BackupConst.BUNDLE_KEY_SUCCEESS_COUNT, 1);
        bundle.putInt(BackupConst.BUNDLE_KEY_FAIL_COUNT, 1);
        HwLog.v(TAG, "backup_recover_start out.");
        return bundle;
    }

    private boolean canRecoverDB(int nRecoverVersion, int nCurrentVersion) {
        if (nRecoverVersion != 0 && nRecoverVersion <= nCurrentVersion) {
            return true;
        }
        HwLog.e(TAG, "canRecoverDB is not permit recover operation. RecoverVersion: " + nRecoverVersion + ", CurrentVersion: " + nCurrentVersion);
        return false;
    }
}
