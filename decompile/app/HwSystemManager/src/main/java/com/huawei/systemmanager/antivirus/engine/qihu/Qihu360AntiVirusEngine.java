package com.huawei.systemmanager.antivirus.engine.qihu;

import android.content.Context;
import android.os.Handler;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.antivirus.engine.IAntiVirusEngine;

public class Qihu360AntiVirusEngine implements IAntiVirusEngine {
    public boolean onInit(Context context) {
        return false;
    }

    public void onStartQuickScan(Context context, Handler handler, boolean doCloudScan) {
    }

    public void onStartGlobalScan(Context context, Handler handler, boolean doCloudScan) {
    }

    public void onPauseScan() {
    }

    public void onContinueScan() {
    }

    public void onCancelScan() {
    }

    public void onCheckUrl(String url, Handler handler) {
    }

    public ScanResultEntity onCheckInstalledApk(Context context, String pkgName, Handler handler, boolean doCloudScan) {
        return null;
    }

    public String onGetVirusLibVersion(Context context) {
        return null;
    }

    public void onCheckVirusLibVersion(Handler handler) {
    }

    public void onUpdateVirusLibVersion(Handler handler) {
    }

    public void onCancelCheckOrUpdate() {
    }

    public long onGetVirusLibTimeStamp() {
        return 0;
    }

    public void onFreeMemory() {
    }
}
