package com.huawei.systemmanager.antivirus.engine.avast.scan.impl;

import android.os.Handler;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.antivirus.engine.avast.scan.GlobalScanTask;
import com.huawei.systemmanager.antivirus.engine.avast.scan.ScanDirectoryProgress;
import com.huawei.systemmanager.antivirus.engine.avast.scan.ScanProgress;
import java.util.List;

public class NormalGlobalScanTask extends GlobalScanTask {
    public NormalGlobalScanTask(Handler handler) {
        super(handler);
        this.mIsCloud = false;
    }

    protected void onInstallScanProgress(int scanType, int progress, ScanProgress result) {
        if (progress >= 100) {
            progress = 99;
        }
        this.mProgress = progress;
        handleScanResult(scanType, this.mProgress, result, false, null);
    }

    protected void onInstallScanFinished(int scanType, List<ScanResultEntity> list) {
    }

    protected void onUninstallScanProgress(int scanType, int progress, ScanDirectoryProgress result) {
        this.mProgress++;
        if (this.mProgress >= 100) {
            this.mProgress = 99;
        }
        handleScanResult(scanType, this.mProgress, result, true, null);
    }

    protected void onUninstallScanFinished(int scanType, List<ScanResultEntity> results) {
        handleUninstallScanFinished(scanType, results);
    }
}
