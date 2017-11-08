package com.huawei.systemmanager.spacecleanner.engine.trash;

import com.huawei.systemmanager.comm.Storage.PathEntry;
import com.huawei.systemmanager.spacecleanner.engine.trash.FileTrash.FileTrashFactory;

public class LogFileTrash extends FileTrash {
    public static final FileTrashFactory LogFileFactory = new FileTrashFactory() {
        public FileTrash create(String path, PathEntry pathEntry) {
            return new LogFileTrash(path, pathEntry);
        }
    };

    public LogFileTrash(String file, PathEntry pathEntry) {
        super(file, pathEntry);
    }

    public int getType() {
        return 8;
    }

    public static FileTrash create(String path, PathEntry pathEntry) {
        return LogFileFactory.create(path, pathEntry);
    }

    public boolean isSuggestClean() {
        return false;
    }
}
