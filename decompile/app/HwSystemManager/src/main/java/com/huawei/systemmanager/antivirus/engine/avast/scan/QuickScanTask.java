package com.huawei.systemmanager.antivirus.engine.avast.scan;

import android.content.Context;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Handler;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import java.util.List;

public abstract class QuickScanTask extends AbsPkgScanTask {
    private ScanAllAppsAsyncTask mScanTask;

    class CustomScanAllAppsAsyncTask extends ScanAllAppsAsyncTask {
        public CustomScanAllAppsAsyncTask(Context context) {
            super(context);
        }

        protected void onPreExecute() {
            QuickScanTask.this.sendScanStartMsg(0);
        }

        protected void onPostExecute(Boolean result) {
            if (isCancelled()) {
                QuickScanTask.this.sendScanCanceledMsg(0);
            } else if (result.booleanValue()) {
                QuickScanTask.this.onInstallScanFinished(0, this.mCloudResults);
            } else {
                QuickScanTask.this.sendScanErrorMsg(0, 0);
            }
        }

        protected void onPreScanProgressUpdate(ScanProgress result) {
        }

        protected void onPostScanProgressUpdate(ScanProgress result) {
            if (result.mTotalObjectsToScan != 0) {
                QuickScanTask.this.onInstallScanProgress(0, (result.mScannedObjects * 100) / result.mTotalObjectsToScan, result);
            }
        }

        protected void onCancelled() {
            QuickScanTask.this.sendScanCanceledMsg(0);
        }
    }

    protected abstract void onInstallScanFinished(int i, List<ScanResultEntity> list);

    protected abstract void onInstallScanProgress(int i, int i2, ScanProgress scanProgress);

    public QuickScanTask(Handler handler) {
        this.mScanHandler = handler;
    }

    public void start() {
        if (this.mScanTask == null || this.mScanTask.getStatus() == Status.FINISHED) {
            this.mScanTask = new CustomScanAllAppsAsyncTask(GlobalContext.getContext());
            this.mScanTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Boolean[]{Boolean.valueOf(this.mIsCloud)});
        }
    }

    public void cancel() {
        if (this.mScanTask != null && this.mScanTask.getStatus() != Status.FINISHED) {
            this.mScanTask.cancel(true);
        }
    }
}
