package com.android.mms.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.util.Log;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.huawei.mms.util.HwCloudBackUpUtils;
import com.huawei.mms.util.ZipUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.ZipException;

public class MmsBackupContentProvider extends ContentProvider {
    private static final UriMatcher MATCHER = new UriMatcher(-1);
    private int mCurrentBackupVersion;

    static {
        MATCHER.addURI("com.android.mms.backup", "shared_pref", 1);
    }

    public Bundle call(String method, String arg, Bundle extras) {
        Log.d("Mms.Backup", "Mms.Backup call " + method);
        check();
        super.call(method, arg, extras);
        if ("backup_query".equals(method)) {
            return backupQuery(arg, extras);
        }
        if ("backup_recover_start".equals(method)) {
            return backupRecoverStart(arg, extras);
        }
        if (!"backup_recover_complete".equals(method)) {
            return null;
        }
        backupRecoverComplete(arg, extras);
        return null;
    }

    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        Log.d("Mms.Backup", "Mms.Backup openFile  mode " + mode + " uri " + uri);
        switch (MATCHER.match(uri)) {
            case 1:
                int openFileMode = getOpenFileMode(mode);
                if (openFileMode == 268435456) {
                    return backupSharedPreference();
                }
                if (openFileMode == 536870912) {
                    return restoreSharedPrefernece();
                }
                return null;
            default:
                return null;
        }
    }

    private ParcelFileDescriptor backupSharedPreference() {
        Log.d("Mms.Backup", "Mms.Backup backupSharedPreference");
        ParcelFileDescriptor parcelFileDescriptor = null;
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt(SmartSmsSdkUtil.SMARTSMS_UPDATE_TYPE, SmartSmsSdkUtil.getUpdateType(getContext())).commit();
        File appDataSharedDir = new File(HwCloudBackUpUtils.getPreferenceDirPath(getContext()));
        File backupZipFile = new File(getContext().getCacheDir(), "mms_backup.zip");
        File[] fileList = appDataSharedDir.listFiles();
        if (fileList == null) {
            return null;
        }
        try {
            ZipUtils.zipFiles(Arrays.asList(fileList), backupZipFile);
            parcelFileDescriptor = ParcelFileDescriptor.open(backupZipFile, 268435456);
        } catch (FileNotFoundException fileNotFountException) {
            Log.e("Mms.Backup", fileNotFountException.getMessage());
        } catch (IOException ioException) {
            Log.e("Mms.Backup", ioException.getMessage());
        }
        return parcelFileDescriptor;
    }

    private ParcelFileDescriptor restoreSharedPrefernece() {
        ParcelFileDescriptor parcelFileDescriptor = null;
        File backupZipFile = new File(getContext().getCacheDir(), "mms_backup.zip");
        if (backupZipFile.exists() && !backupZipFile.delete()) {
            return null;
        }
        try {
            if (backupZipFile.createNewFile()) {
                parcelFileDescriptor = ParcelFileDescriptor.open(backupZipFile, 536870912);
            }
        } catch (FileNotFoundException e) {
            Log.e("Mms.Backup", e.getMessage());
        } catch (IOException e2) {
            Log.e("Mms.Backup", e2.getMessage());
        }
        return parcelFileDescriptor;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public String getType(Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public boolean onCreate() {
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private void check() {
        int uid = Binder.getCallingUid();
        Context context = getContext();
        String rperm = getReadPermission();
        int pid = Binder.getCallingPid();
        if (rperm != null) {
            context.enforcePermission(rperm, pid, uid, "Permission Denial: reading " + getClass().getName() + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + rperm);
        }
    }

    private int getOpenFileMode(String mode) {
        int imode = 0;
        if (mode.contains("w")) {
            imode = 536870912;
        }
        if (mode.contains("r")) {
            imode |= 268435456;
        }
        if (mode.contains("+")) {
            return imode | 33554432;
        }
        return imode;
    }

    private Bundle backupQuery(String arg, Bundle extras) {
        Log.d("Mms.Backup", "Mms.Backup backupQuery ");
        ArrayList<String> backupOpenFileUriList = new ArrayList();
        backupOpenFileUriList.add("content://com.android.mms.backup/shared_pref");
        Bundle result = new Bundle();
        int dataVersion = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("pref_key_mms_preference_backup_version", 2);
        int dataVersion2 = dataVersion + 1;
        this.mCurrentBackupVersion = dataVersion;
        result.putInt(NumberInfo.VERSION_KEY, dataVersion2);
        result.putStringArrayList("openfile_uri_list", backupOpenFileUriList);
        Log.d("Mms.Backup", "Mms.Backup backupOpenFileUriList " + backupOpenFileUriList);
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("pref_key_mms_preference_backup_version", this.mCurrentBackupVersion).commit();
        return result;
    }

    private Bundle backupRecoverStart(String arg, Bundle extras) {
        boolean isPermitted = false;
        Log.d("Mms.Backup", "Mms.Backup backupRecoverStart ");
        if (extras == null) {
            Log.w("Mms.Backup", "Caller intent to recover notes' data, while the extas send is null");
            return null;
        }
        Bundle result = new Bundle();
        ArrayList<String> recoveryList = new ArrayList();
        int restoreDataversion = extras.getInt(NumberInfo.VERSION_KEY, 2);
        if (restoreDataversion > PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("pref_key_mms_preference_backup_version", 0)) {
            isPermitted = true;
        }
        if (isPermitted) {
            recoveryList.add("content://com.android.mms.backup/shared_pref");
            this.mCurrentBackupVersion = restoreDataversion;
        }
        result.putBoolean("permit", isPermitted);
        result.putStringArrayList("uri_list", recoveryList);
        Log.d("Mms.Backup", "Mms.Backup backupRecoverStart  recoveryList " + recoveryList);
        return result;
    }

    private Bundle backupRecoverComplete(String arg, Bundle extras) {
        Log.d("Mms.Backup", "Mms.Backup backupRecoverComplete ");
        File restoredZipFile = new File(getContext().getCacheDir(), "mms_backup.zip");
        File unzipBackupSharedDir = new File(HwCloudBackUpUtils.getMmsPreferenceRestoreUnzipDir(getContext()));
        boolean dirResult = false;
        if (unzipBackupSharedDir.exists() && unzipBackupSharedDir.isDirectory()) {
            ZipUtils.deleteDir(unzipBackupSharedDir);
        }
        if (!unzipBackupSharedDir.exists()) {
            dirResult = unzipBackupSharedDir.mkdir();
        }
        if (dirResult && restoredZipFile.exists()) {
            try {
                ZipUtils.unZipFile(restoredZipFile, unzipBackupSharedDir.getAbsolutePath());
                HwCloudBackUpUtils.restoreBackupXmlToSharedPreference(getContext());
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("pref_key_mms_preference_backup_version", this.mCurrentBackupVersion).commit();
            } catch (ZipException e) {
                Log.e("Mms.Backup", e.getMessage());
            } catch (IOException e2) {
                Log.e("Mms.Backup", e2.getMessage());
            }
        }
        return null;
    }
}
