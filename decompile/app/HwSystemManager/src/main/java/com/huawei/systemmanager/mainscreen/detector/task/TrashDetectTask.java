package com.huawei.systemmanager.mainscreen.detector.task;

import android.content.Context;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.mainscreen.detector.item.TrashDetectItem;
import com.huawei.systemmanager.spacecleanner.engine.ITrashScanListener;
import com.huawei.systemmanager.spacecleanner.engine.ScanManager;
import com.huawei.systemmanager.spacecleanner.engine.ScanParams;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.util.HwLog;

public class TrashDetectTask extends DetectTask {
    private static final String TAG = "TrashDetectTask";
    private ITrashScanListener mInnerListener = new ITrashScanListener() {
        public void onScanStart(int scannerType) {
            TrashDetectTask.this.publishTaskStart();
        }

        public void onScanProgressChange(int scannerType, int progress, String info, long normalTrashSize, int normalProgress) {
            TrashDetectTask.this.publishProgressChange(info, (float) progress);
        }

        public void onTrashFound(int scanType, Trash trash, long size) {
        }

        public void onScanEnd(int scannerType, int supportTrashType, boolean canceled) {
            HwLog.i(TrashDetectTask.TAG, "recieve scan end, scanner type:" + scannerType + ", canceled:" + canceled);
            if (scannerType == 50) {
                TrashDetectItem item;
                synchronized (TrashDetectTask.this) {
                    item = TrashDetectItem.create(TrashDetectTask.this.mTrashHandler);
                }
                TrashDetectTask.this.publishItemFount(item);
                TrashDetectTask.this.publishTaskFinish();
                TrashDetectTask.this.releaseTrashScanHandler();
            }
        }
    };
    private TrashScanHandler mTrashHandler;

    public TrashDetectTask(Context context) {
        super(context);
    }

    public String getTaskName() {
        return TAG;
    }

    public void cancel() {
        super.cancel();
        releaseTrashScanHandler();
    }

    protected void doTask() {
        synchronized (this) {
            if (isFinish()) {
                HwLog.e(TAG, "TrashDetectTask ready to do task ,but its already finished, do nothing");
                return;
            }
            this.mTrashHandler = ScanManager.startScan(getContext(), ScanParams.createMainScreenScanParams(), this.mInnerListener);
        }
    }

    private void releaseTrashScanHandler() {
        synchronized (this) {
            if (this.mTrashHandler == null) {
                return;
            }
            this.mTrashHandler.destory();
            this.mTrashHandler = null;
        }
    }

    public int getWeight() {
        return getContext().getResources().getInteger(R.integer.scan_weight_main_screen_quick_trash_scan);
    }
}
