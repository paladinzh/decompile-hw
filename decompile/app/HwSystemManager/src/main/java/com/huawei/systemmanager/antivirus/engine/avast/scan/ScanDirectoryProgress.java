package com.huawei.systemmanager.antivirus.engine.avast.scan;

import com.avast.android.sdk.engine.ScanResultStructure;
import java.util.List;

public class ScanDirectoryProgress {
    public String mPath;
    public List<ScanResultStructure> mResults;

    public ScanDirectoryProgress(int progress, String path, List<ScanResultStructure> results) {
        this.mPath = path;
        this.mResults = results;
    }
}
