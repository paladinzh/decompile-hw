package com.huawei.systemmanager.antivirus.engine.avast.scan;

import com.avast.android.sdk.engine.ScanResultStructure;
import java.util.List;

public final class ScanProgress {
    public final List<ScanResultStructure> mScanResult;
    public final int mScannedObjects;
    public final String mScannedPackageName;
    public final int mTotalObjectsToScan;

    ScanProgress(int totalObjectsToScan, int scannedObjects, String scannedObjectName, List<ScanResultStructure> scanResult) {
        this.mScannedObjects = scannedObjects;
        this.mTotalObjectsToScan = totalObjectsToScan;
        this.mScannedPackageName = scannedObjectName;
        this.mScanResult = scanResult;
    }
}
