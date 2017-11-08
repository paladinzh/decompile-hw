package com.huawei.systemmanager.spacecleanner.autoclean.TrashAnalysis;

import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import java.util.List;

public abstract class AnalysisMode {
    public abstract int getAnalysisTrashType();

    public abstract List<Trash> shouldReport(List<Trash> list);
}
