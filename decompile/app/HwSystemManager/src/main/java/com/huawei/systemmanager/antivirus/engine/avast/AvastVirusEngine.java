package com.huawei.systemmanager.antivirus.engine.avast;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import com.avast.android.sdk.engine.EngineConfig;
import com.avast.android.sdk.engine.EngineConfig.Builder;
import com.avast.android.sdk.engine.EngineInterface;
import com.avast.android.sdk.engine.EngineLoggerInterface;
import com.avast.android.sdk.engine.InvalidConfigException;
import com.avast.android.sdk.engine.UrlCheckResultStructure;
import com.avast.android.sdk.engine.UrlCheckResultStructure.UrlCheckResult;
import com.avast.android.sdk.engine.UrlSource;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.antivirus.engine.IAntiVirusEngine;
import com.huawei.systemmanager.antivirus.engine.avast.scan.CloudScanMgr;
import com.huawei.systemmanager.antivirus.engine.avast.scan.GlobalScanTask;
import com.huawei.systemmanager.antivirus.engine.avast.scan.QuickScanTask;
import com.huawei.systemmanager.antivirus.engine.avast.scan.impl.CloudGlobalScanTask;
import com.huawei.systemmanager.antivirus.engine.avast.scan.impl.CloudQuickScanTask;
import com.huawei.systemmanager.antivirus.engine.avast.scan.impl.NormalGlobalScanTask;
import com.huawei.systemmanager.antivirus.engine.avast.scan.impl.NormalQuickScanTask;
import com.huawei.systemmanager.antivirus.engine.avast.update.ICheckListener;
import com.huawei.systemmanager.antivirus.engine.avast.update.IUpdateListener;
import com.huawei.systemmanager.antivirus.engine.avast.update.UpdateManager;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.securitythreats.comm.SecurityThreatsUtil;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class AvastVirusEngine implements IAntiVirusEngine {
    public static final String CONFIG_NAME = "com.huawei.antivirus.avast_preference";
    private static final String KEY_UUID = "uu_id";
    private static final String SDK_API_KEY = "a4e0379764c3796cc18064debbba9be65206534a";
    private static final String TAG = "AvastVirusEngine";
    private static final String URL_API_KEY = "222B7F7448CC076FDB307322B6F17F";
    private static final Long URL_CALL_ID = Long.valueOf(574581089931100183L);
    private static boolean sInitEngineSucess = false;
    private boolean mCheckResults;
    private GlobalScanTask mGlobalScanTask;
    private AtomicBoolean mIsCanceled;
    private Object mLock;
    private PackageManager mPackageManager;
    private QuickScanTask mQuickScanTask;
    private Handler mScanHandler;
    private UpdateManager mUpdateManager;

    class MyCheckListener implements ICheckListener {
        private Handler mHandler;

        public MyCheckListener(Handler handler) {
            this.mHandler = handler;
        }

        public void onCheckEvent(int arg0) {
            HwLog.i(AvastVirusEngine.TAG, "onCheckEvent error");
            Message msg = Message.obtain(this.mHandler, 1);
            msg.arg1 = arg0;
            msg.sendToTarget();
        }

        public void onCheckStarted() {
            HwLog.i(AvastVirusEngine.TAG, "onCheckStarted");
            Message.obtain(this.mHandler, 0).sendToTarget();
        }

        public void onCheckCanceled() {
            HwLog.i(AvastVirusEngine.TAG, "onCheckCanceled");
            Message.obtain(this.mHandler, 2).sendToTarget();
        }

        public void onCheckFinished(boolean isUpdateAvaiable) {
            HwLog.i(AvastVirusEngine.TAG, "onCheckFinished" + isUpdateAvaiable);
            AvastVirusEngine.this.mCheckResults = isUpdateAvaiable;
            Message.obtain(this.mHandler, 3).sendToTarget();
        }
    }

    private static class MyUpdateListener implements IUpdateListener {
        private Handler mHandler;

        public MyUpdateListener(Handler handler) {
            this.mHandler = handler;
        }

        public void onProgressChanged(Object arg0, int arg1) {
            Message msg = Message.obtain(this.mHandler, 7);
            msg.obj = arg0;
            msg.arg1 = arg1;
            msg.sendToTarget();
        }

        public void onUpdateEvent(Object arg0, int arg1) {
            HwLog.i(AvastVirusEngine.TAG, "onUpdate error");
            Message msg = Message.obtain(this.mHandler, 8);
            msg.obj = arg0;
            msg.arg1 = arg1;
            msg.sendToTarget();
        }

        public void onUpdateFinished() {
            HwLog.i(AvastVirusEngine.TAG, "onUpdateFinished");
            Message.obtain(this.mHandler, 9).sendToTarget();
        }

        public void onUpdateStarted() {
            HwLog.i(AvastVirusEngine.TAG, "onUpdateStarted");
            Message.obtain(this.mHandler, 5).sendToTarget();
        }

        public void onUpdateCanceled() {
            HwLog.i(AvastVirusEngine.TAG, "onUpdate Cancel");
            Message.obtain(this.mHandler, 6).sendToTarget();
        }
    }

    public static void innerInitEngine() {
        SharedPreferences sharedPreferences = GlobalContext.getContext().getSharedPreferences(CONFIG_NAME, 4);
        String uuid = sharedPreferences.getString(KEY_UUID, null);
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
            sharedPreferences.edit().putString(KEY_UUID, uuid).commit();
        }
        Builder builder = EngineConfig.newBuilder().setGuid(uuid).setApiKey(SDK_API_KEY).setAutomaticUpdates(false).setFileCloudScanningEnabled(false).setEngineLogger(new EngineLoggerInterface() {
            public void v(String msg) {
                HwLog.v(AvastVirusEngine.TAG, msg);
            }

            public void v(String msg, Throwable tr) {
                HwLog.v(AvastVirusEngine.TAG, msg, tr);
            }

            public void d(String msg) {
                HwLog.d(AvastVirusEngine.TAG, msg);
            }

            public void d(String msg, Throwable tr) {
                HwLog.d(AvastVirusEngine.TAG, msg, tr);
            }

            public void i(String msg) {
                HwLog.i(AvastVirusEngine.TAG, msg);
            }

            public void i(String msg, Throwable tr) {
                HwLog.i(AvastVirusEngine.TAG, msg, tr);
            }

            public void w(String msg) {
                HwLog.w(AvastVirusEngine.TAG, msg);
            }

            public void w(String msg, Throwable tr) {
                HwLog.w(AvastVirusEngine.TAG, msg, tr);
            }

            public void e(String msg) {
                HwLog.e(AvastVirusEngine.TAG, msg);
            }

            public void e(String msg, Throwable tr) {
                HwLog.e(AvastVirusEngine.TAG, msg, tr);
            }

            public void a(String msg) {
                HwLog.v(AvastVirusEngine.TAG, msg);
            }

            public void a(String msg, Throwable tr) {
                HwLog.v(AvastVirusEngine.TAG, msg, tr);
            }
        });
        builder.setUrlInfoCredentials(URL_CALL_ID, URL_API_KEY);
        try {
            EngineInterface.init(GlobalContext.getContext(), builder.build());
            sInitEngineSucess = true;
        } catch (InvalidConfigException e) {
            sInitEngineSucess = false;
            HwLog.e("TAG", "InvalidConfig", e);
        }
    }

    public AvastVirusEngine() {
        this.mLock = new Object();
        this.mScanHandler = null;
        this.mIsCanceled = new AtomicBoolean(false);
        this.mQuickScanTask = null;
        this.mGlobalScanTask = null;
        this.mUpdateManager = null;
        this.mPackageManager = null;
        this.mPackageManager = GlobalContext.getContext().getPackageManager();
    }

    public boolean onInit(Context context) {
        if (sInitEngineSucess) {
            return sInitEngineSucess;
        }
        synchronized (AvastVirusEngine.class) {
            innerInitEngine();
        }
        return sInitEngineSucess;
    }

    public void onStartQuickScan(Context context, Handler handler, boolean doCloudScan) {
        if (sInitEngineSucess && handler != null) {
            synchronized (this.mLock) {
                if (this.mScanHandler != null) {
                    this.mScanHandler.sendEmptyMessage(30);
                }
                this.mScanHandler = handler;
            }
            try {
                this.mIsCanceled.set(false);
                this.mQuickScanTask = doCloudScan ? new CloudQuickScanTask(handler) : new NormalQuickScanTask(handler);
                this.mQuickScanTask.start();
                synchronized (this.mLock) {
                    this.mScanHandler = null;
                }
            } catch (NullPointerException e) {
                handler.sendEmptyMessage(15);
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
        if (sInitEngineSucess && handler != null) {
            synchronized (this.mLock) {
                if (this.mScanHandler != null) {
                    this.mScanHandler.sendEmptyMessage(30);
                }
                this.mScanHandler = handler;
            }
            try {
                this.mIsCanceled.set(false);
                this.mGlobalScanTask = doCloudScan ? new CloudGlobalScanTask(handler) : new NormalGlobalScanTask(handler);
                this.mGlobalScanTask.start();
                synchronized (this.mLock) {
                    this.mScanHandler = null;
                }
            } catch (NullPointerException e) {
                handler.sendEmptyMessage(15);
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
    }

    public void onContinueScan() {
    }

    public void onCancelScan() {
        if (sInitEngineSucess) {
            if (this.mQuickScanTask != null) {
                this.mIsCanceled.set(true);
                this.mQuickScanTask.cancel();
                this.mQuickScanTask = null;
            }
            if (this.mGlobalScanTask != null) {
                this.mIsCanceled.set(true);
                this.mGlobalScanTask.cancel();
                this.mGlobalScanTask = null;
            }
        }
    }

    public void onCheckUrl(String url, Handler handler) {
        int i;
        boolean isHarmURL = false;
        long begin = SystemClock.elapsedRealtime();
        List<UrlCheckResultStructure> results = EngineInterface.checkUrl(GlobalContext.getContext(), null, url, UrlSource.STOCK);
        HwLog.i(TAG, "onCheckUrl time=" + (SystemClock.elapsedRealtime() - begin));
        for (UrlCheckResultStructure result : results) {
            if (result.result != UrlCheckResult.RESULT_MALWARE && result.result != UrlCheckResult.RESULT_PHISHING && result.result != UrlCheckResult.RESULT_SUSPICIOUS) {
                if (result.result == UrlCheckResult.RESULT_TYPO_SQUATTING) {
                }
            }
            isHarmURL = true;
        }
        if (isHarmURL) {
            i = 20;
        } else {
            i = 19;
        }
        handler.sendEmptyMessage(i);
    }

    public ScanResultEntity onCheckInstalledApk(Context context, String pkgName, Handler handler, boolean doCloudScan) {
        PackageInfo packageInfo;
        onInit(context);
        try {
            packageInfo = PackageManagerWrapper.getPackageInfo(this.mPackageManager, pkgName, 0);
        } catch (NameNotFoundException e) {
            packageInfo = null;
        }
        if (packageInfo == null) {
            return null;
        }
        File inputFile = new File(packageInfo.applicationInfo.sourceDir);
        ScanResultEntity entity = AvastScanResultBuilder.parseScanResultEntity(packageInfo, EngineInterface.scan(context, null, inputFile, packageInfo, 33), false);
        if (!ScanResultEntity.isRiskORVirus(entity) && doCloudScan) {
            List<File> filesToScan = new ArrayList();
            Map<String, PackageInfo> pkgMap = new HashMap();
            filesToScan.add(inputFile);
            pkgMap.put(inputFile.getAbsolutePath(), packageInfo);
            List<ScanResultEntity> entities = CloudScanMgr.cloudScan(context, filesToScan, pkgMap, false);
            if (entities.size() > 0) {
                entity = (ScanResultEntity) entities.get(0);
            }
        }
        AntiVirusTools.refreshData(context, entity);
        sendInsallApkNotification(context, entity);
        return entity;
    }

    private void sendInsallApkNotification(Context context, ScanResultEntity entity) {
        int type = 0;
        switch (entity.type) {
            case 303:
                type = 1;
                break;
            case AntiVirusTools.TYPE_VIRUS /*305*/:
                type = 2;
                break;
        }
        if (type != 0) {
            SecurityThreatsUtil.notifyNewInstallVirusToService(context, entity.packageName, type);
        }
    }

    public String onGetVirusLibVersion(Context context) {
        if (!sInitEngineSucess) {
            return "";
        }
        if (EngineInterface.getVpsInformation(context, null) == null) {
            return "";
        }
        return EngineInterface.getVpsInformation(context, null).version;
    }

    public void onCheckVirusLibVersion(Handler handler) {
        if (sInitEngineSucess) {
            if (this.mUpdateManager == null) {
                this.mUpdateManager = new UpdateManager();
            }
            try {
                this.mUpdateManager.checkUpdateAvailable(GlobalContext.getContext(), new MyCheckListener(handler));
                if (!true) {
                    handler.sendEmptyMessage(8);
                }
            } catch (NullPointerException e) {
                if (null == null) {
                    handler.sendEmptyMessage(8);
                }
            } catch (ExceptionInInitializerError e2) {
                if (null == null) {
                    handler.sendEmptyMessage(8);
                }
            } catch (UnsatisfiedLinkError e3) {
                if (null == null) {
                    handler.sendEmptyMessage(8);
                }
            } catch (Exception e4) {
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
        if (sInitEngineSucess) {
            if (!this.mCheckResults || this.mUpdateManager == null) {
                handler.sendEmptyMessage(4);
            } else {
                this.mUpdateManager.update(new MyUpdateListener(handler));
            }
        }
    }

    public void onCancelCheckOrUpdate() {
    }

    public long onGetVirusLibTimeStamp() {
        return 0;
    }

    public void onFreeMemory() {
    }
}
