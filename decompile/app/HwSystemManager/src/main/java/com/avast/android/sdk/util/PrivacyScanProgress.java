package com.avast.android.sdk.util;

import com.avast.android.sdk.engine.PrivacyScanResult;

/* compiled from: Unknown */
public final class PrivacyScanProgress {
    public final PrivacyScanResult mPrivacyScanResult;
    public final int mScannedObjects;
    public final String mScannedPackageName;
    public final int mTotalObjectsToScan;

    PrivacyScanProgress(int i, int i2, String str, PrivacyScanResult privacyScanResult) {
        this.mScannedObjects = i2;
        this.mTotalObjectsToScan = i;
        this.mScannedPackageName = str;
        this.mPrivacyScanResult = privacyScanResult;
    }
}
