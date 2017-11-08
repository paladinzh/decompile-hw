package com.huawei.systemmanager.antivirus.engine.avast.scan;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.Message;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.antivirus.engine.avast.AvastScanResultBuilder;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.util.List;
import java.util.Map;

public class AbsPkgScanTask {
    protected static final int MAX_PROGRESS_LIMIT = 100;
    protected boolean mIsCloud = false;
    protected PackageManager mPackageManager = GlobalContext.getContext().getPackageManager();
    protected int mProgress = 0;
    protected Handler mScanHandler;

    protected void sendScanStartMsg(int scanType) {
        Message msg = this.mScanHandler.obtainMessage(10);
        msg.arg2 = scanType;
        msg.sendToTarget();
    }

    protected void sendScanCanceledMsg(int scanType) {
        Message msg = this.mScanHandler.obtainMessage(16);
        msg.arg2 = scanType;
        msg.sendToTarget();
    }

    protected void sendScanErrorMsg(int scanType, int errCode) {
        Message msg = this.mScanHandler.obtainMessage(14);
        msg.arg2 = scanType;
        msg.sendToTarget();
    }

    protected void sendScanContinueMsg(int scanType) {
        Message msg = this.mScanHandler.obtainMessage(18);
        msg.arg2 = scanType;
        msg.sendToTarget();
    }

    protected void sendScanPausedMsg(int scanType) {
        Message msg = this.mScanHandler.obtainMessage(17);
        msg.arg2 = scanType;
        msg.sendToTarget();
    }

    protected void handleScanResult(int scanType, int progress, ScanProgress result, boolean isUninstalledApkFile, Map<String, String> virusPaths) {
        try {
            ScanResultEntity resultEntity = AvastScanResultBuilder.parseScanResultEntity(PackageManagerWrapper.getPackageInfo(this.mPackageManager, result.mScannedPackageName, 0), result.mScanResult, isUninstalledApkFile);
            if (virusPaths != null && ScanResultEntity.isRiskORVirus(resultEntity)) {
                virusPaths.put(resultEntity.apkFilePath, resultEntity.apkFilePath);
            }
            Message message = this.mScanHandler.obtainMessage(11);
            message.obj = resultEntity;
            message.arg1 = progress;
            message.arg2 = scanType;
            message.sendToTarget();
        } catch (NameNotFoundException e) {
        }
    }

    protected void handleScanResult(int scanType, int progress, ScanResultEntity entity) {
        Message message = this.mScanHandler.obtainMessage(11);
        message.obj = entity;
        message.arg1 = progress;
        message.arg2 = scanType;
        message.sendToTarget();
    }

    protected void handleScanResult(int scanType, int progress, ScanDirectoryProgress result, boolean isUninstalledApkFile, Map<String, String> virusPaths) {
        PackageInfo packageInfo = this.mPackageManager.getPackageArchiveInfo(result.mPath, 1);
        if (packageInfo != null) {
            ScanResultEntity resultEntity = AvastScanResultBuilder.parseScanResultEntity(packageInfo, result.mPath, result.mResults, isUninstalledApkFile);
            if (virusPaths != null && ScanResultEntity.isRiskORVirus(resultEntity)) {
                virusPaths.put(resultEntity.apkFilePath, resultEntity.apkFilePath);
            }
            Message message = this.mScanHandler.obtainMessage(11);
            message.obj = resultEntity;
            message.arg1 = progress;
            message.arg2 = scanType;
            message.sendToTarget();
        }
    }

    protected void handleScanFinished(int scanType, List<ScanResultEntity> list) {
        Message message = this.mScanHandler.obtainMessage(12);
        message.arg2 = scanType;
        message.sendToTarget();
    }

    protected void handleUninstallScanFinished(int scanType, List<ScanResultEntity> list) {
        Message message = this.mScanHandler.obtainMessage(12);
        message.arg2 = scanType;
        message.sendToTarget();
    }
}
