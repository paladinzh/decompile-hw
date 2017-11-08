package com.huawei.systemmanager.spacecleanner.autoclean.ScanType;

import android.content.Context;
import com.huawei.systemmanager.spacecleanner.autoclean.AutoCleanConst;
import com.huawei.systemmanager.spacecleanner.autoclean.TrashAnalysis.AnalysisMode;
import com.huawei.systemmanager.spacecleanner.engine.base.SpaceConst;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class FileAnalysisScan extends AutoScan {
    private static final String TAG = "AutoCleanFileAnalysisScan";

    public FileAnalysisScan(List<AnalysisMode> analysisModes) {
        this.mAnalysisModes = analysisModes;
    }

    public boolean shouldStart(Context ctx) {
        int freePercent = AutoCleanConst.getInstance().getFreePercentInternal();
        if (freePercent >= 50) {
            HwLog.i(TAG, "Internal free percent is large,do not start file Analysis scan.Percent:" + freePercent);
            return false;
        } else if (!AutoCleanConst.getInstance().checkIfNotCommonlyUsedSwitchOn()) {
            HwLog.i(TAG, "Switch is not open,do not start file Analysis scan.");
            return false;
        } else if (checkReachTime(ctx)) {
            return true;
        } else {
            HwLog.i(TAG, "Is not Reach time,do not start file Analysis scan.");
            return false;
        }
    }

    public boolean shouldClean() {
        return false;
    }

    public int trashToScan() {
        int trash = 0;
        for (AnalysisMode mode : this.mAnalysisModes) {
            trash |= mode.getAnalysisTrashType();
        }
        return trash;
    }

    public List<AnalysisMode> getAnalysisModes() {
        return this.mAnalysisModes;
    }

    public boolean shouldAnalysis() {
        return true;
    }

    public String toString() {
        boolean shouldClean = shouldClean();
        return "AutoCleanFileAnalysisScanshould clean:" + shouldClean + " should analysis:" + shouldAnalysis();
    }

    private boolean checkReachTime(Context ctx) {
        long lastFileAnalysisTime = ctx.getSharedPreferences("space_prefence", 0).getLong(AutoCleanConst.KEY_LAST_FILE_ANALYSIS_TIME, 0);
        long currentTime = System.currentTimeMillis();
        long diffTime = currentTime - lastFileAnalysisTime;
        if (diffTime >= 0) {
            return diffTime >= SpaceConst.LARGE_FILE_EXCEED_INTERVAL_TIME;
        } else {
            HwLog.i(TAG, "Time may be modified by user! Do analysis! currentTime:" + currentTime + " lastFileAnalysisTime:" + lastFileAnalysisTime);
            return true;
        }
    }
}
