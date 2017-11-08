package com.huawei.systemmanager.antivirus.engine.avast.scan.impl;

import android.os.Handler;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.antivirus.engine.avast.scan.QuickScanTask;
import com.huawei.systemmanager.antivirus.engine.avast.scan.ScanProgress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudQuickScanTask extends QuickScanTask {
    private Map<String, String> virusPaths;

    public CloudQuickScanTask(Handler handler) {
        super(handler);
        this.mIsCloud = true;
        this.virusPaths = new HashMap();
    }

    protected void onInstallScanProgress(int scanType, int progress, ScanProgress result) {
        if (progress >= 100) {
            progress = 99;
        }
        this.mProgress = progress;
        handleScanResult(scanType, this.mProgress, result, false, this.virusPaths);
    }

    protected void onInstallScanFinished(int scanType, List<ScanResultEntity> results) {
        for (ScanResultEntity entity : results) {
            if (this.virusPaths.get(entity.apkFilePath) == null) {
                int i = this.mProgress + 1;
                this.mProgress = i;
                this.mProgress = i >= 100 ? 99 : this.mProgress;
                handleScanResult(scanType, this.mProgress, entity);
            }
        }
        handleScanFinished(scanType, results);
    }
}
