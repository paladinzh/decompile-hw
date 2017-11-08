package com.huawei.systemmanager.spacecleanner.engine.trash;

import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash.SimpleTrash;
import java.io.IOException;

public abstract class AppCacheTrash extends SimpleTrash implements IAppTrashInfo {
    protected final long mCacheSize;

    public AppCacheTrash(long cacheSize) {
        this.mCacheSize = cacheSize;
    }

    public String getName() {
        return getAppLabel();
    }

    public int getType() {
        return 1;
    }

    public long getTrashSize() {
        return this.mCacheSize;
    }

    public int getPosition() {
        return 2;
    }

    public boolean isSuggestClean() {
        return true;
    }

    public void printf(Appendable appendable) throws IOException {
        appendable.append("  ").append("apkName:").append(getAppLabel()).append(", pkgName:").append(getPackageName()).append(",size").append(FileUtil.getFileSize(getTrashSize()));
    }
}
