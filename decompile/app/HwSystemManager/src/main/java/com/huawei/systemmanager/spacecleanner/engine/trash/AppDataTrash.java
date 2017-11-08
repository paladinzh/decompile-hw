package com.huawei.systemmanager.spacecleanner.engine.trash;

import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash.SimpleTrash;
import java.io.IOException;

public abstract class AppDataTrash extends SimpleTrash implements IAppTrashInfo {
    protected final long mDataSize;

    public AppDataTrash(long cacheSize) {
        this.mDataSize = cacheSize;
    }

    public String getName() {
        return getAppLabel();
    }

    public long getTrashSize() {
        return this.mDataSize;
    }

    public int getPosition() {
        return 2;
    }

    public boolean isSuggestClean() {
        return false;
    }

    public void printf(Appendable appendable) throws IOException {
        appendable.append("  ").append("apkName:").append(getAppLabel()).append(", pkgName:").append(getPackageName()).append(",size").append(FileUtil.getFileSize(getTrashSize()));
    }
}
