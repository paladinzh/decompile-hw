package com.huawei.systemmanager.antivirus.engine.tencent.scan;

import android.os.Handler;
import android.os.Message;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.antivirus.engine.CustomizeVirusCheck;
import com.huawei.systemmanager.antivirus.statistics.AntivirusStatsUtils;
import com.huawei.systemmanager.antivirus.statistics.VirusInfoBuilder;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom.HwCustTrashConst;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import java.util.List;
import tmsdk.common.module.qscanner.QScanAdPluginEntity;
import tmsdk.common.module.qscanner.QScanResultEntity;
import tmsdk.fg.module.qscanner.QScannerManagerV2;

public class AbsPkgScanTask {
    protected static final int MAX_PROGRESS_LIMIT = 100;
    static final String[] sWhiteList = new String[]{HwCustTrashConst.GALLERY_DEFAULT_PKG_NAME, "com.google.android.gms", "com.google.android.gsf.login", "com.huawei.android.totemweather"};
    private String TAG = "AbsPkgScanTask";
    protected boolean mIsCloud;
    protected int mProgress = 0;
    protected Handler mScanHandler;
    protected QScannerManagerV2 mScanerManager;
    protected VirusInfoBuilder mTecentBuilder = new VirusInfoBuilder(AntivirusStatsUtils.VENDOR_TENCENT);

    protected void sendScanStartMsg(int scanType) {
        Message msg = this.mScanHandler.obtainMessage(10);
        msg.arg2 = scanType;
        msg.sendToTarget();
    }

    protected void sendScanCanceledMsg(int scanType) {
        cancel(scanType);
        Message msg = this.mScanHandler.obtainMessage(16);
        msg.arg2 = scanType;
        msg.sendToTarget();
    }

    protected void sendScanContinueMsg(int scanType) {
        Message msg = this.mScanHandler.obtainMessage(18);
        msg.arg2 = scanType;
        msg.sendToTarget();
    }

    protected void sendScanErrorMsg(int scanType, int errCode) {
        Message msg = this.mScanHandler.obtainMessage(14);
        msg.arg2 = scanType;
        msg.sendToTarget();
    }

    protected void sendScanPausedMsg(int scanType) {
        Message msg = this.mScanHandler.obtainMessage(17);
        msg.arg2 = scanType;
        msg.sendToTarget();
    }

    protected void handleScanResult(int scanType, int progress, QScanResultEntity result, boolean isUninstalledApkFile) {
        handleScanResult(scanType, progress, new ScanResultEntity(result, isUninstalledApkFile), isUninstalledApkFile);
    }

    public void handleScanResult(int scanType, int progress, ScanResultEntity result, boolean isUninstalledApkFile) {
        if (!isUninstalledApkFile && isSystemApp(result.packageName)) {
            result.type = 301;
        }
        Message message = this.mScanHandler.obtainMessage(11);
        message.obj = result;
        message.arg1 = progress;
        message.arg2 = scanType;
        message.sendToTarget();
    }

    public void handleScanFinished(int scanType, List<QScanResultEntity> list) {
        Message message = this.mScanHandler.obtainMessage(12);
        message.arg2 = scanType;
        message.sendToTarget();
    }

    protected void printAdInfo(QScanResultEntity result) {
        if (!Utility.isNullOrEmptyList(result.plugins)) {
            int nIndex = 0;
            for (QScanAdPluginEntity ad : result.plugins) {
                nIndex++;
                HwLog.i(this.TAG, nIndex + " : " + ad.name);
            }
        }
    }

    protected static boolean isInWhiteList(String pkg) {
        String name = pkg.trim();
        for (String whiteName : sWhiteList) {
            if (name.equalsIgnoreCase(whiteName)) {
                return true;
            }
        }
        return false;
    }

    protected static boolean isSystemApp(String pkg) {
        return HsmPackageManager.getInstance().isSystem(pkg);
    }

    protected void checkVirusByCloudConfig(QScanResultEntity result) {
        CustomizeVirusCheck.checkVirusByCloudConfig(result, true);
    }

    public int getProgress() {
        return this.mProgress;
    }

    public void incProgress() {
        this.mProgress++;
        if (this.mProgress >= 100) {
            this.mProgress = 99;
        }
    }

    public boolean isCompleted(int scanType) {
        return false;
    }

    public void cancel(int scanType) {
    }
}
