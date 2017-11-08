package com.huawei.systemmanager.backup;

import android.content.ContentProvider;
import android.net.Uri;
import android.os.Bundle;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;

public abstract class HsmContentProvider extends ContentProvider {
    private static final String TAG = "HsmContentProvider";
    private int mRecoverFailedCount = 0;
    private int mRecoverSucceedCount = 0;
    private int mRecoverVersion = 0;

    protected abstract boolean canRecoverDB(int i);

    protected abstract ArrayList<String> getBackupSupportedUriList();

    protected abstract int getDBVersion();

    protected abstract boolean onRecoverComplete(int i);

    protected abstract boolean onRecoverStart(int i);

    public Bundle call(String method, String arg, Bundle extras) {
        if (method == null) {
            HwLog.w(TAG, "Call method is null");
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
            HwLog.w(TAG, "call: Unknown call method = " + method);
            return super.call(method, arg, extras);
        }
    }

    protected Bundle backup_query(String arg, Bundle extras) {
        Bundle bundle = new Bundle();
        int curDBVersion = getDBVersion();
        bundle.putInt("version", curDBVersion);
        ArrayList<String> uriList = getBackupSupportedUriList();
        if (Utility.isNullOrEmptyList(uriList)) {
            HwLog.v(TAG, "backup_query: No backup uri , DB version = " + curDBVersion);
            return bundle;
        }
        bundle.putStringArrayList(BackupConst.BUNDLE_KEY_URI_LIST, uriList);
        bundle.putStringArrayList(BackupConst.BUNDLE_KEY_URI_LIST_COUNT, uriList);
        HwLog.v(TAG, "backup_query: DB version = " + curDBVersion + ", uriList size = " + uriList.size());
        return bundle;
    }

    protected Bundle backup_recover_start(String arg, Bundle extras) {
        Bundle bundle = new Bundle();
        if (extras == null) {
            bundle.putBoolean(BackupConst.BUNDLE_KEY_PERMIT, false);
            HwLog.w(TAG, "backup_recover_start: Invalid extras");
            return bundle;
        }
        int nRecoverVersion = extras.getInt("version");
        if (nRecoverVersion <= 0) {
            bundle.putBoolean(BackupConst.BUNDLE_KEY_PERMIT, false);
            HwLog.i(TAG, "backup_recover_start: Invalid recover version = " + nRecoverVersion);
            return bundle;
        } else if (!isRecoverSupported(nRecoverVersion)) {
            bundle.putBoolean(BackupConst.BUNDLE_KEY_PERMIT, false);
            HwLog.i(TAG, "backup_recover_start: Not supported , nRecoverVersion = " + nRecoverVersion);
            return bundle;
        } else if (onRecoverStart(nRecoverVersion)) {
            this.mRecoverVersion = nRecoverVersion;
            resetRecoverStats();
            bundle.putBoolean(BackupConst.BUNDLE_KEY_PERMIT, true);
            HwLog.i(TAG, "backup_recover_start: Supported, nRecoverVersion = " + nRecoverVersion);
            return bundle;
        } else {
            bundle.putBoolean(BackupConst.BUNDLE_KEY_PERMIT, false);
            HwLog.i(TAG, "backup_recover_start: Fail to start recover , nRecoverVersion = " + nRecoverVersion);
            return bundle;
        }
    }

    protected boolean isRecoverSupported(int nRecoverVersion) {
        if (nRecoverVersion <= getDBVersion()) {
            return canRecoverDB(nRecoverVersion);
        }
        HwLog.i(TAG, "isRecoverSupported: Can't recover from higher version : " + nRecoverVersion + ", Current version :" + getDBVersion());
        return false;
    }

    protected Bundle backup_recover_complete(String arg, Bundle extras) {
        Bundle bundle = new Bundle();
        if (!onRecoverComplete(this.mRecoverVersion)) {
            resetRecoverStats();
            HwLog.w(TAG, "backup_recover_complete: Failed");
        }
        bundle.putInt(BackupConst.BUNDLE_KEY_SUCCEESS_COUNT, getRecoverSucceedCount());
        bundle.putInt(BackupConst.BUNDLE_KEY_FAIL_COUNT, getRecoverFailedCount());
        this.mRecoverVersion = 0;
        resetRecoverStats();
        return bundle;
    }

    protected int getRecoverVersion() {
        return this.mRecoverVersion;
    }

    protected int getRecoverSucceedCount() {
        return this.mRecoverSucceedCount;
    }

    protected int getRecoverFailedCount() {
        return this.mRecoverFailedCount;
    }

    protected int increaseRecoverSucceedCount() {
        int i = this.mRecoverSucceedCount + 1;
        this.mRecoverSucceedCount = i;
        return i;
    }

    protected int increaseRecoverSucceedCount(int count) {
        this.mRecoverSucceedCount += count;
        return this.mRecoverSucceedCount;
    }

    protected int decreaseRecoverSucceedCount() {
        int i = this.mRecoverSucceedCount - 1;
        this.mRecoverSucceedCount = i;
        return i;
    }

    protected int decreaseRecoverSucceedCount(int count) {
        this.mRecoverSucceedCount -= count;
        if (this.mRecoverSucceedCount < 0) {
            this.mRecoverSucceedCount = 0;
        }
        return this.mRecoverSucceedCount;
    }

    protected int increaseRecoverFailedCount() {
        int i = this.mRecoverFailedCount + 1;
        this.mRecoverFailedCount = i;
        return i;
    }

    protected int increaseRecoverFailedCount(int count) {
        this.mRecoverFailedCount += count;
        return this.mRecoverFailedCount;
    }

    protected int decreaseRecoverFailedCount() {
        int i = this.mRecoverFailedCount - 1;
        this.mRecoverFailedCount = i;
        return i;
    }

    protected int decreaseRecoverFailedCount(int count) {
        this.mRecoverFailedCount -= count;
        if (this.mRecoverFailedCount < 0) {
            this.mRecoverFailedCount = 0;
        }
        return this.mRecoverFailedCount;
    }

    protected void resetRecoverStats() {
        this.mRecoverSucceedCount = 0;
        this.mRecoverFailedCount = 0;
    }

    protected void notifiChanged(Uri uri) {
        getContext().getContentResolver().notifyChange(uri, null);
    }
}
