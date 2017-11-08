package com.huawei.systemmanager.antivirus.engine.avast.scan;

import android.content.Context;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Handler;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.comm.Storage.StorageHelper;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import java.util.ArrayList;
import java.util.List;

public abstract class GlobalScanTask extends AbsPkgScanTask {
    private ScanAllAppsAsyncTask mInstalledScanTask;
    private ScanDirectoryAsyncTask mUnInstalledScanTask;

    class CustomScanAllAppsAsyncTask extends ScanAllAppsAsyncTask {
        public CustomScanAllAppsAsyncTask(Context context) {
            super(context);
        }

        protected void onPreExecute() {
            GlobalScanTask.this.sendScanStartMsg(0);
        }

        protected void onPostExecute(Boolean result) {
            if (isCancelled()) {
                GlobalScanTask.this.sendScanCanceledMsg(0);
            } else if (result.booleanValue()) {
                if (GlobalScanTask.this.mUnInstalledScanTask == null || GlobalScanTask.this.mUnInstalledScanTask.getStatus() == Status.FINISHED) {
                    List<String> rootDir = new ArrayList();
                    rootDir.add(StorageHelper.getStorage().getInnerRootPath());
                    rootDir.addAll(StorageHelper.getStorage().getSdcardRootPath());
                    GlobalScanTask.this.mUnInstalledScanTask = new CustomScanDirectoryAsyncTask(GlobalContext.getContext(), rootDir);
                    GlobalScanTask.this.mUnInstalledScanTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Boolean[]{Boolean.valueOf(GlobalScanTask.this.mIsCloud)});
                }
            } else {
                GlobalScanTask.this.sendScanErrorMsg(0, 0);
            }
        }

        protected void onPreScanProgressUpdate(ScanProgress result) {
        }

        protected void onPostScanProgressUpdate(ScanProgress result) {
            GlobalScanTask.this.onInstallScanProgress(0, (result.mScannedObjects * 100) / result.mTotalObjectsToScan, result);
        }

        protected void onCancelled() {
            GlobalScanTask.this.sendScanCanceledMsg(0);
        }
    }

    private class CustomScanDirectoryAsyncTask extends ScanDirectoryAsyncTask {
        public CustomScanDirectoryAsyncTask(Context context) {
            super(context);
        }

        public CustomScanDirectoryAsyncTask(Context context, List<String> rootDirs) {
            super(context, rootDirs);
        }

        protected void onPreExecute() {
        }

        protected void onPostScanProgressUpdate(ScanDirectoryProgress result) {
            GlobalScanTask.this.onUninstallScanProgress(1, -1, result);
        }

        protected void onPostExecute(Boolean result) {
            if (isCancelled()) {
                GlobalScanTask.this.sendScanCanceledMsg(1);
            } else if (result.booleanValue()) {
                GlobalScanTask.this.onUninstallScanFinished(1, this.mCloudResults);
            } else {
                GlobalScanTask.this.sendScanErrorMsg(0, 0);
            }
        }

        protected void onCancelled() {
            GlobalScanTask.this.sendScanCanceledMsg(1);
        }
    }

    protected abstract void onInstallScanFinished(int i, List<ScanResultEntity> list);

    protected abstract void onInstallScanProgress(int i, int i2, ScanProgress scanProgress);

    protected abstract void onUninstallScanFinished(int i, List<ScanResultEntity> list);

    protected abstract void onUninstallScanProgress(int i, int i2, ScanDirectoryProgress scanDirectoryProgress);

    public GlobalScanTask(Handler handler) {
        this.mScanHandler = handler;
    }

    public void start() {
        if (this.mInstalledScanTask == null || this.mInstalledScanTask.getStatus() == Status.FINISHED) {
            this.mInstalledScanTask = new CustomScanAllAppsAsyncTask(GlobalContext.getContext());
            this.mInstalledScanTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Boolean[]{Boolean.valueOf(this.mIsCloud)});
        }
    }

    public void cancel() {
        cancelInstallScanTask();
        cancelUninstallScanTask();
    }

    private void cancelInstallScanTask() {
        if (this.mInstalledScanTask != null && this.mInstalledScanTask.getStatus() != Status.FINISHED) {
            this.mInstalledScanTask.cancel(true);
        }
    }

    private void cancelUninstallScanTask() {
        if (this.mUnInstalledScanTask != null && this.mUnInstalledScanTask.getStatus() != Status.FINISHED) {
            this.mUnInstalledScanTask.cancel(true);
        }
    }
}
