package com.huawei.systemmanager.antivirus.engine.tencent.scan.impl;

import android.os.Handler;
import com.huawei.systemmanager.antivirus.engine.tencent.scan.GlobalScanTask;
import java.util.List;
import tmsdk.common.module.qscanner.QScanResultEntity;
import tmsdk.fg.module.qscanner.QScannerManagerV2;

public class NormalGlobalScanTask extends GlobalScanTask {
    public NormalGlobalScanTask(QScannerManagerV2 scanManager, Handler handler) {
        super(scanManager, handler);
        this.mIsCloud = false;
    }

    protected void onInstallScanProgress(int scanType, int progress, QScanResultEntity result) {
        if (progress >= 100) {
            progress = 99;
        }
        this.mProgress = progress;
        handleScanResult(scanType, this.mProgress, result, false);
    }

    protected void onInstallScanFinished(int scanType, List<QScanResultEntity> list) {
    }

    protected void onUninstallScanProgress(int scanType, int progress, QScanResultEntity result) {
        this.mProgress++;
        if (this.mProgress >= 100) {
            this.mProgress = 99;
        }
        handleScanResult(scanType, this.mProgress, result, true);
    }

    protected void onUninstallScanFinished(int scanType, List<QScanResultEntity> results) {
        handleScanFinished(scanType, results);
    }
}
