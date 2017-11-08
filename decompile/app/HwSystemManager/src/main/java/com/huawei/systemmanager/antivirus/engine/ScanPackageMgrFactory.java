package com.huawei.systemmanager.antivirus.engine;

import com.huawei.systemmanager.antivirus.engine.trustlook.TrustLookAntiVirusEngine;

public class ScanPackageMgrFactory {
    public static IScanPackageMgr newInstance() {
        return new TrustLookAntiVirusEngine();
    }
}
