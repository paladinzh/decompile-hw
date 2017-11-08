package com.huawei.systemmanager.spacecleanner.engine.trash;

import com.huawei.systemmanager.comm.Storage.PathEntry;

public class ThumbnailTrash extends FileTrash {
    public ThumbnailTrash(String file, PathEntry pathEntry) {
        super(file, pathEntry);
    }

    public int getType() {
        return 2048;
    }

    public boolean isSuggestClean() {
        return false;
    }
}
