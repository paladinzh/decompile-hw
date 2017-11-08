package com.huawei.systemmanager.mainscreen.detector.task;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.adblock.ui.model.AbsTxUrlsTask;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.antivirus.engine.AntiVirusEngineFactory;
import com.huawei.systemmanager.antivirus.engine.IAntiVirusEngine;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;
import com.huawei.systemmanager.comm.component.GenericHandler;
import com.huawei.systemmanager.comm.component.GenericHandler.MessageHandler;
import com.huawei.systemmanager.comm.concurrent.HsmExecutor;
import com.huawei.systemmanager.customize.AbroadUtils;
import com.huawei.systemmanager.mainscreen.detector.item.AdDetectItem;
import com.huawei.systemmanager.mainscreen.detector.item.VirusAppItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VirusDetectTask extends DetectTask implements MessageHandler {
    private static final String TAG = "VirusDetectTask";
    private List<ScanResultEntity> mAdResult = Lists.newArrayList();
    private IAntiVirusEngine mAntiVirusEngine = AntiVirusEngineFactory.newInstance();
    private Handler mHandler;
    private Map<String, ScanResultEntity> mScanResultsBanUrl = new HashMap();
    private List<ScanResultEntity> mVirusResult = Lists.newArrayList();

    private class AdDetectTask extends AbsTxUrlsTask {
        AdDetectTask(Context context, boolean scanSuccess, Map<String, ScanResultEntity> scanResultsBanUrl) {
            super(context, scanSuccess, scanResultsBanUrl);
        }

        protected void onTaskFinished(int totalAdCount, int checkedAdCount) {
            HwLog.i(VirusDetectTask.TAG, "AdDetectTask onTaskFinished totalAdCount=" + totalAdCount + ", checkedAdCount=" + checkedAdCount);
            if (VirusDetectTask.this.isFinish()) {
                HwLog.i(VirusDetectTask.TAG, "AdDetectTask onTaskFinished, it is already finished!");
                return;
            }
            VirusDetectTask.this.publishItemFount(VirusAppItem.create(VirusDetectTask.this.mVirusResult));
            VirusDetectTask.this.publishItemFount(AdDetectItem.create(VirusDetectTask.this.mAdResult, totalAdCount, checkedAdCount));
            VirusDetectTask.this.publishTaskFinish();
            VirusDetectTask.this.releaseHandler();
        }
    }

    public VirusDetectTask(Context context) {
        super(context);
        this.mAntiVirusEngine.onInit(context);
    }

    protected void doTask() {
        synchronized (this) {
            if (isFinish()) {
                HwLog.i(TAG, "VirusDetectTask ready to do task, but its already finish, do nothing just return");
                return;
            }
            HandlerThread thread = new HandlerThread("VirusDetectTask_scan_thread");
            thread.start();
            this.mHandler = new GenericHandler(this, thread.getLooper());
            Handler handler = this.mHandler;
            Context context = getContext();
            AntiVirusTools.updateTimerRemindTimeStamp(this.mContext);
            this.mAntiVirusEngine.onStartQuickScan(context, handler, false);
        }
    }

    public String getTaskName() {
        return TAG;
    }

    public void cancel() {
        super.cancel();
        this.mAntiVirusEngine.onCancelScan();
        this.mAntiVirusEngine.onFreeMemory();
    }

    public void destory() {
        super.destory();
        releaseHandler();
        this.mAntiVirusEngine.onFreeMemory();
    }

    public void onHandleMessage(Message msg) {
        switch (msg.what) {
            case 10:
                HwLog.i(TAG, "receive msg MSG_SCAN_START");
                publishTaskStart();
                break;
            case 11:
                if (!isFinish()) {
                    ScanResultEntity result = msg.obj;
                    publishProgressChange(result.packageName, (float) msg.arg1);
                    if (AntiVirusTools.TYPE_VIRUS == result.type || 303 == result.type) {
                        HwLog.i(TAG, "find virus=" + result.packageName);
                        this.mVirusResult.add(result);
                    } else if (AntiVirusTools.TYPE_ADVERTISE == result.type) {
                        HwLog.i(TAG, "find ad=" + result.packageName);
                        this.mAdResult.add(result);
                    }
                    recordBanUrlEntity(result);
                    break;
                }
                return;
            case 12:
            case 15:
            case 30:
                HwLog.i(TAG, "received msg :" + msg.what);
                scanFinish();
                break;
            case 14:
                HwLog.i(TAG, "received msg :" + msg.what);
                setCanceled();
                scanFinish();
                break;
            case 16:
                HwLog.i(TAG, "receive msg Scan cancel");
                if (!isFinish()) {
                    setCanceled();
                    scanFinish();
                    break;
                }
                return;
        }
    }

    private void scanFinish() {
        if (isFinish()) {
            HwLog.i(TAG, "scanFinish called, but is already finished!");
            return;
        }
        if (AbroadUtils.isAbroad(getContext())) {
            publishItemFount(VirusAppItem.create(this.mVirusResult));
            publishTaskFinish();
            releaseHandler();
        } else {
            new AdDetectTask(getContext(), false, this.mScanResultsBanUrl).executeOnExecutor(HsmExecutor.THREAD_POOL_EXECUTOR, new Void[0]);
        }
    }

    private void releaseHandler() {
        synchronized (this) {
            if (this.mHandler != null) {
                this.mHandler.getLooper().quit();
                this.mHandler = null;
            }
        }
    }

    public int getWeight() {
        return getContext().getResources().getInteger(R.integer.scan_weight_main_screen_virus_scan);
    }

    private void recordBanUrlEntity(ScanResultEntity result) {
        if (301 != result.type && !result.mBanUrls.isEmpty()) {
            HwLog.i(TAG, "recordBanUrlEntity find ad=" + result.getPackageName());
            this.mScanResultsBanUrl.put(result.getPackageName(), result);
        }
    }
}
