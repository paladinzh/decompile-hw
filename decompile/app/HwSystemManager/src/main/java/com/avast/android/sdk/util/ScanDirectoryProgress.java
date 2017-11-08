package com.avast.android.sdk.util;

import com.avast.android.sdk.engine.ScanResultStructure;
import java.util.List;

/* compiled from: Unknown */
public final class ScanDirectoryProgress {
    public final List<ScanResultStructure> mScanResult;
    public final String mScannedFile;
    public final int mScannedObjects;

    ScanDirectoryProgress(int i, String str, List<ScanResultStructure> list) {
        this.mScannedObjects = i;
        this.mScannedFile = str;
        this.mScanResult = list;
    }
}
