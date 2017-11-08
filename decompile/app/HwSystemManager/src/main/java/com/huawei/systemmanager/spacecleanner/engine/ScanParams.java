package com.huawei.systemmanager.spacecleanner.engine;

import com.huawei.systemmanager.comm.Storage.PathEntry;
import com.huawei.systemmanager.comm.Storage.PathEntrySet;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom.HwAppCustomMgr;

public class ScanParams {
    private static final int DEFAULT_MAX_TRASH_NUM = 60000;
    public static final int PARAMS_AUTO_SCAN = 4;
    public static final int PARAMS_QUICK_SCAN = 3;
    public static final int PARAMS_SCAN_ALL = 0;
    public static final int PARAMS_SCAN_CACHE = 2;
    public static final int PARAMS_SCAN_PKG = 1;
    private PathEntrySet entrySet = PathEntry.EMPTY_ENTRY_SET;
    private Object mCarry;
    private int mMaxTrashNum = 60000;
    private int mTrashType;
    private int mType;
    private boolean mUseSquenceExecute = false;

    public int getTrashType() {
        return this.mTrashType;
    }

    public PathEntrySet getEntrySet() {
        return this.entrySet;
    }

    public Object getCarry() {
        return this.mCarry;
    }

    public int getType() {
        return this.mType;
    }

    public int getMaxTrashNum() {
        return this.mMaxTrashNum;
    }

    public boolean useSequenceExecute() {
        return this.mUseSquenceExecute;
    }

    public static ScanParams createDeepScanParams() {
        ScanParams params = new ScanParams();
        params.mType = 0;
        PathEntrySet entrySet = PathEntry.buildRootEntrySet();
        params.entrySet = entrySet;
        params.mCarry = new HwAppCustomMgr(entrySet);
        return params;
    }

    public static ScanParams createInternalDeepScanParams() {
        ScanParams params = new ScanParams();
        params.mType = 0;
        PathEntrySet entrySet = PathEntry.buildInternalRootEntrySet();
        params.entrySet = entrySet;
        params.mCarry = new HwAppCustomMgr(entrySet);
        return params;
    }

    public static ScanParams createQuickPkgScanParams(String pkg) {
        ScanParams params = new ScanParams();
        params.mType = 1;
        params.mCarry = pkg;
        return params;
    }

    public static ScanParams createMainScreenScanParams() {
        ScanParams params = new ScanParams();
        params.mType = 3;
        PathEntrySet entrySet = PathEntry.buildRootEntrySet();
        params.entrySet = entrySet;
        params.mCarry = new HwAppCustomMgr(entrySet);
        return params;
    }

    public static ScanParams createAppCacheParams() {
        ScanParams params = new ScanParams();
        params.mType = 2;
        return params;
    }

    public static ScanParams createAutoScan(int trash) {
        ScanParams params = new ScanParams();
        params.mType = 4;
        PathEntrySet entrySet = PathEntry.buildRootEntrySet();
        params.entrySet = entrySet;
        params.mCarry = new HwAppCustomMgr(entrySet);
        params.mUseSquenceExecute = true;
        params.mTrashType = trash;
        return params;
    }

    public boolean hasSdcard() {
        if (this.entrySet == null) {
            return false;
        }
        return this.entrySet.hasSdcard();
    }
}
