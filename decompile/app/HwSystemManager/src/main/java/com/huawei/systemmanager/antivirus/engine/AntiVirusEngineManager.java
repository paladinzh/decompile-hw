package com.huawei.systemmanager.antivirus.engine;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;
import com.huawei.systemmanager.antivirus.engine.tencent.TencentAntiVirusEngine;
import com.huawei.systemmanager.util.HwLog;

public class AntiVirusEngineManager {
    private static final String TAG = "AntiVirusEngineManager";
    private static AntiVirusEngineManager mInstance;
    private boolean isUrlChecking = false;
    private IAntiVirusEngine mAntiVirusEngine = null;
    private Context mContext;

    private AntiVirusEngineManager(Context context) {
        this.mContext = context;
    }

    public static synchronized AntiVirusEngineManager getInstance(Context context) {
        AntiVirusEngineManager antiVirusEngineManager;
        synchronized (AntiVirusEngineManager.class) {
            if (mInstance == null) {
                mInstance = new AntiVirusEngineManager(context);
                mInstance.init();
            }
            antiVirusEngineManager = mInstance;
        }
        return antiVirusEngineManager;
    }

    public synchronized boolean init() {
        if (this.mAntiVirusEngine != null) {
            return true;
        }
        this.mAntiVirusEngine = new TencentAntiVirusEngine();
        if (this.mAntiVirusEngine.onInit(this.mContext)) {
            return true;
        }
        this.mAntiVirusEngine = null;
        HwLog.w(TAG, "init: Fail to init engine");
        return false;
    }

    public void startQuickScan(Context context, Handler handler, boolean doCloudScan) {
        if (this.mAntiVirusEngine != null) {
            HwLog.d(TAG, "start quick scan");
            this.mAntiVirusEngine.onStartQuickScan(context, handler, doCloudScan);
        }
    }

    public void startGlobalScan(Context context, Handler handler, boolean doCloudScan) {
        if (this.mAntiVirusEngine != null) {
            HwLog.d(TAG, "start global scan");
            this.mAntiVirusEngine.onStartGlobalScan(context, handler, doCloudScan);
        }
    }

    public void pauseScan() {
        if (this.mAntiVirusEngine != null) {
            HwLog.d(TAG, "pause scan");
            this.mAntiVirusEngine.onPauseScan();
        }
    }

    public void continueScan() {
        if (this.mAntiVirusEngine != null) {
            HwLog.d(TAG, "continue scan");
            this.mAntiVirusEngine.onContinueScan();
        }
    }

    public void cancelScan() {
        if (this.mAntiVirusEngine != null) {
            HwLog.d(TAG, "cancel scan");
            this.mAntiVirusEngine.onCancelScan();
        }
    }

    public void checkUrl(String url, Handler handler) {
        if (!(this.mAntiVirusEngine == null || this.isUrlChecking)) {
            HwLog.d(TAG, "check the url");
            this.isUrlChecking = true;
            this.mAntiVirusEngine.onCheckUrl(url, handler);
            this.isUrlChecking = false;
        }
    }

    public void checkInstalledApk(Context context, String pkgName, Handler handler, boolean doCloudScan) {
        if (this.mAntiVirusEngine != null) {
            HwLog.d(TAG, "scan the installed apk package name = " + pkgName);
            this.mAntiVirusEngine.onCheckInstalledApk(context, pkgName, handler, doCloudScan);
            ((NotificationManager) this.mContext.getSystemService("notification")).cancel(0);
        }
    }

    public String getVirusLibVersion(Context context) {
        if (this.mAntiVirusEngine == null) {
            return "";
        }
        HwLog.d(TAG, "get the virus lib version");
        return this.mAntiVirusEngine.onGetVirusLibVersion(context);
    }

    public void checkVirusLibVersion(Handler handler) {
        if (this.mAntiVirusEngine != null) {
            HwLog.d(TAG, "check the virus lib version");
            this.mAntiVirusEngine.onCheckVirusLibVersion(handler);
        }
    }

    public void updateVirusLibVersion(Handler handler) {
        if (this.mAntiVirusEngine != null) {
            HwLog.d(TAG, "update virus lib");
            this.mAntiVirusEngine.onUpdateVirusLibVersion(handler);
        }
    }

    public void cancelCheckOrUpdate() {
        if (this.mAntiVirusEngine != null) {
            HwLog.d(TAG, "cancel check or update the virus lib version");
            this.mAntiVirusEngine.onCancelCheckOrUpdate();
        }
    }

    public void freeMemory() {
        if (this.mAntiVirusEngine != null) {
            this.mAntiVirusEngine.onFreeMemory();
        }
    }
}
