package com.avast.android.sdk.util;

import com.avast.android.sdk.engine.CloudScanResultStructure;
import com.avast.android.sdk.engine.ScanResultStructure;
import java.util.List;

/* compiled from: Unknown */
public final class ScanProgress {
    public final CloudScanResultStructure mCloudScanResult;
    public final List<ScanResultStructure> mScanResult;
    public final int mScannedObjects;
    public final String mScannedPackageName;
    public final int mTotalObjectsToScan;

    ScanProgress(int i, int i2, String str, List<ScanResultStructure> list, CloudScanResultStructure cloudScanResultStructure) {
        this.mScannedObjects = i2;
        this.mTotalObjectsToScan = i;
        this.mScannedPackageName = str;
        this.mScanResult = list;
        this.mCloudScanResult = cloudScanResultStructure;
    }
}
