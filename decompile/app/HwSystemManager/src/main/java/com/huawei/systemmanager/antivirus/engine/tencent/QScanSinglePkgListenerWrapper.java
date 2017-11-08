package com.huawei.systemmanager.antivirus.engine.tencent;

import android.content.Context;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.antivirus.engine.CustomizeVirusCheck;
import com.huawei.systemmanager.antivirus.engine.IScanPackageMgr;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;
import com.huawei.systemmanager.securitythreats.comm.SecurityThreatsUtil;
import com.huawei.systemmanager.util.HwLog;
import tmsdk.common.module.qscanner.QScanResultEntity;
import tmsdk.fg.module.qscanner.QScanListenerV2;

class QScanSinglePkgListenerWrapper extends QScanListenerV2 {
    private static final String TAG = "AntiVirusQScanListener";
    private String mApkPath;
    private String mApkSource;
    private Context mContext;
    private boolean mFlag;
    private boolean mIsCloud;
    private boolean mIsInstalled;
    private String mPkgName;
    private QScanResultEntity mResult;
    private int mResultType;
    private IScanPackageMgr mScanMgr;

    QScanSinglePkgListenerWrapper(Context context, String pkgName, IScanPackageMgr scanMgr, boolean isCloud, boolean isInstalled, String path, String source) {
        this(context, pkgName, scanMgr, isCloud);
        this.mIsInstalled = isInstalled;
        this.mApkPath = path;
        this.mApkSource = source;
    }

    QScanSinglePkgListenerWrapper(Context context, String pkgName, IScanPackageMgr scanMgr, boolean isCloud) {
        this.mScanMgr = null;
        this.mIsCloud = false;
        this.mIsInstalled = true;
        this.mResultType = -1;
        this.mContext = context;
        this.mFlag = false;
        this.mPkgName = pkgName;
        this.mScanMgr = scanMgr;
        this.mIsCloud = isCloud;
    }

    QScanSinglePkgListenerWrapper(Context context, String pkgName) {
        this.mScanMgr = null;
        this.mIsCloud = false;
        this.mIsInstalled = true;
        this.mResultType = -1;
        this.mContext = context;
        this.mFlag = false;
        this.mPkgName = pkgName;
    }

    public void onScanProgress(int scanType, int progress, QScanResultEntity result) {
        if (this.mFlag) {
            HwLog.i(TAG, "we have scanned something for pkg: " + this.mPkgName);
            return;
        }
        this.mFlag = true;
        CustomizeVirusCheck.checkVirusByCloudConfig(result, false);
        handleScanResult(result);
    }

    private void handleScanResult(QScanResultEntity result) {
        boolean z = false;
        this.mResult = result;
        int type = 0;
        switch (result.type) {
            case 2:
                type = 1;
                break;
            case 3:
                type = 2;
                break;
        }
        HwLog.i(TAG, "handleScanResult type=" + type);
        this.mResultType = type;
        if (this.mScanMgr == null || !this.mIsCloud) {
            ScanResultEntity scanResult = new ScanResultEntity(result, false);
            if (type != 0 && this.mIsInstalled) {
                SecurityThreatsUtil.notifyNewInstallVirusToService(this.mContext, result.packageName, type);
                AntiVirusTools.refreshData(this.mContext, scanResult);
            } else if (type == 0 && this.mIsInstalled) {
                AntiVirusTools.refreshData(this.mContext, scanResult);
            }
        } else if (this.mIsInstalled || type == 0) {
            if (!this.mIsInstalled) {
                z = true;
            }
            ScanResultEntity entity = this.mScanMgr.scanPackage(new ScanResultEntity(result, z), this.mApkPath, this.mApkSource);
            HwLog.i(TAG, "handleScanResult tl type=" + entity.type);
            if (type == 0) {
                if (303 == entity.type) {
                    type = 1;
                } else if (AntiVirusTools.TYPE_VIRUS == entity.type) {
                    type = 2;
                }
            }
            if (type != 0 && this.mIsInstalled) {
                SecurityThreatsUtil.notifyNewInstallVirusToService(this.mContext, result.packageName, type);
                AntiVirusTools.refreshData(this.mContext, entity);
            } else if (type == 0 && this.mIsInstalled) {
                AntiVirusTools.refreshData(this.mContext, entity);
            }
            this.mResultType = type;
            this.mScanMgr = null;
        } else {
            HwLog.i(TAG, "handleScanResult uninstall and virus apk, directly return");
        }
    }

    public QScanResultEntity getResult() {
        return this.mResult;
    }

    public int getResulType() {
        return this.mResultType;
    }
}
