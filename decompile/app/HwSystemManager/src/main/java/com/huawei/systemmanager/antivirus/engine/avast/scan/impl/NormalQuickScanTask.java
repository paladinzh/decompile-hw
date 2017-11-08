package com.huawei.systemmanager.antivirus.engine.avast.scan.impl;

import android.os.Handler;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.antivirus.engine.avast.scan.QuickScanTask;
import com.huawei.systemmanager.antivirus.engine.avast.scan.ScanProgress;
import java.util.List;

public class NormalQuickScanTask extends QuickScanTask {
    public NormalQuickScanTask(Handler handler) {
        super(handler);
        this.mIsCloud = false;
    }

    protected void onInstallScanProgress(int scanType, int progress, ScanProgress result) {
        handleScanResult(scanType, progress, result, false, null);
    }

    protected void onInstallScanFinished(int scanType, List<ScanResultEntity> results) {
        handleScanFinished(scanType, results);
    }
}
