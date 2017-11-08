package com.huawei.systemmanager.applock.datacenter;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import com.google.common.base.Preconditions;
import com.huawei.systemmanager.applock.datacenter.tbl.AppLockAuthSuccessTable;
import com.huawei.systemmanager.applock.datacenter.tbl.AppLockPreferenceTable;
import com.huawei.systemmanager.applock.datacenter.tbl.AppLockStatusTable;
import com.huawei.systemmanager.applock.datacenter.tbl.AppLockTableViews.BackupPrefView;
import com.huawei.systemmanager.applock.datacenter.tbl.AppLockTableViews.LockedAppView;
import com.huawei.systemmanager.applock.datacenter.tbl.AppLockTableViews.UnlockedAppView;
import com.huawei.systemmanager.applock.utils.compatibility.AppLockPwdUtils;
import com.huawei.systemmanager.applock.utils.sp.FingerprintBindUtils;
import com.huawei.systemmanager.applock.utils.sp.ReloadSwitchUtils;
import com.huawei.systemmanager.applock.utils.sp.SPBackupRestoreUtils;
import com.huawei.systemmanager.backup.BackupConst;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;

public class AppLockProvider extends ContentProvider {
    private static final int APPLOCK_SHAREPREFERENCE_DATA_INDICATOR = 6;
    private static final int APP_LOCKED_VIEW_INDICATOR = 1;
    private static final int APP_LOCK_FINGERPRINT_BIND_STATUS_INDICATOR = 5;
    private static final int APP_LOCK_STATUS_BACKUPRESTORE_INDICATOR = 11;
    private static final int APP_LOCK_STATUS_INDICATOR = 0;
    private static final int APP_UNLOCKED_VIEW_INDICATOR = 2;
    public static final String AUTHORITY = "com.huawei.systemmanager.applockprovider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://com.huawei.systemmanager.applockprovider");
    private static final int AUTH_SUCCESS_PACKAGE_ALL_INDICATOR = 4;
    private static final int AUTH_SUCCESS_PACKAGE_INDICATOR = 3;
    private static final String METHOD_UNBIND_FINGERPRINT = "unbind_fingerprint";
    private static final String PACKAGE_NAME = "packageName";
    private static final int SHARE_PREFERENCE_BACKUPRESTORE_INDICATOR = 12;
    private static final int SHARE_PREFERENCE_DB_BACKUPRESTORE_INDICATOR = 13;
    private static final String TAG = "AppLockProvider";
    private static final UriMatcher uriMatchers = new UriMatcher(-1);
    private int mRecoverFailedCount = 0;
    private int mRecoverSuccessCount = 0;

    public interface AuthSuccessPackageAllProvider {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AuthSuccessPackageProvider.CONTENT_URI, "all");
    }

    public interface AuthSuccessPackageProvider {
        public static final String COL_PACKAGE_NAME = "packageName";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AppLockProvider.AUTHORITY_URI, AppLockAuthSuccessTable.TABLE_NAME);
    }

    public interface BackupProvider {
        public static final Uri LOCK_STATUS_BACKUPRESTORE_URI = Uri.withAppendedPath(AppLockProvider.AUTHORITY_URI, "lockstatus_bkp");
        public static final Uri SHARE_PREF_BACKUPRESTORE_URI = Uri.withAppendedPath(AppLockProvider.AUTHORITY_URI, "sharepreference_bkp");
        public static final Uri SHARE_PREF_DB_BACKUPRESTORE_URI = Uri.withAppendedPath(AppLockProvider.AUTHORITY_URI, "sharepreference_db_bkp");
    }

    public interface FingerStatus {
        public static final int APPLOCK_BIND_STATE_CLOSE = 0;
        public static final int APPLOCK_BIND_STATE_OPEN = 1;
        public static final int APPLOCK_CANNOT_BIND_FINGERPRINT = -1;
        public static final String KEY_FINGERPRINT_BIND_TYPE = "fingerprintBindType";
    }

    public interface LockStatusProvider {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AppLockProvider.AUTHORITY_URI, AppLockStatusTable.TABLE_NAME);
    }

    public interface LockedPackageProvider {
        public static final String COL_PACKAGE_NAME = "packageName";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(LockStatusProvider.CONTENT_URI, "locked");
    }

    public interface PreferenceDataProvider {
        public static final Uri APPLOCK_PREFERENCE_DATA_URI = Uri.withAppendedPath(AppLockProvider.AUTHORITY_URI, "preferencedata");
    }

    public interface UnlockedPackageProvider {
        public static final String COL_PACKAGE_NAME = "packageName";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(LockStatusProvider.CONTENT_URI, "unlocked");
    }

    static {
        uriMatchers.addURI(AUTHORITY, AppLockStatusTable.TABLE_NAME, 0);
        uriMatchers.addURI(AUTHORITY, "applockstatus/locked", 1);
        uriMatchers.addURI(AUTHORITY, "applockstatus/unlocked", 2);
        uriMatchers.addURI(AUTHORITY, AppLockAuthSuccessTable.TABLE_NAME, 3);
        uriMatchers.addURI(AUTHORITY, "authsuccesspackage/all", 4);
        uriMatchers.addURI(AUTHORITY, "fingerprintstatus", 5);
        uriMatchers.addURI(AUTHORITY, "preferencedata", 6);
        uriMatchers.addURI(AUTHORITY, "lockstatus_bkp", 11);
        uriMatchers.addURI(AUTHORITY, "sharepreference_bkp", 12);
        uriMatchers.addURI(AUTHORITY, "sharepreference_db_bkp", 13);
    }

    public boolean onCreate() {
        return true;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (MultiUserUtils.isInMultiUserMode()) {
            return null;
        }
        AppLockDBHelper helper = AppLockDBHelper.getInstance(getContext().getApplicationContext());
        switch (uriMatchers.match(uri)) {
            case 0:
            case 11:
                return helper.queryAppLockData(AppLockStatusTable.TABLE_NAME, projection, selection, selectionArgs);
            case 1:
                return helper.queryAppLockData(LockedAppView.VIEW_NAME, projection, selection, selectionArgs);
            case 2:
                return helper.queryAppLockData(UnlockedAppView.VIEW_NAME, projection, selection, selectionArgs);
            case 3:
                return helper.queryAuthSuccessPackage(AppLockAuthSuccessTable.TABLE_NAME, projection, selection, selectionArgs);
            case 5:
                return createFingerprintStatusCursor();
            case 6:
                return helper.queryAppLockPreferenceData(AppLockPreferenceTable.TABLE_NAME, projection, selection, selectionArgs);
            case 12:
                HwLog.w(TAG, "Don't support backup File SharedPreference anymore!");
                return null;
            case 13:
                return helper.queryAppLockPreferenceData(BackupPrefView.VIEW_NAME, null, null, null);
            default:
                return null;
        }
    }

    public Uri insert(Uri uri, ContentValues values) {
        if (MultiUserUtils.isInMultiUserMode()) {
            return null;
        }
        AppLockDBHelper helper = AppLockDBHelper.getInstance(getContext().getApplicationContext());
        long rowId = -1;
        switch (uriMatchers.match(uri)) {
            case 0:
            case 11:
                rowId = helper.replaceLockStatus(values);
                break;
            case 3:
                rowId = helper.addAuthSuccessPackage(values);
                break;
            case 6:
            case 13:
                rowId = helper.updateAppLockPreferenceData(values);
                break;
            case 12:
                rowId = (long) restoreSharePreference(values);
                break;
            default:
                HwLog.e(TAG, "unsupport insert uri: " + uri.toString());
                break;
        }
        if (-1 == rowId) {
            HwLog.e(TAG, "insert failed");
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, rowId);
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (MultiUserUtils.isInMultiUserMode()) {
            return 0;
        }
        AppLockDBHelper helper = AppLockDBHelper.getInstance(getContext().getApplicationContext());
        switch (uriMatchers.match(uri)) {
            case 3:
                helper.deleteAuthSuccessPackage(selection, selectionArgs);
                break;
            case 4:
                helper.clearAuthSuccessPackage();
                break;
            default:
                HwLog.e(TAG, "unsupport delete uri: " + uri.toString());
                break;
        }
        return 0;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (MultiUserUtils.isInMultiUserMode()) {
            return 0;
        }
        AppLockDBHelper helper = AppLockDBHelper.getInstance(getContext().getApplicationContext());
        switch (uriMatchers.match(uri)) {
            case 0:
                if (-1 == helper.replaceLockStatus(values)) {
                    HwLog.e(TAG, "update faild!");
                    break;
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return 1;
            default:
                HwLog.e(TAG, "Only APP_LOCK_STATUS_INDICATOR is support for insert operation!");
                break;
        }
        return 0;
    }

    public int bulkInsert(Uri uri, ContentValues[] values) {
        if (MultiUserUtils.isInMultiUserMode()) {
            return 0;
        }
        AppLockDBHelper helper = AppLockDBHelper.getInstance(getContext().getApplicationContext());
        switch (uriMatchers.match(uri)) {
            case 0:
                int updateRows = helper.batchReplaceLockStatus(values);
                if (updateRows == 0) {
                    HwLog.w(TAG, "bulkInsert none row effected for APP_LOCK_STATUS_INDICATOR");
                    return 0;
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return updateRows;
            default:
                HwLog.w(TAG, "only APP_LOCK_STATUS_INDICATOR support bulk insert. Others use default");
                return super.bulkInsert(uri, values);
        }
    }

    public Bundle call(String method, String arg, Bundle extras) {
        if (method == null) {
            HwLog.d(TAG, "call invalid method");
            return null;
        } else if (method.equals(BackupConst.METHOND_BACKUP_QUERY)) {
            return backup_query();
        } else {
            if (method.equals(BackupConst.METHOND_BACKUP_RECOVER_START)) {
                return backup_recover_start(extras);
            }
            if (method.equals(BackupConst.METHOND_BACKUP_RECOVER_COMPLETE)) {
                return backup_recover_complete();
            }
            if (method.equals(METHOD_UNBIND_FINGERPRINT)) {
                return unbindFingerprint();
            }
            HwLog.w(TAG, "Invalid call method = " + method);
            return super.call(method, arg, extras);
        }
    }

    private Cursor createFingerprintStatusCursor() {
        HwLog.d(TAG, "createFingerprintQueryCursor");
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{FingerStatus.KEY_FINGERPRINT_BIND_TYPE});
        matrixCursor.addRow(new Object[]{Integer.valueOf(getFingerprintBindState())});
        return matrixCursor;
    }

    private int getFingerprintBindState() {
        if (AppLockPwdUtils.isPasswordSet(getContext())) {
            int bindState;
            if (FingerprintBindUtils.getFingerprintBindStatus(getContext())) {
                bindState = 1;
            } else {
                bindState = 0;
            }
            HwLog.d(TAG, "getFingerprintBindState result: " + bindState);
            return bindState;
        }
        HwLog.d(TAG, "getFingerprintBindState password not set yet!");
        return -1;
    }

    private int restoreSharePreference(ContentValues values) {
        return SPBackupRestoreUtils.restoreSharePreference(getContext(), values);
    }

    private Bundle backup_query() {
        Bundle bundle = new Bundle();
        bundle.putInt("version", AppLockDBHelper.getDatabaseVersion());
        ArrayList<String> uriList = new ArrayList();
        uriList.add(BackupProvider.LOCK_STATUS_BACKUPRESTORE_URI.toString());
        uriList.add(BackupProvider.SHARE_PREF_BACKUPRESTORE_URI.toString());
        uriList.add(BackupProvider.SHARE_PREF_DB_BACKUPRESTORE_URI.toString());
        bundle.putStringArrayList(BackupConst.BUNDLE_KEY_URI_LIST, uriList);
        bundle.putStringArrayList(BackupConst.BUNDLE_KEY_URI_LIST_COUNT, uriList);
        HwLog.d(TAG, "backup_query result: " + bundle);
        return bundle;
    }

    private Bundle backup_recover_start(Bundle extras) {
        try {
            Preconditions.checkArgument(extras != null, "call extras bundle is null");
            Bundle bundle = new Bundle();
            bundle.putBoolean(BackupConst.BUNDLE_KEY_PERMIT, canRecoverDB(extras.getInt("version"), AppLockDBHelper.getDatabaseVersion()).booleanValue());
            return bundle;
        } catch (IllegalArgumentException ex) {
            HwLog.e(TAG, "backup_recover_start catch IllegalArgumentException: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    private Bundle backup_recover_complete() {
        ReloadSwitchUtils.setApplicationListNeedReload(getContext());
        Bundle bundle = new Bundle();
        bundle.putInt(BackupConst.BUNDLE_KEY_SUCCEESS_COUNT, this.mRecoverSuccessCount);
        bundle.putInt(BackupConst.BUNDLE_KEY_FAIL_COUNT, this.mRecoverFailedCount);
        HwLog.d(TAG, String.format("backup_recover_complete: Success = %1$d, Failure = %2$d", new Object[]{Integer.valueOf(this.mRecoverSuccessCount), Integer.valueOf(this.mRecoverFailedCount)}));
        return bundle;
    }

    private Boolean canRecoverDB(int nRecoverVersion, int nCurrentVersion) {
        if (nRecoverVersion == 0 || nRecoverVersion > nCurrentVersion) {
            HwLog.e(TAG, "canRecoverDB is not permit recover operation. RecoverVersion: " + nRecoverVersion + ", CurrentVersion: " + nCurrentVersion);
            return Boolean.valueOf(false);
        }
        HwLog.d(TAG, String.format("backup_recover_start:Try to recover from version : %1$d, Current version : %2$d", new Object[]{Integer.valueOf(nRecoverVersion), Integer.valueOf(nCurrentVersion)}));
        this.mRecoverFailedCount = 0;
        this.mRecoverSuccessCount = 0;
        resetDataBeforeRecovery();
        return Boolean.valueOf(true);
    }

    private void resetDataBeforeRecovery() {
        if (!MultiUserUtils.isInMultiUserMode()) {
            AppLockDBHelper.getInstance(getContext()).resetLockStatusBeforeRecovery();
        }
    }

    private Bundle unbindFingerprint() {
        FingerprintBindUtils.setFingerprintBindStatus(getContext(), false);
        Bundle retBundle = new Bundle();
        retBundle.putInt(FingerStatus.KEY_FINGERPRINT_BIND_TYPE, 0);
        return retBundle;
    }
}
