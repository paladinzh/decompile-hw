package com.huawei.systemmanager.spacecleanner.engine.hwscanner;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver.Stub;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.Environment;
import android.os.RemoteException;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.spacecleanner.engine.ScanParams;
import com.huawei.systemmanager.spacecleanner.engine.base.Task;
import com.huawei.systemmanager.spacecleanner.engine.hwadapter.HwAppCacheTrash;
import com.huawei.systemmanager.spacecleanner.engine.hwadapter.HwAppDataTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.AppCacheTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.AppDataTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.PreInstalledAppTrash;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ApkCacheScanTask extends Task {
    private static final long CACEH_DIR = 12288;
    private static final String TAG = "ApkCacheScanTask";

    public ApkCacheScanTask(Context ctx) {
        super(ctx);
    }

    public String getTaskName() {
        return TAG;
    }

    public int getType() {
        return 1;
    }

    protected int getWeight() {
        return getContext().getResources().getInteger(R.integer.scan_weight_trash_app_cache);
    }

    protected void doTask(ScanParams p) {
        PackageManager pkgManager = getContext().getPackageManager();
        List<HsmPkgInfo> pkgList = HsmPackageManager.getInstance().getInstalledPackages(8192);
        final boolean flagToScanAll = this.mParams != null && this.mParams.getType() == 0;
        int size = pkgList.size();
        final CountDownLatch latch = new CountDownLatch(size);
        for (int i = 0; i < size; i++) {
            if (isCanceled()) {
                HwLog.i(TAG, "interrupt getPackageSizeInfo , for the task is canceled!");
                break;
            }
            final HsmPkgInfo info = (HsmPkgInfo) pkgList.get(i);
            final String pkgName = info.mPkgName;
            onPublishProgress((i * 100) / size, pkgName);
            pkgManager.getPackageSizeInfo(pkgName, new Stub() {
                public void onGetStatsCompleted(PackageStats packageStats, boolean b) throws RemoteException {
                    ApkCacheScanTask.this.onGetPkgStatsForCache(packageStats, pkgName);
                    ApkCacheScanTask.this.onGetPkgStatsForData(packageStats, pkgName);
                    if (flagToScanAll && info.isRemoveAblePreInstall()) {
                        ApkCacheScanTask.this.onGetPkgStatsForPreInstall(packageStats, info);
                    }
                    latch.countDown();
                }
            });
        }
        try {
            HwLog.i(TAG, "apk cache scan task do task flag is: " + latch.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            HwLog.e(TAG, "doTask latch exception");
        }
        onPublishEnd();
    }

    private void onGetPkgStatsForCache(PackageStats data, String pkgName) {
        PackageStats stats = data;
        if (data != null) {
            long cache = data.cacheSize + data.externalCacheSize;
            if (cache > CACEH_DIR) {
                AppCacheTrash trash = HwAppCacheTrash.create(pkgName, cache);
                if (trash != null) {
                    onPublishItemUpdate(trash);
                    return;
                } else {
                    HwLog.e(getTaskName(), "create HwAppCacheTrash failed! pkg=" + pkgName);
                    return;
                }
            }
            return;
        }
        HwLog.e(TAG, "onGetPkgStatsForCache stats is null!! pkg:" + pkgName);
    }

    private void onGetPkgStatsForPreInstall(PackageStats data, HsmPkgInfo pkgInfo) {
        if (pkgInfo == null) {
            HwLog.e(getTaskName(), "onGetPkgStatsForPreInstall failed! pkgInfo is null");
            return;
        }
        PackageStats stats = data;
        if (data != null) {
            PreInstalledAppTrash trash = PreInstalledAppTrash.create(data.codeSize, pkgInfo);
            if (trash != null) {
                onPublishItemUpdate(trash);
            } else {
                HwLog.e(getTaskName(), "create onGetPkgStatsForPreInstall failed! pkg=" + pkgInfo.getPackageName());
            }
        }
    }

    private void onGetPkgStatsForData(PackageStats data, String pkgName) {
        PackageStats stats = data;
        PackageManager pkgManager = getContext().getPackageManager();
        if (pkgManager == null) {
            HwLog.e(TAG, "onGetPkgStatsForData, pkgManager == null");
            return;
        }
        DevicePolicyManager dpm = (DevicePolicyManager) getContext().getSystemService("device_policy");
        if (data != null) {
            ApplicationInfo appInfo = null;
            try {
                appInfo = pkgManager.getApplicationInfo(pkgName, 8192);
            } catch (Exception e) {
                HwLog.e(TAG, "onGetPkgStatsForData , exception");
            }
            if (!HsmPackageManager.getInstance().isRemovable(pkgName)) {
                return;
            }
            if (dpm == null || r0 == null || r0.manageSpaceActivityName != null || !((r0.flags & 65) == 1 || dpm.packageHasActiveAdmins(pkgName))) {
                long dataSize = data.dataSize;
                if (Environment.isExternalStorageEmulated()) {
                    dataSize += data.externalDataSize;
                }
                if (dataSize > CACEH_DIR) {
                    AppDataTrash trash = HwAppDataTrash.create(pkgName, dataSize);
                    if (trash != null) {
                        onPublishItemUpdate(trash);
                    } else {
                        HwLog.e(getTaskName(), "create HwAppDataTrash failed! pkg=" + pkgName);
                    }
                }
            } else {
                HwLog.i(TAG, "onGetPkgStatsForData pkgName  " + pkgName + "  is not allowed delete data");
                return;
            }
        }
        HwLog.e(TAG, "onGetPkgStatsForData stats is null!! pkg:" + pkgName);
    }

    public List<Integer> getSupportTrashType() {
        return HsmCollections.newArrayList(Integer.valueOf(1), Integer.valueOf(262144), Integer.valueOf(524288));
    }

    public boolean isNormal() {
        return true;
    }
}
