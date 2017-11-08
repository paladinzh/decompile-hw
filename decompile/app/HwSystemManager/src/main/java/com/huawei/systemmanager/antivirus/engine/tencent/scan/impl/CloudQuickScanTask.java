package com.huawei.systemmanager.antivirus.engine.tencent.scan.impl;

import android.content.Context;
import android.os.AsyncTask.Status;
import android.os.Handler;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.antivirus.engine.tencent.CommonUtils;
import com.huawei.systemmanager.antivirus.engine.tencent.scan.QuickScanTask;
import com.huawei.systemmanager.antivirus.engine.trustlook.CloudQuickScanAyncTask;
import com.huawei.systemmanager.antivirus.statistics.AntivirusStatsUtils;
import com.huawei.systemmanager.antivirus.statistics.VirusInfoBuilder;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import tmsdk.common.module.qscanner.QScanResultEntity;
import tmsdk.fg.module.qscanner.QScannerManagerV2;

public class CloudQuickScanTask extends QuickScanTask {
    private static final String TAG = "CloudQuickScanTask";
    private AtomicBoolean mIsFinishWork;
    private CloudQuickScanAyncTask mScanCloudTask;
    private Set<String> mSets;
    private VirusInfoBuilder mTrustLookBuilder;

    private class CustomScanCloudTask extends CloudQuickScanAyncTask {
        protected CustomScanCloudTask(Context context) {
            super(context);
        }

        protected void onPostExecute(Boolean paramBoolean) {
            this.mIsFinishScan.set(true);
            CloudQuickScanTask.this.checkAndSendResult(isCancelled());
        }
    }

    public CloudQuickScanTask(QScannerManagerV2 scanManager, Handler handler) {
        super(scanManager, handler);
        this.mScanCloudTask = null;
        this.mIsFinishWork = new AtomicBoolean(false);
        this.mIsCloud = true;
        this.mSets = new HashSet();
        this.mTrustLookBuilder = new VirusInfoBuilder(AntivirusStatsUtils.VENDOR_TRUSTLOOK);
    }

    public void start() {
        if (this.mScanCloudTask == null || this.mScanCloudTask.getStatus() == Status.FINISHED) {
            this.mScanCloudTask = new CustomScanCloudTask(GlobalContext.getContext());
            this.mScanCloudTask.execute(new Void[0]);
        }
        super.start();
    }

    public void cancel(int scanType) {
        if (this.mScanCloudTask != null && this.mScanCloudTask.getStatus() != Status.FINISHED) {
            this.mScanCloudTask.cancel(true);
        }
    }

    protected void onInstallScanProgress(int scanType, int progress, QScanResultEntity result) {
        this.mTecentBuilder.increaseCloudScanCount();
        if (progress >= 100) {
            progress = 99;
        }
        this.mProgress = progress;
        if (CommonUtils.isTecentTellDangerApk(result)) {
            this.mSets.add(result.packageName);
        }
        handleScanResult(scanType, this.mProgress, result, false);
    }

    protected void onInstallScanFinished(int scanType, List<QScanResultEntity> results) {
        Set<String> filter = new HashSet();
        for (QScanResultEntity entity : results) {
            if (CommonUtils.isRiskOrVirus(entity)) {
                this.mTecentBuilder.increaseVirusScanCount();
                AntivirusStatsUtils.reportScanVirusInfo(entity.packageName, entity.version, entity.dexSha1, AntivirusStatsUtils.VENDOR_TENCENT);
            }
            if (entity.type == 0) {
                this.mTecentBuilder.increaseUnKnownCount();
            }
            filter.add(entity.packageName);
            if (!CommonUtils.isFindAPK(this.mSets, entity.packageName)) {
                incProgress();
                handleScanResult(scanType, this.mProgress, entity, false);
            }
        }
        this.mSets.addAll(filter);
        AntivirusStatsUtils.reportScanCloudCount(this.mTecentBuilder);
        this.mIsFinishWork.set(true);
        checkAndSendResult(false);
    }

    private void checkAndSendResult(boolean isCanceled) {
        if (!isCanceled) {
            if (this.mScanCloudTask.isCompleted() && this.mIsFinishWork.get()) {
                List<ScanResultEntity> results = this.mScanCloudTask.getResult();
                if (results == null) {
                    handleScanFinished(0, null);
                    return;
                }
                for (ScanResultEntity entity : results) {
                    this.mTrustLookBuilder.increaseCloudScanCount();
                    if (ScanResultEntity.isRiskORVirus(entity)) {
                        this.mTrustLookBuilder.increaseVirusScanCount();
                        AntivirusStatsUtils.reportScanVirusInfo(entity.packageName, entity.virusName, entity.mHash, AntivirusStatsUtils.VENDOR_TRUSTLOOK);
                    }
                    if (entity.type == 302) {
                        this.mTrustLookBuilder.increaseUnKnownCount();
                    }
                    if (!CommonUtils.isFindAPK(this.mSets, entity.packageName)) {
                        incProgress();
                        handleScanResult(0, this.mProgress, entity, false);
                    }
                }
                this.mSets.clear();
                AntivirusStatsUtils.reportScanCloudCount(this.mTrustLookBuilder);
                handleScanFinished(0, null);
                return;
            }
            HwLog.i(TAG, "task is not completed, skip");
        }
    }
}
