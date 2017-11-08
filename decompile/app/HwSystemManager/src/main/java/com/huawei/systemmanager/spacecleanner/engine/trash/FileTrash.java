package com.huawei.systemmanager.spacecleanner.engine.trash;

import android.content.Context;
import android.text.TextUtils;
import com.google.common.base.Objects;
import com.huawei.systemmanager.comm.Storage.PathEntry;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.rainbow.vaguerule.VagueRegConst;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash.SimpleTrash;
import com.huawei.systemmanager.spacecleanner.utils.TrashUtils;
import com.huawei.systemmanager.util.HwLog;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public abstract class FileTrash extends SimpleTrash {
    protected String mPath;
    protected final PathEntry mPathEny;
    protected long mSizeCache = -1;

    public static abstract class FileTrashFactory {
        public abstract FileTrash create(String str, PathEntry pathEntry);

        public FileTrash create(File file, PathEntry pathEntry) {
            return create(file.getPath(), pathEntry);
        }
    }

    public FileTrash(String path, PathEntry pathEntry) {
        this.mPath = path;
        this.mPathEny = pathEntry;
    }

    public String getName() {
        return this.mPath;
    }

    public long getTrashSize() {
        if (isCleaned()) {
            return 0;
        }
        if (this.mSizeCache >= 0) {
            return this.mSizeCache;
        }
        this.mSizeCache = FileUtil.getSingleFileSize(this.mPath);
        return this.mSizeCache;
    }

    public boolean isSuggestClean() {
        return false;
    }

    public String getPath() {
        return this.mPath;
    }

    public List<String> getFiles() {
        if (TextUtils.isEmpty(this.mPath)) {
            return Collections.emptyList();
        }
        return HsmCollections.newArrayList(this.mPath);
    }

    public boolean isValidate() {
        return !TextUtils.isEmpty(this.mPath);
    }

    public boolean removeFile(String path) {
        if (!Objects.equal(this.mPath, path)) {
            return false;
        }
        this.mPath = "";
        return true;
    }

    public boolean cleanFile(String path) {
        if (path != null && path.equalsIgnoreCase(this.mPath)) {
            setCleaned();
        }
        return true;
    }

    public boolean clean(Context cotnext) {
        setCleaned();
        FileUtil.deleteFile(this.mPath);
        return true;
    }

    public String toString() {
        return this.mPath;
    }

    public int getPosition() {
        return this.mPathEny.mPosition;
    }

    public void printf(Appendable appendable) throws IOException {
        appendable.append("     ").append(VagueRegConst.PTAH_PREFIX).append(this.mPath).append(", size:").append(FileUtil.getFileSize(getTrashSize()));
    }

    public String getUniqueDes() {
        return getPath();
    }

    public PathEntry getPathEntry() {
        return this.mPathEny;
    }

    public long getLastModified() {
        if (TextUtils.isEmpty(this.mPath)) {
            return 0;
        }
        return FileUtil.getlastModified(this.mPath);
    }

    public long getLastAccess() {
        if (TextUtils.isEmpty(this.mPath)) {
            return 0;
        }
        return FileUtil.getlastAccess(this.mPath);
    }

    public String getRootFolderPath() {
        if (TextUtils.isEmpty(this.mPath)) {
            return null;
        }
        String rootPath = this.mPathEny.mPath;
        if (this.mPath.startsWith(rootPath)) {
            String folderPath = this.mPath.substring(rootPath.length());
            if (folderPath.lastIndexOf(File.separatorChar) == 0) {
                return rootPath;
            }
            int sparatorIndex = folderPath.indexOf(File.separatorChar);
            while (sparatorIndex == 0) {
                folderPath = folderPath.substring(1);
                sparatorIndex = folderPath.indexOf(File.separatorChar);
            }
            if (sparatorIndex > 0) {
                folderPath = folderPath.substring(0, sparatorIndex);
            }
            return rootPath + File.separator + folderPath;
        }
        HwLog.e(Trash.TAG, "getRootFolderPath error!");
        return null;
    }

    public String getTerminateFolderPath() {
        return TrashUtils.getTerminateFolderPath(getPath());
    }
}
