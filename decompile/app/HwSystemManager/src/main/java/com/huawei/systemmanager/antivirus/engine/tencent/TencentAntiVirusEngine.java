package com.huawei.systemmanager.antivirus.engine.tencent;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.antivirus.engine.IAntiVirusEngine;
import com.huawei.systemmanager.antivirus.engine.IScanPackageMgr;
import com.huawei.systemmanager.antivirus.engine.ScanPackageMgrFactory;
import com.huawei.systemmanager.antivirus.engine.tencent.scan.GlobalScanTask;
import com.huawei.systemmanager.antivirus.engine.tencent.scan.QuickScanTask;
import com.huawei.systemmanager.antivirus.engine.tencent.scan.impl.CloudGlobalScanTask;
import com.huawei.systemmanager.antivirus.engine.tencent.scan.impl.CloudQuickScanTask;
import com.huawei.systemmanager.antivirus.engine.tencent.scan.impl.NormalGlobalScanTask;
import com.huawei.systemmanager.antivirus.engine.tencent.scan.impl.NormalQuickScanTask;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.sdk.tmsdk.TMSEngineFeature;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.qscanner.QScanResultEntity;
import tmsdk.common.module.update.CheckResult;
import tmsdk.common.module.update.ICheckListener;
import tmsdk.common.module.update.IUpdateListener;
import tmsdk.common.module.update.UpdateInfo;
import tmsdk.common.module.update.UpdateManager;
import tmsdk.common.module.urlcheck.UrlCheckResult;
import tmsdk.fg.creator.ManagerCreatorF;
import tmsdk.fg.module.qscanner.QScannerManagerV2;
import tmsdk.fg.module.urlcheck.UrlCheckManager;

public class TencentAntiVirusEngine implements IAntiVirusEngine {
    private static final String TAG = "TencentAntiVirusEngine";
    private static final String TMS_SO_ANTIVIRUS = "ams-1.1.9-m-mfr";
    private CheckResult mCheckResults;
    private AtomicBoolean mIsCanceled = new AtomicBoolean(false);
    private boolean mIsInitSucuss;
    private Object mLock = new Object();
    private Handler mScanHandler = null;
    IScanPackageMgr mScanMgr = null;
    private QScannerManagerV2 mScanerManager;
    private UpdateManager mUpdateManager;
    private UrlCheckManager mUrlCheckManager;

    private class MyCheckListener implements ICheckListener {
        private Handler mHandler;

        public MyCheckListener(Handler handler) {
            this.mHandler = handler;
        }

        public void onCheckEvent(int arg0) {
            Message msg = Message.obtain(this.mHandler, 1);
            msg.arg1 = arg0;
            msg.sendToTarget();
            HwLog.i(TencentAntiVirusEngine.TAG, "check error");
        }

        public void onCheckStarted() {
            Message.obtain(this.mHandler, 0).sendToTarget();
            HwLog.i(TencentAntiVirusEngine.TAG, "check Start");
        }

        public void onCheckCanceled() {
            Message.obtain(this.mHandler, 2).sendToTarget();
            HwLog.i(TencentAntiVirusEngine.TAG, "check Canceled");
        }

        public void onCheckFinished(CheckResult result) {
            TencentAntiVirusEngine.this.mCheckResults = result;
            Message.obtain(this.mHandler, 3).sendToTarget();
            HwLog.i(TencentAntiVirusEngine.TAG, "check Finish");
        }
    }

    private static class MyUpdateListener implements IUpdateListener {
        private Handler mHandler;

        public MyUpdateListener(Handler handler) {
            this.mHandler = handler;
        }

        public void onProgressChanged(UpdateInfo arg0, int arg1) {
            Message msg = Message.obtain(this.mHandler, 7);
            msg.obj = arg0;
            msg.arg1 = arg1;
            msg.sendToTarget();
            HwLog.d(TencentAntiVirusEngine.TAG, "onProgressChanged");
        }

        public void onUpdateEvent(UpdateInfo arg0, int arg1) {
            Message msg = Message.obtain(this.mHandler, 8);
            msg.obj = arg0;
            msg.arg1 = arg1;
            msg.sendToTarget();
            HwLog.i(TencentAntiVirusEngine.TAG, "onUpdateEvent");
        }

        public void onUpdateFinished() {
            Message.obtain(this.mHandler, 9).sendToTarget();
            HwLog.i(TencentAntiVirusEngine.TAG, "onUpdateFinished");
        }

        public void onUpdateStarted() {
            Message.obtain(this.mHandler, 5).sendToTarget();
            HwLog.i(TencentAntiVirusEngine.TAG, "onUpdateStarted");
        }

        public void onUpdateCanceled() {
            Message.obtain(this.mHandler, 6).sendToTarget();
            HwLog.i(TencentAntiVirusEngine.TAG, "onUpdateCanceled");
        }
    }

    static {
        System.loadLibrary(TMS_SO_ANTIVIRUS);
    }

    private void initEngine() {
        try {
            this.mUpdateManager = (UpdateManager) ManagerCreatorC.getManager(UpdateManager.class);
            this.mUrlCheckManager = (UrlCheckManager) ManagerCreatorF.getManager(UrlCheckManager.class);
        } catch (NullPointerException e) {
            HwLog.e(TAG, "NullPointerException found ");
            return;
        } catch (NoClassDefFoundError e2) {
            HwLog.e(TAG, "tms jar not exists.");
            e2.printStackTrace();
        }
        this.mScanMgr = ScanPackageMgrFactory.newInstance();
    }

    private boolean initQScanner() {
        if (this.mScanerManager != null) {
            HwLog.w(TAG, "initQScanner: Already initialized ,skip");
            return true;
        }
        int nInitCode = -1;
        try {
            this.mScanerManager = (QScannerManagerV2) ManagerCreatorF.getManager(QScannerManagerV2.class);
            nInitCode = this.mScanerManager.initScanner();
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
        HwLog.i(TAG, "initQScanner: nInitCode = " + nInitCode);
        if (nInitCode == 0) {
            return true;
        }
        HwLog.w(TAG, "initQScanner: Some error happens on init");
        this.mScanerManager = null;
        return false;
    }

    public boolean onInit(Context context) {
        this.mIsInitSucuss = innerOnInit(context);
        return this.mIsInitSucuss;
    }

    private boolean innerOnInit(Context context) {
        if (TMSEngineFeature.isSupportTMS()) {
            initEngine();
            return true;
        }
        HwLog.w(TAG, "onInit: TMS is not supported");
        return false;
    }

    public void onStartQuickScan(Context context, Handler handler, boolean doCloudScan) {
        if (!this.mIsInitSucuss) {
            this.mIsInitSucuss = innerOnInit(context);
        }
        if (handler == null) {
            HwLog.w(TAG, "onStartQuickScan: Invalid handler");
        } else if (TMSEngineFeature.isSupportTMS()) {
            synchronized (this.mLock) {
                if (this.mScanHandler != null) {
                    this.mScanHandler.sendEmptyMessage(30);
                }
                this.mScanHandler = handler;
            }
            try {
                this.mIsCanceled.set(false);
                if (initQScanner()) {
                    QuickScanTask quickScanTask;
                    if (doCloudScan) {
                        quickScanTask = new CloudQuickScanTask(this.mScanerManager, handler);
                    } else {
                        quickScanTask = new NormalQuickScanTask(this.mScanerManager, handler);
                    }
                    quickScanTask.start();
                    synchronized (this.mLock) {
                        this.mScanHandler = null;
                    }
                    return;
                }
                handler.sendEmptyMessage(15);
                synchronized (this.mLock) {
                    this.mScanHandler = null;
                }
            } catch (NullPointerException e) {
                handler.sendEmptyMessage(15);
                HwLog.e(TAG, "NullPointerException found ");
                synchronized (this.mLock) {
                    this.mScanHandler = null;
                }
            } catch (Throwable th) {
                synchronized (this.mLock) {
                    this.mScanHandler = null;
                }
            }
        }
    }

    public void onStartGlobalScan(Context context, Handler handler, boolean doCloudScan) {
        if (!this.mIsInitSucuss) {
            this.mIsInitSucuss = innerOnInit(context);
        }
        if (handler == null) {
            HwLog.w(TAG, "onStartGlobalScan: Invalid handler");
        } else if (TMSEngineFeature.isSupportTMS()) {
            synchronized (this.mLock) {
                if (this.mScanHandler != null) {
                    this.mScanHandler.sendEmptyMessage(30);
                }
                this.mScanHandler = handler;
            }
            try {
                this.mIsCanceled.set(false);
                if (initQScanner()) {
                    GlobalScanTask globalScanTask;
                    if (doCloudScan) {
                        globalScanTask = new CloudGlobalScanTask(this.mScanerManager, handler);
                    } else {
                        globalScanTask = new NormalGlobalScanTask(this.mScanerManager, handler);
                    }
                    globalScanTask.start();
                    synchronized (this.mLock) {
                        this.mScanHandler = null;
                    }
                    return;
                }
                handler.sendEmptyMessage(15);
                synchronized (this.mLock) {
                    this.mScanHandler = null;
                }
            } catch (NullPointerException e) {
                handler.sendEmptyMessage(15);
                HwLog.e(TAG, "NullPointerException found ", e);
                synchronized (this.mLock) {
                    this.mScanHandler = null;
                }
            } catch (Throwable th) {
                synchronized (this.mLock) {
                    this.mScanHandler = null;
                }
            }
        }
    }

    public void onPauseScan() {
        if (!this.mIsInitSucuss) {
            this.mIsInitSucuss = innerOnInit(GlobalContext.getContext());
        }
        if (TMSEngineFeature.isSupportTMS() && this.mScanerManager != null) {
            this.mScanerManager.pauseScan();
        }
    }

    public void onContinueScan() {
        if (!this.mIsInitSucuss) {
            this.mIsInitSucuss = innerOnInit(GlobalContext.getContext());
        }
        if (TMSEngineFeature.isSupportTMS() && this.mScanerManager != null) {
            this.mScanerManager.continueScan();
        }
    }

    public void onCancelScan() {
        if (!this.mIsInitSucuss) {
            this.mIsInitSucuss = innerOnInit(GlobalContext.getContext());
        }
        if (TMSEngineFeature.isSupportTMS() && this.mScanerManager != null) {
            this.mIsCanceled.set(true);
            this.mScanerManager.cancelScan();
        }
    }

    public void onCheckUrl(String url, Handler handler) {
        if (!this.mIsInitSucuss) {
            this.mIsInitSucuss = innerOnInit(GlobalContext.getContext());
        }
        if (TMSEngineFeature.isSupportTMS()) {
            HwLog.i(TAG, "check url");
            if (this.mUrlCheckManager == null) {
                initEngine();
            }
            try {
                UrlCheckResult result = this.mUrlCheckManager.checkUrl(url);
                if (result != null) {
                    switch (result.result) {
                        case 0:
                            handler.sendEmptyMessage(19);
                            break;
                        case 2:
                            handler.sendEmptyMessage(21);
                            break;
                        case 3:
                            handler.sendEmptyMessage(20);
                            break;
                    }
                }
            } catch (NullPointerException e) {
                HwLog.e(TAG, "nullpointer exception in checkUrl:", e);
            } catch (Exception e2) {
                HwLog.e(TAG, "unknown exception in checkUrl:", e2);
            }
        }
    }

    public ScanResultEntity onCheckInstalledApk(Context context, String pkgName, Handler handler, boolean doCloudScan) {
        if (!this.mIsInitSucuss) {
            this.mIsInitSucuss = innerOnInit(context);
        }
        if (!TMSEngineFeature.isSupportTMS()) {
            return null;
        }
        try {
            if (!initQScanner()) {
                return null;
            }
            List<String> list = new ArrayList();
            list.add(pkgName);
            QScanSinglePkgListenerWrapper scanListener = new QScanSinglePkgListenerWrapper(context, pkgName, this.mScanMgr, doCloudScan);
            this.mScanerManager.scanSelectedPackages(list, scanListener, doCloudScan);
            QScanResultEntity entity = scanListener.getResult();
            if (entity != null) {
                return new ScanResultEntity(entity, false);
            }
            return null;
        } catch (NullPointerException e) {
            HwLog.e(TAG, "NullPointerException found ");
            return null;
        }
    }

    public String onGetVirusLibVersion(Context context) {
        if (!this.mIsInitSucuss) {
            this.mIsInitSucuss = innerOnInit(context);
        }
        if (!TMSEngineFeature.isSupportTMS()) {
            return "";
        }
        if (this.mScanerManager == null) {
            initQScanner();
        }
        String virusLibVersion = null;
        try {
            if (this.mScanerManager != null) {
                virusLibVersion = this.mScanerManager.getVirusBaseVersion(context);
            } else {
                HwLog.e(TAG, "NullPointerException found in onGetVirusLibVersion");
            }
        } catch (ExceptionInInitializerError e) {
            HwLog.e(TAG, "ExceptionInInitializerError found in onGetVirusLibVersion");
        } catch (UnsatisfiedLinkError e2) {
            HwLog.e(TAG, "UnsatisfiedLinkError found in onGetVirusLibVersion");
            e2.printStackTrace();
        } catch (Exception e3) {
            HwLog.e(TAG, "Unknown Exception:", e3);
        }
        return virusLibVersion;
    }

    public void onCheckVirusLibVersion(Handler handler) {
        if (!this.mIsInitSucuss) {
            this.mIsInitSucuss = innerOnInit(GlobalContext.getContext());
        }
        if (TMSEngineFeature.isSupportTMS()) {
            if (this.mUpdateManager == null) {
                initEngine();
            }
            try {
                this.mUpdateManager.check(1073741824, new MyCheckListener(handler));
                if (!true) {
                    handler.sendEmptyMessage(8);
                }
            } catch (NullPointerException e) {
                HwLog.e(TAG, "NullPointerException found in onCheckVirusLibVersion");
                if (null == null) {
                    handler.sendEmptyMessage(8);
                }
            } catch (ExceptionInInitializerError e2) {
                HwLog.e(TAG, "ExceptionInInitializerError found in onCheckVirusLibVersion");
                if (null == null) {
                    handler.sendEmptyMessage(8);
                }
            } catch (UnsatisfiedLinkError e3) {
                HwLog.e(TAG, "UnsatisfiedLinkError found in onCheckVirusLibVersion");
                e3.printStackTrace();
                if (null == null) {
                    handler.sendEmptyMessage(8);
                }
            } catch (Exception e4) {
                HwLog.e(TAG, "Unknown Exception:", e4);
                if (null == null) {
                    handler.sendEmptyMessage(8);
                }
            } catch (Throwable th) {
                if (null == null) {
                    handler.sendEmptyMessage(8);
                }
            }
        }
    }

    public void onUpdateVirusLibVersion(Handler handler) {
        if (!this.mIsInitSucuss) {
            this.mIsInitSucuss = innerOnInit(GlobalContext.getContext());
        }
        if (TMSEngineFeature.isSupportTMS()) {
            if (this.mCheckResults == null || this.mCheckResults.mUpdateInfoList == null || this.mCheckResults.mUpdateInfoList.size() <= 0 || this.mUpdateManager == null) {
                handler.sendEmptyMessage(4);
            } else {
                this.mUpdateManager.update(this.mCheckResults.mUpdateInfoList, new MyUpdateListener(handler));
            }
        }
    }

    public void onCancelCheckOrUpdate() {
        if (!this.mIsInitSucuss) {
            this.mIsInitSucuss = innerOnInit(GlobalContext.getContext());
        }
        if (TMSEngineFeature.isSupportTMS()) {
            if (this.mUpdateManager == null) {
                initEngine();
            }
            try {
                this.mUpdateManager.cancel();
            } catch (NullPointerException e) {
            }
        }
    }

    public void onFreeMemory() {
        if (!this.mIsInitSucuss) {
            this.mIsInitSucuss = innerOnInit(GlobalContext.getContext());
        }
        if (TMSEngineFeature.isSupportTMS() && this.mScanerManager != null) {
            this.mScanerManager.freeScanner();
            this.mScanerManager = null;
            HwLog.i(TAG, "onFreeMemory: Scanner manager is freed");
        }
    }

    public long onGetVirusLibTimeStamp() {
        return 0;
    }

    public int onCheckUninstalledApk(Context context, String pkgName, boolean doCloudScan, String path, String source) {
        Utility.initSDK(context);
        if (!this.mIsInitSucuss) {
            HwLog.i(TAG, "onCheckUninstalledApk mIsInitSucuss false");
            this.mIsInitSucuss = innerOnInit(context);
        }
        if (TMSEngineFeature.isSupportTMS()) {
            try {
                if (initQScanner()) {
                    HwLog.i(TAG, "onCheckUninstalledApk begin");
                    List<String> list = new ArrayList();
                    list.add(path);
                    QScanSinglePkgListenerWrapper scanListener = new QScanSinglePkgListenerWrapper(context, pkgName, this.mScanMgr, doCloudScan, false, path, source);
                    this.mScanerManager.scanSelectedApks(list, scanListener, doCloudScan);
                    int type = scanListener.getResulType();
                    HwLog.i(TAG, "onCheckUninstalledApk pkgName=" + pkgName + ", type=" + type);
                    return type;
                }
                HwLog.i(TAG, "onCheckUninstalledApk initQScanner false");
                return -1;
            } catch (Exception e) {
                HwLog.e(TAG, "onCheckUninstalledApk Exception", e);
                return -1;
            }
        }
        HwLog.i(TAG, "onCheckUninstalledApk isSupportTMS false");
        return -1;
    }
}
