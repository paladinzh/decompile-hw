package com.huawei.systemmanager.spacecleanner.autoclean.TrashAnalysis;

import com.google.common.collect.Lists;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.engine.trash.UnusedAppTrash;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class UnUsedAppAnalysisMode extends AnalysisMode {
    private static final String TAG = "AutoCleanUnUsedAppAnalysisMode";

    public int getAnalysisTrashType() {
        return 2;
    }

    public List<Trash> shouldReport(List<Trash> trashList) {
        List<Trash> reportTrash = Lists.newArrayList();
        for (Trash trash : trashList) {
            if (!(trash instanceof UnusedAppTrash)) {
                HwLog.e(TAG, "error,trash is not unused app trash.");
            } else if (((UnusedAppTrash) trash).isNotCommonlyUsed()) {
                reportTrash.add(trash);
            }
        }
        return reportTrash;
    }
}
