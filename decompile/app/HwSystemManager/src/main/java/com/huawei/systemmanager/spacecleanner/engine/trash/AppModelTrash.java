package com.huawei.systemmanager.spacecleanner.engine.trash;

import java.util.Map;

public abstract class AppModelTrash extends FolderTrash implements IAppTrashInfo {
    public abstract int getDiffDays();

    public abstract String getModelName();

    public AppModelTrash(Map<String, Long> fileMap) {
        super(fileMap);
    }

    public String getName() {
        return getModelName();
    }
}
