package com.huawei.systemmanager.spacecleanner.engine.trash;

import android.text.TextUtils;
import com.huawei.systemmanager.comm.Storage.PathEntry;
import com.huawei.systemmanager.util.HwLog;

public class MiminumFolderTrashGroup extends TrashGroup {
    private String mPath;
    private PathEntry mPathEntry;

    public MiminumFolderTrashGroup(int type, boolean suggestClean, String filePath, PathEntry pathEntry) {
        super(type, suggestClean);
        this.mPath = filePath;
        this.mPathEntry = pathEntry;
    }

    public String getName() {
        return this.mPath;
    }

    public String getUniqueDes() {
        return getPath();
    }

    public String getPath() {
        return this.mPath;
    }

    public int getPosition() {
        return this.mPathEntry.mPosition;
    }

    public String getMiniumFolderName() {
        String rootPath = this.mPathEntry.mPath;
        if (TextUtils.isEmpty(rootPath) || TextUtils.isEmpty(this.mPath)) {
            return this.mPath;
        }
        int startIndex = rootPath.length() + 1;
        if (startIndex >= 0 && startIndex <= this.mPath.length()) {
            return this.mPath.substring(startIndex);
        }
        HwLog.e(Trash.TAG, "getFolderName, startIndex error:" + startIndex);
        return this.mPath;
    }
}
