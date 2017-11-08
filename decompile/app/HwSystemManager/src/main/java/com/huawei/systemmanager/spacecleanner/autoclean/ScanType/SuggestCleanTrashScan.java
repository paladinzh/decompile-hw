package com.huawei.systemmanager.spacecleanner.autoclean.ScanType;

import android.content.Context;
import com.huawei.systemmanager.spacecleanner.autoclean.AutoCleanConst;
import com.huawei.systemmanager.spacecleanner.autoclean.TrashAnalysis.AnalysisMode;
import java.util.List;

public class SuggestCleanTrashScan extends AutoScan {
    private static final String TAG = "AutoCleanSuggestCleanTrashScan";

    public boolean shouldStart(Context ctx) {
        if (AutoCleanConst.getInstance().checkIfAutoCleanSwitchOn()) {
            return AutoCleanConst.getInstance().checkIfUserAgreement(ctx);
        }
        return false;
    }

    public boolean shouldClean() {
        return true;
    }

    public int trashToScan() {
        return 90113;
    }

    public List<AnalysisMode> getAnalysisModes() {
        return null;
    }

    public boolean shouldAnalysis() {
        return false;
    }

    public String toString() {
        boolean shouldClean = shouldClean();
        return "AutoCleanSuggestCleanTrashScanshould clean:" + shouldClean + " should analysis:" + shouldAnalysis();
    }
}
