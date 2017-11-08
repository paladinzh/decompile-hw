package com.huawei.systemmanager.antivirus.engine.tencent.scan.impl;

import android.content.Context;
import android.os.AsyncTask.Status;
import android.os.Handler;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.antivirus.engine.tencent.CommonUtils;
import com.huawei.systemmanager.antivirus.engine.tencent.scan.GlobalScanTask;
import com.huawei.systemmanager.antivirus.engine.trustlook.CloudGlobalScanAyncTask;
import com.huawei.systemmanager.antivirus.statistics.AntivirusStatsUtils;
import com.huawei.systemmanager.antivirus.statistics.VirusInfoBuilder;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import tmsdk.common.module.qscanner.QScanResultEntity;
import tmsdk.fg.module.qscanner.QScannerManagerV2;

public class CloudGlobalScanTask extends GlobalScanTask {
    private static final String TAG = "CloudGlobalScanTask";
    private List<QScanResultEntity> mInstallScanResults;
    private Set<String> mInstallSets;
    private AtomicBoolean mIsFinishWork;
    private CustomScanCloudTask mScanCloudTask;
    private VirusInfoBuilder mTrustLookBuilder;
    private Set<String> mUnInstallSets;

    private class CustomScanCloudTask extends CloudGlobalScanAyncTask {
        public CustomScanCloudTask(Context context) {
            super(context);
        }

        protected void onPostExecute(Boolean paramBoolean) {
            this.mIsFinishScan.set(true);
            CloudGlobalScanTask.this.checkAndSendMessages(isCancelled());
        }
    }

    public CloudGlobalScanTask(QScannerManagerV2 scanManager, Handler handler) {
        super(scanManager, handler);
        this.mScanCloudTask = null;
        this.mIsFinishWork = new AtomicBoolean(false);
        this.mIsCloud = true;
        this.mInstallSets = new HashSet();
        this.mUnInstallSets = new HashSet();
        this.mInstallScanResults = new ArrayList();
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
        if (progress >= 100) {
            progress = 99;
        }
        this.mProgress = progress;
        if (CommonUtils.isTecentTellDangerApk(result)) {
            this.mInstallSets.add(result.path);
        }
        handleScanResult(scanType, this.mProgress, result, false);
        this.mTecentBuilder.increaseCloudScanCount();
    }

    protected void onInstallScanFinished(int scanType, List<QScanResultEntity> results) {
        this.mInstallScanResults.addAll(results);
    }

    protected void onUninstallScanProgress(int scanType, int progress, QScanResultEntity result) {
        incProgress();
        if (CommonUtils.isTecentTellDangerApk(result)) {
            this.mUnInstallSets.add(result.path);
        }
        handleScanResult(scanType, this.mProgress, result, true);
        this.mTecentBuilder.increaseCloudScanCount();
    }

    protected void onUninstallScanFinished(int scanType, List<QScanResultEntity> results) {
        handCloudResults(0, this.mInstallScanResults, this.mInstallSets, false);
        handCloudResults(1, results, this.mUnInstallSets, true);
        AntivirusStatsUtils.reportScanCloudCount(this.mTecentBuilder);
        this.mIsFinishWork.set(true);
        checkAndSendMessages(false);
    }

    private void checkAndSendMessages(boolean isCanceled) {
        if (!isCanceled) {
            if (this.mIsFinishWork.get() && this.mScanCloudTask.isCompleted()) {
                List<ScanResultEntity> results = this.mScanCloudTask.getResults();
                this.mTrustLookBuilder.setCloudScanCount((long) results.size());
                for (ScanResultEntity entity : results) {
                    calculateAndSendMessage(entity, entity.isUninstalledApk ? this.mUnInstallSets : this.mInstallSets);
                }
                AntivirusStatsUtils.reportScanCloudCount(this.mTrustLookBuilder);
                handleScanFinished(1, null);
                return;
            }
            HwLog.i(TAG, "task is not completed, skip");
        }
    }

    private void calculateAndSendMessage(ScanResultEntity entity, Set<String> sets) {
        if (ScanResultEntity.isRiskORVirus(entity)) {
            this.mTrustLookBuilder.increaseVirusScanCount();
            AntivirusStatsUtils.reportScanVirusInfo(entity.packageName, entity.virusName, entity.mHash, AntivirusStatsUtils.VENDOR_TRUSTLOOK);
        }
        if (entity.type == 302) {
            this.mTrustLookBuilder.increaseUnKnownCount();
        }
        if (!CommonUtils.isFindAPK(sets, entity.apkFilePath)) {
            incProgress();
            handleScanResult(2, getProgress(), entity, entity.isUninstalledApk);
        }
    }

    private void handCloudResults(int scanType, List<QScanResultEntity> results, Set<String> sets, boolean isUninstalled) {
        for (QScanResultEntity entity : results) {
            if (CommonUtils.isRiskOrVirus(entity)) {
                this.mTecentBuilder.increaseVirusScanCount();
                AntivirusStatsUtils.reportScanVirusInfo(entity.packageName, entity.version, entity.dexSha1, AntivirusStatsUtils.VENDOR_TENCENT);
            }
            if (entity.type == 0) {
                this.mTecentBuilder.increaseUnKnownCount();
            }
            if (!CommonUtils.isFindAPK(sets, entity.path)) {
                incProgress();
                handleScanResult(scanType, this.mProgress, entity, isUninstalled);
            }
            sets.add(entity.path);
        }
    }
}
