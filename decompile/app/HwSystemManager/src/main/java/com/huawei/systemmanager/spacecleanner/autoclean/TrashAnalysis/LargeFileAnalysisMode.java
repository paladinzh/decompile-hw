package com.huawei.systemmanager.spacecleanner.autoclean.TrashAnalysis;

import com.google.common.collect.Lists;
import com.huawei.systemmanager.spacecleanner.engine.trash.LargeFileTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class LargeFileAnalysisMode extends AnalysisMode {
    private static final String TAG = "AutoCleanLargeFileAnalysisMode";

    public int getAnalysisTrashType() {
        return 4;
    }

    public List<Trash> shouldReport(List<Trash> trashList) {
        List<Trash> reportTrash = Lists.newArrayList();
        for (Trash trash : trashList) {
            if (!(trash instanceof LargeFileTrash)) {
                HwLog.e(TAG, "error, trash is not large file trash.");
            } else if (((LargeFileTrash) trash).isNotCommonlyUsed()) {
                reportTrash.add(trash);
            }
        }
        return reportTrash;
    }
}
