package com.huawei.systemmanager.spacecleanner.engine.hwadapter;

import android.content.Context;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.spacecleanner.engine.ScanParams;
import com.huawei.systemmanager.spacecleanner.engine.base.CombineTask;
import com.huawei.systemmanager.spacecleanner.engine.base.ITrashEngine;
import com.huawei.systemmanager.spacecleanner.engine.base.ITrashEngine.IUpdateListener;
import com.huawei.systemmanager.spacecleanner.engine.base.Task;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.ApkCacheScanTask;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.ApkFileScanTask;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.BlurPhotoTask;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.HwDeepFileScanTask;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.MediaScanTask;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.ProcessScanTask;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.UnusedAppScanTask;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom.HwCustomScanTask;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class HwTrashEngine implements ITrashEngine {
    private static final String TAG = "HwTrashEngine";
    private final Context mContext;

    public HwTrashEngine(Context context) {
        this.mContext = context;
    }

    public Task getScanner(ScanParams p) {
        int type = p.getType();
        int trashType = p.getTrashType();
        Task customAppTask;
        Task apkCacheTask;
        Task apkFileTask;
        switch (type) {
            case 0:
                Task fileDeepTask = new HwDeepFileScanTask(this.mContext);
                customAppTask = new HwCustomScanTask(this.mContext);
                apkCacheTask = new ApkCacheScanTask(this.mContext);
                apkFileTask = new ApkFileScanTask(this.mContext);
                Task mediaTask = new MediaScanTask(this.mContext);
                Task unusedAppTask = new UnusedAppScanTask(this.mContext);
                Task processTask = new ProcessScanTask(this.mContext);
                Task blurPhotoTask = new BlurPhotoTask(this.mContext);
                return new CombineTask(this.mContext, "HwDeepScanner", fileDeepTask, customAppTask, apkCacheTask, apkFileTask, mediaTask, unusedAppTask, processTask, blurPhotoTask);
            case 2:
                apkCacheTask = new ApkCacheScanTask(this.mContext);
                return new CombineTask(this.mContext, "HwAppCacheScanner", apkCacheTask);
            case 3:
                apkCacheTask = new ApkCacheScanTask(this.mContext);
                apkFileTask = new ApkFileScanTask(this.mContext);
                return new CombineTask(this.mContext, "HwQuickScanner", apkCacheTask, apkFileTask);
            case 4:
                if (trashType <= 0) {
                    HwLog.e(TAG, "AutoClean: ScanParams.PARAMS_AUTO_SCAN,but trash type is error!trashType:" + trashType);
                    return null;
                }
                List listTask = Lists.newArrayList();
                StringBuilder strBuilder = new StringBuilder();
                strBuilder.append("AutoClean:auto scan need");
                customAppTask = new HwCustomScanTask(this.mContext);
                if (customAppTask.isSupportByTrashType(trashType)) {
                    listTask.add(customAppTask);
                    strBuilder.append(" HwCustomScanTask ");
                }
                apkCacheTask = new ApkCacheScanTask(this.mContext);
                if (apkCacheTask.isSupportByTrashType(trashType)) {
                    listTask.add(apkCacheTask);
                    strBuilder.append(" ApkCacheScanTask ");
                }
                Task deepFileScanTask = new HwDeepFileScanTask(this.mContext);
                if (deepFileScanTask.isSupportByTrashType(trashType)) {
                    listTask.add(deepFileScanTask);
                    strBuilder.append(" HwDeepFileScanTask ");
                }
                Task unusedAppScanTask = new UnusedAppScanTask(this.mContext);
                if (unusedAppScanTask.isSupportByTrashType(trashType)) {
                    listTask.add(unusedAppScanTask);
                    strBuilder.append(" UnusedAppScanTask ");
                }
                HwLog.i(TAG, strBuilder.toString());
                if (listTask.size() > 0) {
                    return new CombineTask(this.mContext, "HwAutoScan", listTask);
                }
                HwLog.e(TAG, "AutoClean: ScanParams.PARAMS_AUTO_SCAN,but no task add to scan!");
                return null;
            default:
                HwLog.e(TAG, "get scanner, unknow type:" + type);
                return null;
        }
    }

    public void destory() {
    }

    public void update(IUpdateListener listener) {
    }

    public boolean init() {
        return true;
    }
}
