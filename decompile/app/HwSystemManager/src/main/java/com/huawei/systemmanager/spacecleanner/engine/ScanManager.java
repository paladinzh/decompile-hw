package com.huawei.systemmanager.spacecleanner.engine;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.text.TextUtils;
import com.huawei.systemmanager.spacecleanner.engine.base.ITrashEngine;
import com.huawei.systemmanager.spacecleanner.engine.base.ITrashEngine.IUpdateListener;
import com.huawei.systemmanager.spacecleanner.engine.base.Task;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom.HwCustAppDataObtain;
import com.huawei.systemmanager.spacecleanner.engine.trash.IAppTrashInfo;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import java.util.Set;

public final class ScanManager {
    private static final String TAG = "ScanManager";
    private static final ScanManager sInstance = new ScanManager();

    private ScanManager() {
    }

    public static TrashScanHandler startScan(Context ctx, ScanParams params, ITrashScanListener l) {
        return sInstance.startScanInner(ctx, params, l);
    }

    public static TrashScanHandler startScan(Context ctx, ITrashScanListener l) {
        return sInstance.startScanInner(ctx, ScanParams.createDeepScanParams(), l);
    }

    public static TrashScanHandler startInternalScan(Context ctx, ITrashScanListener l) {
        return sInstance.startScanInner(ctx, ScanParams.createInternalDeepScanParams(), l);
    }

    public static IAppTrashInfo quickScanAppTrash(Context ctx, String pkg) {
        return sInstance.quickScanAppTrashInner(ctx, pkg);
    }

    public static boolean isTooLargeVideoTrash(Context ctx, Set<String> pkgs, long maxSize) {
        return sInstance.isTooLargeVideoTrashInner(ctx, pkgs, maxSize).booleanValue();
    }

    public static TrashScanHandler getCachedHander(long id) {
        return TrashScanHandler.getInstance(id);
    }

    private TrashScanHandler startScanInner(Context ctx, ScanParams params, ITrashScanListener l) {
        TrashScanHandler handler = TrashScanHandler.create();
        handler.addScanListener(l);
        handler.startScan(params);
        return handler;
    }

    private IAppTrashInfo quickScanAppTrashInner(Context ctx, String pkg) {
        if (ctx == null || TextUtils.isEmpty(pkg)) {
            HwLog.e(TAG, "quickScanAppTrashInner, pkg is empty");
            return null;
        }
        ScanParams params = ScanParams.createQuickPkgScanParams(pkg);
        ITrashEngine engine = new TrashEngineImpl(ctx);
        Task scanTask = engine.getScanner(params);
        if (scanTask == null) {
            return null;
        }
        scanTask.start(params);
        List<Trash> trashes = scanTask.getResult();
        engine.destory();
        if (trashes == null || trashes.isEmpty()) {
            return null;
        }
        return (IAppTrashInfo) trashes.get(0);
    }

    private Boolean isTooLargeVideoTrashInner(Context ctx, Set<String> pkgs, long maxSize) {
        boolean z = true;
        long totalTrash = 0;
        for (String pkg : pkgs) {
            IAppTrashInfo appInfo = quickScanAppTrashInner(ctx, pkg);
            if (appInfo != null) {
                HwLog.i(TAG, "pkg:" + pkg + "  size:" + appInfo.getTrashSize() + " suggest:" + appInfo.isSuggestClean());
                totalTrash += appInfo.getTrashSize();
                if (totalTrash >= maxSize) {
                    return Boolean.valueOf(true);
                }
            }
        }
        if (totalTrash <= maxSize) {
            z = false;
        }
        return Boolean.valueOf(z);
    }

    public static void update(Context ctx, IUpdateListener listener) {
        new TrashEngineImpl(ctx).update(listener);
    }

    public static void getAllHwCustData() {
        HwCustAppDataObtain.getInstance().getAllHwCustTrash();
    }

    public static void getHwCustData(PackageInfo pi) {
        HwCustAppDataObtain.getInstance().getHwCustTrash(pi);
    }

    public static void deleteHwCustData(String pkgName) {
        HwCustAppDataObtain.getInstance().deleteHwCustTrash(pkgName);
    }
}
