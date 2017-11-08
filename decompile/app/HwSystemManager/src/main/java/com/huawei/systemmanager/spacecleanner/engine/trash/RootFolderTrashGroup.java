package com.huawei.systemmanager.spacecleanner.engine.trash;

import com.huawei.systemmanager.comm.Storage.PathEntry;
import com.huawei.systemmanager.util.HwLog;
import java.io.File;

public class RootFolderTrashGroup extends TrashGroup {
    private String mFolderName;
    private String mPath;
    private PathEntry mPathEntry;

    public RootFolderTrashGroup(int type, boolean suggestClean, String filePath, PathEntry pathEntry) {
        super(type, suggestClean);
        this.mFolderName = getRootFolderName(filePath);
        this.mPath = filePath;
        this.mPathEntry = pathEntry;
    }

    public String getName() {
        return this.mPath;
    }

    public String getUniqueDes() {
        return getPath();
    }

    public String getFolderName() {
        return this.mFolderName;
    }

    public String getPath() {
        return this.mPath;
    }

    public int getPosition() {
        return this.mPathEntry.mPosition;
    }

    public boolean isInRootDirectory() {
        if (this.mPathEntry.mPath.equals(this.mPath)) {
            return true;
        }
        return false;
    }

    private String getRootFolderName(String filePath) {
        int separatorIndex = filePath.lastIndexOf(File.separatorChar);
        if (separatorIndex >= 0) {
            return filePath.substring(separatorIndex);
        }
        HwLog.e(Trash.TAG, "getRootFolderName failed! separatorIndex < 0");
        return null;
    }
}
