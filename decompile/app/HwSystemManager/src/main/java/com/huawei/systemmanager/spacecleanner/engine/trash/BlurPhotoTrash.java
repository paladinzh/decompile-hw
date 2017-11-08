package com.huawei.systemmanager.spacecleanner.engine.trash;

import com.huawei.systemmanager.comm.Storage.PathEntry;

public class BlurPhotoTrash extends PhotoTrash {
    public BlurPhotoTrash(String path, PathEntry pathEntry) {
        super(path, pathEntry);
    }

    public int getType() {
        return 8388608;
    }
}
