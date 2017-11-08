package com.huawei.systemmanager.antivirus.engine.tencent;

import android.os.Handler;
import android.os.Message;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.antivirus.engine.CustomizeVirusCheck;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom.HwCustTrashConst;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import tmsdk.common.module.qscanner.QScanAdPluginEntity;
import tmsdk.common.module.qscanner.QScanResultEntity;
import tmsdk.fg.module.qscanner.QScanListenerV2;

class QScanMutliPkgListenerWrapper extends QScanListenerV2 {
    private static final String TAG = "AntiVirusQScanListener";
    private static final String[] sWhiteList = new String[]{HwCustTrashConst.GALLERY_DEFAULT_PKG_NAME, "com.google.android.gms"};
    List<QScanResultEntity> mCachedresults;
    private int mGlobalProgress = 0;
    private Handler mHandler;
    private int mScanMode;

    QScanMutliPkgListenerWrapper(Handler handler, int scanMode) {
        this.mHandler = handler;
        this.mScanMode = scanMode;
    }

    public void onScanStarted(int scanType) {
        HwLog.d(TAG, "onScanStarted: scanType = " + scanType);
        if (scanType == 0) {
            Message msg = this.mHandler.obtainMessage(10);
            msg.arg2 = scanType;
            msg.sendToTarget();
        }
    }

    public void onScanProgress(int scanType, int progress, QScanResultEntity result) {
        boolean isUninstalled = false;
        HwLog.d(TAG, "onScanProgress: " + progress + ", pkg = " + result.packageName + ", type = " + result.type);
        if (!isInWhiteList(result.packageName)) {
            CustomizeVirusCheck.checkVirusByCloudConfig(result, false);
            printAdInfo(result);
            progress = correctProgress(progress);
            if (scanType != 0) {
                isUninstalled = true;
            }
            handleScanResult(scanType, progress, result, isUninstalled);
        }
    }

    public void onScanCanceled(int scanType) {
        HwLog.d(TAG, "onScanCanceled: scanType = " + scanType);
        Message msg = this.mHandler.obtainMessage(16);
        msg.arg2 = scanType;
        msg.sendToTarget();
    }

    public void onScanContinue(int scanType) {
        HwLog.d(TAG, "onScanContinue: scanType = " + scanType);
        Message msg = this.mHandler.obtainMessage(18);
        msg.arg2 = scanType;
        msg.sendToTarget();
    }

    public void onScanError(int scanType, int errCode) {
        HwLog.d(TAG, "onScanError: scanType = " + scanType + ", errCode = " + errCode);
        Message msg = this.mHandler.obtainMessage(14);
        msg.arg2 = scanType;
        msg.sendToTarget();
    }

    public void onScanFinished(int scanType, List<QScanResultEntity> results) {
        HwLog.d(TAG, "onScanFinished: scanType = " + scanType);
        if (this.mScanMode == 0) {
            handleScanFinished(scanType, results);
            return;
        }
        if (Utility.isNullOrEmptyList(this.mCachedresults)) {
            this.mCachedresults = results;
        } else {
            this.mCachedresults.addAll(results);
        }
        if (scanType != 0) {
            handleScanFinished(scanType, this.mCachedresults);
            this.mCachedresults = null;
        }
    }

    public void onScanPaused(int scanType) {
        HwLog.d(TAG, "onScanPaused: scanType = " + scanType);
        Message msg = this.mHandler.obtainMessage(17);
        msg.arg2 = scanType;
        msg.sendToTarget();
    }

    private void handleScanResult(int scanType, int progress, QScanResultEntity result, boolean isUninstalledApkFile) {
        ScanResultEntity resultEntity = new ScanResultEntity(result, isUninstalledApkFile);
        Message message = this.mHandler.obtainMessage(11);
        message.obj = resultEntity;
        message.arg1 = progress;
        message.arg2 = scanType;
        message.sendToTarget();
    }

    private void handleScanFinished(int scanType, List<QScanResultEntity> list) {
        Message message = this.mHandler.obtainMessage(12);
        message.arg2 = scanType;
        message.sendToTarget();
    }

    private int correctProgress(int progress) {
        if (this.mScanMode == 0) {
            return progress;
        }
        if (progress > 0) {
            progress = (progress * 80) / 100;
            this.mGlobalProgress = progress;
        } else {
            int i = this.mGlobalProgress + 1;
            this.mGlobalProgress = i;
            if (i < 100) {
                progress = this.mGlobalProgress;
            } else {
                progress = 99;
            }
        }
        return progress;
    }

    private void printAdInfo(QScanResultEntity result) {
        if (!Utility.isNullOrEmptyList(result.plugins)) {
            HwLog.d(TAG, "onScanProgress: Find Ad plugins, count = " + result.plugins.size());
            int nIndex = 0;
            for (QScanAdPluginEntity ad : result.plugins) {
                nIndex++;
                HwLog.i(TAG, nIndex + " : " + ad.name);
            }
        }
    }

    private static boolean isInWhiteList(String pkg) {
        String name = pkg.trim();
        for (String whiteName : sWhiteList) {
            if (name.equalsIgnoreCase(whiteName)) {
                return true;
            }
        }
        return false;
    }
}
