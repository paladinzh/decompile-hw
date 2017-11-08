package com.huawei.systemmanager.antivirus.cache;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.antivirus.engine.AntiVirusEngineFactory;
import com.huawei.systemmanager.antivirus.engine.IAntiVirusEngine;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.useragreement.UserAgreementHelper;
import com.huawei.systemmanager.util.HwLog;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class VirusBgDataSyncManager {
    private static final int MSG_EXECUTE_PREPARE = 0;
    private static final int MSG_EXECUTE_START = 1;
    private static final String TAG = "VirusBgDataSyncManager";
    private static final int TIMEOUT = 1800;
    private Handler mHandler;
    private LinkedBlockingQueue<Task> mQueue;
    private HandlerThread mWorker = new HandlerThread("virus_bg_scan");
    private AtomicBoolean misFinish = new AtomicBoolean(true);

    public static class MyHandler extends Handler {
        private IAntiVirusEngine mEngine = null;
        private WeakReference<VirusBgDataSyncManager> mReference = null;
        private ArrayList<ScanResultEntity> mResults = null;

        public MyHandler(VirusBgDataSyncManager manager, Looper looper) {
            super(looper);
            this.mReference = new WeakReference(manager);
            this.mResults = new ArrayList();
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            VirusBgDataSyncManager manager = (VirusBgDataSyncManager) this.mReference.get();
            if (manager != null) {
                switch (msg.what) {
                    case 0:
                        HwLog.i(VirusBgDataSyncManager.TAG, "doBackGroundWork");
                        doBackGroundWork(manager);
                        break;
                    case 1:
                        if (manager.misFinish.get()) {
                            AntiVirusTools.setVirusScanStamp(GlobalContext.getContext(), System.currentTimeMillis());
                            this.mResults.clear();
                            manager.misFinish.set(false);
                            this.mEngine = AntiVirusEngineFactory.newInstance();
                            this.mEngine.onStartQuickScan(GlobalContext.getContext(), this, ((Boolean) msg.obj).booleanValue());
                            break;
                        }
                        HwLog.i(VirusBgDataSyncManager.TAG, "there is one task at the same time");
                        return;
                    case 11:
                        ScanResultEntity result = msg.obj;
                        if (AntiVirusTools.isMaliciousApk(result)) {
                            this.mResults.add(result);
                        }
                        manager.misFinish.set(false);
                        break;
                    case 12:
                        HwLog.i(VirusBgDataSyncManager.TAG, "task finish yet");
                        VirusAppsManager.getIntance().refreshExitTable(GlobalContext.getContext(), this.mResults);
                        manager.misFinish.set(true);
                        manager.execute();
                        break;
                    case 14:
                    case 15:
                    case 16:
                    case 30:
                        manager.misFinish.set(true);
                        break;
                }
            }
        }

        private void doBackGroundWork(VirusBgDataSyncManager manager) {
            boolean isCloudTask = false;
            if (!manager.mQueue.isEmpty()) {
                for (Task task : manager.mQueue) {
                    if (task.mIsCloudTask) {
                        isCloudTask = true;
                        break;
                    }
                }
                manager.mQueue.clear();
                removeMessages(1);
                Message msg = obtainMessage(1);
                msg.obj = Boolean.valueOf(isCloudTask);
                HwLog.i(VirusBgDataSyncManager.TAG, "the task is cloud" + isCloudTask);
                sendMessage(msg);
            }
        }
    }

    public static class Task {
        public boolean mIsCloudTask;

        public Task(boolean isCloudTask) {
            this.mIsCloudTask = isCloudTask;
        }
    }

    public VirusBgDataSyncManager() {
        setDaemon(this.mWorker);
        this.mWorker.start();
        this.mHandler = new MyHandler(this, this.mWorker.getLooper());
        this.mQueue = new LinkedBlockingQueue();
    }

    private void setDaemon(Thread pthread) {
        pthread.setDaemon(true);
    }

    public void destory() {
        if (this.mWorker != null) {
            this.mWorker.quit();
            this.mWorker = null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void request(Context context) {
        Utility.initSDK(context);
        boolean userAgreementState = AntiVirusTools.isCloudScanSwitchOn(context) ? UserAgreementHelper.getUserAgreementState(context) : false;
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        boolean isNetworkConnected = false;
        if (networkInfo != null) {
            isNetworkConnected = networkInfo.isConnected();
        }
        LinkedBlockingQueue linkedBlockingQueue = this.mQueue;
        if (!userAgreementState) {
            isNetworkConnected = false;
        }
        linkedBlockingQueue.add(new Task(isNetworkConnected));
        if (this.misFinish.get()) {
            execute();
            return;
        }
        long timeDiff = System.currentTimeMillis() - AntiVirusTools.getVirusScanStamp(GlobalContext.getContext());
        if (timeDiff > 1800000) {
            HwLog.i(TAG, "time out handle timeDiff" + timeDiff);
            this.misFinish.set(true);
            execute();
        }
    }

    private void execute() {
        this.mHandler.removeMessages(0);
        this.mHandler.sendEmptyMessage(0);
    }
}
