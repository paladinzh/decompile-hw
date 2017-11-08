package com.huawei.systemmanager.spacecleanner.autoclean.ScanType;

import android.content.Context;
import com.huawei.systemmanager.spacecleanner.autoclean.TrashAnalysis.AnalysisMode;
import java.util.ArrayList;
import java.util.List;

public abstract class AutoScan {
    public List<AnalysisMode> mAnalysisModes = new ArrayList();

    public abstract List<AnalysisMode> getAnalysisModes();

    public abstract boolean shouldAnalysis();

    public abstract boolean shouldClean();

    public abstract boolean shouldStart(Context context);

    public abstract int trashToScan();
}
