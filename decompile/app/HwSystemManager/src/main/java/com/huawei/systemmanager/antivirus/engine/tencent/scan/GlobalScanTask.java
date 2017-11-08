package com.huawei.systemmanager.antivirus.engine.tencent.scan;

import android.os.Handler;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import tmsdk.common.module.qscanner.QScanResultEntity;
import tmsdk.fg.module.qscanner.QScanListenerV2;
import tmsdk.fg.module.qscanner.QScannerManagerV2;

public abstract class GlobalScanTask extends AbsPkgScanTask {
    private String TAG = "GlobalScanTask";
    protected QScanListenerV2 mInstallListener = new QScanListenerV2() {
        public void onScanStarted(int scanType) {
            HwLog.i(GlobalScanTask.this.TAG, "install onScanStarted,scantype=" + scanType);
            if (scanType == 0) {
                GlobalScanTask.this.sendScanStartMsg(scanType);
            }
        }

        public void onScanProgress(int scanType, int progress, QScanResultEntity result) {
            HwLog.i(GlobalScanTask.this.TAG, "install onScanProgress,scantype=" + scanType + ",progress=" + progress + ",type=" + result.type);
            GlobalScanTask.this.checkVirusByCloudConfig(result);
            GlobalScanTask.this.printAdInfo(result);
            GlobalScanTask.this.onInstallScanProgress(scanType, progress, result);
        }

        public void onScanCanceled(int scanType) {
            GlobalScanTask.this.sendScanCanceledMsg(scanType);
        }

        public void onScanContinue(int scanType) {
            GlobalScanTask.this.sendScanContinueMsg(scanType);
        }

        public void onScanError(int scanType, int errCode) {
            GlobalScanTask.this.sendScanErrorMsg(scanType, errCode);
        }

        public void onScanFinished(int scanType, List<QScanResultEntity> results) {
            HwLog.i(GlobalScanTask.this.TAG, "install onScanFinished,scantype=" + scanType + ",size=" + results.size());
            GlobalScanTask.this.onInstallScanFinished(scanType, results);
            GlobalScanTask.this.mScanerManager.scanUninstalledApks(GlobalScanTask.this.mUninstallListener, GlobalScanTask.this.mIsCloud);
        }

        public void onScanPaused(int scanType) {
            GlobalScanTask.this.sendScanPausedMsg(scanType);
        }
    };
    protected QScanListenerV2 mUninstallListener = new QScanListenerV2() {
        public void onScanStarted(int scanType) {
            HwLog.i(GlobalScanTask.this.TAG, "uninstall onScanStarted,scantype=" + scanType);
        }

        public void onScanProgress(int scanType, int progress, QScanResultEntity result) {
            if (progress <= 0 && !AbsPkgScanTask.isInWhiteList(result.packageName)) {
                GlobalScanTask.this.checkVirusByCloudConfig(result);
                GlobalScanTask.this.printAdInfo(result);
                GlobalScanTask.this.onUninstallScanProgress(scanType, progress, result);
            }
        }

        public void onScanCanceled(int scanType) {
            GlobalScanTask.this.sendScanCanceledMsg(scanType);
        }

        public void onScanContinue(int scanType) {
            GlobalScanTask.this.sendScanContinueMsg(scanType);
        }

        public void onScanError(int scanType, int errCode) {
            GlobalScanTask.this.sendScanErrorMsg(scanType, errCode);
        }

        public void onScanFinished(int scanType, List<QScanResultEntity> results) {
            HwLog.i(GlobalScanTask.this.TAG, "uninstall onScanFinished,scantype=" + scanType + ",size=" + results.size());
            GlobalScanTask.this.onUninstallScanFinished(scanType, results);
        }

        public void onScanPaused(int scanType) {
            GlobalScanTask.this.sendScanPausedMsg(scanType);
        }
    };

    protected abstract void onInstallScanFinished(int i, List<QScanResultEntity> list);

    protected abstract void onInstallScanProgress(int i, int i2, QScanResultEntity qScanResultEntity);

    protected abstract void onUninstallScanFinished(int i, List<QScanResultEntity> list);

    protected abstract void onUninstallScanProgress(int i, int i2, QScanResultEntity qScanResultEntity);

    public GlobalScanTask(QScannerManagerV2 scanManager, Handler handler) {
        this.mScanerManager = scanManager;
        this.mScanHandler = handler;
    }

    public void start() {
        this.mScanerManager.scanInstalledPackages(this.mInstallListener, this.mIsCloud);
    }
}
