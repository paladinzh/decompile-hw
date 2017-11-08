package com.huawei.systemmanager.spacecleanner.engine.trash;

import android.content.Context;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash.SimpleTrash;
import java.util.List;
import java.util.Map;

public abstract class FolderTrash extends SimpleTrash {
    protected final Map<String, Long> mFiles;
    private long mTotalSize;

    public FolderTrash() {
        this(null);
    }

    public FolderTrash(Map<String, Long> fileMap) {
        this.mTotalSize = 0;
        if (HsmCollections.isMapEmpty(fileMap)) {
            this.mFiles = HsmCollections.newArrayMap();
        } else {
            this.mFiles = fileMap;
        }
        caculateSize();
    }

    public boolean addFiles(Map<String, Long> fileMap) {
        if (HsmCollections.isMapEmpty(fileMap)) {
            return false;
        }
        this.mFiles.putAll(fileMap);
        caculateSize();
        return true;
    }

    public long getTrashSize() {
        return this.mTotalSize;
    }

    public boolean removeFile(String path) {
        Long size = (Long) this.mFiles.remove(path);
        if (size == null) {
            return false;
        }
        if (size.longValue() >= 0) {
            this.mTotalSize -= size.longValue();
        }
        return true;
    }

    public boolean isValidate() {
        return !this.mFiles.isEmpty();
    }

    public boolean cleanFile(String path) {
        if (isCleaned() || ((Long) this.mFiles.remove(path)) == null) {
            return false;
        }
        if (this.mFiles.isEmpty()) {
            setCleaned();
        }
        caculateSize();
        return true;
    }

    public List<String> getFiles() {
        return HsmCollections.newArrayList(this.mFiles.keySet());
    }

    public boolean clean(Context context) {
        setCleaned();
        for (String f : HsmCollections.newArrayList(this.mFiles.keySet())) {
            FileUtil.deleteFile(f);
        }
        return true;
    }

    public int getPosition() {
        return 1;
    }

    public void refreshContent() {
        if (!isCleaned() && this.mFiles.isEmpty()) {
            setCleaned();
            caculateSize();
        }
    }

    public FolderTrash addFolderTrash(FolderTrash another) {
        if (another != null) {
            addFiles(another.mFiles);
        }
        return this;
    }

    private void caculateSize() {
        long totalSize = 0;
        for (Long size : this.mFiles.values()) {
            if (size != null && size.longValue() > 0) {
                totalSize += size.longValue();
            }
        }
        this.mTotalSize = totalSize;
    }
}
