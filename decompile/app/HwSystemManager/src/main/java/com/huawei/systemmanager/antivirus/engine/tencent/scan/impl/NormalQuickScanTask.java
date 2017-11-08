package com.huawei.systemmanager.antivirus.engine.tencent.scan.impl;

import android.os.Handler;
import com.huawei.systemmanager.antivirus.engine.tencent.scan.QuickScanTask;
import java.util.List;
import tmsdk.common.module.qscanner.QScanResultEntity;
import tmsdk.fg.module.qscanner.QScannerManagerV2;

public class NormalQuickScanTask extends QuickScanTask {
    public NormalQuickScanTask(QScannerManagerV2 scanManager, Handler handler) {
        super(scanManager, handler);
        this.mIsCloud = false;
    }

    protected void onInstallScanProgress(int scanType, int progress, QScanResultEntity result) {
        handleScanResult(scanType, progress, result, false);
    }

    protected void onInstallScanFinished(int scanType, List<QScanResultEntity> results) {
        handleScanFinished(scanType, results);
    }
}
