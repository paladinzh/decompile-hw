package com.huawei.systemmanager.antivirus.engine.tencent.scan;

import android.os.Handler;
import java.util.List;
import tmsdk.common.module.qscanner.QScanResultEntity;
import tmsdk.fg.module.qscanner.QScanListenerV2;
import tmsdk.fg.module.qscanner.QScannerManagerV2;

public abstract class QuickScanTask extends AbsPkgScanTask {
    protected QScanListenerV2 mListener = new QScanListenerV2() {
        public void onScanStarted(int scanType) {
            if (scanType == 0) {
                QuickScanTask.this.sendScanStartMsg(scanType);
            }
        }

        public void onScanProgress(int scanType, int progress, QScanResultEntity result) {
            QuickScanTask.this.checkVirusByCloudConfig(result);
            QuickScanTask.this.printAdInfo(result);
            QuickScanTask.this.onInstallScanProgress(scanType, progress, result);
        }

        public void onScanCanceled(int scanType) {
            QuickScanTask.this.sendScanCanceledMsg(scanType);
        }

        public void onScanContinue(int scanType) {
            QuickScanTask.this.sendScanContinueMsg(scanType);
        }

        public void onScanError(int scanType, int errCode) {
            QuickScanTask.this.sendScanErrorMsg(scanType, errCode);
        }

        public void onScanFinished(int scanType, List<QScanResultEntity> results) {
            QuickScanTask.this.onInstallScanFinished(scanType, results);
        }

        public void onScanPaused(int scanType) {
            QuickScanTask.this.sendScanPausedMsg(scanType);
        }
    };

    protected abstract void onInstallScanFinished(int i, List<QScanResultEntity> list);

    protected abstract void onInstallScanProgress(int i, int i2, QScanResultEntity qScanResultEntity);

    public QuickScanTask(QScannerManagerV2 scanManager, Handler handler) {
        this.mScanerManager = scanManager;
        this.mScanHandler = handler;
    }

    public void start() {
        this.mScanerManager.scanInstalledPackages(this.mListener, this.mIsCloud);
    }
}
