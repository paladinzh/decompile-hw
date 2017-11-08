package com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom;

import com.huawei.systemmanager.comm.Storage.PathEntry;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import java.util.Map;

public class HwAppDirectory {
    Map<PathEntry, HwCustTrashInfo> mDirectoryMap = HsmCollections.newArrayMap();
    private final String mPkg;

    public HwAppDirectory(String pkg) {
        this.mPkg = pkg;
    }

    public String getPkg() {
        return this.mPkg;
    }
}
