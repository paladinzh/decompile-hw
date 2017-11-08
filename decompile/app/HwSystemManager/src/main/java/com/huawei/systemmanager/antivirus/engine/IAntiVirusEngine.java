package com.huawei.systemmanager.antivirus.engine;

import android.content.Context;
import android.os.Handler;
import com.huawei.systemmanager.antivirus.ScanResultEntity;

public interface IAntiVirusEngine {
    void onCancelCheckOrUpdate();

    void onCancelScan();

    ScanResultEntity onCheckInstalledApk(Context context, String str, Handler handler, boolean z);

    void onCheckUrl(String str, Handler handler);

    void onCheckVirusLibVersion(Handler handler);

    void onContinueScan();

    void onFreeMemory();

    long onGetVirusLibTimeStamp();

    String onGetVirusLibVersion(Context context);

    boolean onInit(Context context);

    void onPauseScan();

    void onStartGlobalScan(Context context, Handler handler, boolean z);

    void onStartQuickScan(Context context, Handler handler, boolean z);

    void onUpdateVirusLibVersion(Handler handler);
}
