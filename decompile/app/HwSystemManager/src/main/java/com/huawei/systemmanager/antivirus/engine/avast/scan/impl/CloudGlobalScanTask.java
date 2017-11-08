package com.huawei.systemmanager.antivirus.engine.avast.scan.impl;

import android.os.Handler;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.antivirus.engine.avast.scan.GlobalScanTask;
import com.huawei.systemmanager.antivirus.engine.avast.scan.ScanDirectoryProgress;
import com.huawei.systemmanager.antivirus.engine.avast.scan.ScanProgress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudGlobalScanTask extends GlobalScanTask {
    private List<ScanResultEntity> mCloudResults;
    private Map<String, String> virusPaths;

    public CloudGlobalScanTask(Handler handler) {
        super(handler);
        this.mIsCloud = true;
        this.virusPaths = new HashMap();
        this.mCloudResults = new ArrayList();
    }

    protected void onInstallScanProgress(int scanType, int progress, ScanProgress result) {
        if (progress >= 100) {
            progress = 99;
        }
        this.mProgress = progress;
        handleScanResult(scanType, this.mProgress, result, false, this.virusPaths);
    }

    protected void onInstallScanFinished(int scanType, List<ScanResultEntity> results) {
        this.mCloudResults = results;
    }

    protected void onUninstallScanProgress(int scanType, int progress, ScanDirectoryProgress result) {
        this.mProgress++;
        if (this.mProgress >= 100) {
            this.mProgress = 99;
        }
        handleScanResult(scanType, this.mProgress, result, true, this.virusPaths);
    }

    protected void onUninstallScanFinished(int scanType, List<ScanResultEntity> results) {
        this.mCloudResults.addAll(results);
        for (ScanResultEntity entity : this.mCloudResults) {
            if (this.virusPaths.get(entity.apkFilePath) == null) {
                int i = this.mProgress + 1;
                this.mProgress = i;
                this.mProgress = i >= 100 ? 99 : this.mProgress;
                handleScanResult(scanType, this.mProgress, entity);
            }
        }
        handleUninstallScanFinished(scanType, results);
    }
}
