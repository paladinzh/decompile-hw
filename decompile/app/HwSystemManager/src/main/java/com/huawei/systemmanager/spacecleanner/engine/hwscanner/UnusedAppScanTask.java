package com.huawei.systemmanager.spacecleanner.engine.hwscanner;

import android.content.Context;
import android.content.pm.IPackageStatsObserver.Stub;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.RemoteException;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.spacecleanner.engine.ScanParams;
import com.huawei.systemmanager.spacecleanner.engine.base.Task;
import com.huawei.systemmanager.spacecleanner.engine.hwadapter.HwUnusedAppTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.utils.AppCleanUpAndStorageNotifyUtils;
import com.huawei.systemmanager.spacecleanner.utils.RarelyUsedAppBean;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class UnusedAppScanTask extends Task {
    private static final String TAG = "UnusedAppScanTask";
    private Map<String, RarelyUsedAppBean> mPkgs = HsmCollections.newArrayMap();

    public UnusedAppScanTask(Context ctx) {
        super(ctx);
    }

    public String getTaskName() {
        return TAG;
    }

    public int getType() {
        return 51;
    }

    protected void doTask(ScanParams p) {
        Context context = getContext();
        ArrayList<RarelyUsedAppBean> appBeanlist = Lists.newArrayList(AppCleanUpAndStorageNotifyUtils.getUnusedApp(context).values());
        if (appBeanlist.isEmpty()) {
            HwLog.i(TAG, "doTask get appBeanlist is empty, finish scan");
            onPublishEnd();
            return;
        }
        this.mPkgs.clear();
        for (RarelyUsedAppBean bean : appBeanlist) {
            final RarelyUsedAppBean bean2;
            this.mPkgs.put(bean2.getPackgeName(), bean2);
        }
        PackageManager pkgManager = context.getPackageManager();
        int size = appBeanlist.size();
        final CountDownLatch latch = new CountDownLatch(size);
        for (int i = 0; i < size && !isCanceled(); i++) {
            bean2 = (RarelyUsedAppBean) appBeanlist.get(i);
            final String pkgName = bean2.getPackgeName();
            onPublishProgress((i * 100) / size, pkgName);
            pkgManager.getPackageSizeInfo(pkgName, new Stub() {
                public void onGetStatsCompleted(PackageStats packageStats, boolean b) throws RemoteException {
                    UnusedAppScanTask.this.onGetPkgStats(packageStats, bean2, pkgName);
                    latch.countDown();
                }
            });
        }
        try {
            HwLog.i(TAG, "unused apk scan task do task flag is: " + latch.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            HwLog.e(TAG, "doTask latch exception");
        }
        onPublishEnd();
    }

    private void onGetPkgStats(PackageStats stats, RarelyUsedAppBean bean, String pkgName) {
        if (stats == null) {
            HwLog.e(TAG, "onGetPkgStats stat is nul!! pkg:" + pkgName);
            return;
        }
        bean.setAppSize((((stats.externalCodeSize + stats.externalObbSize) + (stats.externalDataSize + stats.externalMediaSize)) + stats.codeSize) + stats.dataSize);
        Trash trash = HwUnusedAppTrash.create(bean);
        if (trash != null) {
            onPublishItemUpdate(trash);
        } else {
            HwLog.i(getTaskName(), "onGetStatsCompleted, create trash failed,pkg=" + pkgName);
        }
    }

    public List<Integer> getSupportTrashType() {
        return HsmCollections.newArrayList(Integer.valueOf(2));
    }

    public boolean isNormal() {
        return false;
    }
}
