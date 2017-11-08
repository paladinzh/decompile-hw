package com.huawei.systemmanager.spacecleanner.engine.trash;

import com.huawei.systemmanager.comm.Storage.PathEntry;
import com.huawei.systemmanager.spacecleanner.engine.trash.FileTrash.FileTrashFactory;

public class BakFileTrash extends FileTrash {
    public static final FileTrashFactory BakFileFactory = new FileTrashFactory() {
        public FileTrash create(String path, PathEntry pathEntry) {
            return new BakFileTrash(path, pathEntry);
        }
    };

    public BakFileTrash(String file, PathEntry pathEntry) {
        super(file, pathEntry);
    }

    public int getType() {
        return 2097152;
    }

    public static FileTrash create(String path, PathEntry pathEntry) {
        return BakFileFactory.create(path, pathEntry);
    }

    public boolean isSuggestClean() {
        return false;
    }
}
