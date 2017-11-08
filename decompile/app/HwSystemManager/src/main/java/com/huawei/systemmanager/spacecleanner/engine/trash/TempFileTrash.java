package com.huawei.systemmanager.spacecleanner.engine.trash;

import com.huawei.systemmanager.comm.Storage.PathEntry;

public class TempFileTrash extends FileTrash {
    public TempFileTrash(String file, PathEntry pathEntry) {
        super(file, pathEntry);
    }

    public int getType() {
        return 16;
    }

    public boolean isSuggestClean() {
        return false;
    }
}
